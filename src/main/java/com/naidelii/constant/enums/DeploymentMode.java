package com.naidelii.constant.enums;

import lombok.Getter;

/**
 * @author lanwei
 */

@Getter
public enum DeploymentMode {
    /**
     * 单机部署
     */
    SINGLE_NODE,
    /**
     * 集群部署
     */
    CLUSTER;

}
