package com.example.springbootcomtrade.controller;

import com.example.springbootcomtrade.dao.UserDao;
import com.example.springbootcomtrade.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpServletRequest; // 导入HttpServletRequest

/**
 * @author nan chao
 * @date 2024-10-25 16:36
 */

@Controller
public class LoginController {

    @Autowired
    private UserDao userDao; // 注入 UserDao


    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        RedirectAttributes redirectAttributes,
                        HttpServletRequest request) {
        System.out.println("接收到的用户名: " + username);
        System.out.println("接收到的密码: " + password);

        User user = userDao.findByUsername(username);

        if (user != null) {
            // 打印查询到的用户信息
            System.out.println("查询到的用户名: " + user.getUsername());
            System.out.println("查询到的用户密码: " + user.getPassword());


            // 直接比较输入的密码和数据库中的密码
            if (password.equals(user.getPassword())) {
                System.out.println("登录成功");
                // 打印会话ID
                System.out.println("会话ID: " + request.getSession().getId());
                return "redirect:/select_comtrade_or_csv";
            } else {
                System.out.println("登录失败");
                redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
                return "redirect:/index.html";
            }
        } else {
            System.out.println("用户不存在");
            redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
            return "redirect:/index.html";
        }

    }

    @GetMapping("/index.html")
    public String loginPage() {
        return "index"; // 返回 index.html 视图
    }

    @GetMapping("/select_comtrade_or_csv")
    public String selectComtradeOrCsv() {
        return "select_comtrade_or_csv"; // 返回对应的 HTML 视图
    }





}
