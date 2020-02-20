package com.iba.service;

import com.iba.exceptions.Exception_400;
import com.iba.model.history.History;
import com.iba.model.project.Project;
import com.iba.model.project.ProjectLang;
import com.iba.model.project.TermComment;
import com.iba.model.project.TermLang;
import com.iba.model.user.User;
import com.iba.model.view.Constants;
import com.iba.repository.*;
import com.iba.utils.EncodeChanger;
import com.iba.utils.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectLangService {

    private final AccessService accessService;

    private final ProjectLangRepository projectLangRepository;

    private final ProjectRepository projectRepository;

    private final TermLangRepository termLangRepository;

    private final BitFlagService bitFlagService;

    private final EncodeChanger encodeChanger;

    private final FileUtils fileUtils;

    private final HistoryService historyService;

    private final HistoryRepository historyRepository;

    private final TermCommentRepository termCommentRepository;

    private final static String resxConstantString = "\n" +
            "       Microsoft ResX Schema \n" +
            "       \n" +
            "       Version 2.0\n" +
            "       \n" +
            "       The primary goals of this format is to allow a simple XML format \n" +
            "       that is mostly human readable. The generation and parsing of the \n" +
            "       various data types are done through the TypeConverter classes \n" +
            "       associated with the data types.\n" +
            "       \n" +
            "       Example:\n" +
            "       \n" +
            "       ... ado.net/XML headers & schema ...\n" +
            "       <resheader name=\"resmimetype\">text/microsoft-resx</resheader>\n" +
            "       <resheader name=\"version\">2.0</resheader>\n" +
            "       <resheader name=\"reader\">System.Resources.ResXResourceReader, System.Windows.Forms, ...</resheader>\n" +
            "       <resheader name=\"writer\">System.Resources.ResXResourceWriter, System.Windows.Forms, ...</resheader>\n" +
            "       <data name=\"Name1\"><value>this is my long string</value><comment>this is a comment</comment></data>\n" +
            "       <data name=\"Color1\" type=\"System.Drawing.Color, System.Drawing\">Blue</data>\n" +
            "       <data name=\"Bitmap1\" mimetype=\"application/x-microsoft.net.object.binary.base64\">\n" +
            "           <value>[base64 mime encoded serialized .NET Framework object]</value>\n" +
            "       </data>\n" +
            "       <data name=\"Icon1\" type=\"System.Drawing.Icon, System.Drawing\" mimetype=\"application/x-microsoft.net.object.bytearray.base64\">\n" +
            "           <value>[base64 mime encoded string representing a byte array form of the .NET Framework object]</value>\n" +
            "           <comment>This is a comment</comment>\n" +
            "       </data>\n" +
            "                   \n" +
            "       There are any number of \"resheader\" rows that contain simple \n" +
            "       name/value pairs.\n" +
            "       \n" +
            "       Each data row contains a name, and value. The row also contains a \n" +
            "       type or mimetype. Type corresponds to a .NET class that support \n" +
            "       text/value conversion through the TypeConverter architecture. \n" +
            "       Classes that don't support this are serialized and stored with the \n" +
            "       mimetype set.\n" +
            "       \n" +
            "       The mimetype is used for serialized objects, and tells the \n" +
            "       ResXResourceReader how to depersist the object. This is currently not \n" +
            "       extensible. For a given mimetype the value must be set accordingly:\n" +
            "       \n" +
            "       Note - application/x-microsoft.net.object.binary.base64 is the format \n" +
            "       that the ResXResourceWriter will generate, however the reader can \n" +
            "       read any of the formats listed below.\n" +
            "       \n" +
            "       mimetype: application/x-microsoft.net.object.binary.base64\n" +
            "       value   : The object must be serialized with \n" +
            "               : System.Runtime.Serialization.Formatters.Binary.BinaryFormatter\n" +
            "               : and then encoded with base64 encoding.\n" +
            "       \n" +
            "       mimetype: application/x-microsoft.net.object.soap.base64\n" +
            "       value   : The object must be serialized with \n" +
            "               : System.Runtime.Serialization.Formatters.Soap.SoapFormatter\n" +
            "               : and then encoded with base64 encoding.\n" +
            "\n" +
            "       mimetype: application/x-microsoft.net.object.bytearray.base64\n" +
            "       value   : The object must be serialized into a byte array \n" +
            "               : using a System.ComponentModel.TypeConverter\n" +
            "               : and then encoded with base64 encoding.\n" +
            "       ";

    @Value("${resx.template.xsd}")
    private String resxXSDNodeString;

    @Value("${resx.template.resmimetype}")
    private String resxResmimetypeNodeString;

    @Value("${resx.template.version}")
    private String resxVersionNodeString;

    @Value("${resx.template.reader}")
    private String resxReaderNodeString;

    @Value("${resx.template.writer}")
    private String resxWriterNodeString;

    @Value("${file.path.load}")
    private String filePath;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(ProjectLangService.class);

    @Autowired
    public ProjectLangService(AccessService accessService, ProjectLangRepository projectLangRepository, ProjectRepository projectRepository,
                              TermLangRepository termLangRepository, BitFlagService bitFlagService, EncodeChanger encodeChanger, FileUtils fileUtils, HistoryService historyService, HistoryRepository historyRepository, TermCommentRepository termCommentRepository) {
        this.accessService = accessService;
        this.projectLangRepository = projectLangRepository;
        this.projectRepository = projectRepository;
        this.termLangRepository = termLangRepository;
        this.bitFlagService = bitFlagService;
        this.encodeChanger = encodeChanger;
        this.fileUtils = fileUtils;
        this.historyService = historyService;
        this.historyRepository = historyRepository;
        this.termCommentRepository = termCommentRepository;
    }

    /**
     * Filters all term langs depending on incoming parameters.
     *
     * @param termLangs   - list of term langs
     * @param searchValue - search value
     * @param searchParam - search category
     * @param sortValue   - sort category
     * @param filterValue - filter value
     * @return filtered list of term langs
     */
    public List<TermLang> doFilterTermLangs(List<TermLang> termLangs, String searchValue, Constants.SearchParam searchParam, Constants.SortValue sortValue, Constants.FilterValue filterValue) {
        if (searchValue != null && !searchValue.equals("")) {
            termLangs = searchBySearchValue(termLangs, searchParam, searchValue);
        }
        termLangs = filterTermLangs(termLangs, filterValue);
        termLangs = sortTermLangs(termLangs, sortValue);
        return termLangs;
    }

    /**
     * Sub list of term langs depending on search params.
     *
     * @param termLangs   - list of term langs
     * @param searchValue - search value
     * @param searchParam - search category
     * @return searched list of term langs
     */
    private List<TermLang> searchBySearchValue(List<TermLang> termLangs, Constants.SearchParam searchParam, String searchValue) {
        switch (searchParam) {
            case TRANSLATION: {
                termLangs = termLangs.stream()
                        .filter(a -> a.getValue().toLowerCase().contains(searchValue.toLowerCase()))
                        .collect(Collectors.toList());
                break;
            }
            case TERMVALUE: {
                termLangs = termLangs.stream()
                        .filter(a -> a.getTerm().getTermValue().toLowerCase().contains(searchValue.toLowerCase()))
                        .collect(Collectors.toList());
                break;
            }
            case REFERENCE: {
                termLangs = termLangs.stream().filter(a -> a.getTerm().getReferenceValue() != null && a.getTerm().getReferenceValue().toLowerCase().contains(searchValue.toLowerCase()))
                        .collect(Collectors.toList());
                break;
            }
            case MODIFIER: {
                termLangs = termLangs.stream()
                        .filter(a -> a.getModifier() != null && (a.getModifier().getFirstName().toLowerCase().contains(searchValue.toLowerCase())
                                || a.getModifier().getLastName().toLowerCase().contains(searchValue.toLowerCase())))
                        .collect(Collectors.toList());
                break;
            }
            default: {
                throw new Exception_400("Bad params!");
            }
        }
        return termLangs;
    }

    /**
     * Sort list of term langs.
     *
     * @param termLangs - list of term langs
     * @param sortValue - sort category
     * @return sorted list of term langs
     */
    private List<TermLang> sortTermLangs(List<TermLang> termLangs, Constants.SortValue sortValue) {
        switch (sortValue) {
            case TERMNAME: {
                termLangs = termLangs.stream()
                        .sorted((a, b) -> a.getTerm().getTermValue().toLowerCase()
                                .compareTo(b.getTerm().getTermValue()))
                        .collect(Collectors.toList());
                break;
            }
            case CREATIONDATE: {
                termLangs = termLangs.stream().sorted(Comparator.comparing(TermLang::getId).reversed()).collect(Collectors.toList());
                break;
            }
            case MODIFIEDDATE: {
                termLangs = termLangs.stream()
                        .sorted(Comparator.comparing(TermLang::getModifiedDate).reversed())
                        .collect(Collectors.toList());
                break;
            }
            default: {
                throw new Exception_400("Bad params!");
            }
        }
        return termLangs;
    }

    /**
     * Filter list of term lang depending on filter params.
     *
     * @param termLangs   - list of term langs
     * @param filterValue - filter value
     * @return filtered list of term langs
     */
    private List<TermLang> filterTermLangs(List<TermLang> termLangs, Constants.FilterValue filterValue) {
        switch (filterValue) {
            case DEFAULT: {
                break;
            }
            case FUZZY: {
                termLangs = termLangs.stream().filter(a -> bitFlagService.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.FUZZY)).collect(Collectors.toList());
                break;
            }
            case NOTFUZZY: {
                termLangs = termLangs.stream().filter(a -> !bitFlagService.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.FUZZY)).collect(Collectors.toList());
                break;
            }
            case TRANSLATED: {
                termLangs = termLangs.stream().filter(a -> !a.getValue().equals("")).collect(Collectors.toList());
                break;
            }
            case UNTRANSLATED: {
                termLangs = termLangs.stream().filter(a -> a.getValue().equals("")).collect(Collectors.toList());
                break;
            }
            case DEFAULTEDIT: {
                termLangs = termLangs.stream().filter(a -> bitFlagService.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED)).collect(Collectors.toList());
                break;
            }
            case AUTOTRANSLATED: {
                termLangs = termLangs.stream().filter(a -> bitFlagService.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.AUTOTRANSLATED)).collect(Collectors.toList());
                break;
            }
            default: {
                throw new Exception_400("Bad params!");
            }
        }
        return termLangs;
    }

    /**
     * Set reference value in exist projectLang
     *
     * @param exist                - exist projectLang
     * @param referenceProjectLang - reference projectLang
     */
    public void setReferenceValue(ProjectLang exist, ProjectLang referenceProjectLang) {
        exist.getTermLangs().forEach(a -> referenceProjectLang.getTermLangs().forEach(b -> {
            if (a.getTerm().getId().equals(b.getTerm().getId())) {
                a.getTerm().setReferenceValue(b.getValue());
            }
        }));
    }

    /**
     * Set stats in to term langs.
     *
     * @param terms - list of terms
     * @return list of terms with stats
     */
    public List<TermLang> setFlags(List<TermLang> terms) {
        logger.debug("Settings flags");
        terms.forEach(this::setFlagsToTerm);
        return terms;
    }

    /**
     * Set flags from db to terms
     *
     * @param term - term fo set flags
     */
    public void setFlagsToTerm(TermLang term) {
        EnumSet<BitFlagService.StatusFlag> flags = bitFlagService.getStatusFlags(term.getStatus());
        List<String> list = new ArrayList<>();
        flags.forEach(b -> list.add(b.toString()));
        term.setFlags(list);
    }

    public File createXMLFile(ProjectLang projectLang) throws ParserConfigurationException, TransformerException {
        logger.debug("Creating properties file");
        Project project = projectRepository.findById((long) projectLang.getProjectId());
        if (!new File(filePath).mkdirs()) {
            logger.debug("Directive exists");
        }
        File file = new File(filePath + project.getProjectName() + "_" + projectLang.getLang().getLangDef() + ".xml");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element resources = document.createElement("resources");
        projectLang.getTermLangs().stream().sorted(Comparator.comparing(TermLang::getId)).collect(Collectors.toList()).forEach(termLang -> {
            Comment comment = null;
            for (BitFlagService.StatusFlag flag : bitFlagService.getStatusFlags(termLang.getStatus())) {
                if (flag.equals(BitFlagService.StatusFlag.FUZZY)) {
                    comment = document.createComment(flag.toString());
                }
            }
            Element string = document.createElement("string");
            string.setAttribute("name", termLang.getTerm().getTermValue());
            Text termLangValue = document.createTextNode("\"" + termLang.getValue().replace("\n", "") + "\"");
            string.appendChild(termLangValue);
            resources.appendChild(string);
            if (comment != null) {
                string.getParentNode().insertBefore(comment, string);
            }
        });
        document.appendChild(resources);
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(file);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(domSource, streamResult);
        return file;
    }

    public File createRESXFile(ProjectLang projectLang) throws ParserConfigurationException, TransformerException, IOException, SAXException {
        logger.debug("Creating properties file");
        Project project = projectRepository.findById((long) projectLang.getProjectId());
        if (!new File(filePath).mkdirs()) {
            logger.debug("Directive exists");
        }
        File file = new File(filePath + project.getProjectName() + "_" + projectLang.getLang().getLangDef() + ".resx");

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Element root = document.createElement("root");
        document.appendChild(root);

        Comment templateComment = document.createComment(resxConstantString);
        root.appendChild(templateComment);

        appendXmlFragment(documentBuilder, root, resxXSDNodeString);
        appendXmlFragment(documentBuilder, root, resxResmimetypeNodeString);
        appendXmlFragment(documentBuilder, root, resxVersionNodeString);
        appendXmlFragment(documentBuilder, root, resxReaderNodeString);
        appendXmlFragment(documentBuilder, root, resxWriterNodeString);

        projectLang.getTermLangs().stream().sorted(Comparator.comparing(TermLang::getId)).collect(Collectors.toList()).forEach(termLang -> {
            Element data = document.createElement("data");
            data.setAttribute("name", termLang.getTerm().getTermValue());
            data.setAttribute("xml:space", "preserve");
            root.appendChild(data);
            if (!termLang.getValue().isEmpty()) {
                Element value = document.createElement("value");
                value.appendChild(document.createTextNode(termLang.getValue()));
                data.appendChild(value);
            }

            for (TermComment termComment : termCommentRepository.findAllByTermId(termLang.getTerm().getId())) {
                if (termComment != null) {
                    Element comment = document.createElement("comment");
                    comment.appendChild(document.createTextNode(termComment.getText()));
                    data.appendChild(comment);
                }
            }

        });

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(file);
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(domSource, streamResult);

        return file;
    }

    private void appendXmlFragment(DocumentBuilder docBuilder, Node parent, String fragment) throws IOException, SAXException {
        Document doc = parent.getOwnerDocument();
        Node fragmentNode = docBuilder.parse(
                new InputSource(new StringReader(fragment)))
                .getDocumentElement();
        fragmentNode = doc.importNode(fragmentNode, true);
        parent.appendChild(fragmentNode);
    }

    /**
     * Export properties file
     *
     * @param projectLang - projectLang for export
     * @param unicode     - in unicode
     * @return file with projectLang
     * @throws IOException if write in file failed
     */
    public File createPropertiesFile(ProjectLang projectLang, boolean unicode) throws IOException {
        logger.debug("Creating properties file");
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectLang.getProjectId());
        if (!new File(filePath).mkdirs()) {
            logger.debug("Directive exists");
        }
        File file = new File(filePath + project.getProjectName() + "_" + projectLang.getLang().getLangDef() + ".properties");
        PrintWriter printWriter = new PrintWriter(file, "UTF-8");
        projectLang.getTermLangs().stream().sorted(Comparator.comparing(TermLang::getId)).collect(Collectors.toList()).forEach(termLang -> {
            for (BitFlagService.StatusFlag flag : bitFlagService.getStatusFlags(termLang.getStatus())) {
                if (flag.equals(BitFlagService.StatusFlag.FUZZY)) {
                    printWriter.write("#" + flag.toString());
                    printWriter.write("\n");
                }
            }
            if (unicode) {
                printWriter.write(termLang.getTerm().getTermValue().replace("\n", "") + "=" + encodeChanger.unicode2UnicodeEsc(termLang.getValue()));
            } else {
                printWriter.write(termLang.getTerm().getTermValue() + "=" + termLang.getValue());
            }
            printWriter.write("\n");
        });
        printWriter.close();
        return file;
    }

    public File createStringsFile(ProjectLang projectLang, boolean unicode) throws IOException {
        logger.debug("Creating strings file");
        Project project = projectRepository.findById((long) projectLang.getProjectId());
        if (!new File(filePath).mkdirs()) {
            logger.debug("Directive exists");
        }
        File file = new File(filePath + project.getProjectName() + "_" + projectLang.getLang().getLangDef() + ".strings");
        PrintWriter printWriter = new PrintWriter(file, "UTF-8");
        projectLang.getTermLangs().forEach(termLang -> {
            for (BitFlagService.StatusFlag flag : bitFlagService.getStatusFlags(termLang.getStatus())) {
                if (flag.equals(BitFlagService.StatusFlag.FUZZY)) {
                    printWriter.write("/*" + flag.toString() + "*/");
                    printWriter.write("\n");
                }
            }
            if (unicode) {
                printWriter.write("\"" + termLang.getTerm().getTermValue().replace("\n", "") + "\"" +
                        "=" + "\"" + encodeChanger.unicode2UnicodeEsc(termLang.getValue()) + "\";");
            } else {
                printWriter.write("\"" + termLang.getTerm().getTermValue() + "\"" + "=" + "\"" + termLang.getValue() + "\";");
            }
            printWriter.write("\n");
        });

        printWriter.close();
        return file;
    }

    /**
     * Export json file.
     *
     * @param projectLang - projectLang for export
     * @return file with projectLang
     * @throws IOException if write in file failed
     */
    public File createJSONFile(ProjectLang projectLang) throws IOException {
        logger.debug("Creating JSON file");
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectLang.getProjectId());
        if (!new File(filePath).mkdirs()) {
            logger.debug("Directive exists");
        }
        File file = new File(filePath + project.getProjectName() + "_" + projectLang.getLang().getLangDef() + ".json");
        PrintWriter pw2 = new PrintWriter(file, "UTF-8");
        pw2.write("{ \n");
        Iterator<TermLang> iterator = projectLang.getTermLangs().iterator();
        while (iterator.hasNext()) {
            TermLang termLang = iterator.next();
            pw2.write("    \"" + JsonFormat(termLang.getTerm().getTermValue()) + "\"" + " : " + "\"" + JsonFormat(termLang.getValue()) + "\"");
            if (iterator.hasNext()) {
                pw2.write(", \n");
            }
        }
        pw2.write(" \n}");
        pw2.close();
        return file;
    }

    /**
     * Edit string to json format.
     *
     * @param value value of string
     * @return string in json format
     */
    private String JsonFormat(String value) {
        value = value.trim().replace("\n", "");
        if (value.length() != 0) {
            if (value.charAt(0) == '"') {
                value = value.substring(1);
            }
            if (value.charAt(value.length() - 1) == '"') {
                value = value.substring(0, value.length() - 1);
            }
        }
        return value.replace("\"", "\\\"");
    }

    /**
     * Flush all termLang's value
     * and drop all flags.
     *
     * @param projectLang - projectLang for flush
     * @param user        - authenticated User
     * @param statList    - list with statistic
     */
    public void flushValueInTermLang(Project project, ProjectLang projectLang, User user, List<History> historyList) {
        projectLang.getTermLangs().forEach(b -> {
            if (b.getTerm().isSelected() && !b.getValue().equals("")) {
                historyList.add(new History(user, project, Constants.StatType.EDIT, b,
                        projectLang, b.getValue(), ""));
                b.setValue("");
                b.setModifiedDate();
                b.setModifier(user);
                bitFlagService.dropFlag(b, BitFlagService.StatusFlag.FUZZY);
                bitFlagService.dropFlag(b, BitFlagService.StatusFlag.AUTOTRANSLATED);
                bitFlagService.dropFlag(b, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
            }
        });
    }


    /**
     * Sets the number of criteria found after filter in projectLang
     *
     * @param projectLang - projectLang for set count of criteria
     */
    public void setFilterCriteries(ProjectLang projectLang) {
        int countFuzzy = 0;
        int countAutotranslated = 0;
        int countDefaultChange = 0;
        for (TermLang termLang : projectLang.getTermLangs()) {
            EnumSet<BitFlagService.StatusFlag> flags = bitFlagService.getStatusFlags(termLang.getStatus());
            for (BitFlagService.StatusFlag flag : flags) {
                if (flag.getValue() == 2) {
                    countFuzzy++;
                }
                if (flag.getValue() == 4) {
                    countAutotranslated++;
                }
                if (flag.getValue() == 1) {
                    countDefaultChange++;
                }
            }
        }
        projectLang.setCountAutotranslated(countAutotranslated);
        projectLang.setCountChangeDefault(countDefaultChange);
        projectLang.setCountFuzzy(countFuzzy);
    }

    /**
     * Import only termLangs from file to projectLang.
     *
     * @param projectLang - projectLang for import
     * @param file        - file
     * @param user        - authenticated User
     * @throws IOException   - File is null
     * @throws JSONException - Parse strings in json failed
     */
    public void importTranslations(ProjectLang projectLang, File file, User user) throws IOException, JSONException, ParserConfigurationException, SAXException {
        logger.debug("Importing translations");
        LinkedHashMap<String, String> translationMap = fileUtils.parseFile(file);
        if (translationMap.isEmpty()) {
            throw new Exception_400("Import failed!");
        }
        Project project = projectRepository.findById((long) projectLang.getProjectId());
        project.getProjectLangs().remove(projectLang);
//        List<Constants.StatType> typeList = new ArrayList<>();
        List<History> historyList = new ArrayList<>();
        historyList.add(historyRepository.save(new History(user, project,
                Constants.StatType.IMPORT_TRANSLATIONS, projectLang)));
        projectLang.getTermLangs()
                .forEach(a -> {
                    if (translationMap.containsKey(a.getTerm().getTermValue()) && !a.getValue().equals(translationMap.get(a.getTerm().getTermValue()))) {
                        historyService.addImportTermLangEvent(translationMap, project, user, historyList, projectLang, a);
                        a.setValue(translationMap.get(a.getTerm().getTermValue()));
                        a.setModifiedDate();
                        a.setModifier(user);
                        bitFlagService.dropFlag(a, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                        if (projectLang.isDefault()) {
                            project.getProjectLangs().forEach(b -> b.getTermLangs().forEach(c -> {
                                if (c.getTerm().getTermValue().equals(a.getTerm().getTermValue()) && !c.getValue().equals("")) {
                                    bitFlagService.addFlag(c, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                                }
                            }));
                        }
                    }
                });
        projectLangRepository.save(projectLang);
        historyRepository.saveAll(historyList);
//        historyService.createStats(typeList, user.getId(), project.getId());
    }

    /**
     * Get value from TermLang,
     * where id lang = id lang translate from.
     *
     * @param from     projectLang translate from
     * @param termLang termLang value
     * @return value if termLang isn't null
     */
    public String getTranslationValue(ProjectLang from, TermLang termLang) {
        for (TermLang term : from.getTermLangs()) {
            if (term.getTerm().getTermValue().equals(termLang.getTerm().getTermValue())) {
                return term.getValue();
            }
        }
        return null;
    }

    /**
     * Get language translate from.
     *
     * @param langTo lang translate to
     * @param from   id of lang translate from
     * @param user   authenticated User
     * @return lang translate from
     */
    public ProjectLang getFromLanguage(ProjectLang langTo, Long from, User user) {
        ProjectLang result;
        if (from != null) {
            result = projectLangRepository.findById((long) from);
        } else {
            result = projectLangRepository.findByDefaultAndProjectId(langTo.getProjectId());
        }
        accessService.isNotProjectLangOrAccessDenied(result, user, false);
        return result;
    }

    /**
     * Translate termLang.
     *
     * @param termLang termLang translate to
     * @param user     authenticated User
     * @param value    auto translated value of TermLang
     */
    public void translateTermLang(TermLang termLang, User user, String value) {
        termLang.setValue(value);
        termLang.setModifier(user);
        termLang.setModifiedDate();
        bitFlagService.dropFlagDropFromTermLang(termLang, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED, termLang);
        bitFlagService.addFlagAddToTermLang(termLang, BitFlagService.StatusFlag.FUZZY, termLang);
        bitFlagService.addFlagAddToTermLang(termLang, BitFlagService.StatusFlag.AUTOTRANSLATED, termLang);
        setFlagsToTerm(termLang);
    }

    /**
     * Set flags if lang translate to is default.
     *
     * @param termLang termLang
     */
    public void setDefWasChangedFlagToTerms(TermLang termLang) {
        List<TermLang> termLangs = termLangRepository.findByTerm(termLang.getTerm());
        termLangs.forEach(a -> {
            if (!a.getValue().equals("") && !a.getId().equals(termLang.getId())) {
                bitFlagService.addFlag(a, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
            }
        });
    }

    public void flushTranslationsInProjectLang(Project project, User user, ProjectLang projectLang, List<History> historyList) {
        for (TermLang notEmptyTermLang : termLangRepository.findAllByProjectLangIdAndValueIsNotEmpty(projectLang.getId())) {
            historyList.add(new History(user, project, Constants.StatType.EDIT, notEmptyTermLang,
                    projectLang, historyList.get(0).getId(), notEmptyTermLang.getValue(), ""));
            notEmptyTermLang.setValue("");
        }
        termLangRepository.resetStatusByProjectLang(projectLang.getId());
    }
}
