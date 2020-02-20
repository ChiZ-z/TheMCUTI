package com.iba.controller;

import com.iba.exceptions.Exception_400;
import com.iba.model.search.ResultSearch;
import com.iba.model.search.SearchItem;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.model.view.PageParams;
import com.iba.service.SearchService;
import com.iba.utils.PagesUtil;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    private final PagesUtil pagesUtil;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(SearchController.class);

    public SearchController(SearchService searchService, PagesUtil pagesUtil) {
        this.searchService = searchService;
        this.pagesUtil = pagesUtil;
    }

    /**
     * TrashSearch - find Terms and TermLangs in whole Project.
     *
     * @param user           - authenticated User
     * @param searchValue    - value for search
     * @param searchListType - type of searchItems
     * @param page           - page
     * @return SearchItems with PageParams
     * @throws Exception_400 if searchListType not exists
     */
    @GetMapping
    public ResultSearch search(@AuthenticationPrincipal User user, @RequestParam String searchValue,
                               @RequestParam Constants.SearchListType searchListType,
                               Pageable page) {
        logger.debug("User " + user.getUsername() + " use trash search");
        if (StringUtils.isEmpty(searchValue)) {
            return new ResultSearch();
        }
        List<SearchItem> searchItems = searchService.search(user, searchValue, searchListType);
        PageParams pageParams = pagesUtil.createPagesParamsByList(searchItems, page);
        ResultSearch resultSearch = new ResultSearch(pageParams, 0L, 0L);
        for (SearchItem searchItem : searchItems) {
            if (searchItem.getCategory() == Constants.SearchListType.TERMS) {
                resultSearch.setTermsCount(resultSearch.getTermsCount() + 1);
            }
            if (searchItem.getCategory() == Constants.SearchListType.TRANSLATIONS) {
                resultSearch.setTranslationCount(resultSearch.getTranslationCount() + 1);
            }
        }
        resultSearch.setSearchItems(pagesUtil.createSubListByPage(searchItems, pageParams, page.getPageSize()));
        return resultSearch;
    }


    /**
     * BorderSearch - find Terms and TermLangs in whole Project with limited size of list.
     *
     * @param size        - size of searchItems list
     * @param user        - authenticated User
     * @param searchValue - value for search
     * @return SearchItems with PageParams
     */
    @GetMapping("/border")
    public ResultSearch headerSearch(@AuthenticationPrincipal User user, @RequestParam String searchValue, @RequestParam int size) {
        logger.debug("User " + user.getUsername() + " use border search");
        if (searchValue.isEmpty()) {
            return new ResultSearch();
        }
        return searchService.headerSearch(user, searchValue, size);
    }
}
