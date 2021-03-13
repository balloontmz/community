package com.tomtiddler.community.dao;

import java.util.List;

import com.tomtiddler.community.entity.Comment;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper {
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);
}
