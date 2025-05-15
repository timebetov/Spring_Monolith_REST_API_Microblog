package com.github.timebetov.microblog.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserDTO {

    private String username;
    private String email;
    private String password;
    private String bio;
    private String picture;
    private String role;
}
