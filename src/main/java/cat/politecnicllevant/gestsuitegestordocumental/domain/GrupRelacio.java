package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(
        name = "pll_grup_relacio",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_grup", "id_grup_relacionat"})
)
public @Data class GrupRelacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_grup", nullable = false)
    private Grup grup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_grup_relacionat", nullable = false)
    private Grup grupRelacionat;
}
