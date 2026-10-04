package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.domain.Grup;
import cat.politecnicllevant.gestsuitegestordocumental.dto.CursAcademicDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DadesFormulariDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.GrupRepository;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import cat.politecnicllevant.gestsuitegestordocumental.service.DadesFormulariService;
import cat.politecnicllevant.gestsuitegestordocumental.service.GoogleDriveService;
import cat.politecnicllevant.gestsuitegestordocumental.service.GrupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FormulariFCTControllerFempoTest {

    private static final String FULL_FEMPO_GENERAL = "1B452x9ovCin40_yQpyJ_-TgunGQYVr50zGESGgE8Y2o";

    private GoogleDriveService googleDriveService;
    private DadesFormulariService dadesFormulariService;
    private CoreRestClient coreRestClient;
    private GrupRepository grupRepository;
    private FormulariFCTController controller;

    @BeforeEach
    void setUp() {
        googleDriveService = mock(GoogleDriveService.class);
        dadesFormulariService = mock(DadesFormulariService.class);
        coreRestClient = mock(CoreRestClient.class);
        grupRepository = mock(GrupRepository.class);
        controller = new FormulariFCTController(googleDriveService, dadesFormulariService, coreRestClient,
                new GrupService(grupRepository, new ModelMapper()));

        CursAcademicDto cursAcademic = new CursAcademicDto();
        cursAcademic.setIdcursAcademic(1L);
        when(coreRestClient.getActualCursAcademic()).thenReturn(ResponseEntity.ok(cursAcademic));

        // Situació real a dev: el gestor documental té ADG32A però no ADG32F
        Grup adg32a = new Grup();
        adg32a.setCoreIdGrup(1L);
        adg32a.setCursGrup("ADG32A");
        adg32a.setIdGoogleSpreadsheet("sheet-adg32");
        adg32a.setFolderGoogleDrive("xxx");
        adg32a.setActiu(true);
        when(grupRepository.findByIdGoogleSpreadsheetIsNotNull()).thenReturn(List.of(adg32a));
    }

    private static DadesFormulariDto formulari(String grup) {
        DadesFormulariDto form = new DadesFormulariDto();
        form.setGrup(grup);
        form.setNomAlumne("FOUZIA");
        form.setMenorEdat(false);
        form.setEmpresaAdministracioPublica(false);
        form.setEmpresaNova(false);
        form.setIsMesuresEducatives(false);
        form.setIsAutoritzacioExtraordinaria(false);
        return form;
    }

    @Test
    @SuppressWarnings("unchecked")
    void grupNoDonatDAltaEsDesaAlFullDelSeuCicle() throws Exception {
        GrupDto grupCore = new GrupDto();
        grupCore.setIdgrup(72L);
        when(coreRestClient.getByCodigrup("ADG32F")).thenReturn(ResponseEntity.ok(grupCore));
        DadesFormulariDto form = formulari("ADG32F");

        ResponseEntity<Notificacio> resposta = controller.saveFormFEMPO(form, "tutor@politecnicllevant.cat");

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(NotificacioTipus.SUCCESS, resposta.getBody().getNotifyType());

        ArgumentCaptor<Map<String, String>> dades = ArgumentCaptor.forClass(Map.class);
        verify(googleDriveService).writeDataPosition(dades.capture(), eq("sheet-adg32"));
        assertEquals("ADG32F", dades.getValue().get("Grup"));
        verify(googleDriveService).writeDataPosition(anyMap(), eq(FULL_FEMPO_GENERAL));
        verify(dadesFormulariService).save(form);
    }

    @Test
    void cicleSenseFempoNoEsDesa() throws Exception {
        when(coreRestClient.getByCodigrup("TMVG0310B")).thenThrow(new RuntimeException("no trobat"));

        ResponseEntity<Notificacio> resposta = controller.saveFormFEMPO(formulari("TMVG0310B"), "tutor@politecnicllevant.cat");

        assertEquals(HttpStatus.NOT_ACCEPTABLE, resposta.getStatusCode());
        assertEquals(NotificacioTipus.ERROR, resposta.getBody().getNotifyType());
        verify(googleDriveService, never()).writeDataPosition(anyMap(), anyString());
        verify(dadesFormulariService, never()).save(any());
    }
}
