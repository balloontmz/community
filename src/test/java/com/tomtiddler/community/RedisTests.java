package com.tomtiddler.community;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {
    @Autowired
    private RedisTemplate<String, Object> template;

    @Test
    public void testStrings() {
        String redisKey = "test:count";
        template.opsForValue().set(redisKey, 100);

        System.out.println(template.opsForValue().get(redisKey));

        template.opsForValue().increment(redisKey);

        System.out.println(template.opsForValue().get(redisKey));

        template.opsForValue().decrement(redisKey);

        System.out.println(template.opsForValue().get(redisKey));
    }

    @Test
    public void testHash() {
        String redisKey = "test:user";
        template.opsForHash().put(redisKey, "id", 1);
        template.opsForHash().put(redisKey, "username", "tomtiddler");

        System.out.println(template.opsForHash().get(redisKey, "id"));
        System.out.println(template.opsForHash().get(redisKey, "username"));
    }

    @Test
    public void testList() {
        String redisKey = "test:ids";

        template.opsForList().leftPush(redisKey, 101);
        template.opsForList().leftPush(redisKey, 102);
        template.opsForList().leftPush(redisKey, 103);

        System.out.println(template.opsForList().size(redisKey));
        System.out.println(template.opsForList().index(redisKey, 0));
        System.out.println(template.opsForList().range(redisKey, 0, -1));

        template.opsForList().leftPop(redisKey);

        // template.delete(redisKey);
    }

    @Test
    public void testSets() {
        String redisKey = "test:teachers";
        template.opsForSet().add(redisKey, "alice", "bob", "john", "tomtiddler", "tom");

        System.out.println(template.opsForSet().size(redisKey));
        System.out.println(template.opsForSet().pop(redisKey));
        System.out.println(template.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedSets() {
        String redisKey = "test:students";
        template.opsForZSet().add(redisKey, "alice", 80);
        template.opsForZSet().add(redisKey, "bob", 90);
        template.opsForZSet().add(redisKey, "john", 100);
        template.opsForZSet().add(redisKey, "bruce", 70);
        template.opsForZSet().add(redisKey, "krustu", 60);

        System.out.println(template.opsForZSet().zCard(redisKey));//多少个
        System.out.println(template.opsForZSet().score(redisKey, "john"));//多少分
        System.out.println(template.opsForZSet().rank(redisKey, "john"));//多少名
        System.out.println(template.opsForZSet().range(redisKey, 0, -1));//所有数据
    }

    @Test
    public void testKeys() {
        template.delete("test:user");
        System.out.println(template.hasKey("test:user"));

        template.expire("test:students", 10, TimeUnit.SECONDS);

        // template.keys("test*");//基本不会在代码中使用
    }

    //多次访问同一个 key
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        BoundValueOperations operations = template.boundValueOps(redisKey);
        operations.increment();
        operations.increment();

        System.out.println(operations.get());
        // BoundHashOperations
        // BoundSetOperations
        // BoundListOperations
        // BoundZSetOperations
    }

    //不要在 redis 事务中查询,不会返回结果
    //redis 事务是先将操作放入队列,提交时统一交给 redis 服务器执行
    @Test
    public void testTransactions() {
        Object obj = template.execute(new SessionCallback(){

            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";

                operations.multi();//启用事务

                operations.opsForSet().add(redisKey, "张三");
                operations.opsForSet().add(redisKey, "李四");
                operations.opsForSet().add(redisKey, "王五");
                
                System.out.println(operations.opsForSet().members(redisKey));

                return operations.exec();//提交事务
            }
            
        });

        System.out.print(obj);
    }
}
