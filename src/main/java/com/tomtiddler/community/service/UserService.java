package com.tomtiddler.community.service;

import com.tomtiddler.community.dao.LoginTicketMapper;
import com.tomtiddler.community.dao.UserMapper;
import com.tomtiddler.community.entity.LoginTicket;
import com.tomtiddler.community.entity.User;
import com.tomtiddler.community.util.CommunityConst;
import com.tomtiddler.community.util.CommunityUtil;
import com.tomtiddler.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConst {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int userId) {
        return userMapper.selectById(userId);
    };

    /**
     * 注册用户
     * @param user
     * @return
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMessage", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMessage", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMessage", "邮箱不能为空！");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMessage", "该账号已存在！");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMessage", "该邮箱已被注册！");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID());
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);//mybatis.configuration.useGeneratedKeys id回填

        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活邮件", content);

        return map; // map 为空代表没有问题
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }
        return ACTIVATION_FAILURE;
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        System.out.printf("计算出的密码值为{%s}", password);
        logger.info("计算出的密码值为：" + password);
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 退出登录
     */
    public void logout(String ticket) {
        LoginTicket ticket2 = loginTicketMapper.selectByTicket(ticket);
        System.out.println("注销之前的ticket值为：" + ticket2);
        loginTicketMapper.updateStatus(ticket, 1);//置为不可用
        ticket2 = loginTicketMapper.selectByTicket(ticket);
        System.out.println("注销之后的ticket值为：" + ticket2);
    }

    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }

    public int changePwd(int userId, String pwd) {
        return userMapper.updatePassword(userId, pwd);
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }
}