package cat.politecnicllevant.gestsuitegestordocumental.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

public @Data class DadesFormulariDto {

    private String Id;
    private String anyCurs;
    private String nomAlumne;
    private String llinatgesAlumne;
    private String poblacio;
    private String dni;
    //@JsonProperty(value = "nExpedient")
    private String numeroExpedient;
    private Boolean menorEdat;
    private String edat;
    private String estudis;
    private String cicleFormatiu;
    private String grup;
    private String duradaCicle;
    private String totalHoresProposadesFct;
    private String horesDiaries;
    private String km;
    private String periode;
    private LocalDate dataInici;
    private LocalDate dataFi;
    private LocalDate dataAcabament;
    private String tipusJornada;
    private String horari;
    private String nomTutor;
    private String llinatgesTutor;
    private String telefonTutor;
    private Boolean empresaNova;
    private Boolean empresaAdministracioPublica;
    private String numeroConveni;
    private String nomEmpresa;
    private String cif;
    private String adrecaEmpresa;
    private String cpempresa;
    private String poblacioEmpresa;
    private String telefonEmpresa;
    private String nomLlocTreball;
    private String adrecaLlocTreball;
    private String cpLlocTreball;
    private String poblacioLlocTreball;
    private String telefonLlocTreball;
    private String activitatLlocTreball;
    private String nomCompletRepresentantLegal;
    private String nifRepresentantLegal;
    private String nomCompletTutorEmpresa;
    private String nifTutorEmpresa;
    private String nacionalitatTutorEmpresa;
    private String municipiTutorEmpresa;
    private String carrecTutorEmpresa;
    private String emailEmpresa;
    private String emailTutorEmpresa;
    private String diaSeguimentCentreEducatiu;
    private String horaSeguimentCentreEducatiu;
    private String diaSeguimentResponsableFct;
    private String horaSeguimentResponsableFct;
    private Boolean flexibilitzacioModulFct;
    private Long idCursAcademic;
}
