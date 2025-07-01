package com.tmv.core.persistence;

import com.tmv.core.model.Imei;
import com.tmv.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository  extends JpaRepository<User, Long>  {
    Optional<User> findByUsername(String username);
}
