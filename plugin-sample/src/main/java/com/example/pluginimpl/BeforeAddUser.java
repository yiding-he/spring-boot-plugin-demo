package com.example.pluginimpl;

import com.example.plugin.BeforeAddUserPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 插件实现类。注意这个插件在运行时会被自动纳入 Spring 上下文，并可以自由使用上下文中的任何 Bean
 */
@Component
@Slf4j
public class BeforeAddUser implements BeforeAddUserPlugin {

    // 注意这个 JdbcTemplate 在插件项目本身没有定义，而是在运行时被 main-service 注入的
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 假设需求为不允许重复的用户名，一旦查询到用户名已经存在则返回 false
     */
    @Override
    public boolean beforeAddUser(String username, String password) {
        try {
            jdbcTemplate.queryForMap("SELECT * FROM users WHERE name=?", username);
            log.warn("User '{}' already exists", username);
            return false;
        } catch (EmptyResultDataAccessException e) {
            return true;
        }
    }
}
