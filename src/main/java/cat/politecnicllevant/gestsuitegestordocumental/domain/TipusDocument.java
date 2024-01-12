package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "pll_tipus_document")
public class TipusDocument {
    @Id
    @Column(name = "idtipusdocument")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipusDocument;

    @Column(name = "nom", unique = true, nullable = false, length = 2048)
    private String nom;

    @ManyToMany
    private Set<Signatura> signatures;
}
