package com.github.timebetov.microblog.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@Builder
public class ResponseDTO {

    private HttpStatus statusCode;
    private String message;
}
