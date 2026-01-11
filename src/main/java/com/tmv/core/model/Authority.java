package com.tmv.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "authority")
public class Authority  implements GrantedAuthority
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // E.g., ROLE_ADMIN, READ_PRIVILEGES, WRITE_PRIVILEGES

    @Column(nullable = false)
    private boolean isRole; // True if it is a role, false if it's an authority (fine-grained permission)

    // Umkehr-Beziehung zur User-Seite
    @ManyToMany(mappedBy = "authorities", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    @Override
    public String getAuthority() {
        return name;
    }
}