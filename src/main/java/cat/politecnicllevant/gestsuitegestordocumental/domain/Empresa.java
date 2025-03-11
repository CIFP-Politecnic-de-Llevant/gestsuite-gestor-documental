package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pll_empresa")
public @Data class Empresa {

    @Id
    @Column(name = "idEmpresa")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmpresa;

    @Column(name = "numero_conveni",nullable = true, length = 128)
    private String numeroConveni;

    @Column(name = "email_empresa",nullable = true, length = 128)
    private String emailEmpresa;

    @Column(name = "nom",nullable = true, length = 255)
    private String nom;

    @Column(name = "cif",nullable = true, length = 128)
    private String cif;

    @Column(name = "adreca",nullable = true, length = 255)
    private String adreca;

    @Column(name = "codi_postal",nullable = true)
    private String codiPostal;

    @Column(name = "poblacio",nullable = true, length = 128)
    private String poblacio;

    @Column(name = "provincia",nullable = true, length = 128)
    private String provincia;

    @Column(name = "telefon",nullable = true, length = 128)
    private String telefon;

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "empresa")
    private Set<LlocTreball> llocsTreball = new HashSet<>();




}
