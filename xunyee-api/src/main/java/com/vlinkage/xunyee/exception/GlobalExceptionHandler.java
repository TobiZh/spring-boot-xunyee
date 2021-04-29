package com.vlinkage.xunyee.exception;

import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.entity.result.code.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 全局异常处理
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
 
 
    /**
     * 处理运行时异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    public R handleThrowable(Throwable e, HttpServletRequest request) {
        log.error("URL:{} ,系统异常: ",request.getRequestURI(), e);
        return R.ERROR(e.getMessage());
    }
 
    /**
     * 处理自定义异常
     */
    @ExceptionHandler(BusinessException.class)
    public R handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("URL:{} ,业务异常:{}", request.getRequestURI(),e.getMsg());
        return R.ERROR(e.getCode(),e.getMsg());
    }

    //处理Get请求中 使用@Valid 验证路径中请求实体校验失败后抛出的异常，详情继续往下看代码
    @ExceptionHandler(BindException.class)
    public R BindExceptionHandler(BindException e,HttpServletRequest request){
        String msgs = this.handle(e.getBindingResult().getFieldErrors());
        log.error("URL:{} ,参数校验异常B:{}", request.getRequestURI(),msgs);
        return R.ERROR(msgs);
    }

    /**
     * validator 统一异常封装
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String msgs = this.handle(e.getBindingResult().getFieldErrors());
        log.error("URL:{} ,参数校验异常M:{}", request.getRequestURI(),msgs);
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
        log.error("URL:{} ,业务校验异常:{}", request.getRequestURI(),e);
        return R.ERROR(-1,e.getClass().getName()+":--->"+e.getMessage());
    }

    /**
     * sql异常
     */
    @ExceptionHandler(PersistenceException.class)
    public R persistenceException(PersistenceException e, HttpServletRequest request) {

        log.error("URL:{} ,参数校验异常M:{}", request.getRequestURI(),e.getMessage());
        return R.ERROR(-1,e.getClass().getName()+":--->"+e.getMessage());
    }
}