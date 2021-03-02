package com.tomtiddler.community.controller;

import com.tomtiddler.community.entity.DiscussPost;
import com.tomtiddler.community.entity.Page;
import com.tomtiddler.community.entity.User;
import com.tomtiddler.community.service.DiscussPostService;
import com.tomtiddler.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        //SpringMvc  page 会自动给装入page中
        page.setRows(discussPostService.findDiscussPostRow(0));
        page.setPath("/index");
        System.out.println("当前的页码为");
        System.out.println(page.getCurrent());

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post :
                    list) {
                Map<String, Object> map = new HashMap<>();

                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "index";
    }
}
