package com.example.springbootcomtrade.pojo;

import javax.persistence.*;

/**
 * @author nan chao
 * @date 2024-10-25 16:41
 */

@Entity
@Table(name = "user") // 指定数据库表名为小写的 user
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
