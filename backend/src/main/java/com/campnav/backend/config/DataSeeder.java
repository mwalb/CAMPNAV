package com.campnav.backend.config;

import com.campnav.backend.model.Campus;
import com.campnav.backend.model.CommunityUser;
import com.campnav.backend.model.University;
import com.campnav.backend.repository.CommunityUserRepository;
import com.campnav.backend.service.StudentLeaderService;
import com.campnav.backend.service.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UniversityService universityService;

    @Autowired
    private CommunityUserRepository userRepository;

    @Autowired
    private StudentLeaderService studentLeaderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Universities (Idempotent)
        University udsm = seedUniversity("UDSM", "University of Dar es Salaam", "University of Dar es Salaam", "UDSM", "Dar es Salaam", -6.7801, 39.2041, 15f, 
                "Hekima ni Uhuru", "https://upload.wikimedia.org/wikipedia/en/2/2a/University_of_Dar_es_Salaam_Logo.png", 1, 657.0);

        seedCampus("UDSM-MLIMANI", "Mwalimu Julius K. Nyerere Mlimani Campus", udsm);

        University udom = seedUniversity("UDOM", "University of Dodoma", "University of Dodoma", "UDOM", "Dodoma", -6.2033, 35.8000, 14f,
                "Embracing Knowledge", "https://upload.wikimedia.org/wikipedia/en/1/1b/UDOM_Logo.png", 2, 6000.0);
        seedCampus("UDOM-MAIN", "University of Dodoma Main Campus", udom);

        University must = seedUniversity("MUST", "Mbeya University of Science and Technology", "Mbeya University of Science and Technology", "MUST", "Mbeya", -8.94315, 33.41636, 15f,
                "Science and Technology for Development", "https://upload.wikimedia.org/wikipedia/en/a/a2/Mbeya_University_of_Science_and_Technology_Logo.png", 3, null);
        seedCampus("MUST-MAIN", "Mbeya University Main Campus", must);

        University sua = seedUniversity("SUA", "Sokoine University of Agriculture", "Sokoine University of Agriculture", "SUA", "Morogoro", -6.8475, 37.6591, 15f, 
                "Ardhi ni Hazina", "https://upload.wikimedia.org/wikipedia/en/3/3d/Sua_logo.png", 4, null);
        seedCampus("SUA-MAIN", "Sokoine University of Agriculture Main Campus", sua);

        // 2. Seed Initial Admin Account
        seedInitialAdmin();

        // 3. Seed Initial University Student Leaders
        seedLeader("udsm_leader", "udsm_leader@campnav.org", "UDSM Student Government Leader", udsm);
        seedLeader("udom_leader", "udom_leader@campnav.org", "UDOM Student Ministry Leader", udom);
        seedLeader("must_leader", "must_leader@campnav.org", "MUST Student Guild Leader", must);
        seedLeader("sua_leader", "sua_leader@campnav.org", "SUA Student Organization Leader", sua);
    }

    private void seedInitialAdmin() {
        String adminEmail = "raphaelfrank720@gmail.com";
        String adminUsername = "mwalb66";
        String adminPassword = System.getenv("ADMIN_PASSWORD") != null ? System.getenv("ADMIN_PASSWORD") : "CAMPNAV1111()";

        CommunityUser adminUser = userRepository.findByUsername(adminUsername)
                .orElseGet(() -> userRepository.findByEmail(adminEmail)
                        .orElse(null));

        if (adminUser == null) {
            adminUser = CommunityUser.builder()
                    .externalId("ADMIN-MWALB66")
                    .username(adminUsername)
                    .email(adminEmail)
                    .fullName("Mwalb Administrator")
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role(CommunityUser.UserRole.ADMIN)
                    .isActive(true)
                    .reputationScore(100)
                    .build();
            userRepository.save(adminUser);
            System.out.println("Initialized Admin account for " + adminEmail);
        } else {
            adminUser.setUsername(adminUsername);
            adminUser.setEmail(adminEmail);
            adminUser.setRole(CommunityUser.UserRole.ADMIN);
            adminUser.setIsActive(true);
            adminUser.setPasswordHash(passwordEncoder.encode(adminPassword));
            userRepository.save(adminUser);
            System.out.println("Updated Admin account credentials.");
        }
    }

    private void seedLeader(String username, String email, String fullName, University university) {
        if (university == null) return;
        CommunityUser user = userRepository.findByUsername(username).orElseGet(() -> {
            CommunityUser newLeader = CommunityUser.builder()
                    .externalId("LEADER-" + username.toUpperCase())
                    .username(username)
                    .email(email)
                    .fullName(fullName)
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(CommunityUser.UserRole.STUDENT_LEADER)
                    .isActive(true)
                    .reputationScore(50)
                    .build();
            return userRepository.save(newLeader);
        });

        if (studentLeaderService.getActiveLeaderForUniversity(university.getId()).isEmpty()) {
            studentLeaderService.appointStudentLeader(university.getId(), user.getUsername(), "admin");
            System.out.println("Assigned Student Leader " + username + " to " + university.getName());
        }
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
