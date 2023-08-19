package com.eskgus.nammunity.domain.reports;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypesRepository extends JpaRepository<Types, Long> {
    Optional<Types> findByDetail(String detail);
}
