package com.conason.reggie.common;

import com.conason.reggie.controller.Request;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.SQL;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 添加员工异常处理
     * @param exception
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Request<String> exceptionHandler(SQLIntegrityConstraintViolationException exception) {
        String msg = exception.getMessage();

        log.error(msg);

        if(exception.getMessage().contains("Duplicate entry")){
            String[] split = msg.split(" ");
            msg = split[2] + "已存在";
            return Request.error(msg);
        }

        return Request.error("未知错误");
    }

    /**
     * 删除菜品分类异常处理
     * @param exception
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public Request<String> exceptionHandler(CustomException exception) {
        String msg = exception.getMessage();

        log.error(msg);

        return Request.error(msg);
    }
}
