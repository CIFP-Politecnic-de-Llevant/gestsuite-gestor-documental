package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
@Table(name = "pll_alumne")
public class Alumne {

    @Id
    @Column( name = "idAlumne")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlumne;

    @Column(name = "nom", nullable = false, length = 128)
    private String nom;

    @Column(name = "cognom1", nullable = false, length = 128)
    private String cognom1;

    @Column(name = "cognom2", nullable = true, length = 128)
    private String cognom2;

    @Column(name = "ensenyament", nullable = false, length = 128)
    private String ensenyament;

    @Column(name = "estudis", nullable = false, length = 128)
    private String estudis;

    @Column(name = "grup", nullable = false, length = 128)
    private String grup;

    @Column(name = "numero_expedient", unique = true, nullable = false)
    private Long numero_expedient;

    @Column(name = "sexe", nullable = false, length = 128)
    private String sexe;

    @Column(name = "data_naixament", nullable = false)
    private LocalDate data_naixament;

    @Column(name = "nacionalitat", nullable = false, length = 128)
    private String nacionalitat;

    @Column(name = "pais_naixament", nullable = false, length = 128)
    private String pais_naixament;

    @Column(name = "provincia_naixament", nullable = false, length = 128)
    private String provincia_naixament;

    @Column(name = "localitat_naixament", nullable = false, length = 128)
    private String localitat_naixament;

    @Column(name = "dni", unique = true, nullable = false, length = 128)
    private String dni;

    @Column(name = "targeta_sanitaria", unique = true, nullable = false, length = 128)
    private String targeta_sanitaria;

    @Column(name = "CIP", unique = true, nullable = true)
    private Long CIP;

    @Column(name = "adreça_completa", nullable = true, length = 128)
    private String adreça_completa;

    @Column(name = "minucipi", nullable = false, length = 128)
    private String minucipi;

    @Column(name = "localitat", nullable = false, length = 128)
    private String localitat;

    @Column(name = "CP", nullable = false)
    private Long CP;

    @Column(name = "telefon", unique = true, nullable = true, length = 128)
    private Long telefon;

    @Column(name = "telefon_fix", unique = true, nullable = true, length = 128)
    private Long telefon_fix;

    @Column(name = "email", unique = true, nullable = false, length = 128)
    private String email;

    @Column(name = "tutor", nullable = true, length = 128)
    private String tutor;

    @Column(name = "pare", nullable = true, length = 128)
    private String pare;

    @Column(name = "mare", nullable = true, length = 128)
    private String mare;
    @Column(name = "telefon_tutor", nullable = false)
    private Long telefon_tutor;

    @Column(name = "telefon_pare", nullable = false)
    private Long telefon_pare;

    @Column(name = "telefon_mare", nullable = false)
    private Long telefon_mare;

    @Column(name = "email_tutor", nullable = true, length = 128)
    private String email_tutor;

    @Column(name = "email_pare", nullable = true, length = 128)
    private Long email_pare;
    @Column(name = "email_mare", nullable = true, length = 128)
    private Long email_mare;

    @Column(name = "dni_tutor", nullable = true, length = 128)
    private String dni_tutor;

    @Column(name = "dni_pare", nullable = true, length = 128)
    private String dni_pare;

    @Column(name = "dni_mare", nullable = true, length = 128)
    private String dni_mare;

    @Column(name = "adreça_tutor", nullable = true, length = 128)
    private String adreça_tutor;

    @Column(name = "adreça_pare", nullable = true, length = 128)
    private String adreça_pare;

    @Column(name = "adreça_mare", nullable = true, length = 128)
    private String adreça_mare;

    @Column(name = "nacionalitat_tutor", nullable = true, length = 128)
    private String nacionalitat_tutor;

    @Column(name = "nacionalitat_pare", nullable = true, length = 128)
    private String nacionalitat_pare;

    @Column(name = "nacionalitat_mare", nullable = true, length = 128)
    private String nacionalitat_mare;

}
