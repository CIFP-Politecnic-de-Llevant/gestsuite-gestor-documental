package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

public @Data class ConvocatoriaCreateRequestDto {
    private ConvocatoriaDto convocatoria;
    private Long previousConvocatoriaId;
    private String previousPathDesti;
}
