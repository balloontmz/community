package com.tomtiddler.community.service;

import com.tomtiddler.community.dao.AlphaDao;
import com.tomtiddler.community.dao.DiscussPostMapper;
import com.tomtiddler.community.dao.UserMapper;
import com.tomtiddler.community.entity.DiscussPost;
import com.tomtiddler.community.entity.User;
import com.tomtiddler.community.util.CommunityUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("singleton")
@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public AlphaService() {
        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init() {
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destory() {
        System.out.println("销毁AlphaService");
    }

    public String find(){
        return alphaDao.select();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Object save1() {
        //新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123456" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("hello");
        post.setContent("新人报到");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        Integer.valueOf("abc");//报错
        return "ok";
    }

    public Object save2() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>(){
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //新增用户
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123456" + user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(new Date());

                userMapper.insertUser(user);

                //新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("你好");
                post.setContent("我是新人");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);
                Integer.valueOf("abc");//报错

                return "ok";
            }
        });
        
    }
}
