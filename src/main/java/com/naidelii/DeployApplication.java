package com.naidelii;

import com.naidelii.config.DeployProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author lanwei
 */
@SpringBootApplication
@EnableConfigurationProperties(DeployProperties.class)
public class DeployApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeployApplication.class, args);
    }
}
