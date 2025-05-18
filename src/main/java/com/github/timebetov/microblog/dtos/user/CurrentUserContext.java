package com.github.timebetov.microblog.dtos.user;

import com.github.timebetov.microblog.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentUserContext {

    private Long userId;
    private String username;
    private String email;
    private User.Role role;

    public boolean isAdmin() {
        return role == User.Role.ADMIN;
    }
}
