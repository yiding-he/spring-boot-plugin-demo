package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}

@Component
@Slf4j
class CreateTable implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserService userService;

    /**
     * 使用插件的例子
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 初始化表
        jdbcTemplate.execute("""
            CREATE TABLE users(
              name VARCHAR(50) NOT NULL PRIMARY KEY,
              pass VARCHAR(64) NOT NULL
            )""");
        log.info("table users created.");

        // 尝试两次插入记录，第二次会因为插件检查到用户名已存在，而拒绝执行并打出相应日志
        log.info("creating user: " + userService.addUser("user1", "pass1"));
        log.info("creating user: " + userService.addUser("user1", "pass2"));
        log.info("find user: " + userService.findUser("user1"));
    }
}

