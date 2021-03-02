package com.tomtiddler.community;

import com.tomtiddler.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void  testTextMail() {
        mailClient.sendMail("1956354744@qq.com", "测试邮箱", "测试内容");
    }

    @Test
    public void  testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "tomtiddler");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("1956354744@qq.com","测试标题",content);
    }
}
