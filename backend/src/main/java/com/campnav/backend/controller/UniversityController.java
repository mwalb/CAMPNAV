package com.campnav.backend.controller;

import com.campnav.backend.dto.CampusLocationDTO;
import com.campnav.backend.dto.CategoryDTO;
import com.campnav.backend.dto.UniversityDTO;
import com.campnav.backend.model.CampusLocation;
import com.campnav.backend.model.Category;
import com.campnav.backend.model.University;
import com.campnav.backend.service.UniversityService;
import com.campnav.backend.repository.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/universities")
@CrossOrigin(origins = "*")
public class UniversityController {

    @Autowired
    private UniversityService universityService;
    
    @Autowired
    private UniversityRepository universityRepository;

    @GetMapping("/debug/all-raw")
    public List<University> getAllRaw() {
        return universityRepository.findAll();
    }

    @GetMapping
    public List<UniversityDTO> getAllUniversities() {
        return universityService.getAllUniversities().stream()
                .map(this::mapToUniversityDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniversityDTO> getUniversityById(@PathVariable Long id) {
        return universityService.getUniversityById(id)
                .map(u -> ResponseEntity.ok(mapToUniversityDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/locations")
    public List<CampusLocationDTO> getLocations(
            @PathVariable Long id,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> categoryIds) {
        return universityService.getLocationsByUniversity(id, categoryId, categoryIds).stream()
                .map(this::mapToLocationDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/categories")
    public List<CategoryDTO> getCategories() {
        return universityService.getAllCategories().stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/search")
    public List<CampusLocationDTO> searchLocations(
            @PathVariable Long id,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam String query) {
        return universityService.searchLocations(id, categoryId, categoryIds, query).stream()
                .map(this::mapToLocationDTO)
                .collect(Collectors.toList());
    }

    private UniversityDTO mapToUniversityDTO(University university) {
        return UniversityDTO.builder()
                .id(university.getId())
                .name(university.getName())
                .shortName(university.getShortName())
                .logoUrl(university.getLogoUrl())
                .description(university.getDescription())
                .country(university.getCountry())
                .city(university.getCity())
                .latitude(university.getLatitude())
                .longitude(university.getLongitude())
                .defaultZoom(university.getDefaultZoom())
                .sortOrder(university.getSortOrder())
                .campusAreaHectares(university.getCampusAreaHectares())
                .isActive(university.getIsActive())
                .build();
    }

    private CategoryDTO mapToCategoryDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .iconName(category.getIconName())
                .sortOrder(category.getSortOrder())
                .build();
    }

    private CampusLocationDTO mapToLocationDTO(CampusLocation location) {
        return CampusLocationDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .description(location.getDescription())
                .universityId(location.getUniversity() != null ? location.getUniversity().getId() : null)
                .categoryId(location.getCategory() != null ? location.getCategory().getId() : null)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .buildingCode(location.getBuildingCode())
                .floor(location.getFloor())
                .roomNumber(location.getRoomNumber())
                .imageUrl(location.getImageUrl())
                .phone(location.getPhone())
                .email(location.getEmail())
                .openingHours(location.getOpeningHours())
                .isActive(location.getIsActive())
                .build();
    }
}
