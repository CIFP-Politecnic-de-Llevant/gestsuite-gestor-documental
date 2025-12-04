package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "pll_dades_formulari")
public @Data class DadesFormulari {

    @Id
    @Column( name = "idDadesFormulari")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "any_curs", nullable = true, length = 32)
    private String anyCurs;

    @Column(name = "nom_alumne", nullable = true, length = 128)
    private String nomAlumne;

    @Column(name = "llinatges_alumne", nullable = true, length = 128)
    private String llinatgesAlumne;

    @Column(name = "poblacio", nullable = true, columnDefinition = "TEXT")
    private String poblacioAlumne;

    @Column(name = "dni", nullable = true, length = 64)
    private String dniAlumne;

    @Column(name = "telefon_alumne", nullable = true, length = 64)
    private String telefonAlumne;

    @Column(name = "email_alumne", nullable = true, length = 128)
    private String emailAlumne;

    @Column(name = "n_expedient", nullable = true, length = 10)
    private String nExpedient;

    @Column(name = "sexe", nullable = false)
    private Boolean menorEdat;

    @Column(name = "edat", nullable = true)
    private Integer edat;

    @Column(name = "estudis", nullable = true, length = 128)
    private String estudis;

    @Column(name = "cicle_formatiu", nullable = true, length = 128)
    private String cicleFormatiu;

    @Column(name = "grup", nullable = true, length = 16)
    private String grup;

    @Column(name = "durada_cicle", nullable = true, length = 32)
    private String duradaCicle;

    @Column(name = "curs_estada", nullable = true, length = 32)
    private String cursEstada;

    @Column(name = "ocasio", nullable = true, length = 32)
    private String ocasio;

    @Column(name = "acumula_estades_primer", nullable = true)
    private Boolean acumulaEstadesPrimer;

    @Column(name = "hores_primer_a_segon", nullable = true)
    private Integer horesPrimerASegon;

    @Column(name = "mobilitat", nullable = true)
    private Boolean mobilitat;

    @Column(name = "zona_mobilitat", nullable = true, length = 128)
    private String zonaMobilitat;

    @Column(name = "caracteristiques_mobilitat", nullable = true, columnDefinition = "TEXT")
    private String caracteristiquesMobilitat;

    @Column(name = "hores_fct_proposades", nullable = true, length = 128)
    private String totalHoresProposadesFct;

    @Column(name = "hores_diaries", nullable = true, length = 128)
    private String horesDiaries;

    @Column(name = "km", nullable = true, length = 128)
    private String km;

    @Column(name = "periode", nullable = true, length = 512)
    private String periode;

    @Column(name = "data_inici", nullable = true)
    private LocalDate dataInici;

    @Column(name = "data_fi", nullable = true)
    private LocalDate dataFi;

    @Column(name = "data_acabament", nullable = true)
    private LocalDate dataAcabament;

    @Column(name = "tipus_jornada", nullable = true, length = 128)
    private String tipusJornada;

    @Column(name = "horari", nullable = true, length = 512)
    private String horari;

    @Column(name = "nom_tutor", nullable = true, length = 128)
    private String nomTutor;

    @Column(name = "llinatges_tutor", nullable = true, length = 256)
    private String llinatgesTutor;

    @Column(name = "telefon_tutor", nullable = true, length = 64)
    private String telefonTutor;

    @Column(name = "email_tutor", nullable = true, length = 128)
    private String email_tutor;

    @Column(name = "empresa_nova", nullable = true)
    private Boolean empresaNova;

    @Column(name = "empresa_administracio_publica", nullable = true)
    private Boolean empresaAdministracioPublica;

    @Column(name = "numero_conveni", nullable = true, length = 128)
    private String numeroConveni;

    @Column(name = "nom_empresa", nullable = true, length = 256)
    private String nomEmpresa;

    @Column(name = "cif", nullable = true, length = 128)
    private String cif;

    @Column(name = "adreca_empresa", nullable = true, columnDefinition = "TEXT")
    private String adrecaEmpresa;

    @Column(name = "cp_empresa", nullable = true, length = 10)
    private String cpEmpresa;

    @Column(name = "poblacio_empresa", nullable = true, length = 128)
    private String poblacioEmpresa;

    @Column(name = "provincia_empresa", nullable = true, length = 128)
    private String provinciaEmpresa;

    @Column(name = "telefon_empresa", nullable = true, length = 64)
    private String telefonEmpresa;

    @Column(name = "email_empresa", nullable = true, length = 128)
    private String emailEmpresa;

    @Column(name = "nom_lloc_treball", nullable = true, length = 128)
    private String nomLlocTreball;

    @Column(name = "adreca_lloc_treball", nullable = true, columnDefinition = "TEXT")
    private String adrecaLlocTreball;

    @Column(name = "cp_lloc_treball", nullable = true, length = 10)
    private String cpLlocTreball;

    @Column(name = "poblacio_lloc_treball", nullable = true, columnDefinition = "TEXT")
    private String poblacioLlocTreball;

    @Column(name = "telefon_lloc_treball", nullable = true, length = 64)
    private String telefonLlocTreball;

    @Column(name = "activitat_lloc_treball", nullable = true, columnDefinition = "TEXT")
    private String activitatLlocTreball;

    @Column(name = "nom_complet_representant_legal", nullable = true, length = 512)
    private String nomCompletRepresentantLegal;

    @Column(name = "nom_representant_legal", nullable = true, length = 128)
    private String nomRepresentantLegal;

    @Column(name = "cognoms_representant_legal", nullable = true, length = 512)
    private String cognomsRepresentantLegal;

    @Column(name = "nif_representant_legal", nullable = true, length = 64)
    private String nifRepresentantLegal;

    @Column(name = "nom_complet_tutor_empresa", nullable = true, length = 128)
    private String nomCompletTutorEmpresa;

    @Column(name = "nom_tutor_empresa", nullable = true, length = 128)
    private String nomTutorEmpresa;

    @Column(name = "cognoms_tutor_empresa", nullable = true, length = 128)
    private String cognomsTutorEmpresa;

    @Column(name = "nif_tutor_empresa", nullable = true, length = 64)
    private String nifTutorEmpresa;

    @Column(name = "nacionalitat_tutor_empresa", nullable = true, length = 128)
    private String nacionalitatTutorEmpresa;

    @Column(name = "municipi_tutor_empresa", nullable = true, length = 256)
    private String municipiTutorEmpresa;

    @Column(name = "carrec_tutor_empresa", nullable = true, length = 128)
    private String carrecTutorEmpresa;

    @Column(name = "telefon_tutor_empresa", nullable = true, length = 128)
    private String telefonTutorEmpresa;

    @Column(name = "email_tutor_empresa", nullable = true, length = 128)
    private String emailTutorEmpresa;

    @Column(name = "dia_seguiment_centre_educatiu", nullable = true, length = 128)
    private String diaSeguimentCentreEducatiu;

    @Column(name = "hora_seguiment_centre_educatiu", nullable = true, length = 128)
    private String horaSeguimentCentreEducatiu;

    @Column(name = "dia_seguiment_responsable_fct", nullable = true, length = 128)
    private String diaSeguimentResponsableFct;

    @Column(name = "hora_seguiment_responsable_fct", nullable = true, length = 128)
    private String horaSeguimentResponsableFct;

    @Column(name = "flexibilitzacio_fct", nullable = true)
    private Boolean flexibilitzacioModulFct;

    @Column(name = "mesures_educatives", nullable = true)
    private Boolean isMesuresEducatives;

    @Column(name = "mesures_educatives_desripcio", nullable = true, columnDefinition = "TEXT")
    private String mesuresEducativesDescripcio;

    @Column(name = "autoritzacio_extraordinaria", nullable = true)
    private Boolean isAutoritzacioExtraordinaria;

    @Column(name = "motiu", nullable = true, columnDefinition = "TEXT")
    private String motiu;

    @Column(name = "curs_academic",nullable = false, length = 16)
    private Long idCursAcademic;
}
