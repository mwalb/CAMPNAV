package com.campnav.backend.controller;

import com.campnav.backend.model.Playlist;
import com.campnav.backend.repository.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@CrossOrigin(origins = "*")
public class PlaylistController {

    @Autowired
    private PlaylistRepository repository;

    @GetMapping
    public List<Playlist> getAllPlaylists() {
        return repository.findAll();
    }

    @PostMapping
    public Playlist addPlaylist(@RequestBody Playlist playlist) {
        return repository.save(playlist);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
