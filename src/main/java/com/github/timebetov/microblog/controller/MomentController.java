package com.github.timebetov.microblog.controller;

import com.github.timebetov.microblog.dto.ResponseDTO;
import com.github.timebetov.microblog.dto.moment.RequestMomentDTO;
import com.github.timebetov.microblog.service.impl.MomentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/moments", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class MomentController {

    private final MomentService momentService;

    @PostMapping("/")
    public ResponseEntity<ResponseDTO> createMoment(@RequestParam Long authorId, @Valid @RequestBody RequestMomentDTO moment) {

        momentService.createMoment(authorId, moment);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(HttpStatus.CREATED, "Moments created successfully"));
    }
}
