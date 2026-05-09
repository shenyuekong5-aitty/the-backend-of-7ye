package org.example.springboot2.tag.service;

import org.example.springboot2.tag.entity.Tag;
import org.example.springboot2.tag.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Tag getById(Long id) {
        return tagRepository.findById(id).orElse(null);
    }

    @Transactional
    public Tag addTag(String content) {
        Tag tag = new Tag();
        // 手动计算新ID（当前最大ID + 1，若表空则从1开始）
        Long maxId = tagRepository.findAll().stream()
                .mapToLong(Tag::getId)
                .max()
                .orElse(0L);
        tag.setId(maxId + 1);
        tag.setContent(content);
        return tagRepository.save(tag);
    }

    @Transactional
    public Tag updateTag(Long id, String newContent) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        tag.setContent(newContent);
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new RuntimeException("标签不存在");
        }
        tagRepository.deleteById(id);
    }
}