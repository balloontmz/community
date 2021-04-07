package com.tomtiddler.community.controller;

import java.util.List;
import java.util.Map;

import com.tomtiddler.community.annotation.LoginRequired;
import com.tomtiddler.community.entity.Event;
import com.tomtiddler.community.entity.Page;
import com.tomtiddler.community.entity.User;
import com.tomtiddler.community.event.EventProducer;
import com.tomtiddler.community.service.FollowService;
import com.tomtiddler.community.service.UserService;
import com.tomtiddler.community.util.CommunityConst;
import com.tomtiddler.community.util.CommunityUtil;
import com.tomtiddler.community.util.HostHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FollowController implements CommunityConst {
    private static final Logger logger = LoggerFactory.getLogger(FollowController.class);

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @LoginRequired
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event().setTopic(TOPIC_FOLLOW).setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType).setEntityId(entityId).setEntityUserId(entityId);
        eventProducer.fireEvent(event);

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

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));// 补充是否已关注
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        logger.info("查询出的用户为：" + user.toString());
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));
        logger.info("分页对象构造完成之后");

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));// 补充是否已关注
            }
        }
        logger.info("取到的结果为：");
        logger.info(userList.toString());
        model.addAttribute("users", userList);

        return "/site/follower";
    }

    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
}
