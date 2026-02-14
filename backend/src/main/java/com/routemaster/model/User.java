package com.routemaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;

    private String password; // BCrypt encoded

    private String email;

    private String fullName;

    private String phone;

    private Set<Role> roles = new HashSet<>();

    private boolean enabled = true;

    public enum Role {
        ROLE_ADMIN,
        ROLE_DRIVER,
        ROLE_VIEWER
    }
}
