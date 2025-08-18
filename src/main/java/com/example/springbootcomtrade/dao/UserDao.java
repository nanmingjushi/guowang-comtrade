package com.example.springbootcomtrade.dao;
import com.example.springbootcomtrade.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * @author nan chao
 * @date 2024-10-25 16:50
 */


@Repository
public interface UserDao extends JpaRepository<User, Long>{

    User findByUsername(String username);

}
