package com.iba.service;

import com.iba.exceptions.Exception_400;
import com.iba.exceptions.Exception_403;
import com.iba.exceptions.Exception_404;
import com.iba.exceptions.Exception_423;
import com.iba.model.history.History;
import com.iba.model.project.*;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.repository.*;
import com.iba.utils.FileUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Value("${file.path.locale}")
    private String localePath;

    private final ProjectRepository projectRepository;

    private final ProjectLangRepository projectLangRepository;

    private final LangRepository langRepository;

    private final UserRepository userRepository;

    private final ProjectContributorRepository contributorRepository;

    private final TermRepository termRepository;

    private final TermLangRepository termLangRepository;

    private final MailService mailService;

    private final BitFlagService bitFlagService;

    private final ProjectContributorRepository projectContributorRepository;

    private final HistoryService historyService;

    private final FileUtils fileUtils;

    private final AccessService accessService;

    private final HistoryRepository historyRepository;

    private final TermCommentRepository termCommentRepository;

    private final ValidatorService validatorService;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(ProjectService.class);

    @Autowired
    public ProjectService(LangRepository langRepository, ProjectRepository projectRepository, ProjectLangRepository projectLangRepository,
                          UserRepository userRepository, ProjectContributorRepository contributorRepository, FileUtils fileUtils,
                          TermRepository termRepository, TermLangRepository termLangRepository, MailService mailService,
                          BitFlagService bitFlagService, HistoryService historyService, ProjectContributorRepository projectContributorRepository,
                          AccessService accessService, HistoryRepository historyRepository, TermCommentRepository termCommentRepository, ValidatorService validatorService) {
        this.langRepository = langRepository;
        this.projectRepository = projectRepository;
        this.projectLangRepository = projectLangRepository;
        this.userRepository = userRepository;
        this.contributorRepository = contributorRepository;
        this.fileUtils = fileUtils;
        this.termRepository = termRepository;
        this.termLangRepository = termLangRepository;
        this.mailService = mailService;
        this.bitFlagService = bitFlagService;
        this.historyService = historyService;
        this.projectContributorRepository = projectContributorRepository;
        this.accessService = accessService;
        this.historyRepository = historyRepository;
        this.termCommentRepository = termCommentRepository;
        this.validatorService = validatorService;
    }

    /**
     * Set progress in list of projects.
     *
     * @param projects list of projects
     * @return set progress in project
     */
    public List<Project> createProjectsProgresses(List<Project> projects) {
        for (Project project : projects) {
            createProjectProgress(project);
        }
        return projects;
    }

    /**
     * Set progress in project.
     *
     * @param project project
     * @return project with progress
     */
    public Project createProjectProgress(Project project) {
        int termsCount = 0;
        int translatedCount = 0;
        for (ProjectLang lang : project.getProjectLangs()) {
            if (lang.getTermLangs().size() > 0) {
                for (TermLang termLang : lang.getTermLangs()) {
                    termsCount++;
                    if (!termLang.getValue().equals("")) {
                        translatedCount++;
                    }
                }
            }
            int langTranslatedCount = 0;
            for (TermLang a : lang.getTermLangs()) {
                if (!a.getValue().equals("")) langTranslatedCount++;
            }
            lang.setTranslatedCount(langTranslatedCount);
            lang.setTermsCount(lang.getTermLangs().size());
            lang.setTermLangs(null);
        }
        if (termsCount == 0) project.setProgress(0.0);
        if (termsCount != 0) project.setProgress((double) translatedCount / (double) termsCount);
        project.setTermsCount(project.getTerms().size());
        project.setTerms(null);
        return project;
    }

    /**
     * Add new project with default lang.
     *
     * @param project new project
     * @param user    authenticated User
     * @param langId  default lang in project
     * @return created project or throw exception
     */
    public Project addProject(Project project, User user, long langId) {
        if (projectRepository.countByAuthorAndProjectNameAndIsDeletedFalse(user, project.getProjectName()) != 0) {
            logger.error("Project name exists");
            throw new Exception_400("Project name exists!");
        }
        ProjectLang projectLang = new ProjectLang(langRepository.findById(langId), true);
        project.getProjectLangs().add(projectLang);
        project.setAuthor(user);
        project.setCreationDate();
        projectRepository.save(project);
        projectLang.setProjectId(project.getId());
        projectLangRepository.save(projectLang);
        historyService.createProjectOrProjectLangEvent(Constants.StatType.ADD_PROJECT, user, project, projectLang);
        logger.debug("User " + user.getUsername() + " created new project");
        return project;
    }

    /**
     * Sorts and filters project list.
     *
     * @param searchValue     search value
     * @param searchParam     search category
     * @param sortValue       sort category
     * @param projectListType type of returned projects
     * @param user            authenticated User
     * @return sorted and filtered project list
     */
    public List<Project> doFilterProjects(String searchValue, Constants.SearchParam searchParam, Constants.SortValue sortValue, Constants.ProjectListType projectListType, User user) {
        List<Project> projectList;
        switch (projectListType) {
            case ALL: {
                projectList = getAllUserProjects(user);
                break;
            }
            case SHARED: {
                projectList = getContributingProjects(user);
                break;
            }
            case MYPROJECTS: {
                projectList = projectRepository.findByAuthorAndIsDeletedFalse(user);
                break;
            }
            default: {
                throw new Exception_400("Bad params!");
            }
        }
        if (searchValue != null && !searchValue.equals("")) {
            projectList = searchBySearchValue(projectList, searchParam, searchValue);
        }
        projectList = sortUserProjects(projectList, sortValue);
        return projectList;
    }

    /**
     * Get all projects where user is author and contributor
     *
     * @param user authenticated User
     * @return all projects where user is author and contributor
     */
    public List<Project> getAllUserProjects(User user) {
        List<Long> contributors = projectContributorRepository.findByContributor(user);
        if (contributors.isEmpty()) {
            contributors.add(-1L);
        }
        return projectRepository.findByAuthorAndContributorsAndIsDeletedFalse(user, contributors);
    }

    /**
     * Get all projects where user is contributor.
     *
     * @param user authenticated User
     * @return all projects where user is contributor
     */
    private List<Project> getContributingProjects(User user) {
        List<Long> contributors = projectContributorRepository.findByContributor(user);
        if (contributors.isEmpty()) {
            contributors.add(-1L);
        }
        return projectRepository.findByContributorsAndIsDeletedFalse(contributors);
    }

    /**
     * Search for projects from the list depending on the search parameters.
     *
     * @param projectList list of projects
     * @param searchValue search value
     * @param searchParam search category
     * @return found projects
     */
    private List<Project> searchBySearchValue(List<Project> projectList, Constants.SearchParam searchParam, String searchValue) {
        switch (searchParam) {
            case PROJECTNAME: {
                projectList = projectList.stream()
                        .filter(a -> a.getProjectName().toLowerCase().contains(searchValue.toLowerCase()))
                        .collect(Collectors.toList());
                break;
            }
            case TERM: {
                projectList = projectList.stream()
                        .filter(a -> a.getTerms().stream().anyMatch(b -> b.getTermValue().toLowerCase().contains(searchValue.toLowerCase())))
                        .collect(Collectors.toList());
                break;
            }
            case CONTRIBUTOR: {
                projectList = projectList.stream()
                        .filter(a -> a.getContributors().stream().anyMatch(b -> b.getContributor().getUsername()
                                .toLowerCase().contains(searchValue.toLowerCase()))).collect(Collectors.toList());
                break;
            }
            case TRANSLATION: {
                projectList = projectList.stream()
                        .filter(a -> a.getProjectLangs().stream().anyMatch(b -> b.getTermLangs().stream()
                                .anyMatch(c -> c.getValue().toLowerCase().contains(searchValue.toLowerCase())))).collect(Collectors.toList());
                break;
            }
            default: {
                throw new Exception_400("Bad params!");
            }
        }
        return projectList;
    }

    /**
     * Sorting project list.
     *
     * @param projects  list of projects
     * @param sortValue sort category
     * @return sorted list of projects.
     */
    private List<Project> sortUserProjects(List<Project> projects, Constants.SortValue sortValue) {
        if (sortValue != null)
            switch (sortValue) {
                case PROJECTNAME: {
                    projects = projects.stream().sorted(Comparator.comparing(a -> a.getProjectName().toLowerCase()))
                            .collect(Collectors.toList());
                    break;
                }
                case PROGRESS: {
                    projects = projects.stream().sorted(Comparator.comparing(this::checkProjectProgress))
                            .collect(Collectors.toList());
                    break;
                }
                default: {
                    throw new Exception_400("Bad sort params");
                }
            }
        return projects;
    }

    /**
     * Sorts and filters project list.
     *
     * @param projectLangs langs in project
     * @param searchValue  search value
     * @param sortValue    sort category
     * @return sorted and filtered project list
     */
    public List<ProjectLang> doFilterProjectLangs(List<ProjectLang> projectLangs, Constants.LocaleType locale, Constants.SortValue sortValue, String searchValue) throws IOException, JSONException, URISyntaxException, ParserConfigurationException, SAXException {
        URL[] urls = {getClass().getResource(localePath)};
        ClassLoader loader = new URLClassLoader(urls);
        ResourceBundle bundle = ResourceBundle.getBundle("Language", LocaleUtils.toLocale(locale.toString()), loader);
        if (searchValue != null && !searchValue.equals("")) {
            projectLangs = projectLangs.stream().filter(a -> a.getLang().getLangName().toLowerCase().contains(searchValue.toLowerCase()))
                    .collect(Collectors.toList());
        }
        projectLangs = sortProjectLangs(projectLangs, sortValue, bundle);
        for (ProjectLang lang : projectLangs) {
            int translatedCount = 0;
            for (TermLang a : lang.getTermLangs()) {
                if (!a.getValue().equals("")) {
                    translatedCount++;
                }
            }
            lang.setTermsCount(lang.getTermLangs().size());
            lang.setTranslatedCount(translatedCount);
            lang.setTermLangs(null);
        }
        return projectLangs;
    }

    /**
     * Sorting list of langs in project.
     *
     * @param projectLangs list of langs in project
     * @param sortValue    sort category
     * @return sorted list of projectLangs
     */
    private List<ProjectLang> sortProjectLangs(List<ProjectLang> projectLangs, Constants.SortValue sortValue, ResourceBundle resourceBundle) {
        switch (sortValue) {
            case LANGUAGENAME: {
                projectLangs = projectLangs.stream()
                        .sorted(Comparator.comparing(a -> resourceBundle.getString(a.getLang().getLangName()))).collect(Collectors.toList());
                break;
            }
            case PROGRESS: {
                projectLangs = projectLangs.stream()
                        .sorted(Comparator.comparing(this::checkProgress)).collect(Collectors.toList());
                break;
            }
            default: {
                throw new Exception_400("Bad sort params");
            }
        }
        return projectLangs;
    }

    /**
     * Count progress in project.
     *
     * @param project project for count
     * @return Progress
     */
    private Double checkProjectProgress(Project project) {
        Double result = 0.0;
        for (ProjectLang projectLang : project.getProjectLangs()) {
            result += checkProgress(projectLang);
        }
        return result / project.getProjectLangs().size();
    }

    /**
     * Count progress in projectLang.
     *
     * @param projectLang projectLang for count
     * @return Progress
     */
    private Double checkProgress(ProjectLang projectLang) {
        if (projectLang.getTermLangs().size() == 0) return 0.0;
        return projectLang.getTermLangs().stream()
                .filter(a -> !a.getValue().equals("")).count() / (double) projectLang.getTermLangs().size();
    }

    /**
     * Add new language to project
     *
     * @param project existing project
     * @param langId  id of new language
     * @param user    authenticated User
     * @return existing project with new language
     * @throws Exception_404 if project not found
     *                       or lang haven't been chosen
     *                       or lang not found
     * @throws Exception_400 if lang exists in this project
     */
    @Transactional
    public Project addProjectLang(Project project, long langId, User user) {
        accessService.isNotObject(project);
        if (langId == -1) {
            logger.error("Project lang haven't been chosen");
            throw new Exception_404("Choose project lang!");
        }
        Lang lang = langRepository.findById(langId);
        accessService.isNotObject(lang);
        if (project.getProjectLangs().stream().anyMatch(a -> a.getLang().getId() == langId)) {
            logger.error("Project lang exists in this project");
            throw new Exception_400("Project lang exist!");
        }
        ProjectLang projectLang = new ProjectLang(lang, false);
        projectLang.setProjectId(project.getId());
        projectLangRepository.save(projectLang);
        for (Term term : project.getTerms()) {
            TermLang termLang = new TermLang(projectLang.getId(), 0, "", term, lang, user);
            projectLang.getTermLangs().add(termLang);
        }
        termLangRepository.saveAll(projectLang.getTermLangs());
        projectLangRepository.save(projectLang);
        project.getProjectLangs().add(projectLang);
        historyService.createProjectOrProjectLangEvent(Constants.StatType.ADD_PROJECT_LANG, user, project, projectLang);
        logger.debug(lang.getLangName() + " added to project");
        return createProjectProgress(project);
    }

    /**
     * Sorts and filters term list.
     *
     * @param searchValue search value
     * @param sortValue   sort category
     * @return sorted and filtered term list
     */
    public List<Term> doFilterTerms(List<Term> terms, Constants.SortValue sortValue, String searchValue) {
        if (searchValue != null && !searchValue.equals("")) {
            terms = terms.stream().filter(a -> a.getTermValue().toLowerCase().contains(searchValue.toLowerCase())).collect(Collectors.toList());
        }
        switch (sortValue) {
            case TERMNAME: {
                terms = terms.stream().sorted(Comparator.comparing(a -> a.getTermValue().toLowerCase())).collect(Collectors.toList());
                break;
            }
            case CREATIONDATE: {
                terms = terms.stream().sorted(Comparator.comparing(Term::getId).reversed()).collect(Collectors.toList());
                break;
            }
            default: {
                throw new Exception_400("Bad sort params");
            }
        }
        return terms;
    }

    /**
     * Sorts and filters contributors list.
     *
     * @param searchValue search value
     * @param sortValue   sort category
     * @return sorted and filtered contributors list
     */
    public List<ProjectContributor> doFilterContributors(List<ProjectContributor> contributors, Constants.SortValue sortValue, String searchValue, Constants.ContributorRole contributorRole) {
        if (contributorRole != null) {
            /// TODO: 06.08.2019 add switch
            switch (contributorRole) {
                case MODERATOR:
                case AUTHOR:
                case TRANSLATOR: {
                    contributors = contributors.stream().filter(projectContributor -> projectContributor.getRole().equals(contributorRole)).collect(Collectors.toList());
                    break;
                }
                default: {
                    throw new Exception_400("Bad contributor role");
                }
            }
        }
        if (searchValue != null && !searchValue.equals("")) {
            contributors = contributors.stream().filter(a -> a.getContributor().getLastName().toLowerCase().contains(searchValue.toLowerCase()) ||
                    a.getContributor().getFirstName().toLowerCase().contains(searchValue.toLowerCase()) ||
                    a.getContributor().getUsername().toLowerCase().contains(searchValue.toLowerCase())).collect(Collectors.toList());
        }
        switch (sortValue) {
            case USERNAME: {
                contributors = contributors.stream().sorted(Comparator.comparing(a -> a.getContributor().getUsername().toLowerCase())).collect(Collectors.toList());
                break;
            }
            case USERFIRSTNAME: {
                contributors = contributors.stream().sorted(Comparator.comparing(a -> a.getContributor().getFirstName().toLowerCase())).collect(Collectors.toList());
                break;
            }
            case USERLASTNAME: {
                contributors = contributors.stream().sorted(Comparator.comparing(a -> a.getContributor().getLastName().toLowerCase())).collect(Collectors.toList());
                break;
            }
            default: {
                throw new Exception_400("Bad sort params");
            }
        }
        return contributors;
    }

    /**
     * Add new contributor to project.
     *
     * @param project exists project
     * @param newUser new user in project
     * @param role    role of new user in project
     * @throws Exception_403 if user add author of project to contibuting project,
     *                       or if contributor exists
     */
    public void addContributor(Project project, String newUser, String role, User authUser) {
        accessService.isNotObject(project);
        if (project.getAuthor().getUsername().equals(newUser)) {
            logger.error("Attempt to add author of the project to contributors");
            throw new Exception_423("You cannot add author of project!");
        }
        if (project.getContributors().stream().anyMatch(a -> a.getContributor().getUsername().equals(newUser))) {
            logger.error("Attempt to add exist contributor");
            throw new Exception_400("Contributor exists is this project!");
        }
        User user = userRepository.findByUsername(newUser);
        accessService.isNotObject(user);
        ProjectContributor projectContributor = new ProjectContributor(user, project.getId(), Constants.ContributorRole.valueOf(role));
        contributorRepository.save(projectContributor);
        historyService.createContributorEvent(Constants.StatType.ADD_CONTRIBUTOR, authUser, project, projectContributor.getContributor());
        logger.debug("User " + user.getUsername() + " added to project");
    }

    /**
     * Delete term from project.
     *
     * @param project incoming project
     * @param terms   list of terms for delete
     */
    @Transactional
    public void deleteTermsFromProject(Project project, List<Term> terms, User user) {
        accessService.isNotObject(terms);
        accessService.isNotObject(project);
        List<History> historyList = new ArrayList<>();
        for (Term term : terms) {
            if (term.isSelected()) {
                if (!project.getTerms().contains(term)) {
                    logger.error("Project don't contains term");
                    throw new Exception_404("Term not found in this project!");
                }
                termRepository.delete(term);
                historyList.add(new History(user, project, Constants.StatType.DELETE_TERM, term.getTermValue(), term));
            }
        }
        historyRepository.saveAll(historyList);
    }

    /**
     * Delete term from project.
     *
     * @param project incoming project
     * @param termId  id of term for delete
     */
    public void deleteTermFromProject(Project project, Term term, User user) {
        accessService.isNotObject(term);
        accessService.isNotObject(project);
        if (!project.getTerms().contains(term)) {
            logger.error("Project don't contains term");
            throw new Exception_404("Term not found in this project!");
        }
        termRepository.delete(term);
        historyService.createTermEvent(Constants.StatType.DELETE_TERM, user, project, term, term.getTermValue(), null);
    }

    /**
     * Add new term in to project,
     * and add empty value of term lang in project langs.
     *
     * @param project   existing project
     * @param termValue new term value
     * @param user      authenticated User
     * @throws Exception_400 if term is empty
     *                       if term value is exist in project
     */
    public void addTerm(Project project, String termValue, User user) {
        if (termValue.equals("")) {
            logger.error("Term value is empty");
            throw new Exception_400("Term value is empty");
        }
        if (termRepository.existsByProjectIdAndTermValue(project.getId(), termValue)) {
            logger.error("Term is exists in project");
            throw new Exception_423("Term value is exist in this project!");
        }
        Term term = new Term();
        term.setTermValue(termValue);
        term.setProjectId(project.getId());
        termRepository.save(term);
        for (ProjectLang projectLang : project.getProjectLangs()) {
            TermLang termLang = new TermLang(projectLang.getId(), 0, "", term, projectLang.getLang(), user);
            termLangRepository.save(termLang);
        }
        historyService.createTermEvent(Constants.StatType.ADD_TERM, user, project, term, "", termValue);
    }

    /**
     * Notification all contributors in project.
     *
     * @param project project
     * @param message notification message
     * @param user    user who sends the message
     */
    public void notifyAllContributors(Project project, String message, User user) {
        for (ProjectContributor contributor : project.getContributors()) {
            if (!StringUtils.isEmpty(contributor.getContributor().getEmail())) {
                mailService.send(contributor.getContributor().getEmail(), "TheMcuti-Support", "Project - " + project.getProjectName() +
                        " notification\n\n" + message + "\n\n" + user.getLastName() + " " + user.getFirstName() + "\n" + user.getEmail());
            }
        }
    }

    /**
     * Notification selected contributors in project.
     *
     * @param contributors list of contributors for notify
     * @param projectName  name of project
     * @param message      notification message
     * @param user         user who sends the message
     */
    public void notifyContributors(List<ProjectContributor> contributors, String projectName, String message, User user) {
        for (ProjectContributor contributor : contributors) {
            if (!StringUtils.isEmpty(contributor.getContributor().getEmail()) && contributor.isSelected()) {
                mailService.send(contributor.getContributor().getEmail(), "TheMcuti-Support", "Project - " + projectName +
                        " notification\n\n" + message + "\n\n" + user.getLastName() + " " + user.getFirstName() + "\n" + user.getEmail());
            }
        }
    }

    /**
     * Notification contributor in project.
     *
     * @param contributor contributor for notify
     * @param projectName name of project
     * @param message     notification message
     * @param user        user who sends the message
     */
    public void notifyContributor(ProjectContributor contributor, String projectName, String message, User user) {
        if (!StringUtils.isEmpty(contributor.getContributor().getEmail())) {
            mailService.send(contributor.getContributor().getEmail(), "TheMcuti-Support", "Project - " + projectName +
                    " notification\n\n" + message + "\n\n" + user.getLastName() + " " + user.getFirstName() + "\n" + user.getEmail());
        }
    }

    /**
     * Flush project only if user is author of project.
     *
     * @param project project for flush
     * @param user    authenticated User
     * @return return flushed project
     */
    @Transactional
    public Project flushProject(Project project, User user) {
        accessService.isNotProjectOrNotAuthor(project, user);
        List<History> historyList = new ArrayList<>();
        historyList.add(historyRepository.save(new History(user, project,
                Constants.StatType.FLUSH_PROJECT)));
        for (Term term : project.getTerms()) {
            historyList.add(new History(user, project, Constants.StatType.DELETE_TERM, term, term.getTermValue(),
                    null, historyList.get(0).getId()));
        }
        if (project.getTerms().size() > 0) {
            termRepository.deleteAll(project.getTerms());
        }
        project.getTerms().clear();
        projectRepository.save(project);
        historyRepository.saveAll(historyList);
        logger.debug("User " + user.getUsername() + " flushed project " + project.getProjectName());
        return createProjectProgress(project);
    }

    /**
     * Import terms with values or not,
     * without delete old terms.
     *
     * @param project       project for import
     * @param file          file
     * @param import_values import values or not
     * @param langId        id of lang for import
     * @param user          authenticated User
     * @return project with new terms and termLangs
     * @throws IOException   if file don't exist
     * @throws JSONException parse strings in json format failed
     */
    @Transactional
    public Project importTermsMerge(Project project, File file, boolean import_values, Long langId, User user) throws IOException, JSONException, ParserConfigurationException, SAXException {
        LinkedHashMap<String, String> termsMap = fileUtils.parseFile(file);
        List<String> termLangsWithFlag = new ArrayList<>();
        LinkedHashMap<String, String> termsWithComment = new LinkedHashMap<>();
        addFlagsOrComments(termsMap, termLangsWithFlag, termsWithComment);
        List<History> historyList = new ArrayList<>();
        checkExistProjectLang(project, langId, user, historyList);
        project.getTerms().forEach(a -> {
            if (termsMap.containsKey(a.getTermValue().trim())) {
                if (import_values)
                    removeTermAndSetTranslation(termsMap, project, langId, a.getTermValue(), user, historyList);
            }
            termsMap.remove(a.getTermValue());
            termsWithComment.remove(a.getTermValue());
            termLangsWithFlag.remove(a.getTermValue());
        });

        return addNewTerms(project, termsMap, langId, import_values, user, historyList, termLangsWithFlag, termsWithComment);
    }

    private void checkExistProjectLang(Project project, Long langId, User user, List<History> historyList) {
        if (langId != null) {
            ProjectLang projectLang = projectLangRepository.findById((long) langId);
            if (projectLang == null || project.getProjectLangs().stream().noneMatch(a -> a.getId().equals(projectLang.getId()))) {
                throw new Exception_400("Import failed");
            }
            historyList.add(historyRepository.save(new History(user, project,
                    Constants.StatType.IMPORT_TERMS, projectLang)));
            historyList.add(historyRepository.save(new History(user, project,
                    Constants.StatType.IMPORT_TRANSLATIONS, projectLang)));
        } else {
            historyList.add(historyRepository.save(new History(user, project,
                    Constants.StatType.IMPORT_TERMS)));
        }
    }

    /**
     * Import terms with values or not,
     * with delete old terms.
     *
     * @param project       project for import
     * @param file          file
     * @param import_values import values or not
     * @param langId        id of lang for import
     * @param user          authenticated User
     * @return project with new terms and termLangs
     * @throws IOException   if file don't exist
     * @throws JSONException parse strings in json format failed
     */
    @Transactional
    public Project importTermsFullReplace(Project project, File file, boolean import_values, Long langId, User user) throws IOException, JSONException, ParserConfigurationException, SAXException {
        LinkedHashMap<String, String> termsMap = fileUtils.parseFile(file);
        List<String> termLangsWithFlag = new ArrayList<>();
        LinkedHashMap<String, String> termsWithComment = new LinkedHashMap<>();
        addFlagsOrComments(termsMap, termLangsWithFlag, termsWithComment);
        List<Term> removeTerm = new ArrayList<>();
        List<TermLang> removeTermLang = new ArrayList<>();
        List<History> historyList = new ArrayList<>();
        checkExistProjectLang(project, langId, user, historyList);
        project.getTerms().forEach(a -> {
            if (termsMap.containsKey(a.getTermValue().trim())) {
                if (import_values)
                    removeTermAndSetTranslation(termsMap, project, langId, a.getTermValue(), user, historyList);
            } else {
                removeTerm.add(a);
                historyList.add(new History(user, project, Constants.StatType.DELETE_TERM, a,
                        a.getTermValue(), null, historyList.get(0).getId()));
                project.getProjectLangs().forEach(d -> d.getTermLangs().forEach(e -> {
                    if (e.getTerm().getTermValue().equals(a.getTermValue()))
                        removeTermLang.add(e);
                }));
            }
            termsMap.remove(a.getTermValue());
            termsWithComment.remove(a.getTermValue());
            termLangsWithFlag.remove(a.getTermValue());
        });
        removeTerm.forEach(a -> project.getTerms().remove(a));
        removeTermLang.forEach(b -> project.getProjectLangs().forEach(c -> c.getTermLangs().remove(b)));
        if (removeTerm.size() > 0) {
            termRepository.deleteAll(removeTerm);
        }
        return addNewTerms(project, termsMap, langId, import_values, user, historyList, termLangsWithFlag, termsWithComment);
    }

    public void addFlagsOrComments(LinkedHashMap<String, String> termsMap, List<String> termLangsWithFlag, LinkedHashMap<String, String> termsWithComment) {
        if (termsMap.isEmpty()) {
            throw new Exception_400("Import failed");
        }
        Iterator<Map.Entry<String, String>> iterator = termsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (entry.getKey().startsWith(BitFlagService.StatusFlag.FUZZY.toString())) {
                termLangsWithFlag.add(entry.getKey().substring(6));
                iterator.remove();
            }
            if (entry.getKey().startsWith("COMMENT")) {
                termsWithComment.put(entry.getKey().substring(8), entry.getValue());
                iterator.remove();
            }
        }
    }

    /**
     * Remove new term by import or set new termLang.
     *
     * @param project  project for import
     * @param termsMap map of terms to remove with termLangs
     * @param langId   id of lang
     * @param user     authenticated User
     * @param typeList list of stats
     */
    private void removeTermAndSetTranslation(Map<String, String> termsMap, Project project, Long langId, String term, User user, List<History> historyList) {
        project.getProjectLangs().forEach(b -> {
            if (b.getId() == (long) langId) {
                b.getTermLangs().forEach(c -> {
                    if (c.getTerm().getTermValue().equals(term) && !c.getValue().equals(termsMap.get(term))) {
                        historyService.addImportTermLangEvent(termsMap, project, user, historyList, b, c);
                        c.setModifier(user);
                        c.setModifiedDate();
                        c.setValue(termsMap.get(term));
                        bitFlagService.dropFlagDropFromTermLang(c, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED, c);
                        bitFlagService.dropFlagDropFromTermLang(c, BitFlagService.StatusFlag.AUTOTRANSLATED, c);
                        if (b.isDefault()) {
                            project.getProjectLangs().forEach(e -> {
                                if (!e.isDefault()) {
                                    e.getTermLangs().forEach(d -> {
                                        if (d.getTerm().getTermValue().equals(term) && !d.getValue().equals("")) {
                                            bitFlagService.addFlagAddToTermLang(d, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED, d);
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    /**
     * Add new term by import.
     *
     * @param project       - project for import
     * @param termsMap      - map of terms to add with termLangs
     * @param langId        - id of lang
     * @param import_values - true if need add termLangs
     * @param user          - authenticated User
     * @param typeList      - list of stats
     * @return project with new terms
     */
    @Transactional
    public Project addNewTerms(Project project, LinkedHashMap<String, String> termsMap, Long langId, boolean import_values,
                               User user, List<History> historyList, List<String> termLangsWithFlag, LinkedHashMap<String, String> termsWithComment) {
        List<Term> termForComments = new ArrayList<>();
        for (String key : termsMap.keySet()) {
            Term term = new Term(project.getId(), key);
            project.getTerms().add(term);
            historyList.add(new History(user, project, Constants.StatType.ADD_TERM, term, "", term.getTermValue(), historyList.get(0).getId()));
            for (ProjectLang lang : project.getProjectLangs()) {
                TermLang termLang = new TermLang(lang.getId(), 0, "", term, lang.getLang(), user);
                if (import_values && langId != null && lang.getId() == (long) langId && !termsMap.get(key).isEmpty()) {
                    termLang.setValue(termsMap.get(key));
                    historyList.add(new History(user, project, Constants.StatType.TRANSLATE_BY_IMPORT, termLang,
                            lang, historyList.get(1).getId(),
                            "", termsMap.get(key)));
                    if (termLangsWithFlag.contains(key)) {
                        bitFlagService.addFlagAddToTermLang(termLang, BitFlagService.StatusFlag.FUZZY, termLang);
                    }
                }
                lang.getTermLangs().add(termLang);
            }
            if (termsWithComment.containsKey(key) && termsMap.containsKey(key)) {
                termForComments.add(term);
            }
        }
        termRepository.saveAll(project.getTerms());
        if (!termForComments.isEmpty()) {
            List<TermComment> termComments = new ArrayList<>();
            for (Term term : termForComments) {
                if (!termsWithComment.get(term.getTermValue()).isEmpty()) {
                    termComments.add(new TermComment(term.getId(), user, termsWithComment.get(term.getTermValue())));
                }
            }
            if (!termComments.isEmpty()) {
                termCommentRepository.saveAll(termComments);
            }
        }
        List<TermLang> termLangs = new ArrayList<>();
        project.getProjectLangs().forEach(a -> termLangs.addAll(a.getTermLangs()));
        termLangRepository.saveAll(termLangs);
        projectRepository.save(project);
        historyRepository.saveAll(historyList);
        return project;
    }

    @Transactional
    public Project addNewTermsWithoutHistory(Project project, LinkedHashMap<String, String> termsMap, Long langId, boolean import_values,
                               User user, List<String> termLangsWithFlag, LinkedHashMap<String, String> termsWithComment) {
        List<Term> termForComments = new ArrayList<>();
        for (String key : termsMap.keySet()) {
            Term term = new Term(project.getId(), key);
            project.getTerms().add(term);
            for (ProjectLang lang : project.getProjectLangs()) {
                TermLang termLang = new TermLang(lang.getId(), 0, "", term, lang.getLang(), user);
                if (import_values && langId != null && lang.getId() == (long) langId && !termsMap.get(key).isEmpty()) {
                    termLang.setValue(termsMap.get(key));
                    if (termLangsWithFlag.contains(key)) {
                        bitFlagService.addFlagAddToTermLang(termLang, BitFlagService.StatusFlag.FUZZY, termLang);
                    }
                }
                lang.getTermLangs().add(termLang);
            }
            if (termsWithComment.containsKey(key) && termsMap.containsKey(key)) {
                termForComments.add(term);
            }
        }
        termRepository.saveAll(project.getTerms());
        if (!termForComments.isEmpty()) {
            List<TermComment> termComments = new ArrayList<>();
            for (Term term : termForComments) {
                if (!termsWithComment.get(term.getTermValue()).isEmpty()) {
                    termComments.add(new TermComment(term.getId(), user, termsWithComment.get(term.getTermValue())));
                }
            }
            if (!termComments.isEmpty()) {
                termCommentRepository.saveAll(termComments);
            }
        }
        List<TermLang> termLangs = new ArrayList<>();
        project.getProjectLangs().forEach(a -> termLangs.addAll(a.getTermLangs()));
        termLangRepository.saveAll(termLangs);
        projectRepository.save(project);
        return project;
    }
}
