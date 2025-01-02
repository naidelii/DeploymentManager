package com.naidelii.service.impl;

import com.naidelii.config.DeployProperties;
import com.naidelii.dto.DeployPackageDTO;
import com.naidelii.exception.GlobalException;
import com.naidelii.service.IDeployService;
import com.naidelii.utils.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author lanwei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeployServiceImpl implements IDeployService {

    private final DeployProperties deployProperties;

    // 常量，用于避免硬编码

    private static final String JAR_EXTENSION = ".jar";

    @Override
    public void deployPackage(DeployPackageDTO packageDTO) {
        log.info("准备部署服务：{}", packageDTO.getJarName());

        // 校验上传的文件
        checkFile(packageDTO);
        log.info("参数校验成功，准备保存文件");

        // 保存文件到指定位置
        saveFile(packageDTO.getFile(), packageDTO.getJarName());
        log.info("文件保存成功，开始部署");
        try {
            executeScript(packageDTO.getJarName());
            log.info("服务部署成功");
        } catch (IOException | InterruptedException e) {
            log.error("部署脚本执行失败", e);
            throw new GlobalException("部署失败！");
        }
    }

    private Process getProcess(String jarName) throws IOException {
        // 部署脚本
        String scriptPath = deployProperties.getDeployScriptPath();
        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists() || !scriptFile.canExecute()) {
            throw new IOException("脚本文件未找到或无执行权限:" + scriptPath);
        }
        // 创建 ProcessBuilder 对象并配置环境和输出
        ProcessBuilder processBuilder = new ProcessBuilder(scriptPath, jarName, deployProperties.getJarSavePath());
        // 设置标准输出和错误输出到缓冲区
        processBuilder.redirectErrorStream(true);
        // 启动进程
        return processBuilder.start();
    }

    private void executeScript(String jarName) throws IOException, InterruptedException {
        // 启动进程
        Process process = getProcess(jarName);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            StringBuilder strData = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                strData.append(line).append("\n");
            }
            String result = strData.toString();
            log.info("shell脚本执行日志: \n" + result);
        }
        // 等待脚本执行完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("脚本执行失败，退出码:  " + exitCode);
            throw new GlobalException("部署脚本执行失败");
        }
    }

    /**
     * 校验上传的文件
     */
    private void checkFile(DeployPackageDTO packageDTO) {
        // jar包名
        if (!packageDTO.getJarName().endsWith(JAR_EXTENSION)) {
            log.error("jarName参数不符合规范：{}", packageDTO.getJarName());
            throw new GlobalException("参数不符合规范！");
        }
        // 获取当前时间和请求时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime requestTime = convertTimestampToLocalDateTime(packageDTO.getTimeStamp());

        // 校验时间差和超时
        validateTimeout(requestTime, now);

        // 校验文件
        validateFile(packageDTO.getFile(), packageDTO.getFileSize());

        // 校验密文
        validateCiphertext(packageDTO);
    }

    /**
     * 校验文件是否符合要求
     */
    private void validateFile(MultipartFile file, Long fileSize) {
        String filename = Objects.requireNonNull(file.getOriginalFilename());
        if (!filename.endsWith(JAR_EXTENSION)) {
            log.error("文件扩展名不正确，文件名: {}", filename);
            throw new GlobalException("文件无效！");
        }
        if (!Objects.equals(fileSize, file.getSize())) {
            log.error("文件大小不一致，文件大小: {}, 请求大小: {}", file.getSize(), fileSize);
            throw new GlobalException("文件无效");
        }
    }


    /**
     * 校验请求时间是否超时
     */
    private void validateTimeout(LocalDateTime requestTime, LocalDateTime now) {
        Duration duration = Duration.between(requestTime, now);
        if (duration.toMillis() > getTimeoutInMillis()) {
            throw new GlobalException("请求超时");
        }
        log.info("请求时间: {}, 当前时间: {}", requestTime, now);
    }

    /**
     * 保存文件到目标路径
     */
    private void saveFile(MultipartFile file, String jarName) {
        // 构建JAR包的目标文件路径（保存路径）
        File jarFile = new File(deployProperties.getJarSavePath(), jarName);

        try {
            // 检查并创建目标目录（如果不存在）
            File targetDir = jarFile.getParentFile();
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                log.error("无法创建目录: {}", targetDir.getAbsolutePath());
                throw new GlobalException("无法创建目录: " + targetDir.getAbsolutePath());
            }
            // 将JAR包文件保存到目标路径
            file.transferTo(jarFile);
            log.info("文件保存成功: {}", jarFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("保存文件失败: {}", e.getMessage());
            throw new GlobalException("文件保存失败");
        }
    }

    /**
     * 将毫秒级时间戳转换为 LocalDateTime
     */
    private LocalDateTime convertTimestampToLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * 获取配置的超时时间（毫秒）
     */
    private long getTimeoutInMillis() {
        long timeoutValue = deployProperties.getTimeout().getValue();
        TimeUnit timeoutUnit = deployProperties.getTimeout().getTimeUnit();
        return timeoutUnit.toMillis(timeoutValue);
    }

    /**
     * 校验密文是否一致
     */
    private void validateCiphertext(DeployPackageDTO packageDTO) {
        String rawData = String.format("%s%s%s",
                packageDTO.getJarName(),
                packageDTO.getTimeStamp(),
                packageDTO.getFileSize());
        boolean isCiphertextValid = CryptoUtil.matches(rawData, deployProperties.getSalt(), packageDTO.getCiphertext());
        if (!isCiphertextValid) {
            throw new GlobalException("非法请求");
        }
    }

}
