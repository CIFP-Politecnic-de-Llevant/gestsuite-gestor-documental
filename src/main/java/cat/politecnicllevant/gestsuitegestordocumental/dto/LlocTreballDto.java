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
    private String municipi;
    private boolean validat;
    private String emailCreator;
    private EmpresaDto empresa;
}
