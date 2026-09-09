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
        Category religion = categories.get("religious-places");
        Category bank = categories.get("banks-finance");
        Category health = categories.get("health-medical");
        Category shop = categories.get("shopping-services");
        Category tech = categories.get("technology-ict");
        Category pub = categories.get("public-services");

        // Seed/Update UDSM
        Optional<University> udsmOpt = universityService.getAllUniversities().stream().filter(u -> u.getShortName().equals("UDSM")).findFirst();
        University udsm = udsmOpt.orElse(University.builder().shortName("UDSM").build());
        udsm.setName("University of Dar es Salaam");
        udsm.setCity("Dar es Salaam");
        udsm.setCountry("Tanzania");
        udsm.setLatitude(-6.7801);
        udsm.setLongitude(39.2041);
        udsm.setDefaultZoom(15f);
        udsm.setLogoUrl("https://upload.wikimedia.org/wikipedia/en/2/2a/University_of_Dar_es_Salaam_Logo.png");
        udsm.setDescription("Knowledge First");
        udsm = universityService.saveUniversity(udsm);

        // Seed/Update ARDHI
        Optional<University> aruOpt = universityService.getAllUniversities().stream().filter(u -> u.getShortName().equals("ARDHI")).findFirst();
        University aru = aruOpt.orElse(University.builder().shortName("ARDHI").build());
        aru.setName("Ardhi University");
        aru.setCity("Dar es Salaam");
        aru.setCountry("Tanzania");
        aru.setLatitude(-6.7720);
        aru.setLongitude(39.2070);
        aru.setDefaultZoom(15f);
        aru.setLogoUrl("https://upload.wikimedia.org/wikipedia/en/d/d4/Ardhi_University_logo.png");
        aru.setDescription("For Real Estate and Environmental Sciences");
        aru = universityService.saveUniversity(aru);

        // Locations
        if (universityService.getLocationsByUniversity(udsm.getId(), null, null).isEmpty()) {
            universityService.saveLocation(CampusLocation.builder().name("UDSM Main Library").latitude(-6.7810).longitude(39.2030).university(udsm).category(lib).build());
            universityService.saveLocation(CampusLocation.builder().name("CoICT Complex").latitude(-6.7750).longitude(39.2360).university(udsm).category(lect).build());
            universityService.saveLocation(CampusLocation.builder().name("Hall 1").latitude(-6.7820).longitude(39.2050).university(udsm).category(accom).build());
            universityService.saveLocation(CampusLocation.builder().name("Yombo Cafeteria").latitude(-6.7830).longitude(39.2020).university(udsm).category(food).build());
            universityService.saveLocation(CampusLocation.builder().name("UDSM Chapel").latitude(-6.7840).longitude(39.2060).university(udsm).category(religion).build());
            universityService.saveLocation(CampusLocation.builder().name("CRDB Bank").latitude(-6.7850).longitude(39.2070).university(udsm).category(bank).build());
            universityService.saveLocation(CampusLocation.builder().name("UDSM Health Centre").latitude(-6.7860).longitude(39.2080).university(udsm).category(health).build());
            universityService.saveLocation(CampusLocation.builder().name("UDSM Bookshop").latitude(-6.7870).longitude(39.2090).university(udsm).category(shop).build());
        }

        if (universityService.getLocationsByUniversity(aru.getId(), null, null).isEmpty()) {
            universityService.saveLocation(CampusLocation.builder().name("ARU Administration Block").latitude(-6.7725).longitude(39.2075).university(aru).category(admin).build());
            universityService.saveLocation(CampusLocation.builder().name("ARU Library").latitude(-6.7715).longitude(39.2065).university(aru).category(lib).build());
            universityService.saveLocation(CampusLocation.builder().name("Block A Hostel").latitude(-6.7735).longitude(39.2085).university(aru).category(accom).build());
            universityService.saveLocation(CampusLocation.builder().name("ARU Cafeteria").latitude(-6.7745).longitude(39.2095).university(aru).category(food).build());
            universityService.saveLocation(CampusLocation.builder().name("Computer Centre").latitude(-6.7755).longitude(39.2105).university(aru).category(tech).build());
            universityService.saveLocation(CampusLocation.builder().name("Post Office").latitude(-6.7765).longitude(39.2115).university(aru).category(pub).build());
        }
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
