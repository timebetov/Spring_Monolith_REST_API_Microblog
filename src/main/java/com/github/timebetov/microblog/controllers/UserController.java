package com.github.timebetov.microblog.controllers;

import com.github.timebetov.microblog.dtos.ResponseDTO;
import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.dtos.user.UpdateUserDTO;
import com.github.timebetov.microblog.dtos.user.UserDTO;
import com.github.timebetov.microblog.services.IFollowService;
import com.github.timebetov.microblog.services.IUserService;
import com.github.timebetov.microblog.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Controller", description = "CRUD REST API to GET, UPDATE, DELETE and Follow Controller")
@RestController
@RequestMapping(value = "/users", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final IUserService userService;
    private final IFollowService followService;

    @Operation(summary = "READ User REST API", description = "REST API to retrieve Current authenticated User information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status Ok"),
            @ApiResponse(responseCode = "400", description = "HTTP Status Bad Request"),
            @ApiResponse(responseCode = "404", description = "HTTP Status Not Found")
    })
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile() {

        CurrentUserContext currentUser = SecurityUtils.getCurrentUserContext();
        UserDTO profile = userService.getById(currentUser.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profile);
    }

    @Operation(summary = "READ Users REST API", description = "REST API to retrieve all users")
    @ApiResponse(responseCode = "200", description = "HTTP Status Ok")
    @GetMapping("/fetch")
    public ResponseEntity<List<UserDTO>> fetchUsers() {

        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(users);
    }

    @Operation(summary = "READ User REST API By Id", description = "REST API to retrieve user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status Ok"),
            @ApiResponse(responseCode = "404", description = "HTTP Status Not Found")
    })
    @GetMapping("/fetch/{userId}")
    public ResponseEntity<UserDTO> fetchUserById(@PathVariable Long userId) {

        UserDTO foundUser = userService.getById(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(foundUser);
    }

    @Operation(summary = "READ User REST API By Username", description = "REST API to retrieve user by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status Ok"),
            @ApiResponse(responseCode = "404", description = "HTTP Status Not Found")
    })
    @GetMapping("/fetch/@{username}")
    public ResponseEntity<UserDTO> fetchUserByUsername(@PathVariable String username) {

        UserDTO foundUser = userService.getByUsername(username);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(foundUser);
    }

    @Operation(summary = "READ User REST API By Email", description = "REST API to retrieve user by email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status Ok"),
            @ApiResponse(responseCode = "404", description = "HTTP Status Not Found")
    })
    @GetMapping("/fetch/email/{email}")
    public ResponseEntity<UserDTO> fetchUserByEmail(@PathVariable String email) {

        UserDTO foundUser = userService.getByEmail(email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(foundUser);
    }

    @Operation(summary = "UPDATE User REST API By Id", description = "REST API to update user information by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status Ok"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "HTTP Status Not Found")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<ResponseDTO> updateUser(@PathVariable Long userId,
                                                  @RequestBody @Valid UpdateUserDTO userDetails) {

        userService.updateUser(userId, userDetails);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(HttpStatus.OK, "User updated successfully"));
    }

    @Operation(summary = "DELETE User REST API By Id", description = "REST API to DELETE user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "HTTP Status No Content"),
            @ApiResponse(responseCode = "404", description = "HTTP Status Not Found")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Follow user by id REST API", description = "REST API to follow other user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status Ok"),
            @ApiResponse(responseCode = "400", description = "HTTP Status Bad Request"),
            @ApiResponse(responseCode = "404", description = "HTTP Status Not Found")
    })
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

    @Operation(summary = "Unfollow user by id REST API", description = "REST API to unfollow follows user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "HTTP Status No Content"),
            @ApiResponse(responseCode = "400", description = "HTTP Status Bad Request"),
            @ApiResponse(responseCode = "404", description = "HTTP Status Not Found")
    })
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

    @Operation(summary = "GET followers by id REST API", description = "REST API to retrieve followers of user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status Ok"),
            @ApiResponse(responseCode = "404", description = "HTTP Status Not Found")
    })
    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable Long userId) {

        List<UserDTO> followers = followService.getFollowers(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(followers);
    }

    @Operation(summary = "GET followings by id REST API", description = "REST API to retrieve followings of user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP Status Ok"),
            @ApiResponse(responseCode = "404", description = "HTTP Status Not Found")
    })
    @GetMapping("/followings/{userId}")
    public ResponseEntity<List<UserDTO>> getFollowings(@PathVariable Long userId) {

        List<UserDTO> followings = followService.getFollowings(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(followings);
    }

}
