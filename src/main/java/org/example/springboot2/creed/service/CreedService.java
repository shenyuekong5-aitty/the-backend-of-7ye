package org.example.springboot2.creed.service;

import org.example.springboot2.creed.entity.Creed;
import org.example.springboot2.creed.repository.CreedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreedService {

    @Autowired
    private CreedRepository creedRepository;

    public List<Creed> getAllCreeds() {
        return creedRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Creed getById(Long id) {
        return creedRepository.findById(id).orElse(null);
    }

    @Transactional
    public Creed addCreed(String content) {
        Creed creed = new Creed();
        // 手动分配 ID：取当前最大 id + 1，若无数据则从 1 开始
        Long maxId = creedRepository.findAll().stream()
                .mapToLong(Creed::getId)
                .max()
                .orElse(0L);
        creed.setId(maxId + 1);
        creed.setContent(content);
        return creedRepository.save(creed);
    }

    @Transactional
    public Creed updateCreed(Long id, String newContent) {
        Creed creed = creedRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("信条不存在"));
        creed.setContent(newContent);
        return creedRepository.save(creed);
    }

    @Transactional
    public void deleteCreed(Long id) {
        if (!creedRepository.existsById(id)) {
            throw new RuntimeException("信条不存在");
        }
        creedRepository.deleteById(id);
    }
}