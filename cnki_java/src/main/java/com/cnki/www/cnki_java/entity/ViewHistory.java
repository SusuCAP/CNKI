package com.cnki.www.cnki_java.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "view_history")
public class ViewHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;
    
    @Column(nullable = false)
    private LocalDateTime viewTime;
    
    // 构造函数
    public ViewHistory() {
        this.viewTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Paper getPaper() { return paper; }
    public void setPaper(Paper paper) { this.paper = paper; }
    
    public LocalDateTime getViewTime() { return viewTime; }
    public void setViewTime(LocalDateTime viewTime) { this.viewTime = viewTime; }
} 