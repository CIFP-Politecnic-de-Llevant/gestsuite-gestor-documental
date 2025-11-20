package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Grup;
import cat.politecnicllevant.gestsuitegestordocumental.domain.GrupRelacio;
import cat.politecnicllevant.gestsuitegestordocumental.domain.GrupRelacioId;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.GrupRelacioRepository;
import cat.politecnicllevant.gestsuitegestordocumental.repository.GrupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GrupRelacioService {

    private final GrupRepository grupRepository;
    private final GrupRelacioRepository grupRelacioRepository;

    @Transactional(readOnly = true)
    public List<GrupDto> getGrupsRelacionats(Long grupId) {
        return grupRelacioRepository.findAllByGrup_IdGrupGestorDocumental(grupId)
                .stream()
                .map(GrupRelacio::getGrupRelacionat)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<GrupDto> actualitzarRelacions(Long grupId, List<Long> grupsRelacionats) {
        Grup grupPrincipal = grupRepository.findById(grupId)
                .orElseThrow(() -> new IllegalArgumentException("No s'ha trobat el grup indicat"));

        Set<Long> grupsObjectiu = new HashSet<>(grupsRelacionats);
        grupsObjectiu.remove(grupId);

        List<GrupRelacio> relacionsActuals = grupRelacioRepository.findAllByGrup_IdGrupGestorDocumental(grupId);
        for (GrupRelacio relacio : relacionsActuals) {
            Long idRelacionat = relacio.getGrupRelacionat().getIdGrupGestorDocumental();
            if (!grupsObjectiu.contains(idRelacionat)) {
                grupRelacioRepository.delete(relacio);
            }
        }

        for (Long grupRelacionatId : grupsObjectiu) {
            if (!grupRelacioRepository.existsByGrup_IdGrupGestorDocumentalAndGrupRelacionat_IdGrupGestorDocumental(grupId, grupRelacionatId)) {
                Grup grupRelacionat = grupRepository.findById(grupRelacionatId)
                        .orElseThrow(() -> new IllegalArgumentException("Grup relacionat no trobat"));
                GrupRelacio novaRelacio = new GrupRelacio();
                novaRelacio.setId(new GrupRelacioId(grupId, grupRelacionatId));
                novaRelacio.setGrup(grupPrincipal);
                novaRelacio.setGrupRelacionat(grupRelacionat);
                grupRelacioRepository.save(novaRelacio);
            }
        }

        return getGrupsRelacionats(grupId);
    }

    private GrupDto mapToDto(Grup grup) {
        GrupDto dto = new GrupDto();
        dto.setIdgrup(grup.getIdGrupGestorDocumental());
        dto.setCoreIdGrup(grup.getCoreIdGrup());
        dto.setIdGoogleSpreadsheet(grup.getIdGoogleSpreadsheet());
        dto.setFolderGoogleDrive(grup.getFolderGoogleDrive());
        dto.setCursGrup(grup.getCursGrup());
        return dto;
    }
}
