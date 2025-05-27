package com.github.timebetov.microblog.controllers;

import com.github.timebetov.microblog.dtos.ResponseDTO;
import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.dtos.user.UpdateUserDTO;
import com.github.timebetov.microblog.dtos.user.UserDTO;
import com.github.timebetov.microblog.services.IFollowService;
import com.github.timebetov.microblog.services.IUserService;
import com.github.timebetov.microblog.utils.SecurityUtils;
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
    private final IFollowService followService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO> registerUser(@RequestBody @Valid CreateUserDTO requestDTO) {

        userService.createUser(requestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(HttpStatus.CREATED, "User registered successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile() {

        CurrentUserContext currentUser = SecurityUtils.getCurrentUserContext();
        UserDTO profile = userService.getById(currentUser.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profile);
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
    public ResponseEntity<ResponseDTO> updateUser(@PathVariable Long userId,
                                                  @RequestBody @Valid UpdateUserDTO userDetails) {

        userService.updateUser(userId, userDetails);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(HttpStatus.OK, "User updated successfully"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/follow/{userId}")
    public ResponseEntity<ResponseDTO> followUser(@PathVariable Long userId) {

        CurrentUserContext currentUser = SecurityUtils.getCurrentUserContext();
        boolean result = followService.followUser(currentUser.getUserId(), userId);

        if (result) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDTO(HttpStatus.OK, "User followed successfully"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(HttpStatus.BAD_REQUEST, "User already followed"));
        }
    }

    @DeleteMapping("/unfollow/{userId}")
    public ResponseEntity<ResponseDTO> unfollowUser(@PathVariable Long userId) {

        CurrentUserContext currentUser = SecurityUtils.getCurrentUserContext();
        boolean result = followService.unfollowUser(currentUser.getUserId(), userId);

        if (result) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(HttpStatus.BAD_REQUEST, "User not following"));
        }
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable Long userId) {

        List<UserDTO> followers = followService.getFollowers(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(followers);
    }

    @GetMapping("/followings/{userId}")
    public ResponseEntity<List<UserDTO>> getFollowings(@PathVariable Long userId) {

        List<UserDTO> followings = followService.getFollowings(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(followings);
    }

}
