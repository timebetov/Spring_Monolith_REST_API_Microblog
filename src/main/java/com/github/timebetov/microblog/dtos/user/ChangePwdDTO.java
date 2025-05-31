package com.github.timebetov.microblog.dtos.user;

import com.github.timebetov.microblog.validations.FieldsValueMatch;
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
@FieldsValueMatch(
        field = "newPassword",
        fieldMatch = "confirmPassword",
        message = "Passwords do not match!"
)
@Schema(description = "Schema to hold information Current and New Password used to change")
public class ChangePwdDTO {

    @Schema(example = "user1PWD")
    @NotBlank(message = "Current Password must be not blank")
    @Size(min = 8, max = 20, message = "Current Password must be between 8 and 20 characters long")
    private String currentPassword;

    @Schema(example = "user1newPWD")
    @NotBlank(message = "New Password must be not blank")
    @Size(min = 8, max = 20, message = "New Password must be between 8 and 20 characters long")
    private String newPassword;

    @Schema(example = "user1newPWD")
    @NotBlank(message = "Confirm Password must be not blank")
    private String confirmPassword;
}
