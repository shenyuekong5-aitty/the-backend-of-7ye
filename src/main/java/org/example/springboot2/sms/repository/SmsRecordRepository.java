package org.example.springboot2.sms.repository;

import org.example.springboot2.sms.entity.SmsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsRecordRepository extends JpaRepository<SmsRecord, Long> {

    // 查找最近一条未使用的验证码（用于校验）
    Optional<SmsRecord> findTopByPhoneAndUsedOrderByCreateTimeDesc(String phone, Boolean used);
}