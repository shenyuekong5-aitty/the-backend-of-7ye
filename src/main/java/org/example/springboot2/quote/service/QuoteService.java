package org.example.springboot2.quote.service;

import org.example.springboot2.quote.entity.Quote;
import org.example.springboot2.quote.repository.QuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuoteService {

    @Autowired
    private QuoteRepository quoteRepository;

    /**
     * 获取所有语录（按 id 倒序）
     */
    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /**
     * 根据 id 获取单条语录
     */
    public Quote getById(Long id) {
        return quoteRepository.findById(id).orElse(null);
    }

    /**
     * 新增语录
     */
    @Transactional
    public Quote addQuote(String content) {
        Quote quote = new Quote();
        quote.setContent(content);
        return quoteRepository.save(quote);
    }

    /**
     * 修改语录
     */
    @Transactional
    public Quote updateQuote(Long id, String newContent) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("语录不存在"));
        quote.setContent(newContent);
        return quoteRepository.save(quote);
    }

    /**
     * 删除语录
     */
    @Transactional
    public void deleteQuote(Long id) {
        if (!quoteRepository.existsById(id)) {
            throw new RuntimeException("语录不存在");
        }
        quoteRepository.deleteById(id);
    }
}