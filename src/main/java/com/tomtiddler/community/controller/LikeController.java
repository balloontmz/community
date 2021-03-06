package com.tomtiddler.community.controller;

import java.util.HashMap;
import java.util.Map;

import com.tomtiddler.community.annotation.LoginRequired;
import com.tomtiddler.community.entity.Event;
import com.tomtiddler.community.entity.User;
import com.tomtiddler.community.event.EventProducer;
import com.tomtiddler.community.service.LikeService;
import com.tomtiddler.community.util.CommunityConst;
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
public class LikeController implements CommunityConst {
    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(LikeController.class);

    @LoginRequired
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();
        logger.info("test1");
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        logger.info("test2");
        // 数量
        long likeCnt = likeService.findEntityLikeCount(entityType, entityId);
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        logger.info("test3");
        Map<String, Object> result = new HashMap<>();
        result.put("likeCount", likeCnt);
        result.put("likeStatus", likeStatus);

        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event().setTopic(TOPIC_LIKE).setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType).setEntityId(entityId).setEntityUserId(entityUserId)
                    .setData("postId", postId);

            eventProducer.fireEvent(event);
        }
        return CommunityUtil.getJSONString(0, "操作成功", result);
    }
}
