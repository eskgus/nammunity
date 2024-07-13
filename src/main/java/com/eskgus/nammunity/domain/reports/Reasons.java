package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.common.Visitor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Reasons implements Element {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String detail;

    @OneToMany(mappedBy = "reasons", cascade = CascadeType.REMOVE)
    private List<ContentReports> contentReports;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
