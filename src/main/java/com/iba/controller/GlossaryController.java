package com.iba.controller;

import com.iba.exceptions.*;
import com.iba.model.glossary.*;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.model.view.PageParams;
import com.iba.repository.*;
import com.iba.service.AccessService;
import com.iba.service.GlossaryService;
import com.iba.service.UserService;
import com.iba.service.ValidatorService;
import com.iba.utils.PagesUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequestMapping("/glossary")
@RestController
public class GlossaryController {

    private final GlossaryRepository glossaryRepository;

    private final GlossaryService glossaryService;

    private final GroupItemRepository groupItemRepository;

    private final CategoryRepository categoryRepository;

    private final FollowersRepository followersRepository;

    private final UserRepository userRepository;

    private final PagesUtil pagesUtil;

    private final ValidatorService validatorService;

    private final TranslationItemRepository translationItemRepository;

    private final AccessService accessService;

    private final UserService userService;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(GlossaryController.class);

    @Autowired
    public GlossaryController(GlossaryRepository glossaryRepository, GlossaryService glossaryService, GroupItemRepository groupItemRepository,
                              CategoryRepository categoryRepository, FollowersRepository followersRepository,
                              UserRepository userRepository, PagesUtil pagesUtil, ValidatorService validatorService,
                              TranslationItemRepository translationItemRepository, AccessService accessService, UserService userService) {
        this.glossaryRepository = glossaryRepository;
        this.glossaryService = glossaryService;
        this.groupItemRepository = groupItemRepository;
        this.userRepository = userRepository;
        this.validatorService = validatorService;
        this.categoryRepository = categoryRepository;
        this.followersRepository = followersRepository;
        this.pagesUtil = pagesUtil;
        this.translationItemRepository = translationItemRepository;
        this.accessService = accessService;
        this.userService = userService;
    }


    /**
     * Filter all glossaries depending on incoming parameters.
     *
     * @param user         authenticated User
     * @param glossaryType type of the glossaries
     * @param searchValue  search value
     * @param filterValue  filter value
     * @param sortValue    sort value
     * @param page         page
     * @return not entity class Glossaries which have glossary list, page parameters, progress
     * @throws Exception_400 if glossary type, or filter type, or sort value don't exists
     */
    @GetMapping("")
    public Glossaries doFilterGlossaries(@RequestParam Constants.GlossaryType glossaryType, @AuthenticationPrincipal User user,
                                         @RequestParam(required = false) String searchValue,
                                         @RequestParam Constants.FilterValue filterValue,
                                         @RequestParam Constants.SortValue sortValue,
                                         Pageable page) {
        List<Glossary> glossaries = glossaryService.doFilterGlossaries(user, glossaryType, searchValue, filterValue, sortValue);
        PageParams pageParams = pagesUtil.createPagesParamsByList(glossaries, page);
        return new Glossaries(pagesUtil.createSubListByPage(glossaries, pageParams, page.getPageSize()), pageParams);
    }

    /**
     * Create new Glossary.
     *
     * @param glossary new Glossary
     * @param user     authenticated User
     * @return created Glossary
     * @throws Exception_400 if validation of the Glossary failed
     */
    @PostMapping("/create-glossary")
    public Glossary createGlossary(@RequestBody Glossary glossary, @AuthenticationPrincipal User user) {
        if (!validatorService.validateGlossary(glossary)) {
            throw new Exception_400("Validation failed");
        }
        return glossaryService.createGlossary(glossary, user);
    }

    /**
     * Get information of the glossaries by glossary type.
     *
     * @param glossaryType type of the glossaries
     * @param user         authenticated User
     * @return not entity class GlossariesInfo with information about glossaries
     * @throws Exception_400 if Glossary type don't exists
     */
    @GetMapping("info")
    public GlossariesInfo getGlossariesInfo(@RequestParam Constants.GlossaryType glossaryType, @AuthenticationPrincipal User user) {
        return glossaryService.getGlossariesInfo(glossaryType, user);
    }

    /**
     * Filter GroupItems depending on incoming parameters.
     *
     * @param glossary           Glossary
     * @param searchValue        search value
     * @param langId             id of the lang
     * @param categoryIds        category id's
     * @param sortValue          sort value
     * @param user               authenticated User
     * @param httpServletRequest request for get locale if lang id is null
     * @param page               page
     * @return Glossary with filtered GroupItems and sorted categories
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     */
    @GetMapping("{glossaryId}/groups")
    public Glossary doFilterGroups(@PathVariable("glossaryId") Glossary glossary,
                                   @RequestParam(required = false) String searchValue,
                                   @RequestParam(required = false) Long langId,
                                   @RequestParam(required = false) List<Long> categoryIds,
                                   @RequestParam Constants.SortValue sortValue,
                                   @AuthenticationPrincipal User user,
                                   HttpServletRequest httpServletRequest,
                                   Pageable page) {
        accessService.glossaryAccessDenied(glossary, user, false);
        List<GroupItem> groups = glossaryService.doFilterGroups(glossary.getGroupItems(), langId, searchValue, sortValue, categoryIds, httpServletRequest);
        PageParams pageParams = pagesUtil.createPagesParamsByList(groups, page);
        glossary.getCategories().sort(Comparator.comparing(Category::getId, Comparator.reverseOrder()));
        glossary.setGroupItems(pagesUtil.createSubListByPage(groups, pageParams, page.getPageSize()));
        glossary.setPageParams(pageParams);
        return glossaryService.setGlossaryProgress(glossary, user);
    }

    /**
     * Get information about Glossary.
     *
     * @param glossary Glossary
     * @param user     authenticated User
     * @return not entity class GlossaryInfo with information about this Glossary
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     */
    @GetMapping("/{glossaryId}/info")
    public GlossaryInfo getGlossaryInfo(@PathVariable("glossaryId") Glossary glossary,
                                        @AuthenticationPrincipal User user) {
        accessService.glossaryAccessDenied(glossary, user, false);
        return glossaryService.getGlossaryInfo(glossary, user);
    }

    /**
     * Update Glossary - glossary name, description or type.
     *
     * @param glossary               exists Glossary
     * @param user                   authenticated User
     * @param newGlossaryName        new glossary name
     * @param newGlossaryDescription new glossary description
     * @param newGlossaryType        new glossary type
     * @return updated Glossary
     * @throws Exception_400 if validation of the Glossary failed
     * @throws Exception_403 if access denied or User isn't author of the this Glossary
     * @throws Exception_404 if Glossary not found
     */
    @PutMapping("/{glossaryId}/update")
    public Glossary updateGlossary(@PathVariable("glossaryId") Glossary glossary, @AuthenticationPrincipal User user,
                                   @RequestParam String newGlossaryName,
                                   @RequestParam String newGlossaryDescription,
                                   @RequestParam Constants.GlossaryType newGlossaryType) {
        accessService.glossaryAccessDenied(glossary, user, true);
        if (!glossary.getAuthor().equals(user)) {
            throw new Exception_403("Access denied!");
        }
        if (!validatorService.validateGlossary(glossary)) {
            throw new Exception_400("Validation failed");
        }
        glossary.setGlossaryName(newGlossaryName);
        glossary.setDescription(newGlossaryDescription);
        glossary.setGlossaryType(newGlossaryType);
        glossaryRepository.save(glossary);
        return glossary;
    }

    /**
     * Get Glossary only with information and moderators.
     *
     * @param glossary Glossary
     * @param user     authenticated User
     * @return Glossary with moderators
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     */
    @GetMapping("/{glossaryId}/settings")
    public Glossary getGlossaryForSettings(@PathVariable("glossaryId") Glossary glossary, @AuthenticationPrincipal User user) {
        accessService.glossaryAccessDenied(glossary, user, true);
        glossary.setFollowers(glossary.getFollowers().stream().filter(follower -> follower.getRole().equals(Constants.FollowerRole.MODERATOR)).collect(Collectors.toList()));
        glossaryService.setGlossaryProgress(glossary, user);
        return glossary;
    }

    /**
     * Add moderator to Glossary.
     *
     * @param glossary  Glossary
     * @param user      authenticated User
     * @param firstName first name of the new moderator
     * @param email     email of the new moderator
     * @return Glossary with Followers and new moderator
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary or User(new moderator) not found
     * @throws Exception_421 if Follower role don't exists
     * @throws Exception_422 if moderator exists in this glossary
     */
    @PostMapping("/{glossaryId}/add-moderator")
    public Glossary addModerator(@PathVariable("glossaryId") Glossary glossary, @AuthenticationPrincipal User user,
                                 @RequestParam String firstName,
                                 @RequestParam String email) {
        accessService.glossaryAccessDenied(glossary, user, true);
        User moderator = userRepository.findByFirstNameAndEmail(firstName, email);
        Follower existFollower = followersRepository.findByUserAndGlossaryId(moderator, glossary.getId());
        if (existFollower == null) {
            accessService.isNotObject(moderator);
            Follower follower = new Follower(glossary.getId(), moderator, Constants.FollowerRole.ANONYMOUS);
            follower.setActivationCode(UUID.randomUUID().toString());
            followersRepository.save(follower);
            userService.sendModeratorActivationLinkToEmail(moderator, follower.getActivationCode(), glossary.getGlossaryName(), glossary.getId());
            return glossary;
        } else if (existFollower.getRole().equals(Constants.FollowerRole.FOLLOWER)) {
            existFollower.setActivationCode(UUID.randomUUID().toString());
            followersRepository.save(existFollower);
            userService.sendModeratorActivationLinkToEmail(moderator, existFollower.getActivationCode(), glossary.getGlossaryName(), glossary.getId());
            return glossary;
        } else if (existFollower.getRole().equals(Constants.FollowerRole.MODERATOR)) {
            throw new Exception_422("Moderator exists in the glossary");
        } else {
            throw new Exception_421("Follower role not exist");
        }
    }

    /**
     * Remove moderator from Glossary.
     *
     * @param glossary Glossary
     * @param user     authenticated User
     * @param follower moderator
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary or Follower not found
     */
    @DeleteMapping("/{glossaryId}/remove-moderator")
    public void removeModerator(@PathVariable("glossaryId") Glossary glossary, @AuthenticationPrincipal User user,
                                @RequestBody Follower follower) {
        accessService.glossaryAccessDenied(glossary, user, true);
        accessService.isNotObject(follower);
        follower.setRole(Constants.FollowerRole.FOLLOWER);
        followersRepository.save(follower);
    }

    /**
     * Filter Followers depending on incoming parameters.
     *
     * @param glossary    Glossary
     * @param user        authenticated User
     * @param searchValue search value
     * @param sortValue   sort value
     * @param page        page
     * @return Glossary with filtered list of the Followers
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     */
    @GetMapping("/{glossaryId}/followers")
    public Glossary doFilterFollowers(@PathVariable("glossaryId") Glossary glossary, @AuthenticationPrincipal User user,
                                      @RequestParam(required = false) String searchValue,
                                      @RequestParam Constants.SortValue sortValue,
                                      Pageable page) {
        accessService.glossaryAccessDenied(glossary, user, true);
        List<Follower> followers = glossaryService.doFilterFollowers(glossary.getFollowers(), searchValue, sortValue);
        PageParams pageParams = pagesUtil.createPagesParamsByList(followers, page);
        glossary.setFollowers(pagesUtil.createSubListByPage(followers, pageParams, page.getPageSize()));
        glossary.setPageParams(pageParams);
        return glossary;
    }

    /**
     * Activate new moderator by activation code.
     *
     * @param code activation code
     * @throws Exception_404 if activation code not found
     */
    @GetMapping("/activate/{code}")
    public void activateModerator(@PathVariable("code") String code) {
        if (code == null) {
            throw new Exception_404("Activation code not found");
        }
        Follower follower = followersRepository.findByActivationCode(code);
        accessService.isNotObject(follower);
        follower.setRole(Constants.FollowerRole.MODERATOR);
        follower.setActivationCode(null);
        followersRepository.save(follower);
    }

    /**
     * Get Categories from Glossary.
     *
     * @param glossary Glossary
     * @param user     authenticated User
     * @return list of the sorted Categories
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     */
    @GetMapping("/{glossaryId}/categories")
    public List<Category> getCategoriesFromGlossary(@PathVariable("glossaryId") Glossary glossary,
                                                    @AuthenticationPrincipal User user) {
        accessService.glossaryAccessDenied(glossary, user, false);
        glossary.getCategories().sort(Comparator.comparing(Category::getId, Comparator.reverseOrder()));
        return glossary.getCategories();
    }

    /**
     * Create GroupItem.
     *
     * @param glossary  Glossary
     * @param groupItem new GroupItem
     * @param comment   comment to new GroupItem
     * @param user      authenticated User
     * @throws Exception_400 if validation of the GroupItem failed
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     */
    @PostMapping("/{glossaryId}/create-group")
    public void createGroup(@PathVariable("glossaryId") Glossary glossary, @RequestBody GroupItem groupItem,
                            @RequestParam(required = false) String comment,
                            @AuthenticationPrincipal User user) {
        accessService.glossaryAccessDenied(glossary, user, true);
        if (!validatorService.validateGroupItem(groupItem)) {
            throw new Exception_400("Validation failed");
        }
        GroupItem newGroupItem = new GroupItem(glossary.getId(), groupItem.getTranslationItems(), groupItem.getCategories());
        if (comment != null) {
            newGroupItem.setComment(comment);
        }
        categoryRepository.saveAll(newGroupItem.getCategories());
        groupItemRepository.save(newGroupItem);
        newGroupItem.getTranslationItems().forEach(translationItem -> translationItem.setGroupItemId(newGroupItem.getId()));
        translationItemRepository.saveAll(newGroupItem.getTranslationItems());
    }

    /**
     * Add Category to exists GroupItem.
     *
     * @param groupItem GroupItem
     * @param category  name of Category
     * @param user      authenticated User
     * @return GroupItem with new category
     * @throws Exception_400 if validation of the Category failed
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     */
    @PostMapping("{groupItemId}/add-category")
    public GroupItem addCategoryToGroup(@PathVariable("groupItemId") GroupItem groupItem, @RequestBody Category category,
                                        @AuthenticationPrincipal User user) {
        accessService.glossaryAccessDenied(glossaryRepository.findById((long) groupItem.getGlossaryId()), user, true);
        if (!validatorService.validateCategory(category.getCategoryName())) {
            throw new Exception_400("Validation failed");
        }
        categoryRepository.save(category);
        groupItem.getCategories().add(category);
        return groupItem;
    }

    /**
     * Update GroupItem.
     *
     * @param groupItem       exists GroupItem
     * @param updateGroupItem new GroupItem
     * @param user            authenticated User
     * @return updated GroupItem
     * @throws Exception_400 if validation of the GroupItem failed
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     */
    @PutMapping("/update-group/{groupItemId}")
    public GroupItem updateGroupItem(@PathVariable("groupItemId") GroupItem groupItem, @RequestBody GroupItem updateGroupItem,
                                     @AuthenticationPrincipal User user) {
        accessService.glossaryAccessDenied(glossaryRepository.findById((long) groupItem.getGlossaryId()), user, true);
        if (!validatorService.validateGroupItem(updateGroupItem)) {
            throw new Exception_400("Validation failed");
        }
        return glossaryService.updateGroupItem(groupItem, updateGroupItem);
    }

    /**
     * Delete GroupItem.
     *
     * @param groupItem GroupItem for delete
     * @param user      authenticated User
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     */
    @DeleteMapping("/delete/{groupItemId}/group")
    public void deleteGroup(@PathVariable("groupItemId") GroupItem groupItem, @AuthenticationPrincipal User user) {
        accessService.glossaryAccessDenied(glossaryRepository.findById((long) groupItem.getGlossaryId()), user, true);
        groupItemRepository.delete(groupItem);
    }

    /**
     * Create new category.
     *
     * @param glossary     Glossary
     * @param categoryName new name of Category
     * @param user         authenticated User
     * @return Glossary sorted categories with new category
     * @throws Exception_400 if category exists
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     * @throws Exception_421 if validation of the new Category failed
     */
    @PostMapping("/{glossaryId}/create-category")
    public List<Category> createCategory(@PathVariable("glossaryId") Glossary glossary, @RequestBody String categoryName,
                                         @AuthenticationPrincipal User user) {
        accessService.glossaryAccessDenied(glossary, user, true);
        if (categoryRepository.existsByGlossaryIdAndCategoryName(glossary.getId(), categoryName)) {
            throw new Exception_400("Category exists");
        }
        if (!validatorService.validateCategory(categoryName)) {
            throw new Exception_421("Validation failed");
        }
        Category newCategory = new Category(categoryName);
        newCategory.setGlossaryId(glossary.getId());
        categoryRepository.save(newCategory);
        glossary.getCategories().add(newCategory);
        glossary.getCategories().sort(Comparator.comparing(Category::getId, Comparator.reverseOrder()));
        return glossary.getCategories();
    }


    /**
     * Update category.
     *
     * @param category     exists category
     * @param categoryName new category name
     * @param user         authenticated User
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     */
    @PutMapping("update-category/{categoryId}")
    public void updateCategory(@PathVariable("categoryId") Category category, @RequestParam String categoryName,
                               @AuthenticationPrincipal User user) {
        accessService.glossaryAccessDenied(glossaryRepository.findById((long) category.getGlossaryId()), user, true);
        if (!validatorService.validateCategory(categoryName)) {
            throw new Exception_400("Validation failed");
        }
        category.setCategoryName(categoryName);
        categoryRepository.save(category);
    }

    /**
     * Delete category.
     *
     * @param category category for delete
     * @param user     authenticated User
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     */
    @DeleteMapping("/delete/{categoryId}/category")
    public void deleteCategory(@PathVariable("categoryId") Category category, @AuthenticationPrincipal User user) {
        accessService.glossaryAccessDenied(glossaryRepository.findById((long) category.getGlossaryId()), user, true);
        categoryRepository.delete(category);
    }

    /**
     * Subscribe on Glossary.
     *
     * @param glossary Glossary
     * @param user     authenticated User
     * @return Glossary with authenticated User in Followers
     */
    @PostMapping("/{glossaryId}/subscribe")
    public Glossary subscribeOnGlossary(@PathVariable("glossaryId") Glossary glossary, @AuthenticationPrincipal User user) {
        Follower existFollower = followersRepository.findByUserAndGlossaryId(user, glossary.getId());
        if (existFollower != null) {
            existFollower.setRole(Constants.FollowerRole.FOLLOWER);
            followersRepository.save(existFollower);
            glossaryService.setGlossaryProgress(glossary, user);
            return glossary;
        }
        Follower newFollower = new Follower(glossary.getId(), user, Constants.FollowerRole.FOLLOWER);
        if (glossary.getFollowers().stream().noneMatch(follower -> follower.getUser().equals(user))) {
            glossary.getFollowers().add(newFollower);
        }
        followersRepository.saveAll(glossary.getFollowers());
        glossaryService.setGlossaryProgress(glossary, user);
        return glossary;
    }

    /**
     * Unsubscribe of the Glossary.
     *
     * @param glossary Glossary
     * @param user     authenticated User
     * @return Glossary without authenticated User in Followers
     */
    @PostMapping("/{glossaryId}/unsubscribe")
    public Glossary unsubscribeOnGlossary(@PathVariable("glossaryId") Glossary glossary, @AuthenticationPrincipal User user) {
        Iterator<Follower> iterator = glossary.getFollowers().iterator();
        while (iterator.hasNext()) {
            Follower follower = iterator.next();
            if (follower.getUser().equals(user) && follower.getActivationCode() != null) {
                follower.setRole(Constants.FollowerRole.ANONYMOUS);
                followersRepository.save(follower);
            } else if (follower.getUser().equals(user)) {
                iterator.remove();
                followersRepository.delete(follower);
            }
        }
        glossaryService.setGlossaryProgress(glossary, user);
        return glossary;
    }

    /**
     * Add TranslationItem in exists GroupItem.
     *
     * @param groupItem       GroupItem
     * @param translationItem new TranslationItem
     * @param user            authenticated User
     * @return new TranslationItem
     * @throws Exception_400 if validation of the TranslationItem failed
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Glossary not found
     * @throws Exception_421 if size of the translation items list is max
     */
    @PostMapping("/{groupItemId}/create-translation")
    public TranslationItem createTranslationItem(@PathVariable("groupItemId") GroupItem groupItem, @RequestBody TranslationItem translationItem,
                                                 @AuthenticationPrincipal User user) {
        accessService.glossaryAccessDenied(glossaryRepository.findById((long) groupItem.getGlossaryId()), user, true);
        if (!validatorService.validateTranslationItemValue(translationItem.getTranslationItemValue())) {
            throw new Exception_400("Validation failed");
        }
        if (groupItem.getTranslationItems().size() > 34) {
            throw new Exception_421("Max size of the translation items");
        }
        groupItem.getTranslationItems().add(translationItem);
        translationItemRepository.saveAll(groupItem.getTranslationItems());
        return translationItem;
    }

    @PutMapping("/update-translation-item/{translationItemId}")
    public void updateTranslationItem(@PathVariable("translationItemId") TranslationItem translationItem, @RequestBody TranslationItem newTranslationItem,
                                      @AuthenticationPrincipal User user) {
        // TODO: 01.08.2019 accessService.glossaryAccessDenied(glossaryRepository.findById(), user, true);
        if (!validatorService.validateTranslationItemValue(newTranslationItem.getTranslationItemValue())) {
            throw new Exception_400("Validation failed");
        }
        glossaryService.updateTranslationItem(translationItem, newTranslationItem);
    }

    @DeleteMapping("/delete/{groupItemId}/translation-item/{translationItemId}")
    public GroupItem deleteTranslationItem(@PathVariable("groupItemId") GroupItem groupItem, @PathVariable("translationItemId") TranslationItem translationItem,
                                           @AuthenticationPrincipal User user) {
        // TODO: 01.08.2019 accessService.glossaryAccessDenied(glossaryRepository.findById(), user, true);
        if (groupItem.getTranslationItems().size() <= 2) {
            throw new Exception_400("Min size of the translation items");
        }
        return glossaryService.deleteTranslationItem(groupItem, translationItem);
    }

    @DeleteMapping("/{glossaryId}/delete")
    public void deleteGlossary(@PathVariable("glossaryId") Glossary glossary) {

    }

    @PostMapping("/{glossaryId}/clone")
    public void cloneGlossary(@PathVariable("glossaryId") Glossary glossary) {

    }

}
