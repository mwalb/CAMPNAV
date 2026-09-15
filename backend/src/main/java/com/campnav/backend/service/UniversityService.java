package com.campnav.backend.service;

import com.campnav.backend.model.Campus;
import com.campnav.backend.model.CampusLocation;
import com.campnav.backend.model.Category;
import com.campnav.backend.model.University;
import com.campnav.backend.repository.CampusLocationRepository;
import com.campnav.backend.repository.CampusRepository;
import com.campnav.backend.repository.CategoryRepository;
import com.campnav.backend.repository.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UniversityService {

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private CampusLocationRepository locationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<University> getAllUniversities() {
        return universityRepository.findAllByIsActiveTrueOrderBySortOrderAsc();
    }

    public Optional<University> getUniversityById(Long id) {
        return universityRepository.findById(id);
    }

    public Optional<University> getUniversityByShortName(String shortName) {
        return universityRepository.findByShortName(shortName);
    }

    public Optional<University> getUniversityByExternalId(String externalId) {
        return universityRepository.findByExternalId(externalId);
    }

    public Optional<Campus> getCampusByExternalId(String externalId) {
        return campusRepository.findByExternalId(externalId);
    }

    public Campus saveCampus(Campus campus) {
        return campusRepository.save(campus);
    }

    public Optional<CampusLocation> getLocationByExternalId(String externalId) {
        return locationRepository.findByExternalId(externalId);
    }

    public List<CampusLocation> getLocationsByUniversity(Long universityId, Long categoryId, List<Long> categoryIds) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            return locationRepository.findByUniversityIdAndCategoryIdIn(universityId, categoryIds);
        } else if (categoryId != null) {
            return locationRepository.findByUniversityIdAndCategoryId(universityId, categoryId);
        }
        return locationRepository.findByUniversityId(universityId);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getCategoriesByUniversity(Long universityId) {
        return categoryRepository.findByUniversityId(universityId);
    }

    public Optional<Category> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    public List<CampusLocation> searchLocations(Long universityId, Long categoryId, List<Long> categoryIds, String query) {
        // Simple search across name, officialName, and aliases
        return locationRepository.searchCanonical(universityId, query);
    }
    
    public University saveUniversity(University university) {
        return universityRepository.save(university);
    }
    
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    public CampusLocation saveLocation(CampusLocation location) {
        return locationRepository.save(location);
    }
}
