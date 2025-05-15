package com.github.timebetov.microblog.controllers;

import com.github.timebetov.microblog.dtos.ResponseDTO;
import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.UpdateUserDTO;
import com.github.timebetov.microblog.dtos.user.UserDTO;
import com.github.timebetov.microblog.services.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/users", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO> registerUser(@Valid @RequestBody CreateUserDTO requestDTO) {

        userService.createUser(requestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(HttpStatus.CREATED, "User registered successfully"));
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<UserDTO>> fetchUsers() {

        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(users);
    }

    @GetMapping("/fetch/{userId}")
    public ResponseEntity<UserDTO> fetchUserById(@PathVariable Long userId) {

        UserDTO foundUser = userService.getById(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(foundUser);
    }


    @GetMapping("/fetch/@{username}")
    public ResponseEntity<UserDTO> fetchUserByUsername(@PathVariable String username) {

        UserDTO foundUser = userService.getByUsername(username);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(foundUser);
    }

    @GetMapping("/fetch/email/{email}")
    public ResponseEntity<UserDTO> fetchUserByEmail(@PathVariable String email) {

        UserDTO foundUser = userService.getByEmail(email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(foundUser);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ResponseDTO> updateUser(@PathVariable Long userId, @RequestBody UpdateUserDTO userDetails) {

        userService.updateUser(userId, userDetails);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(HttpStatus.OK, "User updated successfully"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ResponseDTO> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(HttpStatus.OK, "User deleted successfully"));
    }

}
