package com.github.timebetov.microblog.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Schema to hold information to perform Authentication")
public class LoginUserDTO {

    @Schema(description = "Username cannot be blank", example = "user1")
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 4, message = "Username should be at least 4 characters long")
    private String username;

    @Schema(example = "someP@sswordUser1")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;
}
