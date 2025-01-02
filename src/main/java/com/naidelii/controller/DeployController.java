package com.naidelii.controller;

import com.naidelii.dto.DeployPackageDTO;
import com.naidelii.service.IDeployService;
import com.naidelii.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lanwei
 */
@RestController
@RequestMapping("/deploy")
@RequiredArgsConstructor
public class DeployController {

    private final IDeployService deployService;

    @PostMapping("/package")
    public Result<?> deployPackage(@Validated DeployPackageDTO packageDTO) {
        deployService.deployPackage(packageDTO);
        return Result.success();
    }

}
