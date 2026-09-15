package com.campnav.backend.config;

import com.campnav.backend.model.Campus;
import com.campnav.backend.model.CampusLocation;
import com.campnav.backend.model.Category;
import com.campnav.backend.model.University;
import com.campnav.backend.service.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UniversityService universityService;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Universities (Idempotent)
        University udsm = seedUniversity("UDSM", "University of Dar es Salaam", "University of Dar es Salaam", "UDSM", "Dar es Salaam", -6.7801, 39.2041, 15f, 
                "Knowledge First", "https://upload.wikimedia.org/wikipedia/en/2/2a/University_of_Dar_es_Salaam_Logo.png", 1, 657.0);

        // 2. Seed Campus
        seedCampus("UDSM-MLIMANI", "Mwalimu Julius K. Nyerere Mlimani Campus", udsm);

        // NOTE: UDSM Categories and Locations are handled by Flyway V8 migration 
        // to ensure the exact verified coordinates and names are preserved.
        
        // Placeholder Universities
        seedUniversity("UDOM", "University of Dodoma", "University of Dodoma", "UDOM", "Dodoma", -6.2033, 35.8000, 14f, 
                "Embracing Knowledge", "https://upload.wikimedia.org/wikipedia/en/1/1b/UDOM_Logo.png", 2, 6000.0);
        seedUniversity("MUST", "Mbeya University of Science and Technology", "Mbeya University of Science and Technology", "MUST", "Mbeya", -8.9328, 33.3980, 15f, 
                "Science and Technology for Development", "https://upload.wikimedia.org/wikipedia/en/a/a2/Mbeya_University_of_Science_and_Technology_Logo.png", 3, null);
        seedUniversity("SUA", "Sokoine University of Agriculture", "Sokoine University of Agriculture", "SUA", "Morogoro", -6.8475, 37.6591, 15f, 
                "Ardhi ni Hazina", "https://upload.wikimedia.org/wikipedia/en/3/3d/Sua_logo.png", 4, null);
        seedUniversity("MU", "Mzumbe University", "Mzumbe University", "MU", "Morogoro", -6.8167, 37.6667, 15f, 
                "Muscente Discimus", "https://upload.wikimedia.org/wikipedia/en/e/e0/Mzumbe_University_logo.png", 5, 4926.75);
    }

    private University seedUniversity(String externalId, String name, String officialName, String shortName, String city, Double lat, Double lng, Float zoom, 
                                String desc, String logo, Integer sortOrder, Double area) {
        Optional<University> opt = universityService.getUniversityByExternalId(externalId);
        University u = opt.orElse(University.builder().externalId(externalId).shortName(shortName).build());
        u.setName(name);
        u.setOfficialName(officialName);
        u.setShortName(shortName);
        u.setCity(city);
        u.setCountry("Tanzania");
        u.setLatitude(lat);
        u.setLongitude(lng);
        u.setDefaultZoom(zoom);
        u.setDescription(desc);
        u.setLogoUrl(logo);
        u.setSortOrder(sortOrder);
        u.setIsActive(true);
        u.setCampusAreaHectares(area);
        u.setStatus("ACTIVE");
        return universityService.saveUniversity(u);
    }

    private Campus seedCampus(String externalId, String name, University university) {
        Optional<Campus> opt = universityService.getCampusByExternalId(externalId);
        Campus c = opt.orElse(Campus.builder().externalId(externalId).build());
        c.setName(name);
        c.setUniversity(university);
        return universityService.saveCampus(c);
    }
}
