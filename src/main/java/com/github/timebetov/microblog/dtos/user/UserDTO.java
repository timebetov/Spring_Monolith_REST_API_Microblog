package com.github.timebetov.microblog.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        description = "Schema to hold retrieved User information"
)
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String bio;

    @Schema(description = "Field representing Role of User", example = "USER || ADMIN")
    private String role;
    private int followers;
    private int following;

    @Schema(description = "Represents how many moments (posts) user has")
    private int moments;
}
