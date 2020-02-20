package com.iba.model.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Constants {
    public enum ProjectListType {
        ALL,
        MYPROJECTS,
        SHARED
    }

    public enum SearchParam {
        PROJECTNAME,
        TERM,
        CONTRIBUTOR,
        TRANSLATION,
        TERMVALUE,
        REFERENCE,
        MODIFIER
    }

    public enum SortValue {
        PROJECTNAME,
        LANGUAGENAME,
        TERMNAME,
        CREATIONDATE,
        USERNAME,
        USERFIRSTNAME,
        USERLASTNAME,
        PROGRESS,
        MODIFIEDDATE,
        GLOSSARYNAME,
        GROUPNAME,
        LANGCOUNT,
        GROUPSAMOUNT,
        TRANSLATIONSAMOUNT,
        POPULARITY
    }

    public enum FilterValue {
        DEFAULT,
        FUZZY,
        NOTFUZZY,
        TRANSLATED,
        UNTRANSLATED,
        DEFAULTEDIT,
        AUTOTRANSLATED,
        ALL,
        MYGLOSSARIES,
        SUBSCRIPTIONS,
        PUBLIC,
        PRIVATE
    }

    public enum ContactType {
        PHONE,
        SKYPE,
        VK,
        EMAIL,
        INSTAGRAM,
        TWITTER,
        FACEBOOK
    }

    public enum ContributorRole {
        MODERATOR,
        TRANSLATOR,
        AUTHOR
    }

    public enum GlossaryType {
        MYGLOSSARIES,
        ADDED,
        PUBLIC,
        PRIVATE,
        MARKET
    }

    public enum FollowerRole {
        AUTHOR,
        MODERATOR,
        FOLLOWER,
        ANONYMOUS
    }

    public enum ProficiencyLevel {
        HIGH,
        MEDIUM,
        LOW
    }

    public enum SearchListType {
        ALL,
        TERMS,
        TRANSLATIONS
    }

    public enum StatType {
        ALL,
        SUMMARY,
        TRANSLATE,
        EDIT,
        AUTO_TRANSLATE,
        TRANSLATE_BY_IMPORT,
        EDIT_BY_IMPORT,
        IMPORT_TERMS,

        IMPORT_TRANSLATIONS,
        ADD_PROJECT,
        DELETE_PROJECT,
        ADD_PROJECT_LANG,
        DELETE_PROJECT_LANG,
        ADD_TERM,

        DELETE_TERM,
        EDIT_TERM,
        ADD_CONTRIBUTOR,
        DELETE_CONTRIBUTOR,
        FLUSH_PROJECT_LANG,
        FLUSH_PROJECT,


    }

    public enum FileTypes {
        json,
        properties,
        strings,
        xliff,
        xml,
        resx,
        resw,
        po,
        pot,
        xls,
        xlsx
    }

    public enum DateType {
        ALL,
        WEEK,
        MONTH,
        PERIOD
    }

    public enum LocaleType {
        en,
        ru
    }

    public enum AuthProvider {
        google,
        facebook,
        github,
        github_repo,
        gitlab,
        bitbucket
    }

    public enum IntegrationActions {
        IMPORT_TRANSLATIONS,
        IMPORT_TERMS,
        IMPORT_TERMS_TRANSLATIONS,
        EXPORT_TRANSLATIONS,
        EXPORT_TERMS,
        EXPORT_TERMS_TRANSLATIONS,
    }
}
