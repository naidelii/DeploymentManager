package com.naidelii.config;

import com.naidelii.exception.GlobalException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author lanwei
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "deploy")
public class DeployProperties {

    /**
     * 部署脚本存放路径
     */
    private String deployScriptPath;

    /**
     * JAR包保存路径
     */
    private String jarSavePath;

    /**
     * 密码盐
     */
    private String salt;

    /**
     * 超时时间配置
     */
    private Timeout timeout;

    @Data
    public static class Timeout {

        /**
         * 超时时间数值
         */
        private long value;

        /**
         * 超时时间单位（TimeUnit）
         */
        private String unit;

        /**
         * 将超时时间的单位字符串转换为 TimeUnit 枚举
         *
         * @return 对应的 TimeUnit 单位
         * @throws GlobalException 如果 unit 无效时抛出异常
         */
        public TimeUnit getTimeUnit() {
            try {
                // 将单位字符串转换为大写并转为对应的 TimeUnit 枚举
                return TimeUnit.valueOf(unit.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("无效的超时时间单位：{}", unit);
                throw new GlobalException("无效的超时时间单位");
            }
        }

    }
}
