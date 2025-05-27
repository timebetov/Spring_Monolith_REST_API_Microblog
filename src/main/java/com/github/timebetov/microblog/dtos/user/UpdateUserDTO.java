package com.github.timebetov.microblog.dtos.user;

import com.github.timebetov.microblog.validations.AtLeasOneFieldPresent;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AtLeasOneFieldPresent
public class UpdateUserDTO {

    @Size(min = 4, message = "Username should be at least 4 characters long")
    private String username;

    @Email(message = "Email is not a valid")
    private String email;

    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

    private String bio;
    private String picture;
}
