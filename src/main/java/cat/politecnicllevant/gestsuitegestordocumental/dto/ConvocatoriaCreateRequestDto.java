package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

import java.util.List;

public @Data class ConvocatoriaCreateRequestDto {
    private ConvocatoriaDto convocatoria;
    private Long previousConvocatoriaId;
    private String previousPathDesti;
    private Boolean applyDriveChanges;
    private Boolean deleteOriginDocuments;
    private List<String> selectedQFempoFolders;
}
