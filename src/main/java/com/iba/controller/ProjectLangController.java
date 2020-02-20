package com.iba.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.iba.exceptions.Exception_400;
import com.iba.exceptions.Exception_403;
import com.iba.exceptions.Exception_404;
import com.iba.model.history.History;
import com.iba.model.project.Progress;
import com.iba.model.project.Project;
import com.iba.model.project.ProjectLang;
import com.iba.model.project.TermLang;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.model.view.View;
import com.iba.repository.*;
import com.iba.service.*;
import com.iba.utils.FileUtils;
import com.iba.utils.PagesUtil;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/project-lang")
public class ProjectLangController {

    private final ProjectLangRepository projectLangRepository;

    private final ProjectRepository projectRepository;

    private final ProjectLangService projectLangService;

    private final ProjectService projectService;

    private final BitFlagService bitFlagService;

    private final TermLangRepository termLangRepository;

    private final Translator translator;

    private final AccessService accessService;

    private final HistoryService historyService;

    private final FileUtils fileUtils;

    private final PagesUtil pagesUtil;

    private final TermRepository termRepository;

    private final HistoryRepository historyRepository;

    private final TermCommentRepository termCommentRepository;

    @Value("${file.path.load}")
    private String filePath;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(ProjectLangController.class);

    @Autowired
    public ProjectLangController(ProjectLangRepository projectLangRepository, ProjectRepository projectRepository,
                                 TermLangRepository termLangRepository, Translator translator, AccessService accessService,
                                 ProjectLangService projectLangService, ProjectService projectService,
                                 HistoryService historyService, FileUtils fileUtils, PagesUtil pagesUtil, TermRepository termRepository, BitFlagService bitFlagService, HistoryRepository historyRepository, TermCommentRepository termCommentRepository) {
        this.projectLangRepository = projectLangRepository;
        this.projectRepository = projectRepository;
        this.projectLangService = projectLangService;
        this.projectService = projectService;
        this.termLangRepository = termLangRepository;
        this.translator = translator;
        this.accessService = accessService;
        this.historyService = historyService;
        this.fileUtils = fileUtils;
        this.pagesUtil = pagesUtil;
        this.termRepository = termRepository;
        this.bitFlagService = bitFlagService;
        this.historyRepository = historyRepository;
        this.termCommentRepository = termCommentRepository;
    }

    /**
     * Filters all TermLangs depending on incoming parameters.
     *
     * @param projectLang            ProjectLang of TermLangs
     * @param user                   authenticated User
     * @param searchValue            search value
     * @param searchParam            search param
     * @param sortValue              sort value
     * @param filterValue            filter value
     * @param page                   page
     * @param referenceProjectLangId reference ProjectLang
     * @return ProjectLang of Project
     * @throws Exception_400 if filter or sort value not exists
     */
    @GetMapping("/{projectLangId}")
    public ProjectLang doFilterProjectLang(@PathVariable("projectLangId") ProjectLang projectLang, @AuthenticationPrincipal User user,
                                           @RequestParam String searchValue,
                                           @RequestParam Constants.SearchParam searchParam,
                                           @RequestParam Constants.SortValue sortValue,
                                           @RequestParam Constants.FilterValue filterValue,
                                           Pageable page,
                                           @RequestParam(required = false) Long referenceProjectLangId) {
        ProjectLang existProjectLang = new ProjectLang();
        existProjectLang.setTermLangs(projectLangService.setFlags(projectLang.getTermLangs()));
        projectLangService.setFilterCriteries(existProjectLang);
        if (referenceProjectLangId == null && !projectLang.isDefault()) {
            ProjectLang defaultProjectLang = projectLangRepository.findByDefaultAndProjectId(projectLang.getProjectId());
            accessService.isNotObject(defaultProjectLang);
            projectLangService.setReferenceValue(existProjectLang, defaultProjectLang);
        } else if (referenceProjectLangId != null && !referenceProjectLangId.equals(projectLang.getId())) {
            ProjectLang newReferenceProjectLang = projectLangRepository.findById((long) referenceProjectLangId);
            accessService.isNotObject(newReferenceProjectLang);
            projectLangService.setReferenceValue(existProjectLang, newReferenceProjectLang);
        }
        existProjectLang.setTermLangs(projectLangService.doFilterTermLangs(projectLang.getTermLangs(), searchValue, searchParam, sortValue, filterValue));
        existProjectLang.setPageParams(pagesUtil.createPagesParamsByList(existProjectLang.getTermLangs(), page));
        existProjectLang.setTermLangs(pagesUtil.createSubListByPage(existProjectLang.getTermLangs(), existProjectLang.getPageParams(), page.getPageSize()));
        existProjectLang.setRole(accessService.getUserRole(projectRepository.findByIdAndIsDeletedFalse(projectLang.getProjectId()), user));
        existProjectLang.getTermLangs().forEach(termLang -> termLang.getTerm().setCommentsCount(termCommentRepository.countAllByTermId(termLang.getTerm().getId())));
        return existProjectLang;
    }

    /**
     * Returns the progress of the ProjectLang.
     *
     * @param projectLang ProjectLang for count progress
     * @param user        authenticated User
     * @return not entity class Progress with progress of ProjectLang
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @GetMapping("/{projectLangId}/progress")
    public Progress getProgress(@PathVariable("projectLangId") ProjectLang projectLang, @AuthenticationPrincipal User user) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectLang.getProjectId());
        accessService.isNotProjectOrAccessDenied(project, user, false);
        long countOfTranslatedTermLang = termLangRepository.countAllByProjectLangIdAndValueIsNotEmpty(projectLang.getId());
        double countOfAllTermLang = termRepository.countAllByProjectId(project.getId());
        return new Progress(termRepository.countAllByProjectId(project.getId()), countOfTranslatedTermLang,
                countOfAllTermLang > 0.0 || (double) countOfTranslatedTermLang > 0.0 ? (double) countOfTranslatedTermLang / countOfAllTermLang : 0, project.getProjectName(),
                projectLang.getLang().getLangName(), project.getDescription(), projectLang.getLang().getLangDef());
    }

    /**
     * AutoTranslate only selected TermLangs.
     *
     * @param langTo    lang translate to
     * @param termLangs list of termLangs for translate
     * @param from      id of lang translate from
     * @param user      authenticated User
     * @return list of auto translated termLangs
     * @throws IOException   if request to translate failed
     * @throws Exception_400 if choose incorrect Lang
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project or ProjectLang not found
     */
    @PostMapping("{projectLangId}/auto-translate-selected")
    @JsonView(View.ProjectItem.class)
    public List<TermLang> autoTranslateSelected(@PathVariable("projectLangId") ProjectLang langTo,
                                                @RequestBody List<TermLang> termLangs,
                                                @RequestParam(required = false) Long from,
                                                @AuthenticationPrincipal User user) throws IOException {
        logger.debug("User " + user.getUsername() + " is trying to auto translate term langs");
        Project project = projectRepository.findByIdAndIsDeletedFalse(langTo.getProjectId());
        accessService.isNotProjectOrAccessDenied(project, user, false);
        accessService.isNotProjectLangOrAccessDenied(langTo, user, false);
        if (langTo.getId().equals(from)) {
            logger.error("User choose incorrect lang");
            throw new Exception_400("You cannot choose this lang!");
        }
        List<History> historyList = new ArrayList<>();
        ProjectLang langFrom = projectLangService.getFromLanguage(langTo, from, user);
        String fromDef = langFrom.getLang().getLangDef().toLowerCase();
        String toDef = langTo.getLang().getLangDef().toLowerCase();
        if (!fromDef.equals(toDef)) {
            for (TermLang termLang : termLangs) {
                String fromTermValue = projectLangService.getTranslationValue(langFrom, termLang);
                if (fromTermValue != null && termLang.isSelected() && !fromTermValue.equals("")) {
                    String autoTranslatedValue = translator.translate(fromDef, toDef, fromTermValue);
                    projectLangService.translateTermLang(termLang, user, autoTranslatedValue);
                    historyList.add(new History(user, project, Constants.StatType.AUTO_TRANSLATE, termLang, langTo, termLang.getValue(), autoTranslatedValue, fromTermValue));
                    if (langTo.isDefault()) {
                        projectLangService.setDefWasChangedFlagToTerms(termLang);
                    }
                }
            }
            termLangRepository.saveAll(termLangs);
            historyRepository.saveAll(historyList);
        }
        return termLangs;
    }

    /**
     * AutoTranslate only one TermLang.
     *
     * @param langTo   lang translate to
     * @param termLang TermLang for translate
     * @param from     id of lang translate from
     * @param user     authenticated User
     * @return list of auto translated TermLangs
     * @throws IOException   if request to translate failed
     * @throws Exception_400 if choose incorrect Lang
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project or ProjectLang not found
     */
    @PostMapping("{projectLangId}/auto-translate/{termLangId}")
    @JsonView(View.ProjectItem.class)
    public TermLang autoTranslate(@PathVariable("termLangId") TermLang termLang,
                                  @PathVariable("projectLangId") ProjectLang langTo,
                                  @RequestParam(required = false) Long from,
                                  @AuthenticationPrincipal User user) throws IOException {
        logger.debug("User " + user.getUsername() + " is trying to auto translate term langs");
        Project project = projectRepository.findByIdAndIsDeletedFalse(langTo.getProjectId());
        accessService.isNotProjectOrAccessDenied(project, user, false);
        accessService.isNotProjectLangOrAccessDenied(langTo, user, false);
        if (langTo.getId().equals(from)) {
            logger.error("User choose incorrect lang ");
            throw new Exception_400("You cannot choose this lang!");
        }
        ProjectLang langFrom = projectLangService.getFromLanguage(langTo, from, user);
        String fromDef = langFrom.getLang().getLangDef().toLowerCase();
        String toDef = langTo.getLang().getLangDef().toLowerCase();
        String fromTermValue = projectLangService.getTranslationValue(langFrom, termLang);
        String autoTranslatedValue = translator.translate(fromDef, toDef, fromTermValue);
        if (fromTermValue != null && !fromTermValue.equals("") && !fromDef.equals(toDef) && !autoTranslatedValue.equals("")) {
            historyService.createStat(Constants.StatType.AUTO_TRANSLATE, user, project, termLang, langTo, termLang.getValue(), autoTranslatedValue, fromTermValue);
            projectLangService.translateTermLang(termLang, user, autoTranslatedValue);
            if (langTo.isDefault()) {
                projectLangService.setDefWasChangedFlagToTerms(termLang);
            }
        }
        termLangRepository.save(termLang);
        logger.debug("User " + user.getUsername() + " translated term langs");
        return termLang;
    }

    /**
     * Get free reference languages with progress
     * for set reference language.
     *
     * @param projectLang ProjectLang
     * @param user        authenticated User
     * @return list of free reference languages
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project not found
     */
    @GetMapping("{projectLangId}/referenceLang")
    public List<ProjectLang> getReferenceProjectLang(@PathVariable("projectLangId") ProjectLang projectLang, @AuthenticationPrincipal User user) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectLang.getProjectId());
        accessService.isNotProjectOrAccessDenied(project, user, false);
        List<ProjectLang> referenceProjectLangs = project.getProjectLangs().stream().filter(a -> !a.getId().equals(projectLang.getId())).collect(Collectors.toList());
        referenceProjectLangs.forEach(a -> {
            a.setTermsCount(project.getTerms().size());
            a.setTranslatedCount(termLangRepository.countAllByProjectLangIdAndValueIsNotEmpty(a.getId()));
            a.setTermLangs(null);
        });
        return referenceProjectLangs;
    }

    /**
     * Flush selected TermLangs in ProjectLang.
     *
     * @param projectLang ProjectLang
     * @param user        authenticated User
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project or ProjectLang not found
     */
    @PostMapping("/{projectLangId}/flush-selected")
    public void flushSelectedTranslations(@PathVariable("projectLangId") ProjectLang projectLang, @AuthenticationPrincipal User user) {
        logger.debug("User " + user.getUsername() + " is trying to empty selected term langs");
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectLang.getProjectId());
        accessService.isNotProjectOrAccessDenied(project, user, true);
        accessService.isNotProjectLangOrAccessDenied(projectLang, user, true);
        List<History> historyList = new ArrayList<>();
        if (projectLang.isDefault()) {
            List<ProjectLang> projectLangs = project.getProjectLangs();
            projectLangs.forEach(a -> {
                if (a.isDefault()) {
                    projectLangService.flushValueInTermLang(project, a, user, historyList);
                } else {
                    a.getTermLangs().forEach(b -> {
                        if (b.getTerm().isSelected() && !b.getValue().equals("")) {
                            bitFlagService.addFlag(b, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                        }
                    });
                }
            });
        } else {
            projectLangService.flushValueInTermLang(project, projectLang, user, historyList);
        }
        termLangRepository.saveAll(projectLang.getTermLangs());
        historyRepository.saveAll(historyList);
        logger.debug("User " + user.getUsername() + " made selected term langs empty");
    }

    /**
     * Flush all translations in ProjectLang.
     *
     * @param projectLang ProjectLang
     * @param user        authenticated user
     * @throws Exception_403 if access denied
     * @throws Exception_404 if Project or ProjectLang not found
     */
    @PostMapping("/{projectLangId}/flush-all")
    public void flushAllTranslations(@PathVariable("projectLangId") ProjectLang projectLang, @AuthenticationPrincipal User user) {
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectLang.getProjectId());
        accessService.isNotProjectOrAccessDenied(project, user, true);
        accessService.isNotProjectLangOrAccessDenied(projectLang, user, true);
        List<History> historyList = new ArrayList<>();
        historyList.add(historyRepository.save(new History(user, project,
                Constants.StatType.FLUSH_PROJECT_LANG, projectLang)));
        if (projectLang.isDefault()) {
            List<ProjectLang> projectLangs = project.getProjectLangs();
            projectLangs.forEach(a -> {
                if (a.getId().equals(projectLang.getId())) {
                    projectLangService.flushTranslationsInProjectLang(project, user, projectLang, historyList);
                } else {
                    a.getTermLangs().forEach(b -> {
                        if (!b.getValue().equals("")) {
                            bitFlagService.addFlag(b, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                        }
                    });
                }
            });
        } else {
            projectLangService.flushTranslationsInProjectLang(project, user, projectLang, historyList);
        }
        historyRepository.saveAll(historyList);
        projectLangRepository.save(projectLang);
    }

    /**
     * Import translations with or without new terms,
     * depending on incoming parameters.
     *
     * @param projectLang ProjectLang
     * @param file        file for import
     * @param user        authenticated user
     * @param merge       import with new Terms
     * @param replace     import with new Terms and delete old
     * @param page        return necessary depending on the page
     * @return ProjectLang with new Terms and TermLangs
     * @throws IOException                  File is null
     * @throws JSONException                Parse strings in json failed
     * @throws ParserConfigurationException if parse xml failed
     * @throws SAXException                 if read node from xml file failed
     * @throws Exception_400                if file is null
     * @throws Exception_403                if access denied
     * @throws Exception_404                if Project or ProjectLang not found
     */
    @PostMapping("/{projectLangId}/import-translations")
    @JsonView(View.ProjectItem.class)
    public ProjectLang importTranslations(@PathVariable("projectLangId") ProjectLang projectLang, MultipartFile file,
                                          @AuthenticationPrincipal User user, @RequestParam boolean merge, @RequestParam boolean replace,
                                          @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException, JSONException, ParserConfigurationException, SAXException {
        logger.debug("User " + user.getUsername() + " is trying to import translations");
        accessService.isNotProjectLangOrAccessDenied(projectLang, user, false);
        if (file == null) {
            logger.error("File is null");
            throw new Exception_400("File not chosen, Choose the file!");
        }
        if (new File("temp/files/").mkdirs()) {
            logger.debug("Directory was created");
        }
        File convFile = new File(filePath + file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        if (!merge && !replace)
            projectLangService.importTranslations(projectLang, convFile, user);
        else {
            Project project = projectRepository.findByIdAndIsDeletedFalse(projectLang.getProjectId());
            if (project == null) {
                return null;
            }
            if (merge && !replace) {
                projectService.importTermsMerge(project, convFile, true, projectLang.getId(), user);
            } else {
                projectService.importTermsFullReplace(project, convFile, true, projectLang.getId(), user);
            }
        }
        projectLang = projectLangRepository.findById((long) projectLang.getId());
        projectLang.setTermLangs(termLangRepository.findByProjectLangId(projectLang.getId(), page));
        projectLang.setTermLangs(projectLangService.setFlags(projectLang.getTermLangs()));
        logger.debug("User " + user.getUsername() + " imported translations");
        return projectLang;
    }

    /**
     * Export ProjectLang in response.
     *
     * @param projectLang ProjectLang for export
     * @param user        authenticated user
     * @param type        format of file
     * @param unicode     enable unicode
     * @param response    set file in responce
     * @throws IOException                  if file was not been created
     * @throws TransformerException         if export XML or RESX files failed
     * @throws ParserConfigurationException if export XML or RESX files failed
     * @throws SAXException                 if export XML or RESX files failed
     * @throws Exception_400                if file deleted failed or bad type of file
     * @throws Exception_403                if access denied
     * @throws Exception_404                if ProjectLang not found
     */
    @GetMapping("/{projectLangId}/export")
    public void download(@PathVariable("projectLangId") ProjectLang projectLang,
                         @AuthenticationPrincipal User user,
                         @RequestParam Constants.FileTypes type,
                         @RequestParam boolean unicode,
                         HttpServletResponse response) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        logger.debug("User " + user.getUsername() + " is trying to export the ProjectLang");
        accessService.isNotProjectLangOrAccessDenied(projectLang, user, false);
        File file;
        switch (type) {
            case json: {
                file = projectLangService.createJSONFile(projectLang);
                break;
            }
            case properties: {
                file = projectLangService.createPropertiesFile(projectLang, unicode);
                break;
            }
            case strings: {
                file = projectLangService.createStringsFile(projectLang, unicode);
                break;
            }
            case xml: {
                file = projectLangService.createXMLFile(projectLang);
                break;
            }
            case resx: {
                file = projectLangService.createRESXFile(projectLang);
                break;
            }
            default: {
                throw new Exception_400("Bad params");
            }
        }
        if (file.exists()) {
            InputStream inputStream = fileUtils.setFileToResponse(response, file);
            if (!file.delete()) {
                throw new Exception_400("File deleted failed");
            }
            inputStream.close();
        }
        logger.debug("User " + user.getUsername() + " exported the ProjectLang");
    }
}
