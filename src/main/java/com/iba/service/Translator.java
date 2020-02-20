package com.iba.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {

    /**
     * Send request to google translate value of termLang
     *
     * @param langFrom lang translate from
     * @param langTo   lang translate to
     * @param text     value translate to
     * @return translated value
     * @throws IOException if parse failed
     */
    public String translate(String langFrom, String langTo, String text) throws IOException {
        String urlStr = "https://script.google.com/macros/s/AKfycbwNN5fLtyFv-y7fon9FBF8bb-OtZI-7LmS7eqpXzSSe8yrJmHMl/exec" +
                "?q=" + URLEncoder.encode(text, "UTF-8") +
                "&target=" + langTo +
                "&source=" + langFrom;
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return correctResult(response.toString());
    }

    /**
     * Correct response result
     *
     * @param result response in string
     * @return result translated value of TermLang
     */
    private String correctResult(String result) {
        Matcher matcher = Pattern.compile("<\\/.+>").matcher(result);
        while (matcher.find()) {
            result = result.replace(matcher.group(), matcher.group().replaceAll("\\s+", ""));
        }
        matcher = Pattern.compile("<[^\\/].+>").matcher(result);
        while (matcher.find()) {
            result = result.replace(matcher.group(), matcher.group().replaceAll("\\s{2,}", " "));
        }
        matcher = Pattern.compile("<[^\\/].+>.+\\s*<\\/.+>").matcher(result);
        while (matcher.find()) {
            String buffer = matcher.group();
            buffer = buffer.replaceAll(">\\s+", ">");
            buffer = buffer.replaceAll("\\s+<", "<");
            result = result.replace(matcher.group(), buffer);
        }
        return result;
    }
}
