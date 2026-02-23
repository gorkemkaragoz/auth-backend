package com.gorkem.auth_service.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String otpCode;
    private LocalDateTime otpExpiry;

    @Column(updatable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
}
