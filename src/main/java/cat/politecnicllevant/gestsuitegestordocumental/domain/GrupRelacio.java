package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "pll_grup_relacio")
public @Data class GrupRelacio {

    @EmbeddedId
    private GrupRelacioId id;

    @MapsId("grupId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_grup", nullable = false)
    private Grup grup;

    @MapsId("grupRelacionatId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_grup_relacionat", nullable = false)
    private Grup grupRelacionat;
}
