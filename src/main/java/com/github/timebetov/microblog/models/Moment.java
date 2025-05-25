package com.github.timebetov.microblog.models;

import com.github.timebetov.microblog.dtos.user.CurrentUserContext;
import jakarta.persistence.*;
import lombok.*;

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
        PUBLIC {
            @Override
            public boolean canBeViewedBy(CurrentUserContext currentUser, Long authorId, boolean isFollower) {
                return true;
            }
        },
        DRAFT {
            @Override
            public boolean canBeViewedBy(CurrentUserContext currentUser, Long authorId, boolean isFollower) {
                return currentUser.isAdmin() || currentUser.getUserId().equals(authorId);
            }
        },
        FOLLOWERS_ONLY {
            @Override
            public boolean canBeViewedBy(CurrentUserContext currentUser, Long authorId, boolean isFollower) {
                return currentUser.isAdmin() || currentUser.getUserId().equals(authorId) || isFollower;
            }
        };

        public abstract boolean canBeViewedBy(CurrentUserContext currentUser, Long authorId, boolean isFollower);
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
