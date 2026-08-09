package org.example.springboot2.ai.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 表示从 ai-profile/resources/*.md 中解析出的单条文档
 */
public class Document {

    /** 文档唯一标识，如 music_1 */
    private String id;
    /** 资源类型：music / anime / game / book / learning / proposition / cognition / comment / creed / nickname / quote */
    private String type;
    /** 条目标题 */
    private String title;
    /** 权限级别：Public / Friend / Owner */
    private String permission;
    /** 来源文件名 */
    private String sourceFile;
    /** 原始正文（标题 + 所有字段的完整 Markdown 片段） */
    private String content;
    /** 所有键值对字段 */
    private Map<String, String> fields = new HashMap<>();

    public Document() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    /**
     * 判断当前角色是否有权查看此文档
     */
    public boolean isAccessibleBy(String role) {
        if ("Owner".equals(role)) return true;
        if ("Friend".equals(role)) {
            return "Public".equals(permission) || "Friend".equals(permission);
        }
        // Public / 未登录
        return "Public".equals(permission);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", permission='" + permission + '\'' +
                '}';
    }
}
