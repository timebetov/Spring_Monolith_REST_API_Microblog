package com.github.timebetov.microblog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "moments")
public class Moment extends BaseEntity {

    public enum Visibility {
        PUBLIC, DRAFT, FOLLOWERS_ONLY
    }

    @Id
    @GeneratedValue
    private UUID momentId;

    @Column(length = 500)
    private String text;

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "userId", nullable = false)
    private User author;

    @OneToMany(mappedBy = "moment", targetEntity = Comment.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> comments;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Visibility visibility;

}
