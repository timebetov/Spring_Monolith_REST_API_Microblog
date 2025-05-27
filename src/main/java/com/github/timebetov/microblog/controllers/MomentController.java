package com.github.timebetov.microblog.controllers;

import com.github.timebetov.microblog.dtos.ResponseDTO;
import com.github.timebetov.microblog.dtos.moment.MomentDTO;
import com.github.timebetov.microblog.dtos.moment.RequestMomentDTO;
import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.models.Moment;
import com.github.timebetov.microblog.services.IMomentService;
import com.github.timebetov.microblog.utils.SecurityUtils;
import com.github.timebetov.microblog.validations.EnumValues;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/moments", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class MomentController {

    private final IMomentService momentService;

    @PostMapping("/")
    public ResponseEntity<ResponseDTO> createMoment(@RequestBody @Valid RequestMomentDTO moment) {

        Long authorId = SecurityUtils.getCurrentUserId();
        momentService.createMoment(authorId, moment);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(HttpStatus.CREATED, "Moments created successfully"));
    }

    @GetMapping("/my")
    public ResponseEntity<List<MomentDTO>> getMyMoments(@RequestParam(required = false) String visibility) {

        CurrentUserContext currentUser = SecurityUtils.getCurrentUserContext();
        List<MomentDTO> result = momentService.getMoments(currentUser.getUserId(), visibility, currentUser);
        return ResponseEntity.status(HttpStatus.OK).body(result);
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

        CurrentUserContext currentUser = SecurityUtils.getCurrentUserContext();
        MomentDTO result = momentService.getMomentById(UUID.fromString(id), currentUser);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMoment(@PathVariable("id") String id, @RequestBody @Valid RequestMomentDTO moment) {

        Long authorId = momentService.getAuthorId(UUID.fromString(id));
        momentService.updateMoment(UUID.fromString(id), moment, authorId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMoment(@PathVariable("id") String id) {

        Long authorId = momentService.getAuthorId(UUID.fromString(id));
        momentService.deleteMoment(UUID.fromString(id), authorId);
        return ResponseEntity.noContent().build();
    }
}
