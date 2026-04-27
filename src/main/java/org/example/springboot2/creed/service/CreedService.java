package org.example.springboot2.creed.service;

import org.example.springboot2.creed.entity.Creed;
import org.example.springboot2.creed.repository.CreedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreedService {

    @Autowired
    private CreedRepository creedRepository;

    public List<Creed> getAllCreeds() {
        return creedRepository.findAll();
    }
}