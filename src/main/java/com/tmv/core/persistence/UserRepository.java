package com.tmv.core.persistence;

import com.tmv.core.model.Imei;
import com.tmv.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<User, Long>  {
    User findByUsername(String username);
}
