package com.iba.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.exceptions.*;
import com.iba.model.chart.ResultStat;
import com.iba.model.history.History;
import com.iba.model.project.*;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.model.view.PageParams;
import com.iba.model.view.View;
import com.iba.repository.*;
import com.iba.service.AccessService;
import com.iba.service.HistoryService;
import com.iba.service.ProjectService;
import com.iba.service.ValidatorService;
import com.iba.utils.PagesUtil;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Value("${file.path.load}")
    private String filePath;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(ProjectController.class);

    private final ProjectRepository projectRepository;

    private final ProjectLangRepository projectLangRepository;

    private final ProjectService projectService;

    private final ProjectContributorRepository contributorRepository;

    private final TermRepository termRepository;

    private final TermLangRepository termLangRepository;

    private final UserRepository userRepository;

    private final ValidatorService validatorService;

    private final AccessService accessService;

    private final PagesUtil pagesUtil;

    private final TermCommentRepository termCommentRepository;

    private final HistoryService historyService;

    private final HistoryRepository historyRepository;

    @Autowired
    public ProjectController(ProjectRepository projectRepository, ProjectLangRepository projectLangRepository, ProjectService projectService,
                             ProjectContributorRepository contributorRepository, TermRepository termRepository,
                             UserRepository userRepository, ValidatorService validatorService, AccessService accessService,
                             TermLangRepository termLangRepository, PagesUtil pagesUtil, TermCommentRepository termCommentRepository, HistoryService historyService, HistoryRepository historyRepository) {
        this.projectRepository = projectRepository;
        this.projectLangRepository = projectLangRepository;
        this.projectService = projectService;
        this.contributorRepository = contributorRepository;
        this.termRepository = termRepository;
        this.termLangRepository = termLangRepository;
        this.userRepository = userRepository;
        this.validatorService = validatorService;
        this.accessService = accessService;
        this.pagesUtil = pagesUtil;
        this.termCommentRepository = termCommentRepository;
        this.historyService = historyService;
        this.historyRepository = historyRepository;
    }

    /**
     * Filters all User Projects depending on incoming parameters.
     *
     * @param searchValue     search value
     * @param searchParam     search category
     * @param sortValue       sort category
     * @param projectListType type of returned Projects
     * @param page            page
     * @param user            authenticated User
     * @return not entity class Projects which have Project list, page parameters, progress
     * @throws Exception_400 if projectListType or sort param not exists
     */
    @GetMapping("")
    @JsonView(View.ProjectItem.class)
    public Projects doFilterProjects(@RequestParam(required = false) String searchValue,
                                     @RequestParam Constants.SearchParam searchParam,
                                     @RequestParam Constants.SortValue sortValue,
                                     @RequestParam Constants.ProjectListType projectListType,
                                     Pageable page,
                                     @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to filter projects");
        List<Project> projectList = projectService.doFilterProjects(searchValue, searchParam, sortValue, projectListType, user);
        List<Long> notEmptyTranslation = termLangRepository.countListNotEmptyTranslationByUserId(user.getId());
        List<Long> allTranslation = termLangRepository.countListAllTranslationByUserId(user.getId());
        long allTranslations = 0;
        double sum = 0.0;
        for (int i = 0; i < allTranslation.size(); i++) {
            sum += (double) notEmptyTranslation.get(i) / (double) allTranslation.get(i);
            allTranslations += notEmptyTranslation.get(i);
        }
        Progress progress = new Progress(termRepository.countAllTermsByUserId(user.getId()), allTranslations,
                projectRepository.countAllUserProjectsAndIsDeletedFalse(user.getId()), allTranslation.size() == 0 ? 0 : sum / allTranslation.size());
        PageParams pageParams = pagesUtil.createPagesParamsByList(projectList, page);
        return new Projects(projectService.createProjectsProgresses(pagesUtil.createSubListByPage(projectList, pageParams, page.getPageSize())), pageParams, progress);
    }

    /**
     * Returns the progress of the Project.
     *
     * @param project Project
     * @param user    authenticated User
     * @return not entity class Progress with progress of this
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @GetMapping("/{projectId}/progress")
    public Progress getProgress(@PathVariable("projectId") Project project, @AuthenticationPrincipal User user) {
        accessService.isNotProjectOrAccessDenied(project, user, false);
        return new Progress(termRepository.countAllByProjectId(project.getId()),
                termLangRepository.countNotEmptyTranslation(project.getId()),
                termLangRepository.countAllTranslation(project.getId()),
                contributorRepository.countAllByProjectId(project.getId()),
                project.getProjectName(),
                project.getDescription(),
                accessService.getUserRole(project, user));
    }

    /**
     * Add new Project with default ProjectLang
     *
     * @param project new Project
     * @param user    authenticated User
     * @param lang_id default language in Project
     * @return created Project
     * @throws Exception_400 if Project name exists
     */
    @PostMapping("/add")
    public Project addProject(@RequestBody Project project, @AuthenticationPrincipal User user, @RequestParam long lang_id) {
        logger.debug("User " + user.getUsername() + " is trying to add new project");
        return projectService.addProject(project, user, lang_id);
    }

    /**
     * Filters ProjectLangs in Project depending on incoming parameters.
     *
     * @param searchValue search value
     * @param sortValue   sort category
     * @param page        page
     * @param project     Project
     * @param locale      locale for right sorting languages
     * @param user        authenticated User
     * @return existing Project with filtered ProjectLangs
     * @throws IOException   if parse failed
     * @throws JSONException if parse failed
     * @throws Exception_400 if sort param not exists
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @GetMapping("/{projectId}/langs")
    public Project doFilterLangs(@PathVariable("projectId") Project project,
                                 @RequestParam(required = false) String searchValue,
                                 @RequestParam Constants.SortValue sortValue,
                                 @RequestParam Constants.LocaleType locale,
                                 Pageable page,
                                 @AuthenticationPrincipal User user) throws IOException, JSONException, URISyntaxException, ParserConfigurationException, SAXException {
        accessService.isNotProjectOrAccessDenied(project, user, false);
        Project existProject = new Project();
        existProject.setRole(accessService.getUserRole(project, user));
        existProject.setProjectLangs(projectService.doFilterProjectLangs(project.getProjectLangs(), locale, sortValue, searchValue));
        existProject.setPageParams(pagesUtil.createPagesParamsByList(existProject.getProjectLangs(), page));
        existProject.setProjectLangs(pagesUtil.createSubListByPage(existProject.getProjectLangs(), existProject.getPageParams(), page.getPageSize()));
        return existProject;
    }

    /**
     * Update Project information.
     *
     * @param project        Project
     * @param newName        new name of Project
     * @param newDescription new description of Project
     * @param user           authenticated User
     * @return updated Project.
     * @throws Exception_400 if new Project name and
     *                       new Project description not validated
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     * @throws Exception_421 if Project name exists
     */
    @PutMapping("/{projectId}/update")
    public Project updateProject(@PathVariable("projectId") Project project, @RequestParam(required = false) String newName,
                                 @RequestParam(required = false) String newDescription,
                                 @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to update project");
        accessService.isNotProjectOrAccessDenied(project, user, true);
        project.setProjectName(newName);
        project.setDescription(newDescription);
        if (!validatorService.validateProject(project)) {
            throw new Exception_400("Bad credentials");
        }
        if (projectRepository.countByAuthorAndProjectNameAndIsDeletedFalse(user, newName) != 0 && !project.getProjectName().equals(newName)) {
            logger.error("Project name exists");
            throw new Exception_421("Project name exists!");
        }
        projectRepository.save(project);
        project.setTerms(null);
        project.setProjectLangs(null);
        logger.debug("User " + user.getUsername() + " updated project");
        return project;
    }

    /**
     * Add ProjectLang to Project
     *
     * @param project existing Project
     * @param lang_id id of new language
     * @param user    authenticated User
     * @return existing Project with new language
     * @throws Exception_400 if ProjectLang exists in Project
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project or new ProjectLang not found
     */
    @PostMapping("/{projectId}/language/add")
    public Project addProjectLang(@PathVariable("projectId") Project project, @AuthenticationPrincipal User user,
                                  @RequestParam long lang_id) {
        logger.debug("User " + user.getUsername() + " is trying to add new project lang to project");
        accessService.isNotProjectOrAccessDenied(project, user, true);
        return projectService.addProjectLang(project, lang_id, user);
    }

    /**
     * Filters Terms in ProjectLang depending on incoming parameters.
     *
     * @param project     existing Project
     * @param searchValue search value
     * @param sortValue   sort category
     * @param page        page
     * @param user        authenticated User
     * @return existing Project with filtered Terms
     * @throws Exception_400 if sort param not exists
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @GetMapping("/{projectId}/terms")
    public Project getTerms(@PathVariable("projectId") Project project,
                            @RequestParam(required = false) String searchValue,
                            @RequestParam Constants.SortValue sortValue,
                            Pageable page,
                            @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " filter terms");
        accessService.isNotProjectOrAccessDenied(project, user, false);
        Project existProject = new Project();
        existProject.setRole(accessService.getUserRole(project, user));
        existProject.setTerms(projectService.doFilterTerms(project.getTerms(), sortValue, searchValue));
        existProject.setPageParams(pagesUtil.createPagesParamsByList(existProject.getTerms(), page));
        existProject.setTerms(pagesUtil.createSubListByPage(existProject.getTerms(), existProject.getPageParams(), page.getPageSize()));
        existProject.setProjectLangs(project.getProjectLangs());
        existProject.getProjectLangs().forEach(a -> a.setTermLangs(null));
        existProject.getTerms().forEach(a -> a.setCommentsCount(termCommentRepository.countAllByTermId(a.getId())));
        for (Term term : existProject.getTerms()) {
            term.setTranslatedCount(termLangRepository.countNotEmptyTranslationByTermsId(term.getId()));
        }
        return existProject;
    }

    /**
     * Filters all ProjectContributor in Project depending on incoming parameters.
     *
     * @param project         existing Project
     * @param searchValue     search value
     * @param sortValue       sort category
     * @param page            page
     * @param user            authenticated User
     * @param contributorRole role of ProjectContributors
     * @return existing Project with filtered ProjectContributors
     * @throws Exception_400 if sort param or contributors role not exists
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @GetMapping("/{projectId}/contributors")
    public Project getContributors(@PathVariable("projectId") Project project,
                                   @RequestParam(required = false) Constants.ContributorRole contributorRole,
                                   @RequestParam(required = false) String searchValue,
                                   @RequestParam Constants.SortValue sortValue,
                                   Pageable page,
                                   @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " get contributors");
        accessService.isNotProjectOrAccessDenied(project, user, false);
        Project existProject = new Project();
        existProject.setRole(accessService.getUserRole(project, user));
        List<ProjectContributor> contributors = new ArrayList<>(project.getContributors());
        contributors.add(0, new ProjectContributor(project.getAuthor(), project.getId(), Constants.ContributorRole.AUTHOR));
        existProject.setContributors(projectService.doFilterContributors(contributors, sortValue, searchValue, contributorRole));
        existProject.setPageParams(pagesUtil.createPagesParamsByList(existProject.getContributors(), page));
        existProject.setContributors(pagesUtil.createSubListByPage(existProject.getContributors(), existProject.getPageParams(), page.getPageSize()));
        return existProject;
    }

    /**
     * Add new ProjectContributor to Project.
     *
     * @param project  exists Project
     * @param userName new ProjectContributor name in Project
     * @param role     role of new ProjectContributor in Project
     * @param user     authenticated User
     * @throws Exception_400 if contributor exists in Project
     * @throws Exception_403 if authenticated User isn't author of Project
     * @throws Exception_404 if Project not found
     * @throws Exception_423 if new ProjectContributor is author of Project
     */
    @PostMapping("/{projectId}/add/contributor")
    public void addContributor(@PathVariable("projectId") Project project,
                               @AuthenticationPrincipal User user,
                               @RequestBody String userName, @RequestParam String role) {
        logger.debug("User " + user.getUsername() + " is trying to add new contributor to project");
        accessService.isNotProjectOrNotAuthor(project, user);
        projectService.addContributor(project, userName, role, user);
    }

    /**
     * Delete ProjectLang from Project.
     *
     * @param projectLang ProjectLang for delete
     * @param user        authenticated User
     * @throws Exception_400 if ProjectLang is default
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @PostMapping("/language/{projectLangId}/delete")
    public void deleteProjectLang(@PathVariable("projectLangId") ProjectLang projectLang, @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to delete project lang");
        accessService.isNotObject(projectLang);
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectLang.getProjectId());
        accessService.isNotProjectOrAccessDenied(project, user, true);
        if (projectLang.isDefault()) {
            logger.error("User " + user.getUsername() + " is trying to delete default project lang");
            throw new Exception_400("You cannot delete default lang!");
        } else {
            project.getProjectLangs().remove(projectLang);
            projectLangRepository.delete(projectLang);
            historyService.createProjectOrProjectLangEvent(Constants.StatType.DELETE_PROJECT_LANG, user, project, projectLang);
            logger.debug("User " + user.getUsername() + " deleted project lang");
        }
    }

    /**
     * Delete Term from Project.
     *
     * @param project Project
     * @param user    authenticated User
     * @param term    Term for delete
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project or Term not found
     */
    @PostMapping("/{projectId}/delete/{termId}/term")
    public void deleteTerm(@PathVariable("projectId") Project project, @PathVariable("termId") Term term, @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to delete term from project");
        accessService.isNotProjectOrAccessDenied(project, user, true);
        projectService.deleteTermFromProject(project, term, user);
        logger.debug("Term has been deleted from project " + project.getProjectName());
    }

    /**
     * Delete selected Terms from Project.
     *
     * @param project Project
     * @param terms   list of Terms for delete
     * @param user    authenticated User
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project or Term not found
     */
    @PostMapping("/{projectId}/delete-selected/term")
    public void deleteSelectedTerms(@PathVariable("projectId") Project project, @RequestBody List<Term> terms,
                                    @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to delete selected term from project");
        accessService.isNotProjectOrAccessDenied(project, user, true);
        projectService.deleteTermsFromProject(project, terms, user);
        logger.debug("Selected terms have been deleted from project " + project.getProjectName());
    }

    /**
     * Add new Term in Project.
     *
     * @param project Project
     * @param term    new Term
     * @param user    authenticated User
     * @throws Exception_400 if validation of the Term is failed
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     * @throws Exception_423 if Term exists in Project
     */
    @PostMapping("/{projectId}/add/term")
    public void addTermToProject(@PathVariable("projectId") Project project, @RequestBody String term,
                                 @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to add term to project");
        accessService.isNotProjectOrAccessDenied(project, user, true);
        if (!validatorService.validateTermValue(term.trim())) {
            throw new Exception_400("Validate term value failed!");
        }
        projectService.addTerm(project, term.trim(), user);
        logger.debug("User " + user.getUsername() + " added term to project");
    }

    /**
     * Notification all ProjectContributors in Project.
     *
     * @param project Project
     * @param message notification message
     * @param user    authenticated User
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @PostMapping("/{projectId}/notify-all")
    public void notifyAll(@PathVariable("projectId") Project project, @AuthenticationPrincipal User user, @RequestBody String message) {
        logger.debug("User " + user.getUsername() + " is trying to notify project contributors");
        accessService.isNotProjectOrAccessDenied(project, user, false);
        projectService.notifyAllContributors(project, message, user);
        logger.debug("User " + user.getUsername() + " notified project contributors");
    }

    /**
     * Notification selected ProjectContributors in Project.
     *
     * @param contributors list of contributors for notify
     * @param project      Project
     * @param message      notification message
     * @param user         authenticated User
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @PostMapping("/{projectId}/notify-selected")
    public void notifySelected(@PathVariable("projectId") Project project, @RequestBody List<ProjectContributor> contributors,
                               @AuthenticationPrincipal User user, @RequestParam String message) {
        logger.debug("User " + user.getUsername() + " is trying to notify selected project contributors");
        accessService.isNotProjectOrAccessDenied(project, user, false);
        projectService.notifyContributors(contributors, project.getProjectName(), message, user);
        logger.debug("User " + user.getUsername() + " notified project contributors");
    }

    /**
     * Notification ProjectContributor in Project.
     *
     * @param contributor ProjectContributor for notify
     * @param project     Project
     * @param message     notification message
     * @param user        authenticated User
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @PostMapping("/{projectId}/notify-contributor")
    public void notifyContributor(@PathVariable("projectId") Project project, @RequestBody ProjectContributor contributor, @RequestParam String message,
                                  @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to notify contributor");
        accessService.isNotProjectOrAccessDenied(project, user, false);
        projectService.notifyContributor(contributor, project.getProjectName(), message, user);
        logger.debug("User " + user.getUsername() + " notified project contributor");
    }

    /**
     * Delete ProjectContributor
     *
     * @param projectContributor ProjectContributor for delete
     * @param user               authenticated User
     * @throws Exception_403 if User isn't author of Project
     * @throws Exception_404 if Project or ProjectContributor not found
     */
    @PostMapping("/delete/{projectContributorId}/contributor")
    public void deleteContributor(@PathVariable("projectContributorId") ProjectContributor projectContributor, @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to delete contributor from project");
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectContributor.getProjectId());
        accessService.isNotObject(projectContributor);
        accessService.isNotProjectOrNotAuthor(project, user);
        historyService.createContributorEvent(Constants.StatType.DELETE_CONTRIBUTOR, user, project, projectContributor.getContributor());
        contributorRepository.deleteById(projectContributor.getId());
        logger.error("Contributor deleted");
    }

    /**
     * Delete selected ProjectContributors
     *
     * @param contributors list of ProjectContributors for delete
     * @param user         authenticated User
     * @throws Exception_400 if list of ProjectContributors is empty
     * @throws Exception_404 if Project id's of ProjectContributors are not equal
     */
    @PostMapping("/delete-selected/contributor")
    public void deleteSelectedContributors(@RequestBody List<ProjectContributor> contributors, @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to delete selected contributor from project");
        if (contributors.isEmpty()) {
            throw new Exception_400("Not selected any contributors");
        }
        Set<Long> setOfProjectId = new HashSet<>();
        for (ProjectContributor projectContributor : contributors) {
            setOfProjectId.add(projectContributor.getProjectId());
        }
        if (setOfProjectId.size() != 1) {
            throw new Exception_404("Selection failed");
        }
        for (ProjectContributor projectContributor : contributors) {
            if (projectContributor.isSelected()) {
                contributorRepository.deleteById(projectContributor.getId());
            }
        }
        logger.error("Contributor deleted");
    }

    /**
     * Delete Project only if User is author of Project.
     *
     * @param project Project for delete
     * @param user    authenticated User
     * @throws Exception_403 if User isn't author of Project
     * @throws Exception_404 if Project not found
     */
    @DeleteMapping("/{projectId}/delete")
    public void deleteProject(@PathVariable("projectId") Project project, @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to delete the project");
        accessService.isNotProjectOrNotAuthor(project, user);
        historyRepository.save(new History(user, project, Constants.StatType.DELETE_PROJECT));
        projectRepository.delete(project);
        logger.debug("User " + user.getUsername() + " deleted project");
    }

    /**
     * Flush Project only if User is author of Project.
     *
     * @param project Project for flush
     * @param user    authenticated User
     * @return return flushed Project
     * @throws Exception_403 if User isn't author of Project
     * @throws Exception_404 if Project not found
     */
    @DeleteMapping("/{projectId}/flush-project")
    public Project flushProject(@PathVariable("projectId") Project project, @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to flush project");
        accessService.isNotProjectOrNotAuthor(project, user);
        return projectService.flushProject(project, user);
    }

    /**
     * Get free Users for add them to the Project.
     *
     * @param project        Project to add contributor
     * @param user           free Users
     * @param searchUsername search value for find User by username, first name or last name
     * @return list of free Users
     */
    @GetMapping("/{projectId}/get-contributors")
    public List<User> getFreeContributors(@PathVariable("projectId") Project project, @AuthenticationPrincipal User user, @RequestParam String searchUsername) {
        List<User> userList = userRepository.findByFirstNameAndLastNameAndUsername(searchUsername.toLowerCase());
        return userList.stream().filter(a -> !a.getId().equals(user.getId()) && project.getContributors().stream()
                .noneMatch(b -> b.getContributor().getId().equals(a.getId()))).collect(Collectors.toList());
    }

    /**
     * Import Terms with TermLangs in Project.
     *
     * @param project       Project
     * @param file          file for import
     * @param user          authenticated User
     * @param import_values import with TermLangs(values)
     * @param replace       import with Terms
     * @param projectLangId id of ProjectLang for import
     * @return Project with new Terms and TermLangs
     * @throws IOException                  if file is null
     * @throws JSONException                if parse strings in json failed
     * @throws ParserConfigurationException if parse xml failed
     * @throws SAXException                 if read node from xml file failed
     * @throws Exception_400                if Terms list is empty
     * @throws Exception_403                if access denied
     * @throws Exception_404                if Project not found
     */
    @PostMapping("/{projectId}/import-terms")
    public Project importTerms(@PathVariable("projectId") Project project, MultipartFile file,
                               @AuthenticationPrincipal User user,
                               @RequestParam boolean import_values,
                               @RequestParam boolean replace,
                               @RequestParam(required = false) Long projectLangId) throws IOException, JSONException, ParserConfigurationException, SAXException {
        logger.debug("User " + user.getUsername() + " is trying to import terms to project");
        accessService.isNotProjectOrAccessDenied(project, user, true);
        if (new File(filePath).mkdirs()) {
            logger.debug("Directory was created");
        }
        File convFile = new File(filePath + file.getOriginalFilename());
        if (!convFile.createNewFile()) {
            logger.debug("File was not created");
        }
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        if (replace) {
            project = projectService.importTermsFullReplace(project, convFile, import_values, projectLangId, user);
        } else {
            project = projectService.importTermsMerge(project, convFile, import_values, projectLangId, user);
        }
        project.setTermsCount(project.getTerms().size());
        logger.debug("User " + user.getUsername() + " imported terms to project");
        if (!convFile.delete()) {
            logger.debug("File was not deleted");
        }
        return projectService.createProjectProgress(project);
    }

    /**
     * Get all User Projects for choose Project history.
     *
     * @param user authenticated User
     * @return list of Projects
     */
    @JsonView(View.ProjectItem.class)
    @GetMapping("/all-projects")
    public List<Project> getAllUserProjects(@AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to get the projects");
        return projectService.getAllUserProjects(user);
    }

    /**
     * Get statistic of Project, of all Users.
     *
     * @param project Project
     * @param user    authenticated User
     * @return not entity class ResultStat with stats of Project
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @GetMapping("/{projectId}/full-stats")
    public ResultStat getFullProjectStats(@PathVariable("projectId") Project project, @AuthenticationPrincipal User user) {
        accessService.isNotProjectOrAccessDenied(project, user, false);
        return historyService.getAllProjectStats(project);
    }

    /**
     * Get author of Project
     *
     * @param project Project
     * @param user    authenticated User
     * @return author of Project
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @JsonView(View.ProjectItem.class)
    @GetMapping("/{projectId}/author")
    public User getAuthorFromProject(@PathVariable("projectId") Project project, @AuthenticationPrincipal User user) {
        accessService.isNotProjectOrAccessDenied(project, user, false);
        return project.getAuthor();
    }

    /**
     * Get creation date of Project.
     *
     * @param project Project
     * @return creation date (yyyy-MM-dd) of Project
     */
    @GetMapping("/{projectId}/creation-date")
    public Timestamp getCreationDateFromProject(@PathVariable("projectId") Project project) {
        return project.getCreationDate();
    }

}
