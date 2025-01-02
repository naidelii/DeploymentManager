package com.naidelii.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lanwei
 */

@Getter
@AllArgsConstructor
public enum DeploymentMode {
    /**
     * 单机部署
     */
    SINGLE_NODE("Standalone/deploy_service.sh"),
    /**
     * 集群部署
     */
    CLUSTER("Distributed/ansible_deploy.sh");

    private final String scriptPath;

}
