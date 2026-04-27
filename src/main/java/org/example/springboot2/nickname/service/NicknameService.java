package org.example.springboot2.nickname.service;

import org.example.springboot2.nickname.entity.Nickname;
import org.example.springboot2.nickname.repository.NicknameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NicknameService {

    @Autowired
    private NicknameRepository nicknameRepository;

    // 获取昵称列表（支持关键词搜索）
    public List<String> getNicknames(String keyword) {
        List<Nickname> nicknames;
        if (keyword == null || keyword.trim().isEmpty()) {
            nicknames = nicknameRepository.findAll();
        } else {
            nicknames = nicknameRepository.findByNameContainingIgnoreCase(keyword);
        }
        return nicknames.stream()
                .map(Nickname::getName)
                .collect(Collectors.toList());
    }

    // 新增昵称
    @Transactional
    public boolean addNickname(String name) {
        if (nicknameRepository.existsByName(name)) {
            return false; // 已存在
        }
        nicknameRepository.save(new Nickname(name));
        return true;
    }

    // 修改昵称
    @Transactional
    public boolean updateNickname(String oldName, String newName) {
        if (!nicknameRepository.existsByName(oldName)) {
            return false; // 原昵称不存在
        }
        if (nicknameRepository.existsByName(newName)) {
            return false; // 新昵称已存在
        }
        nicknameRepository.deleteByName(oldName);
        nicknameRepository.save(new Nickname(newName));
        return true;
    }

    // 删除昵称
    @Transactional
    public boolean deleteNickname(String name) {
        if (!nicknameRepository.existsByName(name)) {
            return false;
        }
        nicknameRepository.deleteByName(name);
        return true;
    }
}