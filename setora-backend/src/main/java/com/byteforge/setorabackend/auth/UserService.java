package com.byteforge.setorabackend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    public void updateClientDetails(
            User client,
            String surname,
            String firstname,
            String email,
            String phoneNumber
    ) {
        if (surname != null) {
            if (surname.isBlank())
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Surname cannot be blank"
                );

            client.setSurname(surname.trim());
        }

        if (firstname != null) {
            if (firstname.isBlank())
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Firstname cannot be blank"
                );

            client.setFirstname(firstname.trim());
        }

        if (email != null) {
            if (email.isBlank())
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Email cannot be blank"
                );

            client.setEmail(email);
        }

        if (phoneNumber != null)
            client.setPhoneNumber(phoneNumber.trim());

        try {
            userRepository.saveAndFlush(client);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Client details violate a database constraint",
                    e
            );
        }
    }
}