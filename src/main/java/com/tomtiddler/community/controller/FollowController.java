package com.tomtiddler.community.controller;

import com.tomtiddler.community.annotation.LoginRequired;
import com.tomtiddler.community.entity.User;
import com.tomtiddler.community.service.FollowService;
import com.tomtiddler.community.util.CommunityUtil;
import com.tomtiddler.community.util.HostHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FollowController {
    private static final Logger logger = LoggerFactory.getLogger(FollowController.class);

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        logger.info("返回已关注之前");
        return CommunityUtil.getJSONString(0, "已关注");
    }

    @LoginRequired
    @RequestMapping(path = "/unFollow", method = RequestMethod.POST)
    @ResponseBody
    public String unFollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unFollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取关");
    }
}
