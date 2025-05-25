package com.github.timebetov.microblog.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    public enum Role { USER, ADMIN;}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    private String bio;
    private String picture;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany(mappedBy = "follows")
    @JsonIgnore
    private Set<User> followers;

    @ManyToMany
    @JoinTable(name = "USER_FOLLOWS", joinColumns = {
            @JoinColumn(name = "follower_id", referencedColumnName = "userId", nullable = false),
    }, inverseJoinColumns = {
            @JoinColumn(name = "followed_id", referencedColumnName = "userId", nullable = false)
    })
    @JsonIgnore
    private Set<User> follows;

    @JsonIgnore
    @OneToMany(mappedBy = "author", targetEntity = Moment.class, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<Moment> moments;

    @JsonIgnore
    @OneToMany(mappedBy = "author", targetEntity = Comment.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> comments;
}
