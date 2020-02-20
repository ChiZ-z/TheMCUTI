package com.iba.model.glossary;

import com.iba.model.project.Lang;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.model.view.PageParams;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Entity
@Table(name = "glossary")
public class Glossary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String glossaryName;
    private String description;
    private Timestamp creationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lang_id")
    private Lang lang;

    @OneToMany(mappedBy = "glossaryId", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<GroupItem> groupItems = new ArrayList<>();

    @OneToMany(mappedBy = "glossaryId", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "glossaryId", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @Where(clause = "role <> 'ANONYMOUS'")
    private List<Follower> followers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Constants.GlossaryType glossaryType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private Glossary parent;

    @Transient
    private PageParams pageParams;
    @Transient
    private Long groupCount;
    @Transient
    private Long wordsCount;
    @Transient
    private Long followersCount;
    @Transient
    private Long categoriesCount;
    @Transient
    private Constants.FollowerRole followerRole;

    public Glossary() {
    }

    public Glossary(String glossaryName, String description, User author, Constants.GlossaryType glossaryType) {
        this.glossaryName = glossaryName;
        this.description = description;
        this.author = author;
        this.glossaryType = glossaryType;
    }

    public Lang getLang() {
        return lang;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public Constants.FollowerRole getFollowerRole() {
        return followerRole;
    }

    public void setFollowerRole(Constants.FollowerRole followerRole) {
        this.followerRole = followerRole;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate() {
        this.creationDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
    }

    public List<Follower> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Follower> followers) {
        this.followers = followers;
    }

    public PageParams getPageParams() {
        return pageParams;
    }

    public void setPageParams(PageParams pageParams) {
        this.pageParams = pageParams;
    }

    public Long getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(Long groupCount) {
        this.groupCount = groupCount;
    }

    public Long getWordsCount() {
        return wordsCount;
    }

    public void setWordsCount(Long wordsCount) {
        this.wordsCount = wordsCount;
    }

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public List<GroupItem> getGroupItems() {
        return groupItems;
    }

    public void setGroupItems(List<GroupItem> groupItems) {
        this.groupItems = groupItems;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGlossaryName() {
        return glossaryName;
    }

    public void setGlossaryName(String glossaryName) {
        this.glossaryName = glossaryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Constants.GlossaryType getGlossaryType() {
        return glossaryType;
    }

    public void setGlossaryType(Constants.GlossaryType glossaryType) {
        this.glossaryType = glossaryType;
    }

    public Glossary getParent() {
        return parent;
    }

    public void setParent(Glossary parent) {
        this.parent = parent;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public Long getCategoriesCount() {
        return categoriesCount;
    }

    public void setCategoriesCount(Long categoriesCount) {
        this.categoriesCount = categoriesCount;
    }
}
