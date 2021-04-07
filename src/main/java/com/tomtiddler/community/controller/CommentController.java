package com.tomtiddler.community.controller;

import java.util.Date;

import com.tomtiddler.community.entity.Comment;
import com.tomtiddler.community.entity.DiscussPost;
import com.tomtiddler.community.entity.Event;
import com.tomtiddler.community.event.EventProducer;
import com.tomtiddler.community.service.CommentService;
import com.tomtiddler.community.service.DiscussPostService;
import com.tomtiddler.community.util.CommunityConst;
import com.tomtiddler.community.util.HostHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConst {

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService postService;

    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 触发评论事件
        Event event = new Event().setTopic(TOPIC_COMMENT).setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType()).setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost post = postService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(post.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        eventProducer.fireEvent(event);
        
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
