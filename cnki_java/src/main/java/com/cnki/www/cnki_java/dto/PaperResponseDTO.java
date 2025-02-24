package com.cnki.www.cnki_java.dto;

import com.cnki.www.cnki_java.entity.Paper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PaperResponseDTO {
    private Long id;
    private String title;
    private String authors;
    private Integer year;
    private String originPdfUrl;  // 原始链接
    private String pdfUrl;   // 本地PDF文件访问地址
    private String paperId;

    public PaperResponseDTO(Paper paper, String baseUrl) {
        this.id = paper.getId();
        this.title = paper.getTitle();
        this.authors = paper.getAuthors();
        this.year = paper.getYear();
        this.originPdfUrl = paper.getPdfUrl();
        // 确保paperId后缀为.pdf
        String pdfFileName = paper.getPaperId();
        if (!pdfFileName.toLowerCase().endsWith(".pdf")) {
            pdfFileName += ".pdf";
        }
        this.pdfUrl = baseUrl + "/pages/" + pdfFileName;
        this.paperId = paper.getPaperId();
    }
    
    // getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthors() { return authors; }
    public Integer getYear() { return year; }
    public String getOriginPdfUrl() { return originPdfUrl; }
    public String getPdfUrl() { return pdfUrl; }
    public String getPaperId() { return paperId; }
} 