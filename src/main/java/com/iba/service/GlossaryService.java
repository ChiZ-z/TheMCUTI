package com.iba.service;

import com.iba.exceptions.Exception_400;
import com.iba.exceptions.Exception_404;
import com.iba.model.glossary.*;
import com.iba.model.project.Lang;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.repository.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GlossaryService {

    private final GlossaryRepository glossaryRepository;

    private final CategoryRepository categoryRepository;

    private final GroupItemRepository groupItemRepository;

    private final TranslationItemRepository translationItemRepository;

    private final LangRepository langRepository;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(GlossaryService.class);

    @Autowired
    public GlossaryService(GlossaryRepository glossaryRepository, CategoryRepository categoryRepository,
                           GroupItemRepository groupItemRepository, TranslationItemRepository translationItemRepository,
                           LangRepository langRepository) {
        this.glossaryRepository = glossaryRepository;
        this.categoryRepository = categoryRepository;
        this.groupItemRepository = groupItemRepository;
        this.translationItemRepository = translationItemRepository;
        this.langRepository = langRepository;
    }

    @Transactional
    public Glossary setGlossaryProgress(Glossary glossary, User user) {
        glossary.setGroupCount((long) glossary.getGroupItems().size());
        glossary.setWordsCount((long) translationItemRepository.countAllByGlossaryId(glossary.getId()));
        glossary.setFollowersCount((long) glossary.getFollowers().size());
        glossary.setCategoriesCount((long) glossary.getCategories().size());
        if (glossary.getAuthor().equals(user)) {
            glossary.setFollowerRole(Constants.FollowerRole.AUTHOR);
        } else if (glossary.getFollowers().stream().anyMatch(follower -> follower.getUser().equals(user) && follower.getRole().equals(Constants.FollowerRole.FOLLOWER))) {
            glossary.setFollowerRole(Constants.FollowerRole.FOLLOWER);
        } else if (glossary.getFollowers().stream().anyMatch(follower -> follower.getUser().equals(user) && follower.getRole().equals(Constants.FollowerRole.MODERATOR))) {
            glossary.setFollowerRole(Constants.FollowerRole.MODERATOR);
        } else {
            glossary.setFollowerRole(Constants.FollowerRole.ANONYMOUS);
        }
        return glossary;
    }

    @Transactional
    public List<Glossary> doFilterGlossaries(User user, Constants.GlossaryType glossaryType, String searchValue, Constants.FilterValue filterValue, Constants.SortValue sortValue) {
        List<Glossary> glossaries = filterGlossariesByGlossaryType(user, glossaryType);
        for (Glossary glossary : glossaries) {
            setGlossaryProgress(glossary, user);
        }
        glossaries = filterGlossariesByFilterValue(filterValue, glossaries);
        if (searchValue != null && !searchValue.equals("")) {
            glossaries = glossaries.stream().filter(glossary -> glossary.getGlossaryName().toLowerCase().contains(searchValue.toLowerCase())).collect(Collectors.toList());
        }
        sortGlossariesBySortValue(sortValue, glossaries);
        return glossaries;
    }

    @Transactional
    public List<Glossary> filterGlossariesByGlossaryType(User user, Constants.GlossaryType glossaryType) {
        switch (glossaryType) {
            case ADDED: {
                return glossaryRepository.findByFollower(user);
            }
            case MYGLOSSARIES: {
                return glossaryRepository.findByAuthor(user);
            }
            case MARKET: {
                return glossaryRepository.findByGlossaryTypes(Arrays.asList(Constants.GlossaryType.PUBLIC, Constants.GlossaryType.MARKET));
            }
            default: {
                throw new Exception_400("Bad glossary type");
            }
        }
    }

    @Transactional
    public List<Glossary> filterGlossariesByFilterValue(Constants.FilterValue filterValue, List<Glossary> glossaries) {
        switch (filterValue) {
            case ALL: {
                break;
            }
            case PUBLIC: {
                glossaries = glossaries.stream().filter(glossary -> glossary.getGlossaryType().equals(Constants.GlossaryType.PUBLIC)).collect(Collectors.toList());
                break;
            }
            case PRIVATE: {
                glossaries = glossaries.stream().filter(glossary -> glossary.getGlossaryType().equals(Constants.GlossaryType.PRIVATE)).collect(Collectors.toList());
                break;
            }
            default: {
                throw new Exception_400("Bad filter param");
            }
        }
        return glossaries;
    }

    @Transactional
    public void sortGlossariesBySortValue(Constants.SortValue sortValue, List<Glossary> glossaries) {
        switch (sortValue) {
            case CREATIONDATE: {
                glossaries.sort(Comparator.comparing(Glossary::getId));
                break;
            }
            case GLOSSARYNAME: {
                glossaries.sort(Comparator.comparing(Glossary::getGlossaryName));
                break;
            }
            case GROUPSAMOUNT: {
                glossaries.sort(Comparator.comparing(glossary -> glossary.getGroupItems().size()));
                break;
            }
            case TRANSLATIONSAMOUNT: {
                glossaries.sort(Comparator.comparing(glossary -> (long) translationItemRepository.countAllByGlossaryId(glossary.getId())));
                break;
            }
            case POPULARITY: {
                glossaries.sort(Comparator.comparing(glossary -> glossary.getFollowers().size()));
                break;
            }
            default: {
                throw new Exception_400("Bad sort param");
            }
        }
    }

    public Glossary createGlossary(Glossary glossary, User user) {
        glossary.setAuthor(user);
        glossary.setCreationDate();
        glossaryRepository.save(glossary);
        return glossary;
    }

    public List<GroupItem> doFilterGroups(List<GroupItem> groupItems, Long langId, String searchValue, Constants.SortValue sortValue, List<Long> categoryIds, HttpServletRequest httpServletRequest) {
        Lang lang;
        if (langId != null) {
            lang = langRepository.findById((long) langId);
            setDefaultTranslationItem(groupItems, lang);
        } else if (langRepository.findByLangDef(httpServletRequest.getLocalName()) != null) {
            lang = langRepository.findByLangDef(httpServletRequest.getLocalName());
            setDefaultTranslationItem(groupItems, lang);
        } else {
            setFirstDefaultTranslationItem(groupItems);
        }
        if (categoryIds != null && categoryIds.size() > 0) {
            List<Category> categories = categoryRepository.findAllByIds(categoryIds);
            if (categories.size() > 0) {
                groupItems = groupItems.stream().filter(groupItem ->
                        groupItem.getCategories().containsAll(categories)).collect(Collectors.toList());
            }
        }
        if (searchValue != null && !searchValue.equals("")) {
            groupItems = groupItems.stream().filter(groupItem ->
                    !groupItem.isDefaultEmpty() && groupItem.getDefaultTranslationItem().getTranslationItemValue().toLowerCase().contains(searchValue.toLowerCase())).collect(Collectors.toList());
        }
        sortGroups(groupItems, sortValue);
        return groupItems;
    }

    private void sortGroups(List<GroupItem> groupItems, Constants.SortValue sortValue) {
        switch (sortValue) {
            case CREATIONDATE: {
                groupItems.sort(Comparator.comparing(GroupItem::getId).reversed());
                break;
            }
            case GROUPNAME: {
                groupItems.sort(Comparator.comparing(groupItem -> groupItem.getDefaultTranslationItem().getTranslationItemValue()));
                break;
            }
            case LANGUAGENAME: {
                groupItems.sort(Comparator.comparing(groupItem -> groupItem.getDefaultTranslationItem().getLang().getLangName()));
                break;
            }
            case LANGCOUNT: {
                groupItems.sort(Comparator.comparing(groupItem -> groupItem.getTranslationItems().size()));
                break;
            }
            default: {
                throw new Exception_404("Bad sort value");
            }
        }
    }

    private void setFirstDefaultTranslationItem(List<GroupItem> groupItems) {
        for (GroupItem groupItem : groupItems) {
            if (groupItem.getTranslationItems().size() > 0) {
                groupItem.setDefaultTranslationItem(groupItem.getTranslationItems().get(0));
            } else {
                groupItem.setDefaultEmpty(true);
            }
        }
    }

    private void setDefaultTranslationItem(List<GroupItem> groupItems, Lang lang) {
        for (GroupItem groupItem : groupItems) {
            for (TranslationItem item : groupItem.getTranslationItems()) {
                if (item.getLang().equals(lang)) {
                    groupItem.setDefaultTranslationItem(item);
                }
            }
            if (groupItem.getDefaultTranslationItem() == null) {
                groupItem.setDefaultTranslationItem(groupItem.getTranslationItems().get(0));
                groupItem.setDefaultEmpty(true);
            }
        }
    }

    public GlossaryInfo getGlossaryInfo(Glossary glossary, User user) {
        GlossaryInfo glossaryInfo = new GlossaryInfo(
                glossary.getGlossaryName(),
                (long) glossary.getGroupItems().size(),
                (long) translationItemRepository.countAllByGlossaryId(glossary.getId()),
                (long) glossary.getFollowers().size(),
                (long) glossary.getCategories().size(),
                glossary.getGlossaryType());
        if (glossary.getAuthor().equals(user)) {
            glossaryInfo.setFollowerRole(Constants.FollowerRole.AUTHOR);
        } else if (glossary.getFollowers().stream().anyMatch(follower -> follower.getUser().equals(user) && follower.getRole().equals(Constants.FollowerRole.FOLLOWER))) {
            glossaryInfo.setFollowerRole(Constants.FollowerRole.FOLLOWER);
        } else if (glossary.getFollowers().stream().anyMatch(follower -> follower.getUser().equals(user) && follower.getRole().equals(Constants.FollowerRole.MODERATOR))) {
            glossaryInfo.setFollowerRole(Constants.FollowerRole.MODERATOR);
        } else {
            glossaryInfo.setFollowerRole(Constants.FollowerRole.ANONYMOUS);
        }
        return glossaryInfo;
    }

    public GlossariesInfo getGlossariesInfo(Constants.GlossaryType glossaryType, User user) {
        switch (glossaryType) {
            case MYGLOSSARIES: {
                return new GlossariesInfo((long) glossaryRepository.countByAuthor(user),
                        (long) translationItemRepository.countAllByUser(user));
            }
            case ADDED: {
                return new GlossariesInfo((long) glossaryRepository.countByFollower(user),
                        (long) translationItemRepository.countAllByFollower(user));
            }
            case MARKET: {
                return new GlossariesInfo((long) glossaryRepository.countByGlossaryType(Constants.GlossaryType.PUBLIC),
                        (long) translationItemRepository.countAllByGlossaryType(Constants.GlossaryType.PUBLIC));
            }
            default: {
                throw new Exception_400("Bad glossary type");
            }
        }
    }

    public List<Follower> doFilterFollowers(List<Follower> followers, String searchValue, Constants.SortValue sortValue) {
        if (searchValue != null && !searchValue.equals("")) {
            followers = followers.stream().filter(follower -> follower.getUser().getUsername().toLowerCase().contains(searchValue.toLowerCase()) ||
                    follower.getUser().getFirstName().toLowerCase().contains(searchValue.toLowerCase()) ||
                    follower.getUser().getLastName().toLowerCase().contains(searchValue.toLowerCase())).collect(Collectors.toList());
        }
        sortFollowers(followers, sortValue);
        return followers;
    }

    private void sortFollowers(List<Follower> followers, Constants.SortValue sortValue) {
        switch (sortValue) {
            case USERNAME: {
                followers.sort(Comparator.comparing(follower -> follower.getUser().getUsername()));
                break;
            }
            case USERFIRSTNAME: {
                followers.sort(Comparator.comparing(follower -> follower.getUser().getFirstName()));
                break;
            }
            case USERLASTNAME: {
                followers.sort(Comparator.comparing(follower -> follower.getUser().getLastName()));
                break;
            }
            default: {
                throw new Exception_400("Bad search param");
            }
        }
    }


    public GroupItem updateGroupItem(GroupItem groupItem, GroupItem updateGroupItem) {
        if (updateGroupItem.getDefaultTranslationItem() != null) {
            groupItem.setDefaultTranslationItem(updateGroupItem.getDefaultTranslationItem());
        } else {
            groupItem.setDefaultEmpty(true);
        }
        groupItem.setComment(updateGroupItem.getComment());
//        groupItem.setTranslationItems(updateGroupItem.getTranslationItems());
        categoryRepository.saveAll(updateGroupItem.getCategories());
        groupItem.setCategories(updateGroupItem.getCategories());
        groupItemRepository.save(groupItem);
        return groupItem;
    }


    public void updateTranslationItem(TranslationItem translationItem, TranslationItem newTranslationItem) {
//        translationItem.setLang(newTranslationItem.getLang());
        translationItem.setTranslationItemValue(newTranslationItem.getTranslationItemValue());
        translationItemRepository.save(translationItem);
    }

    public GroupItem deleteTranslationItem(GroupItem groupItem, TranslationItem translationItem) {
        translationItemRepository.delete(translationItem);
        groupItem.getTranslationItems().remove(translationItem);
        return groupItem;
    }
}
