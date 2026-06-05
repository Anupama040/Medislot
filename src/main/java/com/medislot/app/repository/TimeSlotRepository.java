package com.medislot.app.repository;

import com.medislot.app.entity.SlotStatus;
import com.medislot.app.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    boolean existsByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate slotDate, LocalTime startTime);
    List<TimeSlot> findByDoctorIdOrderBySlotDateAscStartTimeAsc(Long doctorId);
    List<TimeSlot> findByDoctorIdAndStatusAndSlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(
            Long doctorId,
            SlotStatus status,
            LocalDate today
    );
    long countByStatus(SlotStatus status);
}
