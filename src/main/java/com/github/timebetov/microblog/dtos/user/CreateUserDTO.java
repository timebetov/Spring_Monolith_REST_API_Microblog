package com.github.timebetov.microblog.dtos.user;

import com.github.timebetov.microblog.validations.FieldsValueMatch;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldsValueMatch(
        field = "password",
        fieldMatch = "confirmPassword",
        message = "Passwords do not match!"
)
public class CreateUserDTO {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 4, message = "Username should be at least 4 characters long")
    private String username;

    @NotBlank(message = "Email Address cannot be blank")
    @Email(message = "Email is not a valid")
    private String email;

    @NotBlank(message = "Password must be not blank")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

    @NotBlank(message = "Confirm Password must be not blank")
    private String confirmPassword;
}
