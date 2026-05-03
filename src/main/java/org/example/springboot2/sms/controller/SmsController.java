package org.example.springboot2.sms.controller;

import org.example.springboot2.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    @Value("${aliyun.sms.template-param}")
    private String templateParam;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "手机号不能为空"));
        }
        try {
            String code = smsService.sendCode(phone, signName, templateCode, templateParam);
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 200);
            resp.put("message", "验证码已发送");
            // ⚠️ 开发阶段返回验证码，生产必须删除
            resp.put("testCode", code);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("code", 500, "message", "短信发送失败：" + e.getMessage()));
        }
    }
}