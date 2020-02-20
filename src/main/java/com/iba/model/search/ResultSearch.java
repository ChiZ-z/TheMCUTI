package com.iba.model.search;

import com.iba.model.view.PageParams;

import java.util.List;

public class ResultSearch {
    private List<SearchItem> searchItems;
    private Long termsCount;
    private Long translationCount;
    private PageParams pageParams;

    public ResultSearch() {
    }

    public ResultSearch(PageParams pageParams, Long termsCount, Long translationCount) {
        this.termsCount = termsCount;
        this.translationCount = translationCount;
        this.pageParams = pageParams;
    }

    public ResultSearch(List<SearchItem> searchItems) {
        this.searchItems = searchItems;
    }

    public ResultSearch(List<SearchItem> searchItems, PageParams pageParams) {
        this.searchItems = searchItems;
        this.pageParams = pageParams;
    }

    public Long getTermsCount() {
        return termsCount;
    }

    public void setTermsCount(Long termsCount) {
        this.termsCount = termsCount;
    }

    public Long getTranslationCount() {
        return translationCount;
    }

    public void setTranslationCount(Long translationCount) {
        this.translationCount = translationCount;
    }

    public PageParams getPageParams() {
        return pageParams;
    }

    public void setPageParams(PageParams pageParams) {
        this.pageParams = pageParams;
    }

    public List<SearchItem> getSearchItems() {
        return searchItems;
    }

    public void setSearchItems(List<SearchItem> searchItems) {
        this.searchItems = searchItems;
    }
}
