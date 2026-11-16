package com.byteforge.setorabackend.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private int id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String username = firstName;

    //always saved in lower case, used for login
    @Column(nullable = false, unique = true)
    private String email;

    @Column(columnDefinition = "text")
    private String phoneNumber;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column
    private UserRole role=UserRole.CLIENT;

    //add the rest later on

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public String getName(){
        return this.firstName + " " + this.lastName;
    }

    public User(String firstName, String lastName, String email, String phoneNumber, String passwordHash) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = firstName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
    }
}
