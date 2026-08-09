# Data Sources

> 最后整理日期：2026-08-08  
> 本文件只登记数据结构和同步规则，不保存数据库凭证、用户密码、验证码、令牌或图片原文。

## 数据流与事实来源

```text
服务器 PostgreSQL 数据库（原始事实来源）
    ↓ pg_dump 文本导出
服务器数据库2026-08-08-17-43.txt（受控离线输入）
    ↓ scripts/export_resources.py（字段白名单、脱敏、单向导出）
resources/*.md（AI 只读检索快照）
```

- 数据库是服务器数据的原始事实来源，Markdown 是给 AI 使用的只读快照。
- 只允许 `数据库 → Markdown` 单向同步；不得从 Markdown 反向覆盖数据库。
- 当前快照日期为 2026-08-08。后续导出应保留新快照时间，并在人工检查后替换旧快照。
- 导出文件本身包含 Secret 数据，不得直接交给模型、建立向量索引或上传到普通日志系统。

## 当前离线导出

- 文件：`服务器数据库2026-08-08-17-43.txt`
- 格式：PostgreSQL 18.4 `pg_dump` 文本，UTF-8，包含表结构与逐行 `INSERT`。
- 用途：作为本次资源快照的离线输入，以及后续同步脚本的结构样本。
- 存放要求：仅限 Owner 控制的受限目录；生成安全快照后可按备份策略归档或删除。
- 禁止：把整个 dump 放入 Prompt、向量库、全文索引、公开仓库或 Friend 可访问目录。

## 已接入资源表

| 数据类别 | 原始表 | 关键字段 | 生成文件 | 默认权限 | 当前记录数 | 解释限制 |
| --- | --- | --- | --- | --- | ---: | --- |
| 音乐 | `sys_music` | `id`, `name`, `author` | `resources/music.md` | Owner | 40 | 收录不等于听过、喜欢或当前常听 |
| 番剧 | `sys_anime` | `id`, `name`, `author`, `brief` | `resources/anime.md` | Owner | 27 | 收录不等于看过或推荐；图片字段排除 |
| 游戏 | `sys_game` | `id`, `name`, `author`, `brief` | `resources/games.md` | Owner | 41 | 收录不等于玩过、通关或喜欢 |
| 书籍 | `sys_book` | `id`, `book_name`, `author`, `brief` | `resources/books.md` | Owner | 26 | 收录不等于读过、读完或认同 |
| 学习 | `sys_study` 等 | 标题、正文、优缺点、作者、时间、分类 | `resources/learning.md` | Owner | 19 | 技术内容可能过时；当前做法需核对官方资料 |
| 个人认知 | `sys_cognize` | 标题、正文、作者、创建/更新时间 | `resources/cognition.md` | Owner | 7 | 是特定时间的记录，不必然是当前稳定立场 |
| 命题索引 | `sys_cognize` | ID、标题、时间 | `resources/propositions.md` | Owner | 7 | 只做索引，不由 AI 自动概括结论或补写例外 |
| 自有评论 | `sys_comment` | 内容、目标、时间、`user_id` | `resources/comments.md` | Owner | 16 | 仅导出 `user_id = 1`；可能含测试或占位文本 |
| 信条 | `sys_creed` | `id`, `content` | `resources/creed.md` | Owner | 2 | 保存过不等于现在仍认可 |
| 昵称 | `sys_nickname` | `id`, `name` | `resources/nicknames.md` | Owner | 46 | 不得据此关联现实身份或断言仍在使用 |
| 句子 | `sys_quote` | `id`, `content` | `resources/quotes.md` | Owner | 13 | 收录不等于原创或赞同；出处未验证 |

当前服务器表没有逐条分享级别，因此所有资源保守设为 Owner。后续若要降为 Friend 或 Public，应逐条记录 Owner 的确认，而不是因为数据类型看起来普通就整表降级。

当前导出配置把 `user_id = 1` 视为 Owner，这是根据学习与认知表中的 `author_id = 1`、`author_name = aitty` 确定的项目映射。账号迁移或重建数据库后必须重新核对，并通过 `--owner-user-id` 显式传入新 ID；不能仅凭最小数字猜测 Owner。

## 行为数据

原始行为表包括：

- `sys_view`：查看目标、用户和时间。
- `sys_like`：点赞目标、用户和时间。
- `sys_favorite`：收藏目标、用户和时间。

这些表当前没有直接生成长期事实文件，原因是事件数量少、时间字段不完整，且“查看/点赞/收藏”不能等同于长期偏好。

如后续接入，应先按 Owner、目标类型和统计窗口聚合，只生成类似“最近 30 天查看较多”的 Owner 摘要，并保留：

- 统计开始和结束时间；
- 事件类型与次数；
- 可解析的目标 ID；
- 来源事件表；
- `可信度：inferred`；
- “不代表长期最喜欢”的说明。

行为推断只有经 Owner 明确确认，才可以写入长期记忆或调整为 Friend 可见。

## 已明确排除的数据

以下表或字段不得进入资源 Markdown：

| 范围 | 原因 | 处理方式 |
| --- | --- | --- |
| `sms_record` | 含手机号和验证码，属于 Secret | 整表排除 |
| `sys_user` | 含密码哈希、令牌、手机号、头像和账号状态 | 整表排除；角色鉴权由后端单独处理 |
| `sys_user_token` | 含访问令牌、设备和过期时间，属于 Secret | 整表排除 |
| `sys_anime.cover_img`、`sys_game.cover_img`、`sys_book.cover` | 路径或 Base64 图片，不用于文本检索 | 字段排除 |
| `sys_comment.avatar`、评论者账号信息 | 图片及第三方身份信息 | 字段排除；非 Owner 评论整条排除 |
| `sys_friend`、`sys_friend_memory` | 含关系映射、第三方身份、照片和共同记忆 | 当前不接入；未来需逐条 Owner 审核 |
| `sys_emotion` | 可能含私人情绪及他人回复 | 当前不接入；若接入默认 Owner 并人工审核 |
| `sys_recommendation` | 含第三方提交内容和大体积图片 | 当前不接入；只能在审核后抽取已接受的纯文本条目 |
| 权限、公告等系统表 | 属于运行状态，不是个人知识 | 不进入人格知识库 |

## 同步命令

在项目根目录运行：

```powershell
python scripts\export_resources.py "服务器数据库2026-08-08-17-43.txt" --output resources --owner-user-id 1
```

解析器只允许读取代码中列出的表；新增表必须经过字段、权限和第三方隐私审查后加入白名单。

## 每次同步后的检查

1. 确认导出文件来自预期数据库和时间点。
2. 运行脚本，并核对每个文件的记录数是否异常增加、减少或归零。
3. 搜索资源目录中是否出现 `token`、`password`、手机号、验证码、`data:image` 等敏感模式。
4. 抽查多行正文、单引号、中文标点和时间字段是否完整。
5. 检查评论仍只包含 Owner 的 `user_id = 1` 记录。
6. 检查权限字段；新类别默认 Owner，明确授权后再降低。
7. 运行 `evaluations/` 中的权限与真实性测试。
8. 人工确认无误后再让检索服务加载新快照。
