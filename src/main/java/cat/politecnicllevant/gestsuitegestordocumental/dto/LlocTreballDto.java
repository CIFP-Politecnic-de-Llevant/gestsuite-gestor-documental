package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

public @Data class LlocTreballDto {

    private Long idLlocTreball;
    private String nom;
    private String adreca;
    private Long codiPostal;
    private String telefon;
    private String poblacio;
    private String activitat;
    private String nomCompletRepresentantLegal;
    private String dniRepresentantLegal;
    private String nomCompletTutorEmpresa;
    private String dniTutorEmpresa;
    private String municipi;
    private String carrecTutor;
    private String email;
    private EmpresaDto empresa;
}
