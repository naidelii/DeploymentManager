package com.naidelii.exception;

import com.naidelii.constant.CommonConstants;
import com.naidelii.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author naidelii
 * 异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 处理Exception异常
     *
     * @param e 异常对象
     * @return 返回结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e) {
        log.error("=========Exception：{}", e.getMessage());
        return Result.fail();
    }


    /**
     * 处理自定义异常
     *
     * @param e 自定义异常
     * @return Result
     */
    @ExceptionHandler(GlobalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handlerBootException(GlobalException e) {
        log.error("=========自定义异常：{}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    /**
     * 处理参数校验错误
     *
     * @param e 异常对象
     * @return 返回结果
     */
    @ExceptionHandler({BindException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleBindException(BindException e) {
        log.error("=========handleBindException：{}", e.getMessage());
        String errorMsg = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(n -> String.format("%s: %s", n.getField(), n.getDefaultMessage()))
                .reduce((x, y) -> String.format("%s; %s", x, y))
                .orElse(CommonConstants.PARAM_VERIFY_ERROR_STR);
        return Result.fail(errorMsg);
    }

    /**
     * 处理参数校验错误（@RequestBody）
     *
     * @param e 异常对象
     * @return 返回结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("=========handleMethodArgumentNotValidException：{}", e.getMessage());
        String errorMsg = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(n -> String.format("%s: %s", n.getField(), n.getDefaultMessage()))
                .reduce((x, y) -> String.format("%s; %s", x, y))
                .orElse(CommonConstants.PARAM_VERIFY_ERROR_STR);
        return Result.fail(errorMsg);
    }

}
