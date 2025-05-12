package com.github.timebetov.microblog.dto.moment;

import com.github.timebetov.microblog.model.Moment;
import com.github.timebetov.microblog.validation.MomentVisibilityType;
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
    @Size(max = 500, message = "Text must contain only 500 characters")
    private String text;

    @NotNull(message = "Visibility type must be not null")
    @MomentVisibilityType(enumClass = Moment.Visibility.class)
    private String visibility;
}
