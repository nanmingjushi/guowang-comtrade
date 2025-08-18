package com.example.springbootcomtrade.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 禁用 CSRF 保护
                .authorizeRequests()
                .antMatchers("/", "/index","/index.html", "/css/**", "/js/**", "/images/**", "/data/processData").permitAll() // 允许所有用户访问这些路径

                .and()
                .formLogin()
                .loginPage("/index") // 自定义登录页面的路径
                .permitAll()
                .and()
                .logout()
                .permitAll(); // 允许所有用户登出
    }
}