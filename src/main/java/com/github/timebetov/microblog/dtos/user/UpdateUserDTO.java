package com.github.timebetov.microblog.dtos.user;

import com.github.timebetov.microblog.validations.AtLeasOneFieldPresent;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Schema to hold information in order to update existing user information")
public class UpdateUserDTO {

    @Schema(description = "Username information should be at least 4 characters long", example = "user1")
    @Size(min = 4, message = "Username should be at least 4 characters long")
    private String username;

    @Schema(description = "Email information", example = "user1@test.com")
    @Email(message = "Email is not a valid")
    private String email;

    @Schema(description = "Bio information", example = "Hy, I am a Software Engineer from L.A.")
    private String bio;
}
