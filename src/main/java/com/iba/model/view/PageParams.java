package com.iba.model.view;

import com.fasterxml.jackson.annotation.JsonView;

@JsonView({View.ProjectItem.class, View.HistoryItem.class})
public class PageParams {
    private int size;
    private int pageCount;
    private int currentPage;

    public PageParams() {
    }

    public PageParams(int size, int pageCount, int currentPage) {
        this.size = size;
        this.pageCount = pageCount;
        this.currentPage = currentPage;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
