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

    @Column(name = "municipi",nullable = true, length = 128)
    private String municipi;

    @Column(name = "validat", nullable = false)
    private boolean validat;

    @Column(name = "email_creator",nullable = true, length = 255)
    private String emailCreator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn( name = "idEmpresa")
    private Empresa empresa;
}
