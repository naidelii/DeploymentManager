package com.naidelii.utils;

import com.naidelii.exception.GlobalException;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author lanwei
 */
public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static void validateFileExtension(String filename, String extension) {
        if (!filename.endsWith(extension)) {
            throw new GlobalException("文件扩展名不正确，文件名: " + filename);
        }
    }

    public static void validateTimeout(LocalDateTime requestTime, LocalDateTime now, long timeoutInMillis) {
        Duration duration = Duration.between(requestTime, now);
        if (duration.toMillis() > timeoutInMillis) {
            throw new GlobalException("请求超时");
        }
    }

    public static void validateCiphertext(String rawData, String salt, String ciphertext) {
        if (!CryptoUtil.matches(rawData, salt, ciphertext)) {
            throw new GlobalException("非法请求");
        }
    }
}
