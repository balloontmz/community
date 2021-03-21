package com.tomtiddler.community.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tomtiddler.community.entity.User;
import com.tomtiddler.community.util.CommunityConst;
import com.tomtiddler.community.util.RedisKeyUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class FollowService implements CommunityConst {
    private static final Logger logger = LoggerFactory.getLogger(FollowService.class);
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 关注
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback(){
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
            
        });
    }

    /**
     * 取消关注
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void unFollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback(){
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
            
        });
    }

    //查询关注的实体的数量
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询实体的粉丝的数量
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null ? true :false;
    }

    //查询某个用户关注的人
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object targetId : targetIds) {
            Integer targetIdInt = (Integer)targetId;
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetIdInt);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetIdInt);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

    //查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        logger.info("调用查询粉丝数方法");
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        //set 无序  redis jar 实现的该接口有序 的实现是有序的
        logger.info("offset 为：" + offset + "limit为：" + limit + "查询key为：" + followerKey);
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object targetId : targetIds) {
            Integer targetIdInt = (Integer)targetId;
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetIdInt);
            // logger.info(user.toString());
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetIdInt);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

}
