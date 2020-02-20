package com.iba.model.glossary;

import java.beans.Transient;

public class GlossariesInfo {

    private Long glossaryCount;
    private Long wordsCount;

    public GlossariesInfo() {
    }

    public GlossariesInfo(Long glossaryCount, Long wordsCount) {
        this.glossaryCount = glossaryCount;
        this.wordsCount = wordsCount;
    }

    public Long getGlossaryCount() {
        return glossaryCount;
    }

    public void setGlossaryCount(Long glossaryCount) {
        this.glossaryCount = glossaryCount;
    }

    public Long getWordsCount() {
        return wordsCount;
    }

    public void setWordsCount(Long wordsCount) {
        this.wordsCount = wordsCount;
    }
}
