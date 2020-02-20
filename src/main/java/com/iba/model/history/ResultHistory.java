package com.iba.model.history;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.model.view.PageParams;
import com.iba.model.view.View;

import java.util.List;

@JsonView(View.HistoryItem.class)
public class ResultHistory {
    private PageParams pageParams;
    private List<History> historyList;

    public ResultHistory() {
    }

    public ResultHistory(PageParams pageParams, List<History> historyList) {
        this.pageParams = pageParams;
        this.historyList = historyList;
    }

    public PageParams getPageParams() {
        return pageParams;
    }

    public void setPageParams(PageParams pageParams) {
        this.pageParams = pageParams;
    }

    public List<History> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<History> historyList) {
        this.historyList = historyList;
    }
}
