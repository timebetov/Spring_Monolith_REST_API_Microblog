package com.github.timebetov.microblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MomentDTO {

    private String text;
    private Long authorId;
    private String visibility;
}
