package com.naidelii.utils;

import cn.hutool.crypto.SecureUtil;

import java.util.Objects;

/**
 * CryptoUtil - 提供数据加密和验证功能的工具类
 *
 * <p>该类使用 SHA-256 算法进行数据加密，并支持加盐操作，以增强存储的安全性。</p>
 *
 * @author lanwei
 */
public final class CryptoUtil {

    private CryptoUtil() {
    }

    /**
     * 将数据与盐值进行混淆
     *
     * <p>将明文数据和盐值拼接起来，生成一个新的数据字符串。</p>
     *
     * @param rawData 明文数据
     * @param salt    盐值
     * @return 混淆后的数据（明文+盐值）
     */
    private static String confuse(String rawData, String salt) {
        // 拼接数据和盐值，形成加盐后的数据
        return rawData + salt;
    }

    /**
     * 验证数据是否正确
     *
     * <p>通过加盐和加密的方式，将明文数据与存储的加密数据进行比对，验证是否一致。</p>
     *
     * @param inputData   用户输入的数据（明文）
     * @param salt        盐值
     * @param encodedData 数据库中存储的加密后的数据（密文）
     * @return 如果数据匹配返回 true，否则返回 false
     */
    public static boolean matches(String inputData, String salt, String encodedData) {
        // 对用户输入的数据进行加盐和加密
        String saltedData = confuse(inputData, salt);
        // 使用MD5加密
        String saltedDataNew = SecureUtil.md5(saltedData);
        // 比较加密后的数据和数据库中存储的加密数据是否相等
        return Objects.equals(saltedDataNew, encodedData);
    }

}
