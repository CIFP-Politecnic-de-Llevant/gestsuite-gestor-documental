package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupRelacioId implements Serializable {

    @Column(name = "id_grup")
    private Long grupId;

    @Column(name = "id_grup_relacionat")
    private Long grupRelacionatId;
}
