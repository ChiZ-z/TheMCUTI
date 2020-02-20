package com.iba.model.user;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.model.chart.ResultStat;
import com.iba.model.view.Constants;
import com.iba.model.view.View;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

@Entity
@Table(name = "user")
public class User implements OAuth2User, UserDetails {

    @JsonView({View.ProjectItem.class, View.HistoryItem.class})
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String email;

    @JsonView({View.ProjectItem.class, View.HistoryItem.class})
    private String username;

    private String password;

    @JsonView({View.ProjectItem.class, View.HistoryItem.class})
    private String firstName;

    @JsonView({View.ProjectItem.class, View.HistoryItem.class})
    private String lastName;

    @JsonView({View.ProjectItem.class, View.HistoryItem.class})
    private String profilePhoto;

    private Timestamp creationDate;

    @Enumerated(EnumType.STRING)
    private Constants.AuthProvider provider;

    @JoinColumn(name = "provider_id")
    private String providerId;
    private String refreshToken;
    private String activationCode;
    private boolean mailingAccess;

    @OneToMany(mappedBy = "userId", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Contact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "userId", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<UserLang> langs = new ArrayList<>();

    @OneToMany(mappedBy = "userId", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<JobExperience> jobs = new ArrayList<>();

    @Transient
    private String token;
    @Transient
    private Collection<? extends GrantedAuthority> authorities;
    @Transient
    private Map<String, Object> attributes;
    @Transient
    private String repeatPassword;
    @Transient
    private String oldPassword;
    @Transient
    private ResultStat resultStat;
    @Transient
    private String avatar;

    public User() {
    }

    public User(String username, String email, String firstName, String lastName, String profilePhoto,
                Constants.AuthProvider provider, String providerId, boolean mailingAccess, Map<String, Object> attributes) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePhoto = profilePhoto;
        this.provider = provider;
        this.providerId = providerId;
        this.mailingAccess = mailingAccess;
        this.creationDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        this.attributes = attributes;
    }

    public User(String email, String username, String password, String firstName, String lastName, String repeatPassword) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.repeatPassword = repeatPassword;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public Constants.AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(Constants.AuthProvider provider) {
        this.provider = provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate() {
        this.creationDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public boolean isMailingAccess() {
        return mailingAccess;
    }

    public void setMailingAccess(boolean mailingAccess) {
        this.mailingAccess = mailingAccess;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<UserLang> getLangs() {
        return langs;
    }

    public void setLangs(List<UserLang> langs) {
        this.langs = langs;
    }

    public List<JobExperience> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobExperience> jobs) {
        this.jobs = jobs;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public ResultStat getResultStat() {
        return resultStat;
    }

    public void setResultStat(ResultStat resultStat) {
        this.resultStat = resultStat;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        User user = (User) object;
        return username.equals(user.username) &&
                email.equals(user.email) &&
                id.equals(user.id);
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}
