package com.tomtiddler.community.controller.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class AlphaInterceptor implements HandlerInterceptor {

    private static final Logger loggger = LoggerFactory.getLogger(AlphaInterceptor.class);

    /**
     * 模板之后，最后执行
     */
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        loggger.debug("afterCompletion: " + handler.toString());
        return;
    }

    /**
     * 控制器之后执行
     */
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        loggger.debug("postHandle: " + handler.toString());
        return;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        loggger.debug("preHandle: " + handler.toString());
        return true;
    }

}
