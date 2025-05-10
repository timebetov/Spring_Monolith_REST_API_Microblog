package com.github.timebetov.microblog.dto.moment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestMomentDTO {

    @NotEmpty(message = "Text must be not empty")
    @Size(min = 1, max = 500, message = "Text must be between 1 and 500 characters long")
    private String text;

    @NotNull(message = "Visibility type must be not null")
    private String visibility;
}
