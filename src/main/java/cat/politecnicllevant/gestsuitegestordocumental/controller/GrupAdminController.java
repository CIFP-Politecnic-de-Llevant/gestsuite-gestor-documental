package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.dto.CursDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.SincronitzacioGrupsResponseDto;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import cat.politecnicllevant.gestsuitegestordocumental.service.GrupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class GrupAdminController {

    private final CoreRestClient coreRestClient;
    private final GrupService grupService;

    /**
     * Dona d'alta al gestor documental els grups actius del core que hi falten i corregeix els ids de core.
     * Les rutes /admin/** del gestor documental només són accessibles per ADMINISTRADOR i ADMINISTRADOR_FCT (gateway).
     */
    @PostMapping("/admin/grups/sincronitzar-core")
    public ResponseEntity<SincronitzacioGrupsResponseDto> sincronitzarAmbCore() {
        Map<Long, String> codisGrupsCore = new HashMap<>();
        try {
            List<CursDto> cursos = coreRestClient.getCursos().getBody();
            List<GrupDto> grups = coreRestClient.getGrups().getBody();

            Map<String, String> nomCursPerIdentificador = new HashMap<>();
            for (CursDto curs : cursos) {
                nomCursPerIdentificador.put(curs.getGestibIdentificador(), curs.getGestibNom());
            }

            for (GrupDto grup : grups) {
                String nomCurs = nomCursPerIdentificador.get(grup.getGestibCurs());
                if (!Boolean.TRUE.equals(grup.getActiu()) || nomCurs == null) {
                    continue;
                }
                codisGrupsCore.put(grup.getIdgrup(), nomCurs + grup.getGestibNom());
            }
        } catch (Exception ex) {
            log.error("Error consultant els grups i cursos del core per sincronitzar els grups", ex);
            SincronitzacioGrupsResponseDto error = new SincronitzacioGrupsResponseDto();
            error.setNotifyType(NotificacioTipus.ERROR);
            error.setNotifyMessage("No s'han pogut consultar els grups del core. No s'ha modificat cap grup.");
            return new ResponseEntity<>(error, HttpStatus.NOT_ACCEPTABLE);
        }

        SincronitzacioGrupsResponseDto resultat = grupService.sincronitzarAmbCore(codisGrupsCore);
        log.info("Sincronització de grups amb el core: {}", resultat.getNotifyMessage());
        return new ResponseEntity<>(resultat, HttpStatus.OK);
    }
}
