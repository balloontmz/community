package com.tomtiddler.community.aspect;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
public class ServiceLogAspect {
    private Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.tomtiddler.community.service.*.*(..))")
    public void pointcut() {    
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 用户[1.2.3.4],在[xxxx-xx-xx],访问了[com.tomtiddler.community.service.xxx()].
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getRemoteHost();
    
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    
            String method = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
    
            logger.info(String.format("用户[%s],在[%s],访问了[%s]", ip, now, method));
        }
    }
}
