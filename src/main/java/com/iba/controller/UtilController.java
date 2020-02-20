package com.iba.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/util")
@RestController
public class UtilController {

    @Value("${version}")
    private String version;

    /**
     * @return version of TheMCUTI project
     */
    @GetMapping("/version")
    public Map<String, String> getTheMCUTIVersionFromPom() {
        Map<String,String> map = new HashMap<>();
        map.put("version", version);
        return map;
    }
}
