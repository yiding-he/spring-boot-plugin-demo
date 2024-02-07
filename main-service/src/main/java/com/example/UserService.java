package com.example;

import com.example.plugin.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 使用插件的例子
 */
@Component
public class UserService {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean addUser(String username, String password) {

        // 通过插件来决定是否要执行添加用户记录，任何一个插件返回 false 则不执行
        for (var plugin : pluginManager.getPlugins()) {
            if (!plugin.beforeAddUser(username, password)) {
                return false;
            }
        }

        jdbcTemplate.update("INSERT INTO users (name, pass) VALUES (?, ?)", username, password);
        return true;
    }

    public Map<String, Object> findUser(String username) {
        return jdbcTemplate.queryForMap("SELECT * FROM users WHERE name = ?", username);
    }
}
