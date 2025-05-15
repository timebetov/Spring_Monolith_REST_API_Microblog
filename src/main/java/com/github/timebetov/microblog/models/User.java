package com.github.timebetov.microblog.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    public enum Role { USER, ADMIN;}

    @Id
    @GeneratedValue
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }

    @ManyToMany(mappedBy = "follows")
    private Set<User> followers;

    @ManyToMany
    @JoinTable(name = "USER_FOLLOWS", joinColumns = {
            @JoinColumn(name = "follower_id", referencedColumnName = "userId", nullable = false),
    }, inverseJoinColumns = {
            @JoinColumn(name = "followed_id", referencedColumnName = "userId", nullable = false)
    })
    private Set<User> follows;

    @OneToMany(mappedBy = "author", targetEntity = Moment.class, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<Moment> moments;

    @OneToMany(mappedBy = "author", targetEntity = Comment.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> comments;
}
