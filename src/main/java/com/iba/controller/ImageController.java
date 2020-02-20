package com.iba.controller;

import com.iba.utils.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RequestMapping("/image")
@RestController
public class ImageController {

    @Value("${file.path.image}")
    private String uploadPath;

    @Value("${file.path.default.image}")
    private String defaultAvatarPath;

    private final FileUtils fileUtils;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(ImageController.class);

    @Autowired
    public ImageController(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    /**
     * Set User profile photo in response.
     * If href equal null or empty set default image in response.
     *
     * @param response - response
     * @param href     - url of photo
     * @throws IOException if InputStream failed
     */
    @GetMapping
    public void getAvatar(HttpServletResponse response, @RequestParam String href) throws IOException {
        if (href == null || href.equals("")) {
            sendDefaultImage(response);
            return;
        }
        File file = new File(uploadPath + href);
        if (file.exists()) {
            InputStream inputStream = fileUtils.setFileToResponse(response, file);
            inputStream.close();
        } else {
            sendDefaultImage(response);
        }
    }

    /**
     * Set default image in response.
     *
     * @param response HttpServletResponse
     * @throws IOException if InputStream failed
     */
    private void sendDefaultImage(HttpServletResponse response) throws IOException {
        File defaultAvatar = new File(String.valueOf(getClass().getClassLoader().getResourceAsStream(defaultAvatarPath)));
        InputStream inputStream = fileUtils.setFileToResponse(response, defaultAvatar);
        inputStream.close();
    }

}
