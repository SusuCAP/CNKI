package com.cnki.www.cnki_java.service;

import com.cnki.www.cnki_java.entity.Paper;
import com.cnki.www.cnki_java.entity.SearchHistory;
import com.cnki.www.cnki_java.entity.User;
import com.cnki.www.cnki_java.entity.PaperKeywords;
import com.cnki.www.cnki_java.repository.PaperRepository;
import com.cnki.www.cnki_java.repository.SearchHistoryRepository;
import com.cnki.www.cnki_java.repository.PaperKeywordsRepository;
import com.cnki.www.cnki_java.util.PdfKeywordExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;
import java.io.File;

@Service
public class KnowledgeGraphService {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeGraphService.class);

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private PaperKeywordsRepository paperKeywordsRepository;

    @Autowired
    private ObjectMapper objectMapper;  // Jackson的ObjectMapper

    // 存储每篇论文的关键词
    private Map<String, List<String>> paperKeywords = new HashMap<>();
    
    // 存储论文之间的相似度
    private Map<String, Map<String, Double>> paperSimilarity = new HashMap<>();

    @PostConstruct
    public void initializeGraph() {
        logger.info("开始加载知识图谱...");
        
        // 从数据库加载已有的关键词
        List<Paper> papers = paperRepository.findAll();
        for (Paper paper : papers) {
            paperKeywordsRepository.findByPaper(paper).ifPresent(pk -> {
                try {
                    List<String> keywords = objectMapper.readValue(
                        pk.getKeywords(), 
                        new TypeReference<List<String>>() {}
                    );
                    paperKeywords.put(paper.getPaperId(), keywords);
                } catch (Exception e) {
                    logger.error("解析关键词失败: {}", e.getMessage());
                }
            });
        }
        
        // 检查是否需要提取新论文的关键词
        boolean needsUpdate = papers.stream()
            .anyMatch(p -> !paperKeywordsRepository.existsByPaper(p));
            
        if (needsUpdate) {
            logger.info("发现新论文，开始更新知识图谱...");
            updateKnowledgeGraph();
        } else {
            logger.info("知识图谱已是最新");
        }
    }
    
    private void updateKnowledgeGraph() {
        List<Paper> papers = paperRepository.findAll();
        
        for (Paper paper : papers) {
            // 如果已经有关键词，跳过
            if (paperKeywordsRepository.existsByPaper(paper)) {
                continue;
            }
            
            String pdfPath = "./pages/" + paper.getPaperId() + ".pdf";
            File pdfFile = new File(pdfPath);
            
            if (!pdfFile.exists() || pdfFile.length() == 0) {
                logger.error("PDF文件不存在或为空: {}", pdfPath);
                continue;
            }

            try {
                // 提取关键词
                List<String> keywords = PdfKeywordExtractor.extractKeywords(pdfPath);
                paperKeywords.put(paper.getPaperId(), keywords);
                
                // 保存到数据库
                PaperKeywords pk = new PaperKeywords();
                pk.setPaper(paper);
                pk.setKeywords(objectMapper.writeValueAsString(keywords));
                paperKeywordsRepository.save(pk);
                
                logger.info("更新论文关键词: {}", paper.getTitle());
            } catch (Exception e) {
                logger.error("处理论文失败: {}", e.getMessage());
            }
        }
        
        logger.info("知识图谱更新完成");
    }

    public List<Paper> recommendPapers(User user, int limit) {
        // 1. 获取用户的搜索历史
        List<SearchHistory> searchHistories = searchHistoryRepository.findByUserOrderBySearchTimeDesc(user);
        if (searchHistories.isEmpty()) {
            logger.info("用户 {} 没有搜索历史，返回热门论文", user.getUsername());
            return paperRepository.findAll().stream()
                .limit(limit)
                .collect(Collectors.toList());
        }

        // 2. 提取搜索关键词
        List<String> userKeywords = searchHistories.stream()
            .map(SearchHistory::getKeyword)
            .collect(Collectors.toList());
        logger.info("用户 {} 的搜索关键词: {}", user.getUsername(), userKeywords);

        // 3. 计算每篇论文与用户兴趣的相似度
        Map<Paper, Double> paperScores = new HashMap<>();
        List<Paper> allPapers = paperRepository.findAll();
        
        for (Paper paper : allPapers) {
            List<String> paperKeywordList = paperKeywords.get(paper.getPaperId());
            if (paperKeywordList == null) {
                logger.warn("论文 {} 没有关键词", paper.getPaperId());
                continue;
            }

            // 计算论文关键词与用户搜索关键词的相似度
            double similarity = calculateSimilarity(paperKeywordList, userKeywords);
            paperScores.put(paper, similarity);
            
            logger.debug("论文 [{}] 与用户 {} 的相似度: {}", 
                paper.getTitle(), user.getUsername(), similarity);
        }

        // 4. 按相似度排序并返回前N篇论文
        return paperScores.entrySet().stream()
            .sorted(Map.Entry.<Paper, Double>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private double calculateSimilarity(List<String> keywords1, List<String> keywords2) {
        // 将关键词转换为小写以进行不区分大小写的比较
        Set<String> set1 = keywords1.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        Set<String> set2 = keywords2.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        double similarity = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
        
        logger.debug("计算相似度 - 关键词1: {}, 关键词2: {}, 相似度: {}", 
            keywords1, keywords2, similarity);
            
        return similarity;
    }

    public void rebuildKnowledgeGraph() {
        // Implementation of rebuildKnowledgeGraph method
    }
} 