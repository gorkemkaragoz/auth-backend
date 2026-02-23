package com.gorkem.auth_service.repos;

import com.gorkem.auth_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Kullanıcıyı email ile bulmak için (Login aşamasında lazım olacak)
    Optional<User> findByEmail(String email);

    // Email zaten kayıtlı mı kontrolü için (Register aşamasında lazım olacak)
    boolean existsByEmail(String email);
}