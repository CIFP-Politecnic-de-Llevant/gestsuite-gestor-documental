package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.domain.Grup;
import cat.politecnicllevant.gestsuitegestordocumental.dto.CursDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.SincronitzacioGrupsResponseDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.GrupRepository;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import cat.politecnicllevant.gestsuitegestordocumental.service.GrupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class GrupAdminControllerTest {

    private CoreRestClient coreRestClient;
    private GrupRepository grupRepository;
    private GrupAdminController controller;

    @BeforeEach
    void setUp() {
        coreRestClient = mock(CoreRestClient.class);
        grupRepository = mock(GrupRepository.class);
        controller = new GrupAdminController(coreRestClient, new GrupService(grupRepository, new ModelMapper()));

        Grup adg32a = new Grup();
        adg32a.setCoreIdGrup(1L);
        adg32a.setCursGrup("ADG32A");
        adg32a.setIdGoogleSpreadsheet("sheet-adg32");
        adg32a.setFolderGoogleDrive("xxx");
        adg32a.setActiu(true);
        when(grupRepository.findAll()).thenReturn(List.of(adg32a));
    }

    private static GrupDto grupCore(long idgrup, String nom, String gestibCurs, boolean actiu) {
        GrupDto grup = new GrupDto();
        grup.setIdgrup(idgrup);
        grup.setGestibNom(nom);
        grup.setGestibCurs(gestibCurs);
        grup.setActiu(actiu);
        return grup;
    }

    @Test
    void creaElsGrupsActiusDelCoreAmbElNomDelSeuCurs() {
        CursDto adg32 = new CursDto();
        adg32.setGestibIdentificador("227");
        adg32.setGestibNom("ADG32");
        when(coreRestClient.getCursos()).thenReturn(ResponseEntity.ok(List.of(adg32)));
        when(coreRestClient.getGrups()).thenReturn(ResponseEntity.ok(List.of(
                grupCore(1L, "A", "227", true),
                grupCore(72L, "F", "227", true),
                grupCore(56L, "L", "227", false),   // inactiu al core
                grupCore(99L, "A", "999", true)     // curs desconegut
        )));

        ResponseEntity<SincronitzacioGrupsResponseDto> resposta = controller.sincronitzarAmbCore();

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(List.of("ADG32F"), resposta.getBody().getCreats());
    }

    @Test
    void siElCoreNoResponNoEsTocaCapGrup() {
        when(coreRestClient.getGrups()).thenThrow(new RuntimeException("core caigut"));

        ResponseEntity<SincronitzacioGrupsResponseDto> resposta = controller.sincronitzarAmbCore();

        assertEquals(HttpStatus.NOT_ACCEPTABLE, resposta.getStatusCode());
        assertEquals(NotificacioTipus.ERROR, resposta.getBody().getNotifyType());
        verify(grupRepository, never()).saveAll(anyList());
    }
}
