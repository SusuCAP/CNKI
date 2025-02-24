# CNKI API 文档

## 基础信息
- 基础URL: `http://localhost:8080`
- 所有响应格式均为 JSON
- 默认响应格式：
```json
{
    "code": 200,      // 状态码
    "message": "Success", // 状态信息
    "data": {}        // 响应数据
}
```

## 用户相关接口

### 1. 用户注册
- 请求路径：`/register`
- 请求方法：`POST`
- Content-Type: `multipart/form-data`
- 请求参数：
  ```
  username: string    // 用户名
  password: string    // 密码
  nickname: string    // 昵称
  avatar: file       // 头像文件（可选）
  ```
- 响应示例：
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": {
      "id": 1,
      "username": "test_user",
      "nickname": "测试用户",
      "avatarUrl": "http://localhost:8080/avatars/xxx.jpg"
    }
  }
  ```

### 2. 用户登录
- 请求路径：`/login`
- 请求方法：`POST`
- Content-Type: `application/json`
- 请求参数：
  ```json
  {
    "username": "test_user",
    "password": "123456",
    "captcha": "1234"
  }
  ```
- 响应示例：同注册接口

### 3. 获取验证码
- 请求路径：`/captcha`
- 请求方法：`GET`
- 响应：返回验证码图片

### 4. 更新用户信息
- 请求路径：`/api/user/update`
- 请求方法：`PUT`
- Content-Type: `multipart/form-data`
- 请求参数：
  ```
  username: string        // 当前用户名
  newUsername: string     // 新用户名（可选）
  password: string       // 新密码（可选）
  nickname: string       // 新昵称（可选）
  avatar: file          // 新头像文件（可选）
  ```

### 5. 获取用户信息
- 请求路径：`/api/user/info`
- 请求方法：`GET`
- 请求参数：
  ```
  username: string    // 用户名
  ```
- 响应示例：
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": {
      "username": "test_user",
      "nickname": "测试用户",
      "avatar": "http://localhost:8080/avatars/xxx.jpg"
    }
  }
  ```

### 6. 获取搜索历史
- 请求路径：`/search-history`
- 请求方法：`GET`
- 请求参数：需要用户认证信息
- 响应示例：
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": [
      {
        "keyword": "搜索关键词",
        "searchTime": "2024-02-24T10:00:00"
      }
    ]
  }
  ```

## 论文相关接口

### 1. 论文搜索
- 请求路径：`/api/papers/search`
- 请求方法：`GET`
- 请求参数：
  ```
  keyword: string    // 搜索关键词
  username: string   // 用户名
  ```

### 2. 获取推荐论文
- 请求路径：`/api/papers/recommend`
- 请求方法：`GET`
- 请求参数：
  ```
  username: string   // 用户名
  ```

### 3. 获取最新论文
- 请求路径：`/api/papers/latest`
- 请求方法：`GET`
- 请求参数：无

### 4. 上传论文
- 请求路径：`/api/papers/upload`
- 请求方法：`POST`
- Content-Type: `multipart/form-data`
- 请求参数：
  ```
  file: file        // PDF文件
  title: string     // 论文标题
  authors: string   // 作者
  year: number      // 发表年份
  ```

### 5. 下载论文
- 请求路径：`/api/papers/download/{paperId}`
- 请求方法：`GET`
- 路径参数：
  ```
  paperId: string   // 论文ID
  ```
- 响应：返回PDF文件流，文件名为论文标题

### 6. 查看论文
- 请求路径：`/api/papers/view`
- 请求方法：`POST`
- Content-Type: `application/json`
- 请求参数：
  ```json
  {
    "username": "test_user",
    "paperId": "paper_id"
  }
  ```

### 7. 查看论文历史
- 请求路径：`/api/papers/view-history`
- 请求方法：`GET`
- 请求参数：
  ```
  username: string   // 用户名
  ```

### 8. 收藏/取消收藏论文
- 请求路径：`/api/papers/favorite`
- 请求方法：`POST`
- Content-Type: `application/json`
- 请求参数：
  ```json
  {
    "username": "test_user",
    "paperId": "paper_id",
    "action": "add"  // "add" 或 "remove"
  }
  ```

### 9. 获取收藏列表
- 请求路径：`/api/papers/favorites`
- 请求方法：`GET`
- 请求参数：
  ```
  username: string   // 用户名
  ```

## 静态资源访问

### 1. 访问论文PDF
- 请求路径：`/pages/{paperId}.pdf`
- 请求方法：`GET`
- 响应：返回PDF文件

### 2. 访问用户头像
- 请求路径：`/avatars/{filename}`
- 请求方法：`GET`
- 响应：返回图片文件

## 错误码说明
- 200: 成功
- 400: 请求参数错误
- 401: 未授权
- 403: 禁止访问
- 404: 资源不存在
- 500: 服务器内部错误

## 注意事项
1. 文件上传大小限制为10MB
2. 论文文件仅支持PDF格式
3. 用户头像支持常见图片格式（jpg, png, gif等）
4. 所有API都支持CORS
5. 建议在请求时添加适当的错误处理
6. 部分接口需要用户认证
7. 文件访问URL使用baseUrl配置的地址

## 环境要求
- Java 17+
- MySQL 8.0+
- Maven 3.6+
- 至少10GB磁盘空间（用于存储PDF和头像）

## 知识图谱构建

### 概述
系统通过分析论文PDF内容，提取关键词和关系，构建知识图谱，用于论文推荐和相关性分析。

### 构建过程

1. **文本提取**
   - 使用 PDFBox 从PDF文件中提取文本内容
   - 处理文本格式，移除特殊字符和无关内容
   - 分段落存储，保留文档结构信息

2. **关键词提取**
   - 使用 TF-IDF 算法识别重要词汇
   - 结合专业领域词典进行关键词筛选
   - 保存每篇论文的关键词及其权重

3. **关系构建**
   - 基于关键词共现分析建立论文间关系
   - 计算论文相似度矩阵
   - 构建论文关系网络

4. **知识图谱更新**
   - 新论文上传时自动触发更新
   - 增量式更新，只处理新增内容
   - 定期优化和重建完整图谱

### 相似度计算
```java
similarity = intersection(keywords1, keywords2) / union(keywords1, keywords2)
```

- intersection: 两篇论文共有的关键词数量
- union: 两篇论文的关键词并集数量

### 推荐算法

1. **基于内容的推荐**
   - 分析用户历史浏览和收藏的论文
   - 提取用户兴趣关键词
   - 匹配相似度高的论文

2. **协同过滤**
   - 分析用户行为模式
   - 找到相似用户群组
   - 推荐群组内热门论文

3. **混合推荐**
   - 结合内容和协同过滤的结果
   - 动态调整推荐权重
   - 考虑时效性和热度因素

### 性能优化

1. **缓存机制**
   - 缓存常用的相似度计算结果
   - 使用 Redis 存储热门论文的关系
   - 定期更新缓存数据

2. **并行处理**
   - 多线程处理大规模文本提取
   - 并行计算相似度矩阵
   - 分布式存储知识图谱数据

3. **增量更新**
   - 仅处理新增和修改的论文
   - 维护关键词索引
   - 优化图谱更新效率

### 应用场景

1. **论文推荐**
   - 基于用户兴趣推荐相关论文
   - 发现潜在研究方向
   - 追踪研究热点

2. **相关性分析**
   - 分析论文间的引用关系
   - 发现研究分支和演化
   - 构建知识体系

3. **趋势分析**
   - 识别研究热点变化
   - 预测潜在研究方向
   - 辅助研究决策

### 技术栈
- PDFBox: PDF文本提取
- Lucene: 文本分析和索引
- Redis: 缓存和临时存储
- MySQL: 持久化存储
- Spring Boot: 后端框架
- ThreadPoolExecutor: 并行处理