package com.github.timebetov.microblog.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Schema(
        name = "ErrorResponse",
        description = "Schema to hold error response information"
)
public class ErrorResponseDTO {

    @Schema(description = "API path invoked by client", example = "/api/**")
    private String apiPath;

    @Schema(description = "Error code representing the error happened", example = "BAD_REQUEST")
    private HttpStatus errorCode;

    @Schema(description = "Time representing when the error happened")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime errorTime;

    @Schema(description = "Error message representing the error happened for general errors")
    private String errorMessage;                // for general errors

    @Schema(description = "Error messages representing the error happened for validation errors")
    private Map<String, String> errorDetails;   // for validation errors

    public static ErrorResponseDTO withMsgAndCode(String apiPath, HttpStatus errorCode, String errorMessage) {
        return ErrorResponseDTO.builder()
                .apiPath(apiPath.replace("uri=", ""))
                .errorTime(LocalDateTime.now())
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    public static ErrorResponseDTO withDetailsAndCode(String apiPath, HttpStatus errorCode, Map<String, String> errorDetails) {
        return ErrorResponseDTO.builder()
                .apiPath(apiPath.replace("uri=", ""))
                .errorTime(LocalDateTime.now())
                .errorCode(errorCode)
                .errorDetails(errorDetails)
                .build();
    }
}
