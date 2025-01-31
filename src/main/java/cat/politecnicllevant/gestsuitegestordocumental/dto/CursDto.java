package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

public @Data class CursDto {
    private Long idcurs;
    private String gestibIdentificador;
    private String gestibNom;
    private Boolean actiu;
}
