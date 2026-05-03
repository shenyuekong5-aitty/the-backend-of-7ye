package org.example.springboot2.sms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_record")
public class SmsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 接收短信的手机号 */
    @Column(nullable = false, length = 20)
    private String phone;

    /** 生成的验证码 */
    @Column(nullable = false, length = 10)
    private String code;

    /** 创建时间（发送时间） */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** 是否已被使用（验证通过） */
    @Column(nullable = false)
    private Boolean used = false;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    // ========== Getter / Setter ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }
}