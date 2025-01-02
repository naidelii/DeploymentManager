package com.naidelii.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;

/**
 * @author lanwei
 */
@Slf4j
public final class FileUtils {
    private FileUtils() {
    }

    public static void saveFile(MultipartFile file, File targetFile) throws IOException {
        File targetDir = targetFile.getParentFile();
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("无法创建目录: " + targetDir.getAbsolutePath());
        }
        file.transferTo(targetFile);
    }

    public static void checkFilePermissions(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("文件不存在: " + file.getAbsolutePath());
        }
        if (!file.canExecute()) {
            throw new IOException("文件没有执行权限: " + file.getAbsolutePath());
        }
    }

    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            if (file.delete()) {
                log.info("临时脚本文件已删除: {}", file.getAbsolutePath());
            } else {
                log.warn("无法删除临时脚本文件: {}", file.getAbsolutePath());
            }
        }
    }

    public static File extractFileFromResourceToTempFile(URL resourceUrl, String prefix, String suffix) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix);

        try (InputStream inputStream = resourceUrl.openStream();
             OutputStream outputStream = Files.newOutputStream(tempFile.toPath())) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        // 设置文件为可执行
        if (!tempFile.setExecutable(true)) {
            throw new IOException("无法为脚本设置执行权限: " + tempFile.getAbsolutePath());
        }
        return tempFile;
    }
}
