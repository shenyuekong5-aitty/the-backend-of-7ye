package org.example.springboot2.sms.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dypnsapi.model.v20170525.SendSmsVerifyCodeRequest;
import com.aliyuncs.dypnsapi.model.v20170525.SendSmsVerifyCodeResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsService {

    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    //phone-->code
    private final Map<String, String> activeCodes = new ConcurrentHashMap<>();


    //sendCode 方法的核心就是：向阿里云短信服务发送一个HTTP请求，并最终得到一个包含业务状态码（Code）和请求ID（RequestId）的响应对象。
    public String sendCode(String phone, String signName, String templateCode, String templateParam) throws Exception {
        String code = String.format("%06d", new Random().nextInt(1000000));
        activeCodes.put(phone, code);

        // 1. 创建一个认证配置对象，告诉阿里云：我的身份凭证和区域
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        // 2. 手动指定号码认证服务的域名端点（确保域名是 dypnsapi.aliyuncs.com）
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Dypnsapi", "dypnsapi.aliyuncs.com");
        // 3. 用配置好的profile构建一个客户端对象
        IAcsClient client = new DefaultAcsClient(profile);

        // 创建一个短信发送请求对象，并把必要的信息填进去，然后交给阿里云去执行。
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest();
        request.setPhoneNumber(phone);
        request.setSignName(signName);
        request.setTemplateCode(templateCode);
        String finalParam = templateParam.replace("{code}", code);
        request.setTemplateParam(finalParam);

        //用之前配置好的 client（已包含你的身份凭证和端点），把填好各项信息的 request 发送到阿里云服务器。
        SendSmsVerifyCodeResponse response = client.getAcsResponse(request);
        //阿里云服务器处理完毕后返回一个响应对象
        String resCode = response.getCode();
        System.out.println("短信发送结果 -> Code: " + resCode + ", Message: " + response.getMessage());

        if (!"OK".equals(resCode)) {
            throw new RuntimeException("短信发送失败：" + response.getMessage());
        }
        return code;
    }

    //验证码校验
    public boolean verifyCode(String phone, String code) {
        // 1. 从内存里，根据手机号，拿出之前存的验证码
        String stored = activeCodes.get(phone);
        // 2. 判断拿到的验证码是不是和用户刚输入的一样
        if (stored != null && stored.equals(code)) {
            // 3. 一样就是正确的，立马把这条验证码删掉，防止被重复利用
            activeCodes.remove(phone);
            // 4. 告诉调用方，验证通过
            return true;
        }
        // 5. 否则返回错误
        return false;
    }
}