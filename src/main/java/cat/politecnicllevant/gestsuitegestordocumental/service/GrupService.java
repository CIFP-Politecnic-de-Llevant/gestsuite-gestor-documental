package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.domain.Grup;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.SincronitzacioGrupsResponseDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.GrupRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GrupService {

    private final GrupRepository grupRepository;
    private final ModelMapper modelMapper;


    public List<GrupDto> findAll() {
        List<GrupDto> grupDtos = new ArrayList<>();
        List<Grup> grups = grupRepository.findAll();

        for (Grup grup : grups) {
            GrupDto grupDto = new GrupDto();
            grupDto.setCoreIdGrup(grup.getCoreIdGrup());
            grupDto.setIdGoogleSpreadsheet(grup.getIdGoogleSpreadsheet());
            grupDto.setFolderGoogleDrive(grup.getFolderGoogleDrive());
            grupDto.setCursGrup(grup.getCursGrup());
            grupDto.setIdgrup(grup.getIdGrupGestorDocumental());
            grupDtos.add(grupDto);
        }

        return grupDtos;
    }

    /**
     * Grups que realment es poden fer servir per desar un formulari FEMPO: han de tenir
     * carpeta de Drive i full de càlcul configurats, que és el que exigeix el guardat.
     */
    public List<GrupDto> findAllWithFempo() {
        List<GrupDto> grupWithFempoDtos = new ArrayList<>();
        List<Grup> grupsWithFempo = grupRepository.findByIdGoogleSpreadsheetIsNotNull();
        for (Grup grup : grupsWithFempo) {
            if (!teConfiguracioFempo(grup)) {
                continue;
            }
            grupWithFempoDtos.add(mapToDto(grup));
        }

        return grupWithFempoDtos;
    }

    /**
     * El full de càlcul FEMPO és per cicle (tots els grups d'un cicle comparteixen el mateix), així que
     * un grup que existeix al core però no s'ha donat d'alta al gestor documental (p. ex. un grup F nou)
     * pot fer servir la configuració de qualsevol altre grup FEMPO del seu cicle.
     *
     * @param cicle codi del cicle sense la lletra del grup (p. ex. ADG32 per a ADG32F)
     */
    public GrupDto getFempoByCicle(String cicle) {
        return configuracioFempoDelCicle(grupRepository.findByIdGoogleSpreadsheetIsNotNull(), cicle)
                .map(GrupService::mapToDto)
                .orElse(null);
    }

    /**
     * Dona d'alta al gestor documental els grups actius del core que hi falten, copiant el full de càlcul i
     * la carpeta FEMPO d'un altre grup del seu cicle, i corregeix els grups que tenen un id de core que no és el seu.
     * Els grups dels cicles sense configuració FEMPO no es creen: s'han de configurar a mà.
     *
     * @param codisGrupsCore grups actius del core: id del grup al core -> codi del grup (p. ex. 72 -> ADG32F)
     */
    @Transactional
    public SincronitzacioGrupsResponseDto sincronitzarAmbCore(Map<Long, String> codisGrupsCore) {
        SincronitzacioGrupsResponseDto resultat = new SincronitzacioGrupsResponseDto();
        List<Grup> grups = grupRepository.findAll();

        Map<String, Long> idCorePerCodi = new HashMap<>();
        codisGrupsCore.forEach((idCore, codi) -> idCorePerCodi.put(codi, idCore));

        // Quants grups tindran cada id de core un cop sincronitzats: findByCoreIdGrup falla si n'hi ha de repetits
        Map<Long, Long> usosIdCore = grups.stream()
                .collect(Collectors.groupingBy(
                        grup -> idCorePerCodi.getOrDefault(grup.getCursGrup(), grup.getCoreIdGrup()),
                        Collectors.counting()));

        List<Grup> modificats = new ArrayList<>();
        Set<String> codisExistents = new HashSet<>();

        List<Grup> grupsOrdenats = new ArrayList<>(grups);
        grupsOrdenats.sort(Comparator.comparing(Grup::getCursGrup, Comparator.nullsLast(Comparator.naturalOrder())));
        for (Grup grup : grupsOrdenats) {
            codisExistents.add(grup.getCursGrup());
            Long idCore = idCorePerCodi.get(grup.getCursGrup());
            if (idCore == null || idCore.equals(grup.getCoreIdGrup())) {
                continue;
            }
            if (usosIdCore.get(idCore) > 1) {
                resultat.getConflictes().add(grup.getCursGrup());
                continue;
            }
            grup.setCoreIdGrup(idCore);
            modificats.add(grup);
            resultat.getCorregits().add(grup.getCursGrup());
        }

        List<Map.Entry<Long, String>> grupsCoreOrdenats = new ArrayList<>(codisGrupsCore.entrySet());
        grupsCoreOrdenats.sort(Map.Entry.comparingByValue());
        for (Map.Entry<Long, String> grupCore : grupsCoreOrdenats) {
            Long idCore = grupCore.getKey();
            String codi = grupCore.getValue();
            if (codisExistents.contains(codi)) {
                continue;
            }
            if (usosIdCore.containsKey(idCore)) {
                resultat.getConflictes().add(codi);
                continue;
            }

            String cicle = codi.length() > 1 ? codi.substring(0, codi.length() - 1) : "";
            Optional<Grup> grupDelCicle = configuracioFempoDelCicle(grups, cicle);
            if (grupDelCicle.isEmpty()) {
                resultat.getSenseConfiguracioFempo().add(codi);
                continue;
            }

            Grup nou = new Grup();
            nou.setCursGrup(codi);
            nou.setCoreIdGrup(idCore);
            nou.setIdGoogleSpreadsheet(grupDelCicle.get().getIdGoogleSpreadsheet());
            nou.setFolderGoogleDrive(grupDelCicle.get().getFolderGoogleDrive());
            nou.setActiu(true);
            modificats.add(nou);
            usosIdCore.put(idCore, 1L);
            resultat.getCreats().add(codi);
        }

        if (!modificats.isEmpty()) {
            grupRepository.saveAll(modificats);
        }

        notificarSincronitzacio(resultat);
        return resultat;
    }

    private static void notificarSincronitzacio(SincronitzacioGrupsResponseDto resultat) {
        List<String> missatge = new ArrayList<>();
        if (resultat.getCreats().isEmpty() && resultat.getCorregits().isEmpty()) {
            missatge.add("No hi ha cap grup per crear ni corregir.");
        }
        if (!resultat.getCreats().isEmpty()) {
            missatge.add("Grups creats: " + String.join(", ", resultat.getCreats()) + ".");
        }
        if (!resultat.getCorregits().isEmpty()) {
            missatge.add("Grups amb l'id del core corregit: " + String.join(", ", resultat.getCorregits()) + ".");
        }
        if (!resultat.getConflictes().isEmpty()) {
            missatge.add("No s'han tocat perquè el seu id del core ja el té un altre grup: " + String.join(", ", resultat.getConflictes()) + ".");
        }
        if (!resultat.getSenseConfiguracioFempo().isEmpty()) {
            missatge.add("No s'han creat perquè el seu cicle no té FEMPO configurat: " + String.join(", ", resultat.getSenseConfiguracioFempo()) + ".");
        }
        resultat.setNotifyMessage(String.join(" ", missatge));

        if (!resultat.getConflictes().isEmpty()) {
            resultat.setNotifyType(NotificacioTipus.WARNING);
        } else if (!resultat.getCreats().isEmpty() || !resultat.getCorregits().isEmpty()) {
            resultat.setNotifyType(NotificacioTipus.SUCCESS);
        } else {
            resultat.setNotifyType(NotificacioTipus.INFO);
        }
    }

    /**
     * Grup amb configuració FEMPO d'un cicle: el codi del grup és el cicle més una lletra (ADG32 -> ADG32A).
     */
    private static Optional<Grup> configuracioFempoDelCicle(List<Grup> grups, String cicle) {
        if (!StringUtils.hasText(cicle)) {
            return Optional.empty();
        }

        return grups.stream()
                .filter(GrupService::teConfiguracioFempo)
                .filter(grup -> grup.getCursGrup() != null
                        && grup.getCursGrup().length() == cicle.length() + 1
                        && grup.getCursGrup().startsWith(cicle))
                .findFirst();
    }

    private static boolean teConfiguracioFempo(Grup grup) {
        return !Boolean.FALSE.equals(grup.getActiu())
                && StringUtils.hasText(grup.getIdGoogleSpreadsheet())
                && StringUtils.hasText(grup.getFolderGoogleDrive());
    }

    public GrupDto getById(long id) {
        return modelMapper.map(grupRepository.findById(id).orElse(null), GrupDto.class);
    }

    public GrupDto getByIdGrupCore(long id) {
        Grup grup = grupRepository.findByCoreIdGrup(id);
        if (grup != null) {
            GrupDto grupDto = new GrupDto();
            grupDto.setCoreIdGrup(grup.getCoreIdGrup());
            grupDto.setIdGoogleSpreadsheet(grup.getIdGoogleSpreadsheet());
            grupDto.setFolderGoogleDrive(grup.getFolderGoogleDrive());
            grupDto.setCursGrup(grup.getCursGrup());
            grupDto.setIdgrup(grup.getIdGrupGestorDocumental());
            return grupDto;
        }
        return null;
    }

    public GrupDto getByCursGrup(String cursGrup) {
        Grup grup = grupRepository.findByCursGrup(cursGrup);
        if (grup == null) return null;
        return mapToDto(grup);
    }

    public static GrupDto mapToDto(Grup grup) {
        GrupDto dto = new GrupDto();
        dto.setIdgrup(grup.getIdGrupGestorDocumental());
        dto.setCoreIdGrup(grup.getCoreIdGrup());
        dto.setIdGoogleSpreadsheet(grup.getIdGoogleSpreadsheet());
        dto.setFolderGoogleDrive(grup.getFolderGoogleDrive());
        dto.setCursGrup(grup.getCursGrup());
        dto.setActiu(grup.getActiu());
        return dto;
    }
}
