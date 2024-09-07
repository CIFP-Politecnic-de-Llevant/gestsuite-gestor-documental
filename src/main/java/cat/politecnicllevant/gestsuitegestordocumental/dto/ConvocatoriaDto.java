package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

public @Data class ConvocatoriaDto {
    private Long idConvocatoria;
    private String nom;
    private String pathOrigen;
    private Boolean isUnitatOrganitzativaOrigen;
    private String pathDesti;
    private Boolean isUnitatOrganitzativaDesti;
    private Boolean isActual;
    private Long idCursAcademic;
}
