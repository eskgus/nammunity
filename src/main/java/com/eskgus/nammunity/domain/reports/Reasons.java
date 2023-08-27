package com.eskgus.nammunity.domain.reports;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Reasons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String detail;

    @OneToOne(mappedBy = "reasons", cascade = CascadeType.REMOVE)
    private CommunityReports communityReports;
}