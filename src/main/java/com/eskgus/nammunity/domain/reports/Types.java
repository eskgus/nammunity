package com.eskgus.nammunity.domain.reports;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Types {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String detail;

    @OneToMany(mappedBy = "types", cascade = CascadeType.REMOVE)
    private List<ContentReports> contentReports;
}
