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
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UniversityController {

    @Autowired
    private UniversityService universityService;
    
    @Autowired
    private UniversityRepository universityRepository;

    @GetMapping("/universities/debug/all-raw")
    public List<University> getAllRaw() {
        return universityRepository.findAll();
    }

    @GetMapping("/universities")
    public List<UniversityDTO> getAllUniversities() {
        return universityService.getAllUniversities().stream()
                .map(this::mapToUniversityDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/universities/{id}")
    public ResponseEntity<UniversityDTO> getUniversityById(@PathVariable Long id) {
        return universityService.getUniversityById(id)
                .map(u -> ResponseEntity.ok(mapToUniversityDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/universities/{id}/locations")
    public List<CampusLocationDTO> getLocations(
            @PathVariable Long id,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) String category) {
        
        Long finalCategoryId = categoryId;
        if (category != null && !category.isEmpty()) {
            finalCategoryId = universityService.getCategoryBySlug(category.toLowerCase())
                    .map(Category::getId)
                    .orElse(null);
        }
        
        return universityService.getLocationsByUniversity(id, finalCategoryId, categoryIds).stream()
                .map(this::mapToLocationDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/universities/{id}/categories")
    public List<CategoryDTO> getCategoriesForUniversity(@PathVariable Long id) {
        return universityService.getCategoriesByUniversity(id).stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/categories")
    public List<CategoryDTO> getCategories() {
        return universityService.getAllCategories().stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/categories/{categoryId}/locations")
    public List<CampusLocationDTO> getLocationsByCategory(@PathVariable Long categoryId) {
        // This is a bit tricky without a universityId, but we'll return all locations for this category
        // In a real scenario, this might need more context
        return universityService.getAllUniversities().stream()
                .flatMap(u -> universityService.getLocationsByUniversity(u.getId(), categoryId, null).stream())
                .map(this::mapToLocationDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/universities/{id}/search")
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
                .externalId(university.getExternalId())
                .name(university.getName())
                .officialName(university.getOfficialName())
                .shortName(university.getShortName())
                .logoUrl(university.getLogoUrl())
                .description(university.getDescription())
                .country(university.getCountry())
                .city(university.getCity())
                .status(university.getStatus())
                .createdAt(university.getCreatedAt())
                .updatedAt(university.getUpdatedAt())
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
                .externalId(location.getExternalId())
                .name(location.getName())
                .officialName(location.getOfficialName())
                .aliases(location.getAliases())
                .description(location.getDescription())
                .universityId(location.getUniversity() != null ? location.getUniversity().getId() : null)
                .campusId(location.getCampus() != null ? location.getCampus().getId() : null)
                .categoryId(location.getCategory() != null ? location.getCategory().getId() : null)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .entranceLatitude(location.getEntranceLatitude())
                .entranceLongitude(location.getEntranceLongitude())
                .coordinateType(location.getCoordinateType())
                .confidence(location.getConfidence())
                .status(location.getStatus())
                .buildingCode(location.getBuildingCode())
                .floor(location.getFloor())
                .roomNumber(location.getRoomNumber())
                
                .googlePlaceId(location.getGooglePlaceId())
                .googleName(location.getGoogleName())
                .googleAddress(location.getGoogleAddress())
                .googleType(location.getGoogleType())
                .verificationStatus(location.getVerificationStatus())
                .source(location.getSource())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                
                .imageUrl(location.getImageUrl())
                .phone(location.getPhone())
                .email(location.getEmail())
                .openingHours(location.getOpeningHours())
                .isActive(location.getIsActive())
                .sourcePrimary(location.getSourcePrimary())
                .sourceSecondary(location.getSourceSecondary())
                .sourceMap(location.getSourceMap())
                .sourceNotes(location.getSourceNotes())
                .verifiedDate(location.getVerifiedDate())
                .build();
    }
}
