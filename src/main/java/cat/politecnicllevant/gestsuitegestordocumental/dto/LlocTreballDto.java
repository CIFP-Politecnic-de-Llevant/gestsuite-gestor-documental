package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

public @Data class LlocTreballDto {

    private Long idLlocTreball;
    private String nom;
    private String adreca;
    private String codiPostal;
    private String telefon;
    private String poblacio;
    private String activitat;
    private String nomCompletRepresentantLegal;
    private String nomRepresentantLegal;
    private String cognomsRepresentantLegal;
    private String dniRepresentantLegal;
    private String nomCompletTutorEmpresa;
    private String nomTutorEmpresa;
    private String cognomsTutorEmpresa;
    private String dniTutorEmpresa;
    private String municipi;
    private String carrecTutor;
    private String emailTutorEmpresa;
    private EmpresaDto empresa;
}
