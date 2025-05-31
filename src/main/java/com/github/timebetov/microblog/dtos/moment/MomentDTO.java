package com.github.timebetov.microblog.dtos.moment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Schema to hold Moment (Post) information")
public class MomentDTO {

    private String id;
    private String text;
    private Long authorId;
    @Schema(description = "Visibility type", example = "PUBLIC | FOLLOWERS_ONLY | DRAFT")
    private String visibility;
}
