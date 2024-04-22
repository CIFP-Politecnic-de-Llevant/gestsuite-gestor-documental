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

    @Column(name = "visibilitat_defecte", nullable = false)
    private Boolean visibilitatDefecte;

    @Column(name = "propietari", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipusDocumentPropietari propietari;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Signatura> signatures;
}
