package com.github.timebetov.microblog.controllers;

import com.github.timebetov.microblog.dtos.ResponseDTO;
import com.github.timebetov.microblog.dtos.user.LoginUserDTO;
import com.github.timebetov.microblog.services.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@Valid @RequestBody LoginUserDTO loginUserDTO) {

        String jwtToken = authService.login(loginUserDTO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(HttpStatus.OK, jwtToken));
    }

    @GetMapping("/authenticate")
    public ResponseEntity<ResponseDTO> authenticate() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(HttpStatus.OK, "Authenticated, check Response headers"));
    }
}
