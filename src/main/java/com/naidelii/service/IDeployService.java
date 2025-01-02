package com.naidelii.service;

import com.naidelii.dto.DeployPackageDTO;

/**
 * @author lanwei
 */
public interface IDeployService {

    /**
     * 部署包
     *
     * @param packageDTO 部署包信息
     */
    void deployPackage(DeployPackageDTO packageDTO);

}
