package com.iba.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.iba.exceptions.Exception_400;
import com.iba.model.view.Constants;
import com.iba.service.BitFlagService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.UUID;

@Service
public class FileUtils {

    /**
     * Set file into response.
     *
     * @param response response to set
     * @param file     incoming file
     * @return inputStream with responce
     * @throws IOException create inputStream with file failed
     */
    public InputStream setFileToResponse(HttpServletResponse response, File file) throws IOException {
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
        response.setContentLength((int) file.length());
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        FileCopyUtils.copy(inputStream, response.getOutputStream());
        return inputStream;
    }

    public String getImageFromURL(URL url) {
        String filename = UUID.randomUUID().toString() + ".jpg";
        File file = new File("temp/images/" + filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            BufferedImage image = ImageIO.read(url);
            ImageIO.write(image, "jpg", file);
        } catch (IOException e) {
            throw new Exception_400("Bad photo");
        }
        return filename;
    }


    /**
     * Choose format of parse file.
     *
     * @param file file for parse
     * @return Map (where key=term,value=termLang) by method of parse
     * @throws IOException   if file not exists
     * @throws JSONException parse string in json failed
     */
    public LinkedHashMap<String, String> parseFile(File file) throws IOException, JSONException, ParserConfigurationException, SAXException {
        if (file.getName().endsWith(Constants.FileTypes.properties.toString())) {
            return parseProperties(file);
        }
        if (file.getName().endsWith(Constants.FileTypes.json.toString())) {
            return parseJson(file);
        }
        if (file.getName().endsWith(Constants.FileTypes.strings.toString())) {
            return parseStrings(file);
        }
        if (file.getName().endsWith(Constants.FileTypes.xml.toString())) {
            return parseXML(file);
        }
        if (file.getName().endsWith(Constants.FileTypes.resx.toString()) || file.getName().endsWith(Constants.FileTypes.resw.toString())) {
            return parseRESX(file);
        }
        if (file.getName().endsWith(Constants.FileTypes.po.toString()) || file.getName().endsWith(Constants.FileTypes.pot.toString())) {
            // TODO: 28.11.2019 ADD PARSER!@!
//            System.out.println(parseGettext(file));
            return null;
        }
        if (file.getName().endsWith(Constants.FileTypes.xls.toString())) {
            return parseExcelXLS(file);
        }
        if (file.getName().endsWith(Constants.FileTypes.xlsx.toString())) {
            return parseExcelXLSX(file);
        }
        return null;
    }

    private LinkedHashMap<String, String> parseXML(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        NodeList resourceNodeList = document.getElementsByTagName("resources");
        for (int i = 0; i < resourceNodeList.getLength(); i++) {
            Node resourceNode = resourceNodeList.item(i);
            if (resourceNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList stringsNodeList = resourceNode.getChildNodes();
                boolean flag = false;
                boolean comment = false;
                String commentValue = null;
                for (int j = 0; j < stringsNodeList.getLength(); j++) {
                    Node stringNode = stringsNodeList.item(j);
                    if (stringNode.getNodeType() == Node.COMMENT_NODE) {
                        if (stringNode.getTextContent().toLowerCase().trim().contentEquals("fuzzy")) {
                            flag = true;
                        } else {
                            comment = true;
                            commentValue = stringNode.getTextContent();
                        }
                    } else if (stringNode.getNodeType() == Node.ELEMENT_NODE && stringNode.getNodeName().contentEquals("string")) {
                        Element element = (Element) stringNode;
                        if (flag) {
                            map.put(BitFlagService.StatusFlag.FUZZY + " " + element.getAttribute("name"), "");
                            flag = false;
                        }
                        if (comment) {
                            map.put("COMMENT " + element.getAttribute("name"), commentValue);
                            comment = false;
                            commentValue = null;
                        }
                        map.put(element.getAttribute("name"), StringUtils.substringBetween(element.getTextContent(), "\"", "\""));
                    }
                }
            }
        }
        return editMapByLength(map);
    }

    private LinkedHashMap<String, String> parseRESX(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        NodeList rootNodeList = document.getElementsByTagName("root");
        for (int i = 0; i < rootNodeList.getLength(); i++) {
            Node rootNode = rootNodeList.item(i);
            if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList dataNodeList = rootNode.getChildNodes();
                for (int j = 0; j < dataNodeList.getLength(); j++) {
                    Node dataNode = dataNodeList.item(j);
                    if (dataNode.getNodeType() == Node.ELEMENT_NODE && dataNode.getNodeName().contentEquals("data")) {
                        Element dataElement = (Element) dataNode;
                        NodeList valueAndCommentNodeList = dataNode.getChildNodes();
                        for (int k = 0; k < valueAndCommentNodeList.getLength(); k++) {
                            Node valueAndCommentNode = valueAndCommentNodeList.item(k);
                            if (valueAndCommentNode.getNodeType() == Node.ELEMENT_NODE && valueAndCommentNode.getNodeName().contentEquals("comment")) {
                                if (valueAndCommentNode.getTextContent().contentEquals("fuzzy")) {
                                    map.put(BitFlagService.StatusFlag.FUZZY + " " + dataElement.getAttribute("name"), "");
                                } else {
                                    map.put("COMMENT " + dataElement.getAttribute("name"), valueAndCommentNode.getTextContent());
                                }
                            }
                            if (valueAndCommentNode.getNodeType() == Node.ELEMENT_NODE && valueAndCommentNode.getNodeName().contentEquals("value")) {
                                map.put(dataElement.getAttribute("name"), valueAndCommentNode.getTextContent());
                            }
                        }
                    }
                }
            }
        }
        return editMapByLength(map);
    }

    private LinkedHashMap<String, String> parseGettext(File file) throws FileNotFoundException {
        LinkedHashMap<String, String> propertiesMap = new LinkedHashMap<>();
        BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        return editMapByLength(propertiesMap);
    }


    private LinkedHashMap<String, String> parseExcelXLS(File file) throws IOException {
        LinkedHashMap<String, String> propertiesMap = new LinkedHashMap<>();
        HSSFWorkbook myExcelBook = new HSSFWorkbook(new FileInputStream(file));
        HSSFSheet myExcelSheet = myExcelBook.getSheetAt(myExcelBook.getActiveSheetIndex());
        for (Row row : myExcelSheet) {
            putIntoMapFromCell(propertiesMap, row);
        }
        myExcelBook.close();
        return editMapByLength(propertiesMap);
    }

    private LinkedHashMap<String, String> parseExcelXLSX(File file) throws IOException {
        LinkedHashMap<String, String> propertiesMap = new LinkedHashMap<>();
        XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet myExcelSheet = myExcelBook.getSheetAt(myExcelBook.getActiveSheetIndex());
        for (Row row : myExcelSheet) {
            putIntoMapFromCell(propertiesMap, row);
        }
        myExcelBook.close();
        return editMapByLength(propertiesMap);
    }

    private void putIntoMapFromCell(LinkedHashMap<String, String> propertiesMap, Row row) {
        String term = null;
        String termLang = null;
        String comment = null;
        boolean flag = false;
        for (int i = 0; i < 4; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                switch (i) {
                    case 0: {
                        term = cell.getStringCellValue();
                        break;
                    }
                    case 1: {
                        termLang = cell.getStringCellValue();
                        break;
                    }
                    case 2: {
                        comment = cell.getStringCellValue();
                        break;
                    }
                    case 3: {
                        if (!cell.getStringCellValue().isEmpty()) {
                            flag = true;
                        }
                        break;
                    }
                }
            }
        }
        propertiesMap.put(term, termLang);
        if (comment != null) {
            propertiesMap.put("COMMENT " + term, comment);
        }
        if (flag) {
            propertiesMap.put(BitFlagService.StatusFlag.FUZZY + " " + term, comment);
        }
    }

    private String extractFromCell(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default: {
                throw new Exception_400("Import failed");
            }
        }
    }

    private LinkedHashMap<String, String> parseStrings(File file) throws IOException {
        LinkedHashMap<String, String> propertiesMap = new LinkedHashMap<>();
        BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = buf.readLine();
        boolean flag = false;
        boolean comment = false;
        String commentValue = null;
        while (line != null) {
            if (line.startsWith("/*") && line.endsWith("*/") && StringUtils.substringBetween(line, "/*", "*/").toLowerCase().contentEquals("fuzzy")) {
                flag = true;
            } else if (line.startsWith("/*") && line.endsWith("*/")) {
                comment = true;
                commentValue = StringUtils.substringBetween(line, "/*", "*/");
            } else if (!line.isEmpty()) {
                if (flag) {
                    propertiesMap.put(BitFlagService.StatusFlag.FUZZY + " " + StringUtils.substringBetween(StringUtils.substringBefore(line, "="), "\"", "\""), "");
                    flag = false;
                }
                if (comment && !commentValue.isEmpty()) {
                    propertiesMap.put("COMMENT " + StringUtils.substringBetween(StringUtils.substringBefore(line, "="), "\"", "\""),
                            commentValue);
                    comment = false;
                    commentValue = null;
                }
                propertiesMap.put(StringUtils.substringBetween(StringUtils.substringBefore(line, "="), "\"", "\""),
                        StringUtils.substringBetween(StringUtils.substringAfter(line, "="), "\"", "\""));
            }
            line = buf.readLine();
        }
        return editMapByLength(propertiesMap);
    }

    /**
     * Parse file in properties format.
     *
     * @param file file for parse
     * @return Map of strings (key,value)
     * @throws IOException if file not exists
     */
    private LinkedHashMap<String, String> parseProperties(File file) throws IOException {
        LinkedHashMap<String, String> propertiesMap = new LinkedHashMap<>();
        BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = buf.readLine();
        boolean flag = false;
        boolean comment = false;
        String commentValue = null;
        while (line != null) {
            if (line.startsWith("#") && StringUtils.substringAfter(line, "#").toLowerCase().trim().contentEquals("fuzzy")) {
                flag = true;
            } else if (line.startsWith("#")) {
                comment = true;
                commentValue = StringUtils.substringAfter(line, "#");
            } else if (!line.isEmpty()) {
                if (flag) {
                    propertiesMap.put(BitFlagService.StatusFlag.FUZZY + " " + StringUtils.substringBefore(line, "="), "");
                    flag = false;
                }
                if (comment && !commentValue.isEmpty()) {
                    propertiesMap.put("COMMENT " + StringUtils.substringBefore(line, "="),
                            commentValue);
                    comment = false;
                    commentValue = null;
                }
                propertiesMap.put(StringUtils.substringBefore(line, "="),
                        StringUtils.substringAfter(line, "="));
            }
            line = buf.readLine();
        }
        return editMapByLength(propertiesMap);
    }

    /**
     * Parse file in json format.
     *
     * @param file name of file for parse
     * @return Map of strings (key,value)
     * @throws IOException   if file not exists
     * @throws JSONException parse string in json failed
     */
    private LinkedHashMap<String, String> parseJson(File file) throws IOException, JSONException {
        Gson gson = new Gson();
        JsonElement json = gson.fromJson(new FileReader(file.getPath()), JsonElement.class);
        if (json.toString().contains("[") || json.toString().contains("]")) {
            return null;
        }
        JSONObject jsonObj = new JSONObject(json.toString());
        LinkedHashMap<String, String> map = new Gson().fromJson(jsonObj.toString(), LinkedHashMap.class);
        return editMapByLength(map);
    }

    /**
     * Edit length of all strings.
     *
     * @param termMap map after parse
     * @return result Map
     */
    private LinkedHashMap<String, String> editMapByLength(LinkedHashMap<String, String> termMap) {
        LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
        for (String key : termMap.keySet()) {
            String temp = termMap.get(key);
            if (key.length() > 2000) {
                key = key.substring(0, 2000);
            }
            if (temp.length() > 5000) {
                temp = temp.substring(0, 5000);
            }
            resultMap.put(key.trim(), temp.trim());
        }
        return resultMap;
    }
}
