package com.franchiseproject.identityaccessservice.entity;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements Persistable<UUID> {
    @Id
//    @GeneratedValue
//    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(columnDefinition = "UUID", nullable = false, unique = true)
    UUID id;
    @Column(unique = true, nullable = false)
    String username;
    @Column(name = "full_name", nullable = false)
    String fullName;
    @Column(name = "password_hash", nullable = true)
    String passwordHash;
    @Column(nullable = true, unique = true)
    String email;
    @Column(unique = true)
    String phone;
    boolean gender;
    @Column(name = "is_verify_email")
    boolean isVerifyEmail;
    @Column(name = "avatar_url")
    String avatarUrl;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    UserStatus status;
    @Column(name = "franchise_id", columnDefinition = "UUID", nullable = true)
    UUID franchiseId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", columnDefinition = "UUID", nullable = false)
    Role role;
    @Column(name = "last_login")
    Instant lastLogin;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return false;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}