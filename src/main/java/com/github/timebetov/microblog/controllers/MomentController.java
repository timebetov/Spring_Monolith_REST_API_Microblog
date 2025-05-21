package com.github.timebetov.microblog.controllers;

import com.github.timebetov.microblog.dtos.ResponseDTO;
import com.github.timebetov.microblog.dtos.moment.MomentDTO;
import com.github.timebetov.microblog.dtos.moment.RequestMomentDTO;
import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.services.IMomentService;
import com.github.timebetov.microblog.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/moments", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class MomentController {

    private final IMomentService momentService;

    @PostMapping("/")
    public ResponseEntity<ResponseDTO> createMoment(@Valid @RequestBody RequestMomentDTO moment) {

        Long authorId = SecurityUtils.getCurrentUserId();
        momentService.createMoment(authorId, moment);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(HttpStatus.CREATED, "Moments created successfully"));
    }

    @GetMapping("/")
    public ResponseEntity<List<MomentDTO>> getAllMoments(
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String visibility) {

        CurrentUserContext currentUser = SecurityUtils.getCurrentUserContext();
        List<MomentDTO> result = momentService.getMoments(authorId, visibility, currentUser);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MomentDTO> getMomentById(@PathVariable("id") String id) {

        MomentDTO result = momentService.getMomentById(UUID.fromString(id));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MomentDTO> updateMoment(@PathVariable("id") String id, @RequestBody RequestMomentDTO moment) {

        momentService.updateMoment(UUID.fromString(id), moment);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> deleteMoment(@PathVariable("id") String id) {

        momentService.deleteMoment(UUID.fromString(id));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
