package com.campnav.backend.config;

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
        Map<String, Category> categories = seedCategories();

        Category lib = categories.get("library-study");
        Category lect = categories.get("academic-learning");
        Category admin = categories.get("administration");
        Category accom = categories.get("accommodation");
        Category food = categories.get("food-cafeteria");

        // 1. University of Dar es Salaam (UDSM)
        seedUniversity("University of Dar es Salaam", "UDSM", "Dar es Salaam", -6.7801, 39.2041, 15f, 
                "Knowledge First", "https://upload.wikimedia.org/wikipedia/en/2/2a/University_of_Dar_es_Salaam_Logo.png", 1, true, 657.0);

        // 2. University of Dodoma (UDOM)
        seedUniversity("University of Dodoma", "UDOM", "Dodoma", -6.2033, 35.8000, 14f, 
                "Embracing Knowledge", "https://upload.wikimedia.org/wikipedia/en/1/1b/UDOM_Logo.png", 2, true, 6000.0);

        // 3. Mbeya University of Science and Technology (MUST)
        seedUniversity("Mbeya University of Science and Technology", "MUST", "Mbeya", -8.9328, 33.3980, 15f, 
                "Science and Technology for Development", "https://upload.wikimedia.org/wikipedia/en/a/a2/Mbeya_University_of_Science_and_Technology_Logo.png", 3, true, null);

        // 4. Sokoine University of Agriculture (SUA)
        seedUniversity("Sokoine University of Agriculture", "SUA", "Morogoro", -6.8475, 37.6591, 15f, 
                "Ardhi ni Hazina", "https://upload.wikimedia.org/wikipedia/en/3/3d/Sua_logo.png", 4, true, null);

        // 5. Mzumbe University (MU)
        seedUniversity("Mzumbe University", "MU", "Morogoro", -6.8167, 37.6667, 15f, 
                "Muscente Discimus", "https://upload.wikimedia.org/wikipedia/en/e/e0/Mzumbe_University_logo.png", 5, true, 4926.75);

        // Deactivate Ardhi University if it exists
        deactivateUniversity("ARDHI");

        // Seed some sample locations for the new universities if empty
        seedLocations("UDSM", lib, lect);
        seedLocations("UDOM", lib, lect);
        seedLocations("MUST", lib, lect);
        seedLocations("SUA", lib, lect);
        seedLocations("MU", lib, lect);
    }

    private void seedUniversity(String name, String shortName, String city, Double lat, Double lng, Float zoom, 
                                String desc, String logo, Integer sortOrder, Boolean active, Double area) {
        Optional<University> opt = universityService.getUniversityByShortName(shortName);
        
        University u = opt.orElse(University.builder().shortName(shortName).build());
        u.setName(name);
        u.setCity(city);
        u.setCountry("Tanzania");
        u.setLatitude(lat);
        u.setLongitude(lng);
        u.setDefaultZoom(zoom);
        u.setDescription(desc);
        u.setLogoUrl(logo);
        u.setSortOrder(sortOrder);
        u.setIsActive(active);
        u.setCampusAreaHectares(area);
        universityService.saveUniversity(u);
    }

    private void deactivateUniversity(String shortName) {
        universityService.getUniversityByShortName(shortName)
                .ifPresent(u -> {
                    u.setIsActive(false);
                    universityService.saveUniversity(u);
                });
    }

    private void seedLocations(String shortName, Category lib, Category lect) {
        universityService.getUniversityByShortName(shortName).ifPresent(u -> {
            if (universityService.getLocationsByUniversity(u.getId(), null, null).isEmpty()) {
                universityService.saveLocation(CampusLocation.builder().name(u.getShortName() + " Main Library").latitude(u.getLatitude() - 0.001).longitude(u.getLongitude() - 0.001).university(u).category(lib).isActive(true).build());
                universityService.saveLocation(CampusLocation.builder().name(u.getShortName() + " Lecture Theatre").latitude(u.getLatitude() + 0.001).longitude(u.getLongitude() + 0.001).university(u).category(lect).isActive(true).build());
            }
        });
    }

    private Map<String, Category> seedCategories() {
        String[][] catData = {
            {"Academic & Learning", "academic-learning", "school"},
            {"Accommodation", "accommodation", "hotel"},
            {"Food & Cafeteria", "food-cafeteria", "restaurant"},
            {"Religious Places", "religious-places", "account_balance"},
            {"Banks & Finance", "banks-finance", "payments"},
            {"Library & Study", "library-study", "menu_book"},
            {"Health & Medical", "health-medical", "medical_services"},
            {"Shopping & Services", "shopping-services", "shopping_cart"},
            {"Administration", "administration", "business"},
            {"Transport", "transport", "directions_bus"},
            {"Sports & Recreation", "sports-recreation", "sports_soccer"},
            {"Security & Emergency", "security-emergency", "security"},
            {"Technology & ICT", "technology-ict", "computer"},
            {"Student Services", "student-services", "group"},
            {"Public Services", "public-services", "public"},
            {"Landmarks & Places", "landmarks-places", "place"}
        };

        Map<String, Category> categoryMap = new HashMap<>();
        for (String[] data : catData) {
            final String slug = data[1];
            Optional<Category> existing = universityService.getAllCategories().stream()
                    .filter(c -> c.getSlug() != null && c.getSlug().equals(slug))
                    .findFirst();
            
            Category category = existing.orElse(Category.builder().slug(slug).build());
            category.setName(data[0]);
            category.setIconName(data[2]);
            category = universityService.saveCategory(category);
            categoryMap.put(slug, category);
        }
        return categoryMap;
    }
}
