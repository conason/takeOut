package com.conason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conason.reggie.entity.User;
import com.conason.reggie.service.UserService;
import com.conason.reggie.utils.SMSUtils;
import com.conason.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 验证码发送
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public Request<String> code(@RequestBody User user, HttpSession session){

        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){

            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            log.info(code);
            //阿里云短信服务
            //SMSUtils.sendMessage("注册审批的标签","",phone,code);

            session.setAttribute(phone,code);

            Request.success("验证码发送成功");
        }

        return Request.error("验证码发送失败");
    }

    /**
     * 登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public Request<String> login(@RequestBody Map map, HttpSession session){
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        Object codeInSession = session.getAttribute(phone);

        if(codeInSession != null && codeInSession.equals(code)){
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(userLambdaQueryWrapper);

            if (user == null){
                User user1 = new User();
                user1.setPhone(phone);
                user1.setStatus(1);
                userService.save(user1);
                session.setAttribute("user",user1.getId());
                return Request.success("登录成功");
            }

            session.setAttribute("user",user.getId());

            return Request.success("登录成功");
        }

        return Request.error("登录失败");
    }
}

