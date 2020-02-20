package com.iba.utils;

import com.iba.model.view.PageParams;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PagesUtil {

    /**
     * Filters a list, depending on the page
     * on which the user is located
     *
     * @param pageParams class PageParams with numbers of collection size,
     *                   current page, all pages
     * @param list       incoming list
     * @param size       size of all incoming pages
     * @return filtered list of incoming collection
     */
    public <T> List<T> createSubListByPage(List<T> list, PageParams pageParams, int size) {
        if (!list.isEmpty()) {
            int end = Math.min((pageParams.getCurrentPage() + 1) * size, list.size());
            list = list.subList(pageParams.getCurrentPage() * size, end);
        }
        return list;
    }

    /**
     * Create class with numbers of list size, current page, all pages
     *
     * @param page page with numbers of all pages and current page
     * @param list incoming list
     * @return not entity class PageParams
     */
    public <T> PageParams createPagesParamsByList(List<T> list, Pageable page) {
        PageParams pageParams = new PageParams();
        pageParams.setSize(list.size());
        int tail = 0;
        if (list.size() % page.getPageSize() != 0) tail += 1;
        pageParams.setPageCount(list.size() / page.getPageSize() + tail);
        if (page.getPageNumber() >= pageParams.getPageCount() && page.getPageNumber() > 0) {
            pageParams.setCurrentPage(pageParams.getPageCount() - 1);
        } else {
            pageParams.setCurrentPage(Math.max(page.getPageNumber(), 0));
        }
        return pageParams;
    }
}
