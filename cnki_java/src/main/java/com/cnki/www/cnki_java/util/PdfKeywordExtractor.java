package com.cnki.www.cnki_java.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class PdfKeywordExtractor {
    
    // 停用词列表
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with"
    ));

    public static List<String> extractKeywords(String pdfPath) {
        try {
            // 读取PDF文件
            File file = new File(pdfPath);
            PDDocument document = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            // 分词并统计词频
            Map<String, Integer> wordFreq = Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(word -> word.length() > 2)  // 过滤掉太短的词
                .filter(word -> !STOP_WORDS.contains(word))  // 过滤停用词
                .collect(Collectors.groupingBy(
                    word -> word,
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

            // 按词频排序,取前20个词作为关键词
            return wordFreq.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
} 