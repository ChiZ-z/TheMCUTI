package com.iba.model.chart;

import java.util.ArrayList;
import java.util.List;

public class ChartItem {
    private List<String> nodes =  new ArrayList<>();
    private List<Long> summaryStats =  new ArrayList<>();
    private List<Long> translatedStats =  new ArrayList<>();
    private List<Long> editedStats =  new ArrayList<>();
    private List<Long> autoTranslatedStats =  new ArrayList<>();
    private List<Long> translatedByImportStats =  new ArrayList<>();
    private List<Long> editedByImportStats =  new ArrayList<>();

    public ChartItem() {
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public List<Long> getSummaryStats() {
        return summaryStats;
    }

    public void setSummaryStats(List<Long> summaryStats) {
        this.summaryStats = summaryStats;
    }

    public List<Long> getTranslatedStats() {
        return translatedStats;
    }

    public void setTranslatedStats(List<Long> translatedStats) {
        this.translatedStats = translatedStats;
    }

    public List<Long> getEditedStats() {
        return editedStats;
    }

    public void setEditedStats(List<Long> editedStats) {
        this.editedStats = editedStats;
    }

    public List<Long> getAutoTranslatedStats() {
        return autoTranslatedStats;
    }

    public void setAutoTranslatedStats(List<Long> autoTranslatedStats) {
        this.autoTranslatedStats = autoTranslatedStats;
    }

    public List<Long> getTranslatedByImportStats() {
        return translatedByImportStats;
    }

    public void setTranslatedByImportStats(List<Long> translatedByImportStats) {
        this.translatedByImportStats = translatedByImportStats;
    }

    public List<Long> getEditedByImportStats() {
        return editedByImportStats;
    }

    public void setEditedByImportStats(List<Long> editedByImportStats) {
        this.editedByImportStats = editedByImportStats;
    }
}
