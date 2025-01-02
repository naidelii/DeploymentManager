package com.naidelii.service.impl;

import com.naidelii.config.DeployProperties;
import com.naidelii.constant.CommonConstants;
import com.naidelii.dto.DeployPackageDTO;
import com.naidelii.exception.GlobalException;
import com.naidelii.service.IDeployService;
import com.naidelii.utils.FileUtils;
import com.naidelii.utils.ScriptUtils;
import com.naidelii.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

    @Override
    public void deployPackage(DeployPackageDTO packageDTO) {
        log.info("准备部署服务：{}", packageDTO.getJarName());

        // 校验上传的文件
        validateUpload(packageDTO);

        // 保存文件
        File jarFile = new File(deployProperties.getJarSavePath(), packageDTO.getJarName());

        try {
            FileUtils.saveFile(packageDTO.getFile(), jarFile);
            log.info("文件保存成功: {}", jarFile.getAbsolutePath());

            // 执行部署脚本
            executeScript(deployProperties.getDeploymentMode().getScriptPath(), packageDTO.getJarName(), deployProperties.getJarSavePath());
            log.info("服务部署成功");
        } catch (IOException | InterruptedException e) {
            log.error("部署脚本执行失败", e);
            throw new GlobalException("部署失败！");
        }
    }

    private void executeScript(String scriptPath, String jarName, String jarSavePath) throws IOException, InterruptedException {
        URL resourceUrl = getClass().getClassLoader().getResource(scriptPath);
        if (resourceUrl == null) {
            throw new GlobalException("脚本文件未找到: " + scriptPath);
        }
        File scriptFile = FileUtils.extractFileFromResourceToTempFile(resourceUrl, "deploy_script_", ".sh");
        try {
            FileUtils.checkFilePermissions(scriptFile);
            Process process = ScriptUtils.buildProcess(scriptFile, jarName, jarSavePath);
            String scriptOutput = ScriptUtils.readScriptOutput(process);
            log.info("脚本执行日志: \n{}", scriptOutput);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("脚本执行失败，退出码: {}", exitCode);
                throw new GlobalException("部署脚本执行失败");
            }
        } finally {
            FileUtils.deleteFile(scriptFile);
        }
    }

    private void validateUpload(DeployPackageDTO packageDTO) {
        // 校验文件扩展名
        ValidationUtils.validateFileExtension(packageDTO.getJarName(), CommonConstants.JAR_EXTENSION);

        // 校验文件大小
        validateFileSize(packageDTO);

        // 校验请求时间是否超时
        validateRequestTime(packageDTO);

        // 校验密文是否一致
        validateCiphertext(packageDTO);
    }

    private void validateFileSize(DeployPackageDTO packageDTO) {
        MultipartFile file = packageDTO.getFile();
        String filename = Objects.requireNonNull(file.getOriginalFilename());
        if (!filename.endsWith(CommonConstants.JAR_EXTENSION)) {
            log.error("文件扩展名不正确，文件名: {}", filename);
            throw new GlobalException("文件无效！");
        }
        if (!Objects.equals(packageDTO.getFileSize(), file.getSize())) {
            log.error("文件大小不一致，文件大小: {}, 请求大小: {}", file.getSize(), packageDTO.getFileSize());
            throw new GlobalException("文件无效");
        }
    }

    private void validateRequestTime(DeployPackageDTO packageDTO) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime requestTime = convertTimestampToLocalDateTime(packageDTO.getTimeStamp());
        ValidationUtils.validateTimeout(requestTime, now, getTimeoutInMillis());
        log.info("请求时间: {}, 当前时间: {}", requestTime, now);
    }

    /**
     * 校验密文是否一致
     */
    private void validateCiphertext(DeployPackageDTO packageDTO) {
        String rawData = String.format("%s%s%s",
                packageDTO.getJarName(),
                packageDTO.getTimeStamp(),
                packageDTO.getFileSize());
        ValidationUtils.validateCiphertext(rawData, deployProperties.getSalt(), packageDTO.getCiphertext());
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

}
