package org.example.springboot2.game.service;

import org.example.springboot2.game.entity.Game;
import org.example.springboot2.game.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game addGame(Game game) {
        return gameRepository.save(game);
    }

    public Game updateGame(Long id, Game game) {
        Game existing = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));
        existing.setName(game.getName());
        existing.setAuthor(game.getAuthor());
        existing.setBrief(game.getBrief());
        existing.setCoverImg(game.getCoverImg());
        return gameRepository.save(existing);
    }

    public void deleteGame(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new RuntimeException("游戏不存在");
        }
        gameRepository.deleteById(id);
    }
}