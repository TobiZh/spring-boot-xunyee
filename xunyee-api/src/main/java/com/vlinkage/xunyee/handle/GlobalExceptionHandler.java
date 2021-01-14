package com.vlinkage.xunyee.handle;

import com.vlinkage.common.entity.result.R;
import com.vlinkage.common.entity.result.code.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestControllerAdvice
@Slf4j
/**
 * 全局异常处理
 */
public class GlobalExceptionHandler {
 
 
    /**
     * 处理运行时异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    public R handleThrowable(Throwable e, HttpServletRequest request) {
        //TODO 运行时异常，可以在这里记录，用于发异常邮件通知
        log.error("URL:{} ,系统异常: ",request.getRequestURI(), e);
        return R.ERROR(e.getMessage());
    }
 
    /**
     * 处理自定义异常
     */
    @ExceptionHandler(BusinessException.class)
    public R handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("URL:{} ,业务异常:{}", request.getRequestURI());
        return R.ERROR(e.getCode(),e.getClass().getName()+":--->"+e.getMessage());
    }
 
    /**
     * validator 统一异常封装
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String msgs = this.handle(e.getBindingResult().getFieldErrors());
        log.warn("URL:{} ,参数校验异常:{}", request.getRequestURI(),msgs);
        return R.ERROR(ResultCode.PARAM_IS_INVALID,e.getClass().getName()+":--->"+msgs);
    }
 
    private String handle(List<FieldError> fieldErrors) {
        StringBuilder sb = new StringBuilder();
        for (FieldError obj : fieldErrors) {
            sb.append(obj.getField());
            sb.append("=[");
            sb.append(obj.getDefaultMessage());
            sb.append("]  ");
        }
        return sb.toString();
    }
 
    /**
     * Assert的异常统一封装
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public R illegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("URL:{} ,业务校验异常:{}", request.getRequestURI(),e);
        return R.ERROR(4000,e.getClass().getName()+":--->"+e.getMessage());
    }
 
}