package com.tmv.core.persistence;

import com.tmv.core.model.ApiToken;
import com.tmv.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiTokenRepository  extends JpaRepository<ApiToken, Long> {
    Optional<ApiToken> findById(Long tokenId);
    Optional<ApiToken> findByToken(String token);
    void deleteByToken(String token);
    List<ApiToken> findByUser(User user);

}
