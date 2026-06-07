package com.medislot.app.config;

import com.medislot.app.entity.Specialization;
import com.medislot.app.repository.SpecializationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final SpecializationRepository specializationRepository;

    public DatabaseSeeder(SpecializationRepository specializationRepository) {
        this.specializationRepository = specializationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (specializationRepository.count() == 0) {
            System.out.println("No specializations found. Seeding default specializations...");
            
            Specialization s1 = new Specialization();
            s1.setName("Cardiology");
            s1.setDescription("Heart specialist consultation");
            s1.setActive(true);
            
            Specialization s2 = new Specialization();
            s2.setName("Dermatology");
            s2.setDescription("Skin and hair consultation");
            s2.setActive(true);
            
            Specialization s3 = new Specialization();
            s3.setName("Orthopedics");
            s3.setDescription("Bone and joint consultation");
            s3.setActive(true);
            
            Specialization s4 = new Specialization();
            s4.setName("General Physician");
            s4.setDescription("General health consultation");
            s4.setActive(true);
            
            Specialization s5 = new Specialization();
            s5.setName("Dentist");
            s5.setDescription("Dental consultation and care");
            s5.setActive(true);

            specializationRepository.saveAll(Arrays.asList(s1, s2, s3, s4, s5));
            System.out.println("Default specializations seeded successfully.");
        }
    }
}
