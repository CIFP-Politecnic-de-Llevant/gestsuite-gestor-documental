package cat.politecnicllevant.gestsuitegestordocumental.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "alumnes")
public @Data class DadesFormulari {

    @Id
    private String Id;
    private String anyCurs;
    private String nomAlumne;
    private String llinatgesAlumne;
    private String poblacio;
    private String dni;
    private String nExpedient;
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
    private String dataInici;
    private String dataFi;
    private String dataAcabament;
    private String tipusJornada;
    private String horari;
    private String nomTutor;
    private String llinatgesTutor;
    private String telefonTutor;
    private Boolean empresaNova;
    private Boolean empresaAdministracioPublica;
    private String numeroConveni;
    private String numeroAnnex;
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
    private String nomRepresentantLegal;
    private String llinatgesRepresentantLegal;
    private String nifRepresentantLegal;
    private String nomTutorEmpresa;
    private String llinatgesTutorEmpresa;
    private String nifTutorEmpresa;
    private String nacionalitatTutorEmpresa;
    private String municipiTutorEmpresa;
    private String carrecTutorEmpresa;
    private String emailEmpresa;
    private String diaSeguimentCentreEducatiu;
    private String horaSeguimentCentreEducatiu;
    private String diaSeguimentResponsableFct;
    private String horaSeguimentResponsableFct;
    private Boolean flexibilitzacioModulFct;
}
