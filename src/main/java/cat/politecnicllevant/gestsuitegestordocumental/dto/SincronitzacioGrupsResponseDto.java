package cat.politecnicllevant.gestsuitegestordocumental.dto;

import cat.politecnicllevant.common.model.NotificacioTipus;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public @Data class SincronitzacioGrupsResponseDto {
    // Grups del core donats d'alta al gestor documental
    private List<String> creats = new ArrayList<>();
    // Grups que ja existien però tenien un id de core que no era el seu
    private List<String> corregits = new ArrayList<>();
    // Grups que no s'han creat perquè el seu cicle no té full de càlcul ni carpeta FEMPO
    private List<String> senseConfiguracioFempo = new ArrayList<>();
    // Grups que no s'han tocat perquè el seu id de core ja el té un altre grup
    private List<String> conflictes = new ArrayList<>();
    private NotificacioTipus notifyType;
    private String notifyMessage;
}
