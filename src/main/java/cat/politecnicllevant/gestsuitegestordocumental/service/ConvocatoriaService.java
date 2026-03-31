package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Convocatoria;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ConvocatoriaCreateRequestDto;
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
    public ConvocatoriaDto create(ConvocatoriaCreateRequestDto request){
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
            convocatoria.setPathOrigen("FCT");
            convocatoria.setIsUnitatOrganitzativaOrigen(false);
            convocatoria.setPathDesti(convocatoriaDto.getPathDesti());
            convocatoria.setIsUnitatOrganitzativaDesti(true);
            convocatoria.setIsActual(Boolean.TRUE.equals(convocatoriaDto.getIsActual()));
            convocatoria.setIdCursAcademic(convocatoriaDto.getIdCursAcademic());

            Convocatoria convocatoriaSaved = convocatoriaRepository.save(convocatoria);

            if (Boolean.TRUE.equals(request.getDeleteOriginDocuments())) {
                try {
                    googleDriveService.deleteAllFilesInFolder("FCT", driveUserEmail);
                } catch (Exception ex) {
                    log.error("Error esborrant documents de la carpeta FCT", ex);
                }

                if (request.getSelectedQFempoFolders() != null) {
                    for (String folderName : request.getSelectedQFempoFolders()) {
                        try {
                            googleDriveService.deleteFolderByPath("FEMPO/" + folderName, driveUserEmail);
                        } catch (Exception ex) {
                            log.error("Error esborrant FEMPO/{}", folderName, ex);
                        }
                    }
                }
            }

            return modelMapper.map(convocatoriaSaved, ConvocatoriaDto.class);
        } catch (RuntimeException e) {
            compensateDriveChanges(previousFolderRenamed, newFolderCreated, oldPreviousPathDesti, effectivePreviousPathDesti, newFolderId);
            throw e;
        }
    }

    public List<String> listQFempoFolderNames() {
        try {
            List<File> folders = googleDriveService.getSubfoldersInFolder("FEMPO", "_Q_FEMPO", driveUserEmail);
            return folders.stream().map(File::getName).sorted().collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interromput mentre es llistaven les carpetes Q_FEMPO", e);
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
