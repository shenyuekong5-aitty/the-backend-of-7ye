package org.example.springboot2.tag.service;

import org.example.springboot2.tag.entity.Tag;
import org.example.springboot2.tag.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import org.example.springboot2.tag.dto.TagExcelDTO;

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


// 在类内部添加以下方法

    /**
     * 导出 Excel
     */
    public void exportTags(HttpServletResponse response) throws IOException {
        List<Tag> tags = getAllTags();
        List<TagExcelDTO> data = tags.stream().map(tag -> {
            TagExcelDTO dto = new TagExcelDTO();
            dto.setId(tag.getId());
            dto.setContent(tag.getContent());
            return dto;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("标签列表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), TagExcelDTO.class).sheet("标签").doWrite(data);
    }

    /**
     * 导入 Excel，逐行处理，返回处理结果
     */
    public Map<String, Object> importTags(MultipartFile file) throws IOException {
        List<TagExcelDTO> excelList = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), TagExcelDTO.class, new ReadListener<TagExcelDTO>() {
            @Override
            public void invoke(TagExcelDTO dto, AnalysisContext analysisContext) {
                excelList.add(dto);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
            }
        }).sheet().doRead();

        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failList = new ArrayList<>();

        for (TagExcelDTO dto : excelList) {
            try {
                if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                    throw new RuntimeException("内容不能为空");
                }
                if (dto.getId() != null && tagRepository.existsById(dto.getId())) {
                    // 更新
                    updateTag(dto.getId(), dto.getContent());
                } else {
                    // 新增
                    addTag(dto.getContent());
                }
                successList.add(Map.of("id", dto.getId(), "content", dto.getContent()));
            } catch (Exception e) {
                failList.add(Map.of("id", dto.getId(), "content", dto.getContent(), "reason", e.getMessage()));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", excelList.size());
        result.put("successCount", successList.size());
        result.put("failCount", failList.size());
        result.put("failDetails", failList);
        return result;
    }
}