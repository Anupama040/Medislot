package com.medislot.app.config;

import com.medislot.app.entity.Role;
import com.medislot.app.entity.Specialization;
import com.medislot.app.entity.User;
import com.medislot.app.repository.SpecializationRepository;
import com.medislot.app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SpecializationRepository specializationRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            SpecializationRepository specializationRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.specializationRepository = specializationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            User admin = new User();
            admin.setName("MediSlot Admin");
            admin.setEmail("admin@medislot.com");
            admin.setPhone("9999999999");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
        }

        List<String> defaults = List.of("Cardiology", "Dermatology", "Orthopedics", "General Physician", "Dentist");
        for (String name : defaults) {
            if (!specializationRepository.existsByNameIgnoreCase(name)) {
                Specialization specialization = new Specialization();
                specialization.setName(name);
                specialization.setDescription(name + " consultation and treatment");
                specializationRepository.save(specialization);
            }
        }
    }
}
