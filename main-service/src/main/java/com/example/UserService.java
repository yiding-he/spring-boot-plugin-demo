package com.example;

import com.example.plugin.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserService {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean addUser(String username, String password) {
        for (var plugin : pluginManager.getPlugins()) {
            if (!plugin.beforeAddUser(username, password)) {
                return false;
            }
        }
        jdbcTemplate.update("insert into users (name, pass) values (?, ?)", username, password);
        return true;
    }

    public Map<String, Object> findUser(String username) {
        return jdbcTemplate.queryForMap("select * from users where name = ?", username);
    }
}
