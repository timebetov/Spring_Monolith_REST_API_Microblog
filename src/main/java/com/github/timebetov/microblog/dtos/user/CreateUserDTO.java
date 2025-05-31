package com.github.timebetov.microblog.dtos.user;

import com.github.timebetov.microblog.validations.FieldsValueMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldsValueMatch(
        field = "password",
        fieldMatch = "confirmPassword",
        message = "Passwords do not match!"
)
@Schema(name = "CreateUserRequestDTO", description = "Schema to hold new user information to save")
public class CreateUserDTO {

    @Schema(example = "user1")
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 4, message = "Username should be at least 4 characters long")
    private String username;

    @Schema(example = "user1@test.com")
    @NotBlank(message = "Email Address cannot be blank")
    @Email(message = "Email is not a valid")
    private String email;

    @Schema(example = "user1PWD")
    @NotBlank(message = "Password must be not blank")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

    @Schema(description = "Password and Confirm Password fields must match", example = "user1PWD")
    @NotBlank(message = "Confirm Password must be not blank")
    private String confirmPassword;
}
