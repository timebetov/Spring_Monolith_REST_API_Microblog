package com.github.timebetov.microblog.dtos.moment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MomentDTO {

    private String id;
    private String text;
    private Long authorId;
    private String visibility;
}
