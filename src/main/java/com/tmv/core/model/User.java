package com.tmv.core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "_user")
public class User implements UserDetails
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private String email;

    @Column(nullable=false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Long features;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "userid"),
            inverseJoinColumns = @JoinColumn(name = "authorityid")
    )
    private Set<Authority> authorities = new HashSet<>();

    // Include IMEIs owned by this user
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Imei> imeis = new HashSet<>();

    // Include journeys owned by this user
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Journey> journeys = new HashSet<>();


    // --------------------- UserDetails-Methoden ---------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Kann ggf. angepasst werden, falls du eine Ablaufprüfung implementieren möchtest
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Kann ggf. angepasst werden, wenn du eine Sperrfunktion implementieren möchtest
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Kann angepasst werden, falls du z. B. Passwortablauf implementieren möchtest
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

}