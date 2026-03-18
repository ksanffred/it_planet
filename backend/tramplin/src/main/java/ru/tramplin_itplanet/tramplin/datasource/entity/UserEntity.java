package ru.tramplin_itplanet.tramplin.datasource.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public boolean isVerified() { return isVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setEmail(String email) { this.email = email; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(UserRole role) { this.role = role; }
    public void setVerified(boolean verified) { isVerified = verified; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
