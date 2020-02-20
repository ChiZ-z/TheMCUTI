package com.iba.service;

import com.iba.exceptions.Exception_400;
import com.iba.model.project.Project;
import com.iba.model.search.ResultSearch;
import com.iba.model.search.SearchItem;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.repository.TermLangRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final ProjectService projectService;

    private final TermLangRepository termLangRepository;

    public SearchService(ProjectService projectService, TermLangRepository termLangRepository) {
        this.projectService = projectService;
        this.termLangRepository = termLangRepository;
    }

    /**
     * Search elements by searchListType in all user's projects.
     *
     * @param user           - authenticated user
     * @param searchValue    - search value
     * @param searchListType - type of search
     * @return list of found elements
     */
    public List<SearchItem> search(User user, String searchValue, Constants.SearchListType searchListType) {
        List<SearchItem> searchItemList = new ArrayList<>();
        List<Project> projectList = projectService.getAllUserProjects(user);
        switch (searchListType) {
            case ALL: {
                findByTerms(projectList, searchItemList, searchValue);
                findByTranslation(projectList, searchItemList, searchValue);
                break;
            }
            case TERMS: {
                findByTerms(projectList, searchItemList, searchValue);
                break;
            }
            case TRANSLATIONS: {
                findByTranslation(projectList, searchItemList, searchValue);
                break;
            }
            default: {
                throw new Exception_400("Bad params!");
            }
        }
        return searchItemList;
    }

    /**
     * Find Terms in Projects.
     *
     * @param projectList    - list of projects
     * @param searchValue    - search value
     * @param searchItemList - list of found items
     */
    private void findByTerms(List<Project> projectList, List<SearchItem> searchItemList, String searchValue) {
        projectList.forEach(a -> a.getTerms().forEach(b -> {
            if (b.getTermValue().toLowerCase().contains(searchValue.toLowerCase())) {
                SearchItem searchItem = new SearchItem(Constants.SearchListType.TERMS, a.getProjectName(), b.getTermValue(), searchValue, a.getId());
                searchItem.setTranslatedCount(termLangRepository.countNotEmptyTranslationByTermsId(b.getId()));
                searchItem.setTranslatedAll((long) a.getProjectLangs().size());
                searchItemList.add(searchItem);
            }
        }));
    }

    /**
     * Find TermLangs in Projects.
     *
     * @param projectList    - list of projects
     * @param searchValue    - search value
     * @param searchItemList - list of found items
     */
    private void findByTranslation(List<Project> projectList, List<SearchItem> searchItemList, String searchValue) {
        projectList.forEach(a -> a.getProjectLangs().forEach(b -> {
            b.getTermLangs().forEach(c -> {
                if (c.getValue().toLowerCase().contains(searchValue.toLowerCase())) {
                    SearchItem searchItem = new SearchItem(Constants.SearchListType.TRANSLATIONS, a.getProjectName(), c.getTerm().getTermValue(), searchValue, a.getId());
                    searchItem.setProjectLangId(b.getId());
                    searchItem.setLangIcon(b.getLang().getLangIcon());
                    searchItem.setTranslation(c.getValue());
                    searchItem.setLang(c.getLang().getLangName());
                    searchItemList.add(searchItem);
                }
            });
        }));
    }

    /**
     * Border search.
     *
     * @param user        - authenticated user
     * @param searchValue - search value
     * @param size        - size of list
     * @return list of found elements
     */
    public ResultSearch headerSearch(User user, String searchValue, int size) {
        List<SearchItem> searchItemList = search(user, searchValue, Constants.SearchListType.ALL);
        if (searchItemList.size() > size)
            searchItemList = searchItemList.subList(0, size);
        return new ResultSearch(searchItemList);
    }

}
