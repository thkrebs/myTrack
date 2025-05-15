package com.tmv.core.service;

import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.Journey;
import com.tmv.core.persistence.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;


@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO getUserByName(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + id));

        // Benutzerabfrage aus der Datenbank
        String userQuery = "SELECT username, password, enabled FROM users WHERE username = ?";
        UserDetails user = jdbcTemplate.queryForObject(userQuery,
                (rs, rowNum) -> {
                    String uname = rs.getString("username");
                    String pwd = rs.getString("password");
                    boolean enabled = rs.getBoolean("enabled");

                    UserBuilder builder = User.withUsername(uname);
                    builder.password(pwd);
                    builder.disabled(!enabled);
                    return builder.build();
                },
                username);

        if (user == null) {
            throw new UsernameNotFoundException("Benutzer nicht gefunden: " + username);
        }

        // Rollen aus der Datenbank abrufen
        String rolesQuery = "SELECT authority FROM authorities WHERE username = ?";
        var roles = jdbcTemplate.queryForList(rolesQuery, String.class, username);

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles.toArray(new String[0]))
                .build();
    }
}