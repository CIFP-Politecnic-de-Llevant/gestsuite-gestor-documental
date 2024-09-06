package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

public @Data class ConvocatoriaDto {
    private Long idConvocatoria;
    private String nom;
    private String path;
    private Boolean isUnitatOrganitzativa;
    private Boolean isActual;
    private Long idCursAcademic;
}
