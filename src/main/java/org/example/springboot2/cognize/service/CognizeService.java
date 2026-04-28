package org.example.springboot2.cognize.service;

import org.example.springboot2.cognize.entity.Cognize;
import org.example.springboot2.cognize.repository.CognizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CognizeService {

    @Autowired
    private CognizeRepository cognizeRepository;

    public Map<String, Object> getCognizeList(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Cognize> page = cognizeRepository.findAll(pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("items", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }

    public Cognize getCognizeById(Long id) {
        return cognizeRepository.findById(id).orElse(null);
    }
}