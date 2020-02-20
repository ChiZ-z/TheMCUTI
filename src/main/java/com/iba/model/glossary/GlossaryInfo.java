package com.iba.model.glossary;

import com.iba.model.view.Constants;

import javax.persistence.Transient;

public class GlossaryInfo {

    private String glossaryName;
    private Long groupItemCount;
    private Long wordsCount;
    private Long followersCount;
    private Long categoriesCount;
    private Constants.FollowerRole followerRole;
    private Constants.GlossaryType glossaryType;

    public GlossaryInfo() {
    }

    public GlossaryInfo(String glossaryName, Long groupItemCount, Long wordsCount, Long followersCount, Long categoriesCount, Constants.GlossaryType type) {
        this.glossaryName = glossaryName;
        this.groupItemCount = groupItemCount;
        this.wordsCount = wordsCount;
        this.followersCount = followersCount;
        this.categoriesCount = categoriesCount;
        this.glossaryType = type;
    }

    public String getGlossaryName() {
        return glossaryName;
    }

    public void setGlossaryName(String glossaryName) {
        this.glossaryName = glossaryName;
    }

    public Long getGroupItemCount() {
        return groupItemCount;
    }

    public void setGroupItemCount(Long groupItemCount) {
        this.groupItemCount = groupItemCount;
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

    public Long getCategoriesCount() {
        return categoriesCount;
    }

    public void setCategoriesCount(Long categoriesCount) {
        this.categoriesCount = categoriesCount;
    }

    public Constants.FollowerRole getFollowerRole() {
        return followerRole;
    }

    public void setFollowerRole(Constants.FollowerRole followerRole) {
        this.followerRole = followerRole;
    }

    public Constants.GlossaryType getGlossaryType() {
        return glossaryType;
    }

    public void setGlossaryType(Constants.GlossaryType glossaryType) {
        this.glossaryType = glossaryType;
    }
}
