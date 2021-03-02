package com.tomtiddler.community.service;

import com.tomtiddler.community.dao.DiscussPostMapper;
import com.tomtiddler.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public  int findDiscussPostRow(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }
}
