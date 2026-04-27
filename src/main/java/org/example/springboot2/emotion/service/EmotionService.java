package org.example.springboot2.emotion.service;

import org.example.springboot2.emotion.entity.Emotion;
import org.example.springboot2.emotion.repository.EmotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmotionService {

    @Autowired
    private EmotionRepository emotionRepository;

    public List<Emotion> getAllEmotions() {
        return emotionRepository.findAll();
    }
}