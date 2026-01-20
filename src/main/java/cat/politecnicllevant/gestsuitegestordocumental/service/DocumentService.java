package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Convocatoria;
import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import cat.politecnicllevant.gestsuitegestordocumental.domain.DocumentGeneral;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ConvocatoriaDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentGeneralDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.google.FitxerBucketDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.ConvocatoriaRepository;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DocumentGeneralRepository;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DocumentRepository;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import com.google.api.services.drive.model.File;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ConvocatoriaRepository convocatoriaRepository;
    private final DocumentGeneralRepository documentGeneralRepository;
    private final ModelMapper modelMapper;

    public final CoreRestClient coreRestClient;

    @Value("${public.password}")
    private String publicPassword;


    public List<DocumentDto> findAll(ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);

        return documentRepository.findAllByConvocatoria(convocatoriaEntity)
                .stream()
                .map(d -> modelMapper.map(d, DocumentDto.class))
                .collect(Collectors.toList());
    }

    public List<DocumentDto> findAllIncludingEliminats(ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);

        return documentRepository.findAllByConvocatoria(convocatoriaEntity)
                .stream()
                .map(d -> modelMapper.map(d, DocumentDto.class))
                .collect(Collectors.toList());
    }

    public List<DocumentDto> findAllDocumentsBucketNoTraspassats(ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);

        return documentRepository.findAllByIdFitxerIsNotNullAndTraspassatIsFalseAndConvocatoria(convocatoriaEntity)
                .stream()
                .map(d -> modelMapper.map(d, DocumentDto.class))
                .collect(Collectors.toList());
    }

    public List<DocumentDto> findAllByGrupCodi(String grupCodi, ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);
        return documentRepository.findAllByGrupCodiAndConvocatoria(grupCodi, convocatoriaEntity)
                .stream()
                .map(d -> modelMapper.map(d, DocumentDto.class))
                .collect(Collectors.toList());
    }

    public DocumentDto getDocumentById(Long id, ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);
        Document document = documentRepository.findByIdDocumentAndConvocatoria(id, convocatoriaEntity).orElse(null);
        if (document == null) {
            return null;
        }
        return modelMapper.map(document, DocumentDto.class);
    }

    public DocumentDto getDocumentByOriginalName(String nom, ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);
        Document document = documentRepository.findByNomOriginalAndConvocatoria(nom, convocatoriaEntity).orElse(null);
        if (document == null) return null;
        return modelMapper.map(document, DocumentDto.class);
    }

    public boolean existDocumentByOriginalName(String nom, ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);
        Document document = documentRepository.findByNomOriginalAndConvocatoria(nom, convocatoriaEntity).orElse(null);
        return document != null;
    }

    public DocumentDto getDocumentByIdDriveGoogleDrive(String idDrive, ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);
        Document document = documentRepository.findByIdDriveGoogleDriveAndConvocatoria(idDrive, convocatoriaEntity).orElse(null);

        if (document == null) return null;

        return modelMapper.map(document, DocumentDto.class);
    }

    public DocumentDto getDocumentByIdGoogleDrive(String id, ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);
        Document document = documentRepository.findByIdGoogleDriveAndConvocatoria(id, convocatoriaEntity).orElse(null);

        if (document == null) return null;

        return modelMapper.map(document, DocumentDto.class);
    }

    @Transactional
    public DocumentDto save(DocumentDto documentDto, ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);

        Document document = modelMapper.map(documentDto, Document.class);
        document.setConvocatoria(convocatoriaEntity);

        Document documentSaved = documentRepository.save(document);
        return modelMapper.map(documentSaved, DocumentDto.class);
    }

    public DocumentDto findByNomOriginal(String nomOriginal, ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);
        Document document = documentRepository.findByNomOriginalAndConvocatoria(nomOriginal, convocatoriaEntity).orElse(null);
        if (document == null) return null;
        return modelMapper.map(document, DocumentDto.class);
    }

    @Transactional
    public void deleteByIdDocument(Long idDocument, ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);
        Document document = documentRepository.findByIdDocumentAndConvocatoria(idDocument, convocatoriaEntity).orElse(null);
        if (document == null) {
            return;
        }
        documentRepository.delete(document);
    }

    @Transactional
    public void deleteAllByIdUsuari(Long idusuari, ConvocatoriaDto convocatoria) {
        Convocatoria convocatoriaEntity = modelMapper.map(convocatoria, Convocatoria.class);
        List<Document> documents = documentRepository.findAllByIdUsuariAndConvocatoria(idusuari, convocatoriaEntity);
        documentRepository.deleteAll(documents);
    }

    public DocumentDto getDocumentByGoogleDriveFile(File driveFile, ConvocatoriaDto convocatoria) throws Exception {
        DocumentDto document = new DocumentDto();

        document.setIdGoogleDrive(driveFile.getId());

        if (driveFile.getDriveId() != null) {
            document.setIdDriveGoogleDrive(driveFile.getDriveId());
        }

        document.setNomOriginal(driveFile.getName());

        document.setMimeTypeGoogleDrive(driveFile.getMimeType());

        if (driveFile.getModifiedTime() != null) {
            document.setModifiedTimeGoogleDrive(driveFile.getModifiedTime().toString());
        }

        if (driveFile.getCreatedTime() != null) {
            document.setCreatedTimeGoogleDrive(driveFile.getCreatedTime().toString());
        }

        if (driveFile.getOwners() != null && !driveFile.getOwners().isEmpty()) {
            document.setOwnerGoogleDrive(driveFile.getOwners().get(0).getDisplayName());
            try {
                document.setIdUsuari(coreRestClient.getUsuariByEmail(driveFile.getOwners().get(0).getEmailAddress()).getBody().getIdusuari());
            } catch (Exception e) {
                String token = coreRestClient.getToken(publicPassword).getBody();
                document.setIdUsuari(coreRestClient.getUsuariByEmailSystem(driveFile.getOwners().get(0).getEmailAddress(), token).getBody().getIdusuari());
            }
        }

        document.setConvocatoria(convocatoria);

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

    public List<DocumentGeneralDto> findAllDocumentsGenerals() {
        List<DocumentGeneral> docs = documentGeneralRepository.findAll();
        return docs.stream().map(d -> modelMapper.map(d, DocumentGeneralDto.class)).collect(Collectors.toList());
    }
}
