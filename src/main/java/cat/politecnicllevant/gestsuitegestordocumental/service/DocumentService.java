package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.UsuariDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DocumentRepository;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import com.google.api.services.drive.model.File;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    public final DocumentRepository documentRepository;

    public final CoreRestClient coreRestClient;

    public DocumentService(
            DocumentRepository documentRepository,
            CoreRestClient coreRestClient
    ) {
        this.documentRepository = documentRepository;
        this.coreRestClient = coreRestClient;
    }

    public DocumentDto getDocumentById(Long id) {
        ModelMapper modelMapper = new ModelMapper();
        Document document = documentRepository.findById(id).orElse(null);
        return modelMapper.map(document,DocumentDto.class);
    }

    public DocumentDto getDocumentByIdDrive(String idDrive) {
        ModelMapper modelMapper = new ModelMapper();
        Document document =  documentRepository.findByIdDriveGoogleDrive(idDrive).orElse(null);

        if(document == null) return null;

        return modelMapper.map(document,DocumentDto.class);
    }

    public void save(DocumentDto documentDto){
        ModelMapper modelMapper = new ModelMapper();
        Document document = modelMapper.map(documentDto,Document.class);
        documentRepository.save(document);
    }

    public DocumentDto getDocumentByGoogleDriveFile(File driveFile,String path) throws Exception {
        DocumentDto document = new DocumentDto();

        document.setIdGoogleDrive(driveFile.getId());

        if(driveFile.getDriveId()!=null) {
            document.setIdDriveGoogleDrive(driveFile.getDriveId());
        }

        document.setNom(driveFile.getName());

        document.setMimeTypeGoogleDrive(driveFile.getMimeType());

        if(driveFile.getModifiedTime() != null) {
            document.setModifiedTimeGoogleDrive(driveFile.getModifiedTime().toString());
        }

        if(driveFile.getCreatedTime() != null) {
            document.setCreatedTimeGoogleDrive(driveFile.getCreatedTime().toString());
        }

        if(driveFile.getOwners() != null && !driveFile.getOwners().isEmpty()) {
            document.setOwnerGoogleDrive(driveFile.getOwners().get(0).getDisplayName());
            document.setIdUsuari(coreRestClient.getUsuariByEmail(driveFile.getOwners().get(0).getEmailAddress()).getBody().getIdusuari());
        }

        document.setPathGoogleDrive(path);
        return document;
    }


}
