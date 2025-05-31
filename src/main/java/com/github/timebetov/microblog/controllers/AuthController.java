package com.github.timebetov.microblog.controllers;

import com.github.timebetov.microblog.configs.AppConstants;
import com.github.timebetov.microblog.dtos.ErrorResponseDTO;
import com.github.timebetov.microblog.dtos.ResponseDTO;
import com.github.timebetov.microblog.dtos.user.ChangePwdDTO;
import com.github.timebetov.microblog.dtos.user.CreateUserDTO;
import com.github.timebetov.microblog.dtos.user.LoginUserDTO;
import com.github.timebetov.microblog.services.IAuthService;
import com.github.timebetov.microblog.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

@Tag(
        name = "Authentication Controller",
        description = "Registering, authenticating and logging out from application"
)

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @Operation(
            summary = "Create a new account",
            description = "REST API to create a new user inside Microblog"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Returns the specified success message",
                    content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Returns error information",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> register(@RequestBody @Valid CreateUserDTO createUserDTO) {

        authService.register(createUserDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(HttpStatus.CREATED, "User Registered successfully"));
    }

    @Operation(
            summary = "Authenticate with credentials",
            description = "REST API to Authenticate a user inside Microblog with credentials in body, returns JWT Token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns JWT Token in response body"),
            @ApiResponse(responseCode = "400", description = "Returns error information")
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@RequestBody @Valid LoginUserDTO loginUserDTO) {

        String jwtToken = authService.login(loginUserDTO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Authorization", "Bearer " + jwtToken)
                .body(new ResponseDTO(HttpStatus.OK, jwtToken));
    }

    @SecurityRequirement(name = "basicAuth")
    @Operation(
            summary = "BasicAuthentication with credentials",
            description = "REST API to Authenticate a user inside Microblog using BasicAuthentication"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns JWT Token in Response Headers"),
            @ApiResponse(responseCode = "401", description = "Returns Unauthorized Status")
    })
    @GetMapping("/authenticate")
    public ResponseEntity<ResponseDTO> authenticate() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(HttpStatus.OK, "Authenticated, check Response headers"));
    }

    @Operation(
            summary = "Logout current authenticated user",
            description = "REST API to Logout authenticated user from Microblog and Blacklist the JWT Token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logouts and blacklist the jwt token in Redis"),
            @ApiResponse(responseCode = "400", description = "Returns error information")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(AppConstants.JWT_HEADER) String authHeader) {

        authService.logout(authHeader);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Change password",
            description = "REST API to change current authenticated user password"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns specified success message"),
            @ApiResponse(responseCode = "400", description = "Returns error information")
    })
    @PatchMapping("/changePassword")
    public ResponseEntity<ResponseDTO> changePassword(@RequestBody @Valid ChangePwdDTO req) {

        Long currentUser = SecurityUtils.getCurrentUserId();
        authService.changePassword(currentUser, req);
        return ResponseEntity.ok().body(new ResponseDTO(HttpStatus.OK, "Password changed successfully"));
    }
}
