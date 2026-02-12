package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.DocumentGeneral;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentGeneralDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DocumentGeneralRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DocumentGeneralService {

    private final DocumentGeneralRepository documentGeneralRepository;
    private final ModelMapper modelMapper;

    public List<DocumentGeneralDto> findAll() {
        return documentGeneralRepository.findAll()
                .stream()
                .map(documentGeneral -> modelMapper.map(documentGeneral, DocumentGeneralDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public CreateResult create(DocumentGeneralDto documentGeneralDto) {
        if (documentGeneralRepository.existsById(documentGeneralDto.getIdGoogleDrive())) {
            return CreateResult.CONFLICT;
        }

        DocumentGeneral documentGeneral = modelMapper.map(documentGeneralDto, DocumentGeneral.class);
        documentGeneralRepository.save(documentGeneral);
        return CreateResult.CREATED;
    }

    @Transactional
    public UpdateResult update(String oldIdGoogleDrive, DocumentGeneralDto documentGeneralDto) {
        if (!documentGeneralRepository.existsById(oldIdGoogleDrive)) {
            return UpdateResult.NOT_FOUND;
        }

        String newIdGoogleDrive = documentGeneralDto.getIdGoogleDrive();
        if (newIdGoogleDrive != null
                && !newIdGoogleDrive.equals(oldIdGoogleDrive)
                && documentGeneralRepository.existsById(newIdGoogleDrive)) {
            return UpdateResult.CONFLICT;
        }

        DocumentGeneral documentGeneral = modelMapper.map(documentGeneralDto, DocumentGeneral.class);
        documentGeneralRepository.save(documentGeneral);

        if (!oldIdGoogleDrive.equals(newIdGoogleDrive)) {
            documentGeneralRepository.deleteById(oldIdGoogleDrive);
        }

        return UpdateResult.UPDATED;
    }

    @Transactional
    public boolean deleteById(String idGoogleDrive) {
        if (!documentGeneralRepository.existsById(idGoogleDrive)) {
            return false;
        }
        documentGeneralRepository.deleteById(idGoogleDrive);
        return true;
    }

    public enum UpdateResult {
        UPDATED,
        NOT_FOUND,
        CONFLICT
    }

    public enum CreateResult {
        CREATED,
        CONFLICT
    }
}
