package com.tomtiddler.community.dao;

import com.tomtiddler.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId, int offset, int limit);

    //@Param() 取别名
    //需要动态拼接 sql， 函数有且仅有一个条件，并且会用到这个条件，则必须申明别名
    int selectDiscussPostRows(@Param("userId")int userId);
}
