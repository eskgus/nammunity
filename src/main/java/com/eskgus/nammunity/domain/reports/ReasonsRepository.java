package com.eskgus.nammunity.domain.reports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReasonsRepository extends JpaRepository<Reasons, Long> {
    @Query("SELECT r FROM Reasons r ORDER BY r.id ASC")
    List<Reasons> findAllAsc();
}
