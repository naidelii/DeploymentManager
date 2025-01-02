package com.naidelii.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author lanwei
 */
@Data
public class DeployPackageDTO {

    /**
     * jar包名称
     */
    @NotBlank(message = "jar包名称不能为空")
    private String jarName;

    /**
     * 操作时间戳（毫秒）
     * 存储从1970年1月1日00:00:00 UTC以来的毫秒数
     */
    @NotNull(message = "操作时间不能为空")
    private Long timeStamp;

    /**
     * 文件大小
     */
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;

    /**
     * 密文
     */
    @NotBlank(message = "密文不能为空")
    private String ciphertext;

    /**
     * 文件
     */
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
}
