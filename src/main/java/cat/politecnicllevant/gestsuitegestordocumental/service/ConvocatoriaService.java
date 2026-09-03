package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Convocatoria;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ConvocatoriaCreateRequestDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ConvocatoriaCreateResponseDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ConvocatoriaDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.ConvocatoriaRepository;
import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;
    private final ModelMapper modelMapper;
    private final GoogleDriveService googleDriveService;

    @Value("${app.google.drive.user.email}")
    private String driveUserEmail;

    @Value("${app.google.drive.user.path}")
    private String driveUserPathOrigen;

    @Value("${app.google.drive.user.pathdocsmigrats}")
    private String driveUserPathDocsMigrats;

    public List<ConvocatoriaDto> findAll(){
        return convocatoriaRepository.findAll().stream()
                .sorted(Comparator.comparing(Convocatoria::getIdConvocatoria).reversed())
                .map(e -> modelMapper.map(e,ConvocatoriaDto.class))
                .collect(Collectors.toList());
    }

    public ConvocatoriaDto findConvocatoriaById(Long id){
        Convocatoria c = convocatoriaRepository.findById(id).orElse(null);

        if(c == null){
            return null;
        }
        return modelMapper.map(c,ConvocatoriaDto.class);
    }

    public ConvocatoriaDto findConvocatoriaActual(){
        Convocatoria e = convocatoriaRepository.findByIsActualTrue();
        return modelMapper.map(e,ConvocatoriaDto.class);
    }

    public ConvocatoriaDto save(ConvocatoriaDto empresa){

        Convocatoria e = modelMapper.map(empresa, Convocatoria.class);
        if(Boolean.TRUE.equals(empresa.getIsActual())) {
            convocatoriaRepository.findAll().forEach(convocatoria -> {
                convocatoria.setIsActual(false);
                convocatoriaRepository.save(convocatoria);
            });
        }
        Convocatoria empresaSaved = convocatoriaRepository.save(e);

        return modelMapper.map(empresaSaved,ConvocatoriaDto.class);
    }

    @Transactional
    public ConvocatoriaCreateResponseDto create(ConvocatoriaCreateRequestDto request){
        ConvocatoriaDto convocatoriaDto = request.getConvocatoria();
        Convocatoria previousConvocatoria = null;
        String oldPreviousPathDesti = null;
        boolean applyDriveChanges = Boolean.TRUE.equals(request.getApplyDriveChanges());
        boolean previousFolderRenamed = false;
        boolean newFolderCreated = false;
        String previousFolderId = null;
        String newFolderId = null;
        String effectivePreviousPathDesti = request.getPreviousPathDesti();

        if(request.getPreviousConvocatoriaId() != null) {
            previousConvocatoria = convocatoriaRepository.findById(request.getPreviousConvocatoriaId()).orElse(null);
            if(previousConvocatoria != null) {
                oldPreviousPathDesti = previousConvocatoria.getPathDesti();
            }
        }

        try {
            if(applyDriveChanges && previousConvocatoria != null && request.getPreviousPathDesti() != null && !request.getPreviousPathDesti().isBlank()) {
                previousFolderId = googleDriveService.renameFolderInSharedDriveRoot(oldPreviousPathDesti, request.getPreviousPathDesti()).getId();
                previousFolderRenamed = true;
            }

            if(applyDriveChanges) {
                newFolderId = googleDriveService.createFolderInSharedDriveRoot(convocatoriaDto.getPathDesti()).getId();
                newFolderCreated = true;
            }

            if(applyDriveChanges && previousConvocatoria != null && previousFolderId != null && newFolderId != null) {
                googleDriveService.clonePermissionsBetweenFolderIds(previousFolderId, newFolderId);
            }

            if(Boolean.TRUE.equals(convocatoriaDto.getIsActual())) {
                convocatoriaRepository.findAll().forEach(convocatoria -> {
                    convocatoria.setIsActual(false);
                    convocatoriaRepository.save(convocatoria);
                });
            }

            if(request.getPreviousConvocatoriaId() != null) {
                if(previousConvocatoria != null) {
                    previousConvocatoria.setPathDesti(effectivePreviousPathDesti);
                    convocatoriaRepository.save(previousConvocatoria);
                }
            }

            Convocatoria convocatoria = new Convocatoria();
            convocatoria.setNom(convocatoriaDto.getNom());
            convocatoria.setPathOrigen(driveUserPathOrigen);
            convocatoria.setIsUnitatOrganitzativaOrigen(false);
            convocatoria.setPathDesti(convocatoriaDto.getPathDesti());
            convocatoria.setIsUnitatOrganitzativaDesti(true);
            convocatoria.setIsActual(Boolean.TRUE.equals(convocatoriaDto.getIsActual()));
            convocatoria.setIdCursAcademic(convocatoriaDto.getIdCursAcademic());

            Convocatoria convocatoriaSaved = convocatoriaRepository.save(convocatoria);

            ConvocatoriaCreateResponseDto response = new ConvocatoriaCreateResponseDto();
            response.setConvocatoria(modelMapper.map(convocatoriaSaved, ConvocatoriaDto.class));

            if (Boolean.TRUE.equals(request.getDeleteOriginDocuments())) {
                try {
                    response.setFitxersOrigenNoEsborrats(
                            googleDriveService.deleteAllFilesInFolder(driveUserPathOrigen, driveUserEmail));
                } catch (Exception ex) {
                    log.error("Error esborrant documents de la carpeta {}", driveUserPathOrigen, ex);
                    response.setFitxersOrigenNoEsborrats(-1);
                }

                if (request.getSelectedQFempoFolders() != null) {
                    for (String folderName : request.getSelectedQFempoFolders()) {
                        String path = driveUserPathDocsMigrats + "/" + folderName;
                        try {
                            int fallits = googleDriveService.emptyFolderByPathWithOwnerFallback(path, driveUserEmail);
                            if (fallits == 0) {
                                log.info("Carpeta {} buidada correctament", path);
                                response.getCarpetesBuidades().add(folderName);
                            } else {
                                log.error("No s'ha pogut buidar del tot la carpeta {}", path);
                                response.getCarpetesNoBuidades().add(folderName);
                            }
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            log.error("Interromput mentre es buidava {}", path, ex);
                            response.getCarpetesNoBuidades().add(folderName);
                        } catch (Exception ex) {
                            log.error("Error buidant {}", path, ex);
                            response.getCarpetesNoBuidades().add(folderName);
                        }
                    }
                }
            }

            aplicaNotificacio(response);

            return response;
        } catch (RuntimeException e) {
            compensateDriveChanges(previousFolderRenamed, newFolderCreated, oldPreviousPathDesti, effectivePreviousPathDesti, newFolderId);
            throw e;
        }
    }

    private void aplicaNotificacio(ConvocatoriaCreateResponseDto response) {
        int noBuidades = response.getCarpetesNoBuidades().size();
        int fitxersFallits = response.getFitxersOrigenNoEsborrats() != null ? response.getFitxersOrigenNoEsborrats() : 0;

        if (noBuidades == 0 && fitxersFallits == 0) {
            response.setNotifyType(NotificacioTipus.SUCCESS);
            response.setNotifyMessage("Convocatòria creada correctament");
            return;
        }

        StringBuilder missatge = new StringBuilder("Convocatòria creada, però no s'ha pogut buidar tot l'origen:");
        if (noBuidades > 0) {
            missatge.append(" carpetes ").append(String.join(", ", response.getCarpetesNoBuidades())).append(".");
        }
        if (fitxersFallits > 0) {
            missatge.append(" ").append(fitxersFallits).append(" fitxer/s de ").append(driveUserPathOrigen).append(".");
        } else if (fitxersFallits < 0) {
            missatge.append(" No s'han pogut llistar els fitxers de ").append(driveUserPathOrigen).append(".");
        }
        missatge.append(" Revisa els permisos a Google Drive.");

        response.setNotifyType(NotificacioTipus.WARNING);
        response.setNotifyMessage(missatge.toString());
    }

    public List<String> listQFempoFolderNames() {
        try {
            List<File> folders = googleDriveService.getSubfoldersInFolder(driveUserPathDocsMigrats, "_Q_FEMPO", driveUserEmail);
            return folders.stream().map(File::getName).sorted().collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interromput mentre es llistaven les carpetes Q_FEMPO", e);
        }
    }

    public void emptyQFempoFolders(List<String> folderNames) {
        try {
            googleDriveService.deleteAllFilesInFolder(driveUserPathOrigen, driveUserEmail);
            log.info("Fitxers de la carpeta {} esborrats correctament (test)", driveUserPathOrigen);
        } catch (Exception e) {
            log.error("Error esborrant fitxers de la carpeta {}", driveUserPathOrigen, e);
            throw new IllegalStateException("Error esborrant fitxers de la carpeta " + driveUserPathOrigen, e);
        }

        if (folderNames == null) return;
        for (String folderName : folderNames) {
            String path = driveUserPathDocsMigrats + "/" + folderName;
            try {
                if (googleDriveService.emptyFolderByPathWithOwnerFallback(path, driveUserEmail) != 0) {
                    throw new IllegalStateException("Error buidant la carpeta " + path);
                }
                log.info("Carpeta {} buidada correctament", path);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interromput mentre es buidava la carpeta " + path, e);
            } catch (Exception e) {
                log.error("Error buidant {}", path, e);
                throw new IllegalStateException("Error buidant la carpeta " + path, e);
            }
        }
    }

    @Transactional
    public void delete(Long id){
        convocatoriaRepository.deleteById(id);
    }

    private void compensateDriveChanges(boolean previousFolderRenamed, boolean newFolderCreated, String oldPreviousPathDesti, String newPreviousPathDesti, String newFolderId) {
        if (newFolderCreated) {
            try {
                googleDriveService.deleteFileByIdInSharedDrive(newFolderId);
            } catch (RuntimeException rollbackError) {
                throw new IllegalStateException("Ha fallat la creació/actualització de la convocatòria i també el rollback eliminant la carpeta nova amb id '" + newFolderId + "'", rollbackError);
            }
        }

        if (previousFolderRenamed) {
            try {
                googleDriveService.renameFolderInSharedDriveRoot(newPreviousPathDesti, oldPreviousPathDesti);
            } catch (RuntimeException rollbackError) {
                throw new IllegalStateException("Ha fallat la creació/actualització de la convocatòria i també el rollback reanomenant la carpeta anterior de '" + newPreviousPathDesti + "' a '" + oldPreviousPathDesti + "'", rollbackError);
            }
        }
    }
}
