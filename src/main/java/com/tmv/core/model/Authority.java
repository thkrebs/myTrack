package com.tmv.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "authority")
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // E.g., ROLE_ADMIN, READ_PRIVILEGES, WRITE_PRIVILEGES

    @Column(nullable = false)
    private boolean isRole; // True if it is a role, false if it's an authority (fine-grained permission)

}