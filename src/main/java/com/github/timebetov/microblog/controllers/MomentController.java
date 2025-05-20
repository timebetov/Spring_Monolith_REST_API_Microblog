package com.github.timebetov.microblog.controllers;

import com.github.timebetov.microblog.dtos.ResponseDTO;
import com.github.timebetov.microblog.dtos.moment.MomentDTO;
import com.github.timebetov.microblog.dtos.moment.RequestMomentDTO;
import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import com.github.timebetov.microblog.models.Moment;
import com.github.timebetov.microblog.models.User;
import com.github.timebetov.microblog.services.IMomentService;
import com.github.timebetov.microblog.services.impl.UserService;
import com.github.timebetov.microblog.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/moments", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class MomentController {

    private final IMomentService momentService;
    private final UserService userService;

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
}
