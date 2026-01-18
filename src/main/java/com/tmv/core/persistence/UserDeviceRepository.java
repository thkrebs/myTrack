package com.tmv.core.persistence;

import com.tmv.core.model.User;
import com.tmv.core.model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByToken(String token);
    Optional<UserDevice> findByUserAndToken(User user, String token);
    List<UserDevice> findAllByUser(User user);
    void deleteByToken(String token);
}
