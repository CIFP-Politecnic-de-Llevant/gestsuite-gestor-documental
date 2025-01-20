package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "pll_dades_formulari")
public @Data class DadesFormulari {

    @Id
    @Column( name = "idDadesFormulari")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "any_curs", nullable = true, length = 128)
    private String anyCurs;

    @Column(name = "nom_alumne", nullable = true, length = 128)
    private String nomAlumne;

    @Column(name = "llinatges_alumne", nullable = true, length = 128)
    private String llinatgesAlumne;

    @Column(name = "poblacio", nullable = true, length = 128)
    private String poblacio;

    @Column(name = "dni", nullable = true, length = 128)
    private String dni;

    @Column(name = "n_expedient", nullable = true, length = 128)
    private String nExpedient;

    @Column(name = "sexe", nullable = false)
    private Boolean menorEdat;

    @Column(name = "edat", nullable = false)
    private Integer edat;

    @Column(name = "estudis", nullable = true, length = 128)
    private String estudis;

    @Column(name = "cicle_formatiu", nullable = true, length = 128)
    private String cicleFormatiu;

    @Column(name = "grup", nullable = true, length = 128)
    private String grup;

    @Column(name = "durada_cicle", nullable = true, length = 128)
    private String duradaCicle;

    @Column(name = "hores_fct_proposades", nullable = true, length = 128)
    private String totalHoresProposadesFct;

    @Column(name = "hores_diaries", nullable = true, length = 128)
    private String horesDiaries;

    @Column(name = "km", nullable = true, length = 128)
    private String km;

    @Column(name = "periode", nullable = true, length = 128)
    private String periode;

    @Column(name = "data_inici", nullable = true)
    private LocalDate dataInici;

    @Column(name = "data_fi", nullable = true)
    private LocalDate dataFi;

    @Column(name = "data_acabament", nullable = true)
    private LocalDate dataAcabament;

    @Column(name = "tipus_jornada", nullable = true, length = 128)
    private String tipusJornada;

    @Column(name = "horari", nullable = true, length = 128)
    private String horari;

    @Column(name = "nom_tutor", nullable = true, length = 128)
    private String nomTutor;

    @Column(name = "llinatges_tutor", nullable = true, length = 128)
    private String llinatgesTutor;

    @Column(name = "telefon_tutor", nullable = true, length = 128)
    private String telefonTutor;

    @Column(name = "empresa_nova", nullable = true, length = 128)
    private Boolean empresaNova;

    @Column(name = "empresa_administracio_publica", nullable = true, length = 128)
    private Boolean empresaAdministracioPublica;

    @Column(name = "numero_conveni", nullable = true, length = 128)
    private String numeroConveni;

    @Column(name = "numero_annex", nullable = true, length = 128)
    private String numeroAnnex;

    @Column(name = "nom_empresa", nullable = true)
    private String nomEmpresa;

    @Column(name = "cif", nullable = true)
    private String cif;

    @Column(name = "adreca_empresa", nullable = true)
    private String adrecaEmpresa;

    @Column(name = "cp_empresa", nullable = true)
    private String cpEmpresa;

    @Column(name = "poblacio_empresa", nullable = true)
    private String poblacioEmpresa;

    @Column(name = "telefon_empresa", nullable = true)
    private String telefonEmpresa;

    @Column(name = "nom_lloc_treball", nullable = true)
    private String nomLlocTreball;

    @Column(name = "adreca_lloc_treball", nullable = true)
    private String adrecaLlocTreball;

    @Column(name = "cp_lloc_treball", nullable = true)
    private String cpLlocTreball;

    @Column(name = "poblacio_lloc_treball", nullable = true)
    private String poblacioLlocTreball;

    @Column(name = "telefon_lloc_treball", nullable = true)
    private String telefonLlocTreball;

    @Column(name = "activitat_lloc_treball", nullable = true)
    private String activitatLlocTreball;

    @Column(name = "nom_complet_representant_legal", nullable = true)
    private String nomCompletRepresentantLegal;

    @Column(name = "nif_representant_legal", nullable = true)
    private String nifRepresentantLegal;

    @Column(name = "nom_complet_tutor_empresa", nullable = true)
    private String nomCompletTutorEmpresa;

    @Column(name = "nif_tutor_empresa", nullable = true)
    private String nifTutorEmpresa;

    @Column(name = "nacionalitat_tutor_empresa", nullable = true)
    private String nacionalitatTutorEmpresa;

    @Column(name = "municipi_tutor_empresa", nullable = true)
    private String municipiTutorEmpresa;

    @Column(name = "carrec_tutor_empresa", nullable = true)
    private String carrecTutorEmpresa;

    @Column(name = "email_empresa", nullable = true)
    private String emailEmpresa;

    @Column(name = "dia_seguiment_centre_educatiu", nullable = true)
    private String diaSeguimentCentreEducatiu;

    @Column(name = "hora_seguiment_centre_educatiu", nullable = true)
    private String horaSeguimentCentreEducatiu;

    @Column(name = "dia_seguiment_responsable_fct", nullable = true)
    private String diaSeguimentResponsableFct;

    @Column(name = "hora_seguiment_responsable_fct", nullable = true)
    private String horaSeguimentResponsableFct;

    @Column(name = "flexibilitzacio_fct", nullable = true)
    private Boolean flexibilitzacioModulFct;
}
