package com.byteforge.setorabackend.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String firstname;

    //always saved in lower case, used for login
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String phoneNumber;

    //add the rest later on

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public String getFullName() {
        return surname + " " + firstname;
    }

    public String getNameInitials() {
        String name = getFullName();
        if (name == null || name.isBlank()) {
            return "";
        }

        String[] parts = name.trim().split("[\\s\\-]+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return "";
        }
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
