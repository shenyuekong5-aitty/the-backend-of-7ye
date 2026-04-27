package org.example.springboot2.music.service;

import org.example.springboot2.music.entity.Music;
import org.example.springboot2.music.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MusicService {

    @Autowired
    private MusicRepository musicRepository;

    public List<Music> getAllMusics() {
        return musicRepository.findAll();
    }

    @Transactional
    public Music addMusic(Music music) {
        music.setId(null);
        return musicRepository.save(music);
    }

    @Transactional
    public Music updateMusic(Music music) {
        if (!musicRepository.existsById(music.getId())) {
            throw new RuntimeException("音乐记录不存在");
        }
        return musicRepository.save(music);
    }

    @Transactional
    public void deleteMusic(Long id) {
        musicRepository.deleteById(id);
    }
}