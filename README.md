# AI Interview Coach

**基于 Spring Boot + RAG 的 Java 面试辅导系统**

AI Interview Coach 是一个面向 Java 学习和面试准备的私有知识库系统。用户可以上传自己的 Java 学习资料，系统会完成文档解析、文本切片、Embedding 向量化和 pgvector 存储，并基于资料提供智能问答、面试题生成、答案评分、聊天记录和 SSE 流式输出。

## 技术栈

- Java 19
- Spring Boot 3
- Maven
- MyBatis-Plus
- PostgreSQL
- pgvector
- DashScope `text-embedding-v4`
- DeepSeek API
- Spring MVC SSE
- Docker / Docker Compose

## 系统功能

- 知识库创建、查询、修改和删除
- `.txt`、`.md` 文档上传与管理
- 文档内容解析
- 固定长度文本切片与重叠窗口
- DashScope Embedding 向量化
- PostgreSQL pgvector 向量存储与相似度检索
- 基于私有知识库的 RAG 问答
- AI 面试题生成
- AI 答案评分和改进建议
- 问答会话及聊天消息持久化
- DeepSeek SSE 流式回答
- 统一返回结果、参数校验和全局异常处理

## RAG 核心流程

```text
用户上传资料
      ↓
文档解析
      ↓
文本切片
      ↓
Embedding 向量化
      ↓
pgvector 存储
      ↓
用户提问
      ↓
问题向量化
      ↓
向量相似度检索
      ↓
Prompt 拼接
      ↓
DeepSeek 生成回答
      ↓
返回答案和引用片段
```

## 数据库表

| 表名 | 作用 |
| --- | --- |
| `knowledge_base` | 保存知识库名称、描述和创建时间 |
| `document` | 保存上传文档的元数据、文件路径和处理状态 |
| `document_chunk` | 保存文档解析后的文本切片 |
| `document_chunk_embedding` | 保存切片内容、1024 维向量和 Embedding 模型 |
| `chat_session` | 保存知识库下的聊天会话 |
| `chat_message` | 保存会话中的用户和助手消息 |

文档状态包括：

- `UPLOADED`：文件已上传
- `PARSED`：文件已解析并完成切片
- `INDEXED`：切片已完成向量化
- `FAILED`：处理失败

## 核心接口

所有普通接口默认返回统一结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 健康检查

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| GET | `/api/health` | 检查服务是否正常运行 |

### 知识库管理

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| POST | `/api/kb` | 创建知识库 |
| GET | `/api/kb/list` | 查询知识库列表 |
| GET | `/api/kb/{id}` | 查询知识库详情 |
| PUT | `/api/kb/{id}` | 修改知识库 |
| DELETE | `/api/kb/{id}` | 删除知识库 |

### 文档管理

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| POST | `/api/kb/{kbId}/documents/upload` | 上传 `.txt` 或 `.md` 文件 |
| GET | `/api/kb/{kbId}/documents` | 查询知识库文档列表 |
| GET | `/api/documents/{documentId}` | 查询文档详情 |
| DELETE | `/api/documents/{documentId}` | 删除文档及本地文件 |

### 文档解析与切片

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| POST | `/api/documents/{documentId}/parse` | 解析文档并生成文本切片 |
| GET | `/api/documents/{documentId}/chunks` | 查询文档切片 |

### Embedding 向量化

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| POST | `/api/documents/{documentId}/index` | 生成切片向量并写入 pgvector |
| GET | `/api/documents/{documentId}/embeddings` | 查询向量记录，不返回完整向量 |

### 向量检索

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| POST | `/api/kb/{kbId}/search` | 在指定知识库中检索相似文本切片 |

### RAG 问答

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| POST | `/api/kb/{kbId}/chat` | 非流式 RAG 问答，返回答案和引用片段 |
| POST | `/api/kb/{kbId}/chat/stream` | SSE 流式 RAG 问答 |

### AI 面试辅导

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| POST | `/api/kb/{kbId}/interview/questions/generate` | 根据知识库和主题生成面试题 |
| POST | `/api/kb/{kbId}/interview/answers/evaluate` | 评价用户答案并给出分数和改进建议 |

### 聊天记录

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| GET | `/api/kb/{kbId}/chat/sessions` | 查询知识库下的聊天会话 |
| GET | `/api/chat/sessions/{sessionId}/messages` | 查询会话消息 |
| DELETE | `/api/chat/sessions/{sessionId}` | 删除会话及其全部消息 |

## 本地启动

### 1. 环境要求

- JDK 19
- Maven 3.9+
- Docker Desktop
- 可用的 DashScope API Key
- 可用的 DeepSeek API Key

### 2. 启动 PostgreSQL + pgvector

在项目根目录执行：

```powershell
docker compose up -d postgres
docker ps
```

验证 pgvector：

```powershell
docker exec -it ai-interview-coach-postgres psql `
  -U postgres `
  -d ai_interview_coach `
  -c "SELECT extname FROM pg_extension WHERE extname = 'vector';"
```

### 3. 执行数据库脚本

按编号依次执行：

```powershell
Get-ChildItem .\sql\*.sql |
  Sort-Object Name |
  ForEach-Object {
    Get-Content $_.FullName |
      docker exec -i ai-interview-coach-postgres `
        psql -U postgres -d ai_interview_coach
  }
```

脚本包括：

```text
001_create_knowledge_base.sql
002_create_document.sql
003_create_document_chunk.sql
004_create_document_chunk_embedding.sql
005_create_chat_tables.sql
```

### 4. 配置 API Key

在启动 Spring Boot 的同一个 PowerShell 终端设置：

```powershell
$env:DASHSCOPE_API_KEY="your-dashscope-api-key"
$env:DEEPSEEK_API_KEY="your-deepseek-api-key"
```

API Key 不应写入代码、`application.yml` 或 Git 仓库。

### 5. 启动 Spring Boot

```powershell
mvn spring-boot:run
```

验证服务：

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

## 完整演示流程

下面示例均在项目根目录的 PowerShell 中执行。

### 1. 创建知识库

```powershell
$kbBody = @{
  name = "Java 面试知识库"
  description = "用于准备 Java 后端面试"
} | ConvertTo-Json -Compress

$kbResponse = Invoke-RestMethod `
  -Uri "http://localhost:8080/api/kb" `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body $kbBody

$kbId = $kbResponse.data
$kbId
```

### 2. 创建并上传 `sample.txt`

```powershell
@"
InputStream 是 Java 字节输入流的抽象父类。
FileInputStream 用于从文件中读取字节。
BufferedInputStream 通过缓冲区减少底层 IO 调用次数，提高读取效率。
Reader 是字符输入流的抽象父类，适合处理文本字符。
"@ | Set-Content .\sample.txt -Encoding UTF8

$uploadResponse = curl.exe -s -X POST `
  "http://localhost:8080/api/kb/$kbId/documents/upload" `
  -F "file=@sample.txt" | ConvertFrom-Json

$documentId = $uploadResponse.data.id
$documentId
```

上传文件保存在项目根目录的 `uploads/` 中，该目录已被 Git 忽略。

### 3. 解析文档并生成切片

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/documents/$documentId/parse" `
  -Method Post
```

查询切片：

```powershell
Invoke-RestMethod "http://localhost:8080/api/documents/$documentId/chunks"
```

### 4. 生成 Embedding

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/documents/$documentId/index" `
  -Method Post
```

处理成功后，文档状态变为 `INDEXED`。

### 5. 向量检索

```powershell
$searchBody = @{
  query = "InputStream 和 Reader 有什么区别？"
  topK = 5
} | ConvertTo-Json -Compress

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/kb/$kbId/search" `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body $searchBody
```

### 6. RAG 问答

```powershell
$chatBody = @{
  question = "InputStream 和 Reader 有什么区别？"
  topK = 5
} | ConvertTo-Json -Compress

$chatResponse = Invoke-RestMethod `
  -Uri "http://localhost:8080/api/kb/$kbId/chat" `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body $chatBody

$chatResponse.data
```

响应包含：

- `sessionId`
- `answer`
- `references`

继续已有会话时，在请求体中传入返回的 `sessionId`。当前版本只持久化聊天历史，不会把历史消息拼接到大模型上下文中。

### 7. 生成面试题

```powershell
$questionBody = @{
  topic = "Java IO"
  difficulty = "medium"
  count = 5
  topK = 5
} | ConvertTo-Json -Compress

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/kb/$kbId/interview/questions/generate" `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body $questionBody
```

`difficulty` 支持 `easy`、`medium`、`hard`。

### 8. 评价答案

```powershell
$evaluationBody = @{
  question = "请解释 InputStream、FileInputStream 和 BufferedInputStream 三者之间的关系。"
  userAnswer = "InputStream 是输入流，FileInputStream 可以读文件，BufferedInputStream 有缓冲区。"
  referenceAnswer = "InputStream 是字节输入流的抽象父类；FileInputStream 用于从文件读取字节；BufferedInputStream 通过缓冲减少底层 IO 次数。"
  topK = 5
} | ConvertTo-Json -Compress

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/kb/$kbId/interview/answers/evaluate" `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body $evaluationBody
```

响应包含：

- `score`
- `evaluationText`
- `references`

### 9. SSE 流式问答

先创建 UTF-8 请求文件：

```powershell
@{
  question = "InputStream 是什么？"
  topK = 5
} |
  ConvertTo-Json -Compress |
  Set-Content .\stream-request.json -Encoding UTF8
```

使用 `curl.exe -N` 关闭输出缓冲：

```powershell
curl.exe -N -X POST `
  "http://localhost:8080/api/kb/$kbId/chat/stream" `
  -H "Content-Type: application/json; charset=utf-8" `
  --data-binary "@stream-request.json"
```

演示完成后删除临时请求文件：

```powershell
Remove-Item .\stream-request.json
```

事件顺序：

```text
event: session
data: {"sessionId":1}

event: references
data: [...]

event: message
data: 分段生成的回答

event: done
data: [DONE]
```

## 项目亮点

1. **完整 RAG 链路**  
   覆盖文档上传、解析、切片、Embedding、向量存储、检索、Prompt 构造和大模型回答。

2. **PostgreSQL + pgvector**  
   使用 cosine distance 完成指定知识库范围内的向量相似度检索，业务数据和向量数据统一存储。

3. **私有知识库问答**  
   回答严格基于用户上传的 Java 学习资料，适合课程笔记、面试资料和团队内部文档。

4. **答案可追溯**  
   RAG 问答、面试题生成和答案评分均返回引用片段及相似度分数。

5. **面试辅导闭环**  
   系统不仅回答问题，还可以按主题和难度生成面试题，并对用户答案进行评分和改写。

6. **SSE 流式输出**  
   DeepSeek 生成的 token 会通过 Spring MVC `SseEmitter` 实时返回，降低长回答的等待感。

7. **聊天记录持久化**  
   会话和消息保存到 PostgreSQL，支持会话列表、消息查询和会话删除。

8. **工程化基础能力**  
   包含统一响应、参数校验、全局异常处理、UTF-8 编码、Docker 数据库环境和分阶段 SQL 脚本。

## 我在面试中如何介绍这个项目

> 我做了一个基于 Spring Boot 和 RAG 的 Java 面试辅导系统。用户可以上传自己的 Java 学习资料，后端会先把文档解析并按固定窗口切片，再调用 DashScope `text-embedding-v4` 生成 1024 维向量，保存到 PostgreSQL 的 pgvector 字段中。用户提问时，系统先把问题向量化，通过 cosine distance 在指定知识库里检索最相关的文本片段，然后把检索结果和问题拼接成 Prompt，调用 DeepSeek 生成回答，同时把引用片段和相似度返回给前端。基于同一套检索能力，我还实现了面试题生成和答案评分。工程上增加了聊天记录持久化，并使用 Spring MVC `SseEmitter` 对接 DeepSeek Streaming API，实现回答的实时输出。这个项目让我完整实践了从非结构化文档处理、向量检索到大模型应用落地的整条 RAG 链路。

进一步讲解时，可以重点展开：

- 为什么选择固定长度切片和 overlap
- pgvector 的 `<=>` cosine distance 检索方式
- 如何避免重复索引和维护文档状态
- 为什么回答结果需要携带引用片段
- 非流式与 SSE 流式接口的实现差异
- API Key 环境变量管理和 UTF-8 编码处理

## 注意事项

- 不要将真实 API Key 提交到 Git。
- `target/`、`uploads/`、IDE 配置和 `sample.txt` 已通过 `.gitignore` 排除。
- `text-embedding-v4` 的向量维度固定为 1024，数据库字段维度必须保持一致。
- 删除 Docker volume 后重新启动，`docker/init.sql` 会再次启用 pgvector 扩展。
- DeepSeek 和 DashScope 调用需要可访问对应 API 的网络环境。
