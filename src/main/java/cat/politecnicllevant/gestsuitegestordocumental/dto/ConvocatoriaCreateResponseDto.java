package cat.politecnicllevant.gestsuitegestordocumental.dto;

import cat.politecnicllevant.common.model.NotificacioTipus;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public @Data class ConvocatoriaCreateResponseDto {
    private ConvocatoriaDto convocatoria;
    private List<String> carpetesEsborrades = new ArrayList<>();
    private List<String> carpetesNoEsborrades = new ArrayList<>();
    private Integer fitxersOrigenNoEsborrats = 0;
    private NotificacioTipus notifyType;
    private String notifyMessage;
}
