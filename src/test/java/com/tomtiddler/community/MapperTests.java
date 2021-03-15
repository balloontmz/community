package com.tomtiddler.community;

import com.tomtiddler.community.dao.DiscussPostMapper;
import com.tomtiddler.community.dao.LoginTicketMapper;
import com.tomtiddler.community.dao.MessageMapper;
import com.tomtiddler.community.dao.UserMapper;
import com.tomtiddler.community.entity.DiscussPost;
import com.tomtiddler.community.entity.LoginTicket;
import com.tomtiddler.community.entity.Message;
import com.tomtiddler.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser(){
        User user =  userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public  void testInsertUser() {
        User user  = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nocoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

     @Test
     public void updateUser() {
        int rows = userMapper.updateStatus(150 ,1);
        System.out.println(rows);

         rows = userMapper.updateHeader(150 ,"http://www.newcoder.com/102.png");
         System.out.println(rows);

         rows = userMapper.updatePassword(150 ,"hello");
         System.out.println(rows);
     }

     @Autowired
     private DiscussPostMapper discussPostMapper;

     @Test
    public void testSelectPosts() {
       List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
         for (DiscussPost post :
                 list) {
             System.out.println(post);
         }
         int rows = discussPostMapper.selectDiscussPostRows(149);
         System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(1);
        loginTicket.setStatus(0);
        loginTicket.setTicket("abc");
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000  *60 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectLetters() {
        List<Message> msgs = messageMapper.selectConversations(111, 0, 20);
        for (Message message : msgs) {
            System.out.println(message);
        }

        int cnt = messageMapper.selectConversationCount(111);

        System.out.println("当前会话数量为：" + cnt);

        List<Message> letters = messageMapper.selectLetters("111_112", 0, 20);
        for (Message letter : letters) {
            System.out.println(letter);
        }

        int unreadCnt = messageMapper.selectLetterUnreadCount(111, null);
        System.out.println("未读总数为：" + unreadCnt);

        int unreadCnt2 = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println("未读会话2 " + unreadCnt2);
    }
}
