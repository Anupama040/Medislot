package com.medislot.app.repository;

import com.medislot.app.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Specialization> findByActiveTrueOrderByNameAsc();
    List<Specialization> findAllByOrderByNameAsc();
}
