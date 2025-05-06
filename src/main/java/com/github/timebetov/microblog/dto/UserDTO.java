package com.github.timebetov.microblog.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private String bio;
    private String picture;
    private String role;
}
