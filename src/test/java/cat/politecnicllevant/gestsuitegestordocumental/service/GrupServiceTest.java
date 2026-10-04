package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.domain.Grup;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.SincronitzacioGrupsResponseDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.GrupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrupServiceTest {

    private GrupRepository grupRepository;
    private GrupService grupService;

    @BeforeEach
    void setUp() {
        grupRepository = mock(GrupRepository.class);
        grupService = new GrupService(grupRepository, new ModelMapper());
    }

    private static Grup grup(String cursGrup, String spreadsheet, String folder, boolean actiu) {
        Grup grup = new Grup();
        grup.setCursGrup(cursGrup);
        grup.setIdGoogleSpreadsheet(spreadsheet);
        grup.setFolderGoogleDrive(folder);
        grup.setActiu(actiu);
        return grup;
    }

    @Test
    void grupNoDonatDAltaFaServirLaConfiguracioDelSeuCicle() {
        // ADG32F existeix al core però no a pll_grup; el full FEMPO és el mateix per a tot el cicle ADG32
        when(grupRepository.findByIdGoogleSpreadsheetIsNotNull()).thenReturn(List.of(
                grup("ADG31A", "sheet-adg31", "xxx", true),
                grup("ADG32A", "sheet-adg32", "xxx", true)
        ));

        GrupDto fempo = grupService.getFempoByCicle("ADG32");

        assertNotNull(fempo);
        assertEquals("sheet-adg32", fempo.getIdGoogleSpreadsheet());
        assertEquals("xxx", fempo.getFolderGoogleDrive());
    }

    @Test
    void nomesConsideraGrupsActiusAmbCarpetaIFullDelMateixCicle() {
        when(grupRepository.findByIdGoogleSpreadsheetIsNotNull()).thenReturn(List.of(
                grup("ADG32A", "sheet-inactiu", "xxx", false),
                grup("ADG32B", "sheet-sense-carpeta", "", true),
                grup("ADG321A", "sheet-altre-cicle", "xxx", true),
                grup("ADG32C", "sheet-adg32", "xxx", true)
        ));

        assertEquals("sheet-adg32", grupService.getFempoByCicle("ADG32").getIdGoogleSpreadsheet());
        assertNull(grupService.getFempoByCicle("ADG3"));
    }

    private static Grup grup(String cursGrup, long coreIdGrup) {
        Grup grup = grup(cursGrup, "sheet-" + cursGrup.substring(0, cursGrup.length() - 1), "xxx", true);
        grup.setCoreIdGrup(coreIdGrup);
        return grup;
    }

    @SuppressWarnings("unchecked")
    private List<Grup> grupsDesats() {
        ArgumentCaptor<List<Grup>> desats = ArgumentCaptor.forClass(List.class);
        verify(grupRepository).saveAll(desats.capture());
        return desats.getValue();
    }

    @Test
    void sincronitzacioCreaElsGrupsDelCoreQueFaltenAmbLaConfiguracioDelSeuCicle() {
        when(grupRepository.findAll()).thenReturn(List.of(grup("ADG32A", 1L)));

        SincronitzacioGrupsResponseDto resultat = grupService.sincronitzarAmbCore(Map.of(1L, "ADG32A", 72L, "ADG32F"));

        assertEquals(List.of("ADG32F"), resultat.getCreats());
        Grup nou = grupsDesats().get(0);
        assertEquals("ADG32F", nou.getCursGrup());
        assertEquals(72L, nou.getCoreIdGrup());
        assertEquals("sheet-ADG32", nou.getIdGoogleSpreadsheet());
        assertEquals("xxx", nou.getFolderGoogleDrive());
        assertTrue(nou.getActiu());
        assertEquals(NotificacioTipus.SUCCESS, resultat.getNotifyType());
    }

    @Test
    void sincronitzacioCorregeixElsIdsDeCoreCreuats() {
        // Cas real a dev: TMV31D i TMV31F tenen l'id de core desplaçat
        Grup tmv31d = grup("TMV31D", 58L);
        Grup tmv31f = grup("TMV31F", 61L);
        when(grupRepository.findAll()).thenReturn(List.of(tmv31d, tmv31f));

        SincronitzacioGrupsResponseDto resultat = grupService.sincronitzarAmbCore(Map.of(61L, "TMV31D", 84L, "TMV31F"));

        assertEquals(List.of("TMV31D", "TMV31F"), resultat.getCorregits());
        assertEquals(61L, tmv31d.getCoreIdGrup());
        assertEquals(84L, tmv31f.getCoreIdGrup());
        assertTrue(resultat.getCreats().isEmpty());
    }

    @Test
    void sincronitzacioNoCreaGrupsDeCiclesSenseConfiguracioFempo() {
        when(grupRepository.findAll()).thenReturn(List.of(grup("ADG32A", 1L)));

        SincronitzacioGrupsResponseDto resultat = grupService.sincronitzarAmbCore(
                Map.of(1L, "ADG32A", 79L, "TMVG0310B", 88L, "Munt. instal·lacions edificisA"));

        assertEquals(List.of("Munt. instal·lacions edificisA", "TMVG0310B"), resultat.getSenseConfiguracioFempo());
        assertTrue(resultat.getCreats().isEmpty());
        verify(grupRepository, never()).saveAll(anyList());
    }

    @Test
    void sincronitzacioNoDuplicaUnIdDeCoreJaAssignatAUnAltreGrup() {
        // ADG32L ja no és actiu al core però conserva l'id 72, que ara és el d'ADG32F
        when(grupRepository.findAll()).thenReturn(List.of(grup("ADG32A", 1L), grup("ADG32L", 72L)));

        SincronitzacioGrupsResponseDto resultat = grupService.sincronitzarAmbCore(Map.of(1L, "ADG32A", 72L, "ADG32F"));

        assertEquals(List.of("ADG32F"), resultat.getConflictes());
        assertTrue(resultat.getCreats().isEmpty());
        assertEquals(NotificacioTipus.WARNING, resultat.getNotifyType());
    }

    @Test
    void sincronitzacioSenseCanvisNoDesaRes() {
        when(grupRepository.findAll()).thenReturn(List.of(grup("ADG32A", 1L)));

        SincronitzacioGrupsResponseDto resultat = grupService.sincronitzarAmbCore(Map.of(1L, "ADG32A"));

        assertTrue(resultat.getCreats().isEmpty());
        assertTrue(resultat.getCorregits().isEmpty());
        verify(grupRepository, never()).saveAll(anyList());
        assertEquals(NotificacioTipus.INFO, resultat.getNotifyType());
    }

    @Test
    void cicleSenseConfiguracioFempoRetornaNull() {
        when(grupRepository.findByIdGoogleSpreadsheetIsNotNull()).thenReturn(List.of(
                grup("ADG32A", "sheet-adg32", "xxx", true)
        ));

        assertNull(grupService.getFempoByCicle("TMVG0310"));
        assertNull(grupService.getFempoByCicle(""));
        assertNull(grupService.getFempoByCicle(null));
    }
}
