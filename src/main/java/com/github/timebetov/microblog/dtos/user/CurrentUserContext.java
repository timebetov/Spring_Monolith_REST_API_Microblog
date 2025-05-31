package com.github.timebetov.microblog.dtos.user;

import com.github.timebetov.microblog.models.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Schema to hold Current Authenticated User information")
public class CurrentUserContext {

    @Schema(example = "1")
    private Long userId;
    @Schema(example = "user1")
    private String username;
    @Schema(example = "user1@test.com")
    private String email;
    @Schema(example = "USER")
    private User.Role role;

    public boolean isAdmin() {
        return role == User.Role.ADMIN;
    }
}
