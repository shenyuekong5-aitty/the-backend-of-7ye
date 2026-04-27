package org.example.springboot2.anime.service;

import org.example.springboot2.anime.entity.Anime;
import org.example.springboot2.anime.repository.AnimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnimeService {

    @Autowired
    private AnimeRepository animeRepository;

    public List<Anime> getAllAnimes() {
        return animeRepository.findAll();
    }

    @Transactional
    public Anime addAnime(Anime anime) {
        anime.setId(null);
        return animeRepository.save(anime);
    }

    @Transactional
    public Anime updateAnime(Anime anime) {
        if (!animeRepository.existsById(anime.getId())) {
            throw new RuntimeException("番剧不存在");
        }
        return animeRepository.save(anime);
    }

    @Transactional
    public void deleteAnime(Long id) {
        animeRepository.deleteById(id);
    }
}