package com.tomtiddler.community;

import com.tomtiddler.community.util.SensitiveFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "这里可以赌博，可以嫖娼，可以吸毒，可以开票，哈哈哈！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        String text2 = "这里可以赌博⭐，可以嫖⭐娼，可以吸毒，可以开票，哈哈哈！";
        text2 = sensitiveFilter.filter(text2);
        System.out.println(text2);
    }
}
