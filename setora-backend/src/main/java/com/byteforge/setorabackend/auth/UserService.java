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
            String lastName,
            String firstName,
            String email,
            String phoneNumber
    ) {
        if (lastName != null) {
            if (lastName.isBlank())
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "lastName cannot be blank"
                );

            client.setLastName(lastName.trim());
        }

        if (firstName != null) {
            if (firstName.isBlank())
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "firstName cannot be blank"
                );

            client.setFirstName(firstName.trim());
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

    public void updateAvatarPathByUsername(int userId, String filename) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setAvatarPath(filename);//TODO maybe you can get user directly in controller
        userRepository.save(user);
    }
}