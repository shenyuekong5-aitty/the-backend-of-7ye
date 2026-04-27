package org.example.springboot2.quote.controller;

import org.example.springboot2.quote.entity.Quote;
import org.example.springboot2.quote.service.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quote")
public class QuoteController {

    @Autowired
    private QuoteService quoteService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getQuoteList() {
        List<Quote> quotes = quoteService.getAllQuotes();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("items", quotes);
        data.put("message", "获取名言列表成功");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}