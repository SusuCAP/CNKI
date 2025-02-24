package com.cnki.www.cnki_java.dto;

import org.springframework.web.multipart.MultipartFile;

public class PaperUploadDTO {
    private String title;
    private String authors;
    private Integer year;
    private MultipartFile file;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
} 