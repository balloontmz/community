package com.tomtiddler.community.controller.interceptor;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tomtiddler.community.entity.LoginTicket;
import com.tomtiddler.community.entity.User;
import com.tomtiddler.community.service.UserService;
import com.tomtiddler.community.util.CookieUtil;
import com.tomtiddler.community.util.HostHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    private static final Logger loggger = LoggerFactory.getLogger(AlphaInterceptor.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 模板之后，最后执行
     */
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        loggger.debug("afterCompletion: " + handler.toString());
        hostHolder.clear();
        return;
    }

    /**
     * 控制器之后执行
     */
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        loggger.debug("postHandle: " + handler.toString());
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
        return;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        loggger.debug("preHandle: " + handler.toString());
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        loggger.debug("当前取到的ticket为：" + ticket);
        if (ticket != null) { // 已登录
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            loggger.debug("当前存在ticket");
            //检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                //根据凭据查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                loggger.debug("查询出的用户为：" + user);
                //在本次请求中持有用户
                hostHolder.setUser(user);
            }
        }
        return true;
    }

}
