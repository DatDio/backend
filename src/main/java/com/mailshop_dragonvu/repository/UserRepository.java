package com.mailshop_dragonvu.repository;

import com.mailshop_dragonvu.entity.User;
import com.mailshop_dragonvu.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.authProvider = :provider AND u.providerId = :providerId")
    Optional<User> findByAuthProviderAndProviderId(AuthProvider provider, String providerId);

}
