package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import cat.politecnicllevant.gestsuitegestordocumental.domain.DocumentSignatura;
import cat.politecnicllevant.gestsuitegestordocumental.dto.*;
import cat.politecnicllevant.gestsuitegestordocumental.dto.google.FitxerBucketDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DocumentRepository;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DocumentSignaturaRepository;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import com.google.api.services.drive.model.File;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentService {

    public final DocumentRepository documentRepository;
    public final DocumentSignaturaRepository documentSignaturaRepository;

    public final CoreRestClient coreRestClient;

    @Value("${public.password}")
    private String publicPassword;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentSignaturaRepository documentSignaturaRepository,
            CoreRestClient coreRestClient
    ) {
        this.documentRepository = documentRepository;
        this.documentSignaturaRepository = documentSignaturaRepository;
        this.coreRestClient = coreRestClient;
    }


    public List<DocumentDto> findAll(){
        ModelMapper modelMapper = new ModelMapper();
        return documentRepository.findAll().stream().map(d->modelMapper.map(d, DocumentDto.class)).collect(Collectors.toList());
    }

    public List<DocumentDto> findAllByGrupCodi(String grupCodi){
        ModelMapper modelMapper = new ModelMapper();
        return documentRepository.findAllByGrupCodi(grupCodi).stream().map(d->modelMapper.map(d, DocumentDto.class)).collect(Collectors.toList());
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

        Document documentSaved = documentRepository.save(document);
        return modelMapper.map(documentSaved,DocumentDto.class);
    }

    public DocumentDto findByNomOriginal(String nomOriginal) {
        ModelMapper modelMapper = new ModelMapper();
        Document document = documentRepository.findByNomOriginal(nomOriginal).orElse(null);
        return modelMapper.map(document,DocumentDto.class);
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
            try {
                document.setIdUsuari(coreRestClient.getUsuariByEmail(driveFile.getOwners().get(0).getEmailAddress()).getBody().getIdusuari());
            } catch (Exception e) {
                String token = coreRestClient.getToken(publicPassword).getBody();
                document.setIdUsuari(coreRestClient.getUsuariByEmailSystem(driveFile.getOwners().get(0).getEmailAddress(),token).getBody().getIdusuari());
            }
        }

        return document;
    }

    public String getURLBucket(Long idFitxerBucket) throws IOException {
        ResponseEntity<FitxerBucketDto> fitxerBucketResponse = coreRestClient.getFitxerBucketById(idFitxerBucket);
        FitxerBucketDto fitxerBucket = fitxerBucketResponse.getBody();

        if (fitxerBucket != null) {
            JsonObject jsonFitxerBucket = new JsonObject();
            jsonFitxerBucket.addProperty("idfitxer", fitxerBucket.getIdfitxer());
            jsonFitxerBucket.addProperty("nom", fitxerBucket.getNom());
            jsonFitxerBucket.addProperty("bucket", fitxerBucket.getBucket());
            jsonFitxerBucket.addProperty("path", fitxerBucket.getPath());

            ResponseEntity<String> urlResponse = coreRestClient.generateSignedURL(jsonFitxerBucket.toString());
            String url = urlResponse.getBody();

            //fitxerBucket.setUrl(url);
            return url;
        }

        return null;
    }

}
