package com.github.timebetov.microblog.controllers;

import com.github.timebetov.microblog.dtos.ResponseDTO;
import com.github.timebetov.microblog.dtos.moment.RequestMomentDTO;
import com.github.timebetov.microblog.services.IMomentService;
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

    private final IMomentService momentService;

    @PostMapping("/")
    public ResponseEntity<ResponseDTO> createMoment(@RequestParam Long authorId, @Valid @RequestBody RequestMomentDTO moment) {

        momentService.createMoment(authorId, moment);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(HttpStatus.CREATED, "Moments created successfully"));
    }

//    @GetMapping("/")
//    public ResponseEntity<List<MomentDTO>> getAllMoments(@RequestParam(required = false) Long authorId) {
//
//        List<MomentDTO> result = authorId != null
//                ? momentService.getMoments(authorId, Moment.Visibility.PUBLIC)
//                : momentService.getMoments(null, Moment.Visibility.PUBLIC);
//        return ResponseEntity.status(HttpStatus.OK).body(result);
//    }
}
