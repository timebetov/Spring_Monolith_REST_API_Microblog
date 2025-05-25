package com.github.timebetov.microblog.dtos.user;

import com.github.timebetov.microblog.validations.FieldsValueMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 4, message = "Username should be at least 4 characters long")
    private String username;

    @NotEmpty(message = "Email Address cannot be empty")
    @Email(message = "Email is not a valid")
    private String email;

    @NotNull(message = "Password must be not null")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

    @NotNull(message = "Confirm Password must be not null")
    @Size(min = 8, max = 20, message = "Confirm Password must be between 8 and 20 characters long")
    private String confirmPassword;
}
