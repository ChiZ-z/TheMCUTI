package com.iba.model.glossary;

import com.iba.model.user.User;
import com.iba.model.view.Constants;

import javax.persistence.*;

@Entity
@Table(name = "follower")
public class Follower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long glossaryId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId")
    private User user;

    @Enumerated(EnumType.STRING)
    private Constants.FollowerRole role;

    private String activationCode;

    public Follower() {
    }

    public Follower(Long glossaryId, User user, Constants.FollowerRole followerRole) {
        this.glossaryId = glossaryId;
        this.user = user;
        this.role = followerRole;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGlossaryId() {
        return glossaryId;
    }

    public void setGlossaryId(Long glossaryId) {
        this.glossaryId = glossaryId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Constants.FollowerRole getRole() {
        return role;
    }

    public void setRole(Constants.FollowerRole role) {
        this.role = role;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
}
