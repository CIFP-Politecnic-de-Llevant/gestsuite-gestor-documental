package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

public @Data class GrupDto {
    private Long idgrup;
    private String gestibIdentificador;
    private String gestibNom;
    private String gestibCurs;
    private String gestibTutor1;
    private String gestibTutor2;
    private String gestibTutor3;
    private Boolean actiu;
    private String gsuiteUnitatOrganitzativa;
    private String idGoogleSpreadsheet;
    private String folderGoogleDrive;
}
