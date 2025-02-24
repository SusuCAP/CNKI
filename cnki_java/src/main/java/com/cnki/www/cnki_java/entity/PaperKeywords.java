package com.cnki.www.cnki_java.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "paper_keywords")
public class PaperKeywords {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;
    
    @Column(columnDefinition = "TEXT")
    private String keywords;  // 存储为JSON字符串
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Paper getPaper() { return paper; }
    public void setPaper(Paper paper) { this.paper = paper; }
    
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
} 