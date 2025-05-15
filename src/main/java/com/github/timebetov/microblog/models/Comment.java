package com.github.timebetov.microblog.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "userId", nullable = false)
    private User author;

    @ManyToOne
    @JoinColumn(name = "moment_id", referencedColumnName = "momentId", nullable = false)
    private Moment moment;
}
