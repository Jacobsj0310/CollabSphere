package com.CollabSphere.CollabSphere.UI;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FileUIController {

    @GetMapping("/files")
    public String fileList() {
        return "files"; // files.html
    }

    @GetMapping("/files/upload")
    public String uploadFilePage() {
        return "file-upload"; // file-upload.html
    }
}

