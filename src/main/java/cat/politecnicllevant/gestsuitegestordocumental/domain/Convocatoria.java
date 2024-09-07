package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pll_convocatoria")
public @Data class Convocatoria {

    @Id
    @Column(name = "idConvocatoria")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConvocatoria;

    @Column(name = "nom", nullable = true, length = 128)
    private String nom;

    @Column(name = "path_origen",nullable = false)
    private String pathOrigen;

    @Column(name = "is_unitat_organitzativa_origen",nullable = false)
    private Boolean isUnitatOrganitzativaOrigen;

    @Column(name = "path_desti",nullable = false)
    private String pathDesti;

    @Column(name = "is_unitat_organitzativa_desti",nullable = false)
    private Boolean isUnitatOrganitzativaDesti;


    @Column(name = "actual")
    private Boolean isActual;

    @Column(name = "curs_academic",nullable = true, length = 128)
    private Long idCursAcademic;

}
