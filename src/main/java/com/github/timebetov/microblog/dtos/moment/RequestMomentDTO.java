package com.github.timebetov.microblog.dtos.moment;

import com.github.timebetov.microblog.models.Moment;
import com.github.timebetov.microblog.validations.EnumValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Schema to hold Moment (Post) information to create or update")
public class RequestMomentDTO {

    @Schema(example = "Hi, everyone this is my Moment I want to share...")
    @NotBlank(message = "Text cannot be blank")
    @Size(max = 500, message = "Text must contain maximum 500 characters")
    private String text;

    @Schema(example = "PUBLIC | DRAFT | FOLLOWERS_ONLY")
    @EnumValues(enumClass = Moment.Visibility.class)
    private String visibility;
}
