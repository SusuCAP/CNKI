package com.cnki.www.cnki_java.controller;

import com.cnki.www.cnki_java.common.ApiResponse;
import com.cnki.www.cnki_java.entity.Paper;
import com.cnki.www.cnki_java.repository.PaperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import com.cnki.www.cnki_java.dto.PaperResponseDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.cnki.www.cnki_java.entity.User;
import com.cnki.www.cnki_java.repository.UserRepository;
import com.cnki.www.cnki_java.entity.SearchHistory;
import com.cnki.www.cnki_java.repository.SearchHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cnki.www.cnki_java.service.KnowledgeGraphService;
import com.cnki.www.cnki_java.entity.ViewHistory;
import com.cnki.www.cnki_java.repository.ViewHistoryRepository;
import com.cnki.www.cnki_java.entity.Favorite;
import com.cnki.www.cnki_java.repository.FavoriteRepository;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.StandardCopyOption;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/papers")
public class PaperController {
    private static final Logger logger = LoggerFactory.getLogger(PaperController.class);

    @Autowired
    private PaperRepository paperRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @Autowired
    private ViewHistoryRepository viewHistoryRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Value("${server.base-url}")
    private String baseUrl;

    @GetMapping("/search")
    public ApiResponse<?> searchPapers(
        @RequestParam String keyword,
        @RequestParam String username
    ) {
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            List<Paper> papers = paperRepository.findByTitle(keyword);
            if (papers.isEmpty()) {
                papers = paperRepository.findByTitleContaining(keyword);
            }

            // 记录搜索历史
            try {
                SearchHistory history = new SearchHistory();
                history.setUser(user);
                history.setKeyword(keyword);
                searchHistoryRepository.save(history);
            } catch (Exception e) {
                logger.error("保存搜索记录失败", e);
            }

            // 转换为统一的DTO格式
            List<Map<String, Object>> dtos = papers.stream()
                .map(paper -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", paper.getId());
                    dto.put("paperId", paper.getPaperId());
                    dto.put("title", paper.getTitle());
                    dto.put("authors", paper.getAuthors());
                    dto.put("year", paper.getYear());
                    dto.put("pdfUrl", baseUrl + "/pages/" + paper.getPaperId() + ".pdf");
                    dto.put("isFavorited", favoriteRepository.existsByUserAndPaper(user, paper));
                    return dto;
                })
                .collect(Collectors.toList());

            return ApiResponse.success(dtos);
        } catch (Exception e) {
            logger.error("搜索失败", e);
            return ApiResponse.error(500, "搜索失败: " + e.getMessage());
        }
    }

    @GetMapping("/recommend")
    public ApiResponse<?> getRecommendations(
        @RequestParam String username,
        @RequestParam(defaultValue = "5") int limit
    ) {
        try {
            // 根据用户名查找用户
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            // 获取推荐
            List<Paper> recommendations = knowledgeGraphService.recommendPapers(user, limit);
            
            // 转换为统一的DTO格式
            List<Map<String, Object>> dtos = recommendations.stream()
                .map(paper -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", paper.getId());
                    dto.put("paperId", paper.getPaperId());
                    dto.put("title", paper.getTitle());
                    dto.put("authors", paper.getAuthors());
                    dto.put("year", paper.getYear());
                    dto.put("pdfUrl", baseUrl + "/pages/" + paper.getPaperId() + ".pdf");
                    dto.put("isFavorited", favoriteRepository.existsByUserAndPaper(user, paper));
                    return dto;
                })
                .collect(Collectors.toList());

            logger.info("为用户 {} 生成了 {} 条推荐", username, dtos.size());
            return ApiResponse.success(dtos);
            
        } catch (Exception e) {
            logger.error("推荐失败", e);
            return ApiResponse.error(500, "获取推荐失败: " + e.getMessage());
        }
    }

    @GetMapping("/checkFile/{paperId}")
    public ApiResponse<?> checkPdfFile(@PathVariable String paperId) {
        String pdfPath = "./pages/" + paperId + ".pdf";
        Path path = Paths.get(pdfPath).toAbsolutePath();
        
        logger.info("检查文件: {}", path);
        
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("requestedPath", pdfPath);
        fileInfo.put("absolutePath", path.toString());
        fileInfo.put("exists", Files.exists(path));
        
        if (Files.exists(path)) {
            try {
                long size = Files.size(path);
                boolean isRegularFile = Files.isRegularFile(path);
                boolean isReadable = Files.isReadable(path);
                
                logger.info("文件状态 - 大小: {} bytes, 是否为文件: {}, 是否可读: {}", 
                    size, isRegularFile, isReadable);
                
                fileInfo.put("size", size);
                fileInfo.put("isRegularFile", isRegularFile);
                fileInfo.put("isReadable", isReadable);
                
                try {
                    fileInfo.put("owner", Files.getOwner(path).getName());
                } catch (UnsupportedOperationException e) {
                    fileInfo.put("owner", "不支持获取所有者信息");
                }
                
                // 尝试读取文件的前几个字节
                byte[] header = Files.readAllBytes(path);
                if (header.length >= 4) {
                    String headerHex = String.format("%02X %02X %02X %02X", 
                        header[0], header[1], header[2], header[3]);
                    logger.info("文件头: {}", headerHex);
                    fileInfo.put("fileHeader", headerHex);
                }
                    
            } catch (IOException e) {
                logger.error("读取文件信息失败", e);
                fileInfo.put("error", e.getMessage());
            }
        } else {
            logger.warn("文件不存在: {}", path);
        }
        
        // 检查父目录
        Path parent = path.getParent();
        fileInfo.put("parentDir", parent.toString());
        fileInfo.put("parentExists", Files.exists(parent));
        fileInfo.put("parentIsDirectory", Files.isDirectory(parent));
        fileInfo.put("parentIsReadable", Files.isReadable(parent));
        
        try {
            List<String> contents = Files.list(parent)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
            logger.info("目录内容: {}", contents);
            fileInfo.put("parentContents", contents);
        } catch (IOException e) {
            logger.error("读取目录内容失败", e);
            fileInfo.put("parentError", e.getMessage());
        }
        
        return ApiResponse.success(fileInfo);
    }

    @GetMapping("/latest")
    public ApiResponse<?> getLatestPapers(@RequestParam String username) {
        try {
            // 查找用户
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            // 获取最新年份
            Integer maxYear = paperRepository.findMaxYear();
            if (maxYear == null) {
                logger.warn("数据库中没有论文");
                return ApiResponse.success(Collections.emptyList());
            }

            // 查找论文
            List<Paper> papers = null;
            int searchYear = maxYear;
            
            while (papers == null || papers.isEmpty()) {
                logger.info("查找 {} 年的论文", searchYear);
                papers = paperRepository.findByYear(searchYear);
                if (papers.isEmpty()) {
                    searchYear--;
                    if (searchYear < maxYear - 10) {
                        logger.warn("未找到最近10年的论文");
                        return ApiResponse.success(Collections.emptyList());
                    }
                }
            }

            // 转换为统一的DTO格式
            List<Map<String, Object>> dtos = papers.stream()
                .map(paper -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", paper.getId());
                    dto.put("paperId", paper.getPaperId());
                    dto.put("title", paper.getTitle());
                    dto.put("authors", paper.getAuthors());
                    dto.put("year", paper.getYear());
                    dto.put("pdfUrl", baseUrl + "/pages/" + paper.getPaperId() + ".pdf");
                    dto.put("isFavorited", favoriteRepository.existsByUserAndPaper(user, paper));
                    return dto;
                })
                .collect(Collectors.toList());

            return ApiResponse.success(dtos);
        } catch (Exception e) {
            logger.error("获取最新论文失败", e);
            return ApiResponse.error(500, "获取最新论文失败: " + e.getMessage());
        }
    }

    @PostMapping("/view")
    public ApiResponse<?> recordView(
        @RequestParam String username,
        @RequestParam String paperId
    ) {
        try {
            // 1. 查找用户
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            // 2. 查找论文
            Paper paper = paperRepository.findByPaperId(paperId)
                .orElseThrow(() -> new RuntimeException("论文不存在: " + paperId));
            
            // 3. 检查是否已经存在相同的浏览记录
            if (viewHistoryRepository.existsByUserAndPaper(user, paper)) {
                logger.info("用户 {} 已有论文 {} 的浏览记录，跳过", username, paper.getTitle());
                return ApiResponse.success(null);
            }
            
            // 4. 创建并保存浏览记录
            ViewHistory viewHistory = new ViewHistory();
            viewHistory.setUser(user);
            viewHistory.setPaper(paper);
            viewHistoryRepository.save(viewHistory);
            
            logger.info("记录用户 {} 浏览论文: {}", username, paper.getTitle());
            return ApiResponse.success(null);
            
        } catch (Exception e) {
            logger.error("记录浏览历史失败", e);
            return ApiResponse.error(500, "记录浏览历史失败: " + e.getMessage());
        }
    }

    @GetMapping("/view-history")
    public ApiResponse<?> getViewHistory(@RequestParam String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            List<ViewHistory> history = viewHistoryRepository.findByUserOrderByViewTimeDesc(user);
            
            // 转换为统一的DTO格式
            List<Map<String, Object>> dtos = history.stream()
                .map(vh -> {
                    Paper paper = vh.getPaper();
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", paper.getId());
                    dto.put("paperId", paper.getPaperId());
                    dto.put("title", paper.getTitle());
                    dto.put("authors", paper.getAuthors());
                    dto.put("year", paper.getYear());
                    dto.put("pdfUrl", baseUrl + "/pages/" + paper.getPaperId() + ".pdf");
                    dto.put("isFavorited", favoriteRepository.existsByUserAndPaper(user, paper));
                    dto.put("viewTime", vh.getViewTime());
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ApiResponse.success(dtos);
        } catch (Exception e) {
            logger.error("获取浏览历史失败", e);
            return ApiResponse.error(500, "获取浏览历史失败: " + e.getMessage());
        }
    }

    @PostMapping("/favorite")
    public ApiResponse<?> addFavorite(
        @RequestParam String username,
        @RequestParam String paperId
    ) {
        try {
            // 1. 查找用户
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            // 2. 查找论文
            Paper paper = paperRepository.findByPaperId(paperId)
                .orElseThrow(() -> new RuntimeException("论文不存在: " + paperId));
            
            // 3. 检查是否已经收藏
            if (favoriteRepository.existsByUserAndPaper(user, paper)) {
                return ApiResponse.error(400, "已经收藏过该论文");
            }
            
            // 4. 创建并保存收藏记录
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setPaper(paper);
            favoriteRepository.save(favorite);
            
            // 5. 返回更新后的论文信息
            Map<String, Object> response = new HashMap<>();
            response.put("paperId", paper.getPaperId());
            response.put("title", paper.getTitle());
            response.put("authors", paper.getAuthors());
            response.put("year", paper.getYear());
            response.put("pdfUrl", baseUrl + "/pages/" + paper.getPaperId() + ".pdf");
            response.put("isFavorited", true);
            
            logger.info("用户 {} 收藏论文: {}", username, paper.getTitle());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            logger.error("收藏论文失败", e);
            return ApiResponse.error(500, "收藏论文失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/favorite")
    public ApiResponse<?> removeFavorite(
        @RequestParam String username,
        @RequestParam String paperId
    ) {
        try {
            // 1. 查找用户
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            // 2. 查找论文
            Paper paper = paperRepository.findByPaperId(paperId)
                .orElseThrow(() -> new RuntimeException("论文不存在: " + paperId));
            
            // 3. 查找并删除收藏记录
            favoriteRepository.findByUserAndPaper(user, paper)
                .ifPresentOrElse(
                    favorite -> {
                        favoriteRepository.delete(favorite);
                        logger.info("用户 {} 取消收藏论文: {}", username, paper.getTitle());
                    },
                    () -> {
                        throw new RuntimeException("未找到收藏记录");
                    }
                );
            
            // 4. 返回更新后的论文信息
            Map<String, Object> response = new HashMap<>();
            response.put("paperId", paper.getPaperId());
            response.put("title", paper.getTitle());
            response.put("authors", paper.getAuthors());
            response.put("year", paper.getYear());
            response.put("pdfUrl", baseUrl + "/pages/" + paper.getPaperId() + ".pdf");
            response.put("isFavorited", false);
            
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            logger.error("取消收藏失败", e);
            return ApiResponse.error(500, "取消收藏失败: " + e.getMessage());
        }
    }

    @GetMapping("/favorites")
    public ApiResponse<?> getFavorites(@RequestParam String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            List<Favorite> favorites = favoriteRepository.findByUserOrderByFavoriteTimeDesc(user);
            
            // 转换为统一的DTO格式
            List<Map<String, Object>> dtos = favorites.stream()
                .map(f -> {
                    Paper paper = f.getPaper();
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", paper.getId());
                    dto.put("paperId", paper.getPaperId());
                    dto.put("title", paper.getTitle());
                    dto.put("authors", paper.getAuthors());
                    dto.put("year", paper.getYear());
                    dto.put("pdfUrl", baseUrl + "/pages/" + paper.getPaperId() + ".pdf");
                    dto.put("isFavorited", true);  // 收藏列表中的论文一定是已收藏的
                    dto.put("favoriteTime", f.getFavoriteTime());
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ApiResponse.success(dtos);
        } catch (Exception e) {
            logger.error("获取收藏列表失败", e);
            return ApiResponse.error(500, "获取收藏列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ApiResponse<?> uploadPaper(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("authors") String authors,
            @RequestParam("year") Integer year) {
        
        try {
            logger.debug("开始处理论文上传 - 标题: {}", title);
            
            // 验证文件
            if (file == null || file.isEmpty()) {
                return ApiResponse.error(400, "请选择文件");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (!"application/pdf".equals(contentType)) {
                logger.warn("不支持的文件类型: {}", contentType);
                return ApiResponse.error(400, "只支持PDF文件");
            }
            
            // 生成唯一的paper_id
            String paperId = UUID.randomUUID().toString();
            
            // 确保pages目录存在
            Path pagesDir = Paths.get("pages");
            if (!Files.exists(pagesDir)) {
                Files.createDirectories(pagesDir);
                logger.info("创建pages目录: {}", pagesDir.toAbsolutePath());
            }
            
            // 保存文件
            String fileName = paperId + ".pdf";
            Path filePath = pagesDir.resolve(fileName);
            
            logger.debug("保存文件到: {}", filePath.toAbsolutePath());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 保存到数据库
            Paper paper = new Paper();
            paper.setPaperId(paperId);
            paper.setTitle(title);
            paper.setAuthors(authors);
            paper.setYear(year);
            
            paperRepository.save(paper);
            logger.info("论文信息已保存到数据库 - ID: {}", paper.getId());
            
            // 触发知识图谱重建
            try {
                knowledgeGraphService.rebuildKnowledgeGraph();
                logger.info("已触发知识图谱重建");
            } catch (Exception e) {
                logger.error("知识图谱重建失败", e);
                // 不影响上传成功
            }
            
            // 构建返回数据
            Map<String, Object> response = new HashMap<>();
            response.put("paperId", paperId);
            response.put("title", title);
            response.put("authors", authors);
            response.put("year", year);
            response.put("pdfUrl", baseUrl + "/pages/" + fileName);
            
            return ApiResponse.success(response);
            
        } catch (IOException e) {
            logger.error("文件保存失败", e);
            return ApiResponse.error(500, "文件保存失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("论文上传失败", e);
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{paperId}")
    public ResponseEntity<?> downloadPaper(@PathVariable String paperId) {
        try {
            logger.info("接收到论文下载请求 - PaperId: {}", paperId);
            
            // 查找论文信息
            Paper paper = paperRepository.findByPaperId(paperId)
                .orElseThrow(() -> new RuntimeException("论文不存在"));
            
            logger.debug("找到论文 - 标题: {}", paper.getTitle());
            
            // 构建文件路径
            Path pdfPath = Paths.get("pages", paperId + ".pdf");
            if (!Files.exists(pdfPath)) {
                logger.error("PDF文件不存在: {}", pdfPath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            // 读取文件
            byte[] content = Files.readAllBytes(pdfPath);
            
            // 构建文件名（使用论文标题）
            String filename = paper.getTitle() + ".pdf";
            // 处理文件名中的特殊字符
            filename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                .replace("+", "%20");
            
            logger.info("开始下载论文 - 文件名: {}, 大小: {} bytes", filename, content.length);
            
            // 返回文件
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .body(content);
            
        } catch (Exception e) {
            logger.error("论文下载失败 - PaperId: {}, 错误: {}", paperId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("下载失败: " + e.getMessage());
        }
    }
} 