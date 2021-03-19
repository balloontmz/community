package com.tomtiddler.community.controller.advice;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tomtiddler.community.util.CommunityUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(annotations = Controller.class) //全局 controller 配置类  所有 controller 注解 对应的范围
public class ExceptionAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    // @ModelAttribute //模型绑定
    // @DataBinder //数据绑定
    @ExceptionHandler({Exception.class}) // 错误处理
    public void handlerException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常" + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        String xRequestWith = request.getHeader("X-Requested-With");
        logger.error("当前的请求头为:" + xRequestWith);
        if ("XMLHttpRequest".equals(xRequestWith)) {
            response.setContentType("application/json;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常"));
            return;
        }
        response.sendRedirect(request.getContextPath() + "/error");
    }
}
