package com.tomtiddler.community.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.tomtiddler.community.annotation.LoginRequired;
import com.tomtiddler.community.entity.User;
import com.tomtiddler.community.service.FollowService;
import com.tomtiddler.community.service.LikeService;
import com.tomtiddler.community.service.UserService;
import com.tomtiddler.community.util.CommunityConst;
import com.tomtiddler.community.util.CommunityUtil;
import com.tomtiddler.community.util.HostHolder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConst {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;//上传路径

    @Value("${community.path.domain}")
    private String domain;//本机域名

    @Value("${server.servlet.context-path}")
    private String contextPath;//当前服务路径

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImg, Model model) {
        if (headerImg == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }

        String fileName = headerImg.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确");
            return "/site/setting";
        }

        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储文件
            headerImg.transferTo(dest);    
        } catch (IOException e) {
            logger.error("上传文件失败： " + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常", e);
        }

        //更新当前用户头像路径
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(HttpServletResponse response, @PathVariable("fileName") String fileName) {
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        //响应图片
        response.setContentType("image/" + suffix);
        try (
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(fileName);
        ) 
        {   
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }    
        } catch (IOException e) {
            logger.error("读取头像失败", e.getMessage());
        }
        return;
    }

    @RequestMapping(path = "/changePwd", method = RequestMethod.POST)
    public String changePwd(String oldPassword, String newPassword, Model model) {
        if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
            logger.error("密码为空");
            model.addAttribute("passwordMsg", "密码不能为空！");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            logger.error("密码不正确");
            model.addAttribute("passwordMsg", "密码不正确！");
            return "/site/setting";
        }

        userService.changePwd(user.getId(),  CommunityUtil.md5(newPassword + user.getSalt()));

        model.addAttribute("msg", "密码修改成功！");
        model.addAttribute("target", "/index");
        //修改密码成功跳转
        return "site/operate-result";
    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User currentUser = hostHolder.getUser();
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        
        //用户基本信息
        model.addAttribute("user", user);
        //获赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        //是否已关注
        boolean hasFollowed = false;
        if (currentUser != null) {
            hasFollowed = followService.hasFollowed(currentUser.getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }
}
