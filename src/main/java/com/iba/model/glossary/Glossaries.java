package com.iba.model.glossary;

import com.iba.model.view.PageParams;

import java.util.List;

public class Glossaries {

    private List<Glossary> glossaries;
    private PageParams pageParams;

    public Glossaries() {
    }

    public Glossaries(List<Glossary> glossaries, PageParams pageParams) {
        this.glossaries = glossaries;
        this.pageParams = pageParams;
    }

    public List<Glossary> getGlossaries() {
        return glossaries;
    }

    public void setGlossaries(List<Glossary> glossaries) {
        this.glossaries = glossaries;
    }

    public PageParams getPageParams() {
        return pageParams;
    }

    public void setPageParams(PageParams pageParams) {
        this.pageParams = pageParams;
    }
}
