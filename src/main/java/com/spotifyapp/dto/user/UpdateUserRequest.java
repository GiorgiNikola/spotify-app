package com.spotifyapp.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 3, max = 50)
    private String username;

    private String email;
    private String firstName;
    private String lastName;
}

