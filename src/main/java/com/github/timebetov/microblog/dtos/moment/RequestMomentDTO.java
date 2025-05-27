package com.github.timebetov.microblog.dtos.moment;

import com.github.timebetov.microblog.models.Moment;
import com.github.timebetov.microblog.validations.EnumValues;
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
public class RequestMomentDTO {

    @NotBlank(message = "Text cannot be blank")
    @Size(max = 500, message = "Text must contain maximum 500 characters")
    private String text;

    @EnumValues(enumClass = Moment.Visibility.class)
    private String visibility;
}
