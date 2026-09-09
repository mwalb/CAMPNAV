package com.campnav.backend.controller;

import com.campnav.backend.model.MediaProject;
import com.campnav.backend.repository.MediaProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class MediaProjectController {

    @Autowired
    private MediaProjectRepository repository;

    @GetMapping
    public List<MediaProject> getAllProjects() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaProject> getProjectById(@PathVariable String id) {
        Optional<MediaProject> project = repository.findById(id);
        return project.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public MediaProject createProject(@RequestBody MediaProject project) {
        project.setLastModified(System.currentTimeMillis());
        return repository.save(project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MediaProject> updateProject(@PathVariable String id, @RequestBody MediaProject projectDetails) {
        return repository.findById(id).map(project -> {
            project.setName(projectDetails.getName());
            project.setEditHistory(projectDetails.getEditHistory());
            project.setPreviewUri(projectDetails.getPreviewUri());
            project.setLastModified(System.currentTimeMillis());
            return ResponseEntity.ok(repository.save(project));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        return repository.findById(id).map(project -> {
            repository.delete(project);
            return ResponseEntity.ok().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
