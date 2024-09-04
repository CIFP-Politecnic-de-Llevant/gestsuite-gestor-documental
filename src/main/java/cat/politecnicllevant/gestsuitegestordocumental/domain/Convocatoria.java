package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pll_convocatoria")
public @Data class Convocatoria {

    @Id
    @Column(name = "idConvocatoria")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConvocatoria;

    @Column(name = "nom", nullable = true, length = 128)
    private String nom;

    @Column(name = "path",nullable = false)
    private String path;

    @Column(name = "is_unitat_organitzativa")
    private Boolean isUnitatOrganitzativa;

    @Column(name = "actual")
    private Boolean isActual;

    @Column(name = "curs_academic",nullable = true, length = 128)
    private Long idCursAcademic;

}
