package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;


@Entity
@Table(name = "pll_alumne")
public @Data class Alumne {

    @Id
    @Column( name = "idAlumne")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlumne;

    @Column(name = "nom", nullable = true, length = 128)
    private String nom;

    @Column(name = "cognom1", nullable = true, length = 128)
    private String cognom1;

    @Column(name = "cognom2", nullable = true, length = 128)
    private String cognom2;

    @Column(name = "ensenyament", nullable = true, length = 128)
    private String ensenyament;

    @Column(name = "estudis", nullable = true, length = 128)
    private String estudis;

    @Column(name = "grup", nullable = true, length = 128)
    private String grup;

    @Column(name = "numero_expedient", nullable = true)
    private String numeroExpedient;

    @Column(name = "sexe", nullable = true, length = 128)
    private String sexe;

    @Column(name = "data_naixement", nullable = true)
    private LocalDate dataNaixement;

    @Column(name = "nacionalitat", nullable = true, length = 128)
    private String nacionalitat;

    @Column(name = "pais_naixament", nullable = true, length = 128)
    private String paisNaixement;

    @Column(name = "provincia_naixement", nullable = true, length = 128)
    private String provinciaNaixement;

    @Column(name = "localitat_naixement", nullable = true, length = 256)
    private String localitatNaixement;

    @Column(name = "dni", nullable = true, length = 64)
    private String dni;

    @Column(name = "targeta_sanitaria", nullable = true, length = 128)
    private String targetaSanitaria;

    @Column(name = "CIP", nullable = true, length = 128)
    private String CIP;

    @Column(name = "adreca_completa", nullable = true, length = 2048)
    private String adrecaCompleta;

    @Column(name = "municipi", nullable = true, length = 256)
    private String municipi;

    @Column(name = "localitat", nullable = true, length = 256)
    private String localitat;

    @Column(name = "cp", nullable = true, length = 64)
    private String CP;

    @Column(name = "telefon", nullable = true, length = 64)
    private String telefon;

    @Column(name = "telefon_fix", nullable = true, length = 64)
    private String telefonFix;

    @Column(name = "email", nullable = true, length = 255)
    private String email;

    @Column(name = "tutor", nullable = true, length = 512)
    private String tutor;

    @Column(name = "telefon_tutor", nullable = true, length = 2048)
    private String telefonTutor;

    @Column(name = "email_tutor", nullable = true, length = 512)
    private String emailTutor;

    @Column(name = "dni_tutor", nullable = true, length = 128)
    private String dniTutor;

    @Column(name = "adreca_tutor", nullable = true, length = 2048)
    private String adrecaTutor;

    @Column(name = "nacionalitat_tutor", nullable = true, length = 512)
    private String nacionalitatTutor;

    @Column(name = "idUsuari", nullable = true)
    private Long idUsuari;

    @Column(name = "isFCT", nullable = false)
    private Boolean isFCT;
}
