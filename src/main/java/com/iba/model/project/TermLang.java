package com.iba.model.project;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.iba.model.view.View;
import com.iba.model.user.User;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "term_lang")
@JsonView(View.ProjectItem.class)
public class TermLang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    private Long id;

    private Long projectLangId;
    private int status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+03:00")
    private Date modifiedDate;
    @JsonView({View.HistoryItem.class, View.ProjectItem.class})
    private String value;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "term_id")
    @Where(clause = "is_deleted = false")
    private Term term;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lang_id")
    private Lang lang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "modified_by")
    private User modifier;

    @Transient
    private List<String> flags = new ArrayList<>();

    @Transient
    private boolean selected;

    public TermLang() {
    }

    public TermLang(Long projectLangId, int status, String value, Term term, Lang lang, User modifier) {
        this.projectLangId = projectLangId;
        this.status = status;
        this.modifiedDate = new Date();
        this.value = value;
        this.term = term;
        this.lang = lang;
        this.modifier = modifier;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public Lang getLang() {
        return lang;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public User getModifier() {
        return modifier;
    }

    public void setModifier(User modifier) {
        this.modifier = modifier;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate() {
        this.modifiedDate = new Date();
    }

    public long getProjectLangId() {
        return projectLangId;
    }

    public void setProjectLangId(Long projectLangId) {
        this.projectLangId = projectLangId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
