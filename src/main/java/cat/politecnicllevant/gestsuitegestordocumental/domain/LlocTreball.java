package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pll_lloc_treball")
public @Data class LlocTreball {

    @Id
    @Column(name = "idLlocEmpresa")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLlocTreball;

    @Column(name = "nom",nullable = true, length = 255)
    private String nom;

    @Column(name = "adreca",nullable = true, length = 255)
    private String adreca;

    @Column(name = "codi_potal",nullable = true)
    private String codiPostal;

    @Column(name = "telefon",nullable = true, length = 128)
    private String telefon;

    @Column(name = "poblacio",nullable = true, length = 128)
    private String poblacio;

    @Column(name = "activitat",nullable = true, length = 128)
    private String activitat;

    @Column(name = "nom_complet_representant_legal",nullable = true, length = 255)
    private String nomCompletRepresentantLegal;

    @Column(name = "dni_representant_legal",nullable = true, length = 128)
    private String dniRepresentantLegal;

    @Column(name = "nom_complet_tutor_empresa",nullable = true, length = 255)
    private String nomCompletTutorEmpresa;

    @Column(name = "dni_tutor_empresa",nullable = true, length = 128)
    private String dniTutorEmpresa;

    @Column(name = "municipi",nullable = true, length = 128)
    private String municipi;

    @Column(name = "carrec_tutor",nullable = true, length = 128)
    private String carrecTutor;

    @Column(name = "email",nullable = true, length = 255)
    private String emailTutorEmpresa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn( name = "idEmpresa")
    private Empresa empresa;





}
