package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.TipusDocumentDto;
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
import java.util.stream.Collectors;

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


    public List<DocumentDto> findAll(){
        ModelMapper modelMapper = new ModelMapper();
        return documentRepository.findAll().stream().map(d->modelMapper.map(d, DocumentDto.class)).collect(Collectors.toList());
    }

    public DocumentDto getDocumentById(Long id) {
        ModelMapper modelMapper = new ModelMapper();
        Document document = documentRepository.findById(id).orElse(null);
        return modelMapper.map(document,DocumentDto.class);
    }

    public DocumentDto getDocumentByOriginalName(String nom) {
        ModelMapper modelMapper = new ModelMapper();
        Document document = documentRepository.findByNomOriginal(nom).orElse(null);
        if(document == null) return null;
        return modelMapper.map(document,DocumentDto.class);
    }

    public boolean existDocumentByOriginalName(String nom) {
        ModelMapper modelMapper = new ModelMapper();
        Document document = documentRepository.findByNomOriginal(nom).orElse(null);
        return document != null;
    }

    public DocumentDto getDocumentByIdDriveGoogleDrive(String idDrive) {
        ModelMapper modelMapper = new ModelMapper();
        Document document =  documentRepository.findByIdDriveGoogleDrive(idDrive).orElse(null);

        if(document == null) return null;

        return modelMapper.map(document,DocumentDto.class);
    }

    public DocumentDto getDocumentByIdGoogleDrive(String id) {
        ModelMapper modelMapper = new ModelMapper();
        Document document =  documentRepository.findByIdGoogleDrive(id).orElse(null);

        if(document == null) return null;

        return modelMapper.map(document,DocumentDto.class);
    }

    public DocumentDto save(DocumentDto documentDto){
        ModelMapper modelMapper = new ModelMapper();
        Document document = modelMapper.map(documentDto,Document.class);

        //Comprovem si el document ja existeix i si és així el sobreescrivim
        documentRepository.findByNomOriginal(documentDto.getNomOriginal()).ifPresent(documentOriginal -> document.setIdDocument(documentOriginal.getIdDocument()));

        Document documentSaved = documentRepository.save(document);
        return modelMapper.map(documentSaved,DocumentDto.class);
    }

    public DocumentDto getDocumentByGoogleDriveFile(File driveFile) throws Exception {
        DocumentDto document = new DocumentDto();

        document.setIdGoogleDrive(driveFile.getId());

        if(driveFile.getDriveId()!=null) {
            document.setIdDriveGoogleDrive(driveFile.getDriveId());
        }

        document.setNomOriginal(driveFile.getName());

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

        return document;
    }


}
