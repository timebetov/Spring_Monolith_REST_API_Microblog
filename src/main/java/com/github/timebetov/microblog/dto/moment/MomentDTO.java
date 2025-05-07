package com.github.timebetov.microblog.dto.moment;

import com.github.timebetov.microblog.dto.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
