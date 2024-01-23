package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionRole;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionType;
import cat.politecnicllevant.gestsuitegestordocumental.dto.*;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import cat.politecnicllevant.gestsuitegestordocumental.service.*;
import com.google.api.services.drive.model.File;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RestController
public class FCTController {

    @Value("${app.allowed-users}")
    private String[] autoritzats;

    private final CoreRestClient coreRestClient;

    private final GoogleDriveService googleDriveService;

    private final DocumentService documentService;

    private final TipusDocumentService tipusDocumentService;

    private final SignaturaService signaturaService;

    private final DocumentSignaturaService documentSignaturaService;

    private final Gson gson;

    public FCTController(
            GoogleDriveService googleDriveService,
            CoreRestClient coreRestClient,
            DocumentService documentService,
            TipusDocumentService tipusDocumentService,
            SignaturaService signaturaService,
            DocumentSignaturaService documentSignaturaService,
            Gson gson
    ) {
        this.googleDriveService = googleDriveService;
        this.coreRestClient = coreRestClient;
        this.documentService = documentService;
        this.tipusDocumentService = tipusDocumentService;
        this.signaturaService = signaturaService;
        this.documentSignaturaService = documentSignaturaService;
        this.gson = gson;
    }


    @GetMapping("/autoritzats")
    public ResponseEntity<List<UsuariDto>> getAutoritzats() throws Exception {
        List<UsuariDto> usuarisAutoritzats = new ArrayList<>();

        for(String autoritzat: autoritzats){
            UsuariDto usuariAutoritzat = coreRestClient.getUsuariByEmail(autoritzat).getBody();
            System.out.println(usuariAutoritzat.getGsuiteEmail()+usuariAutoritzat.getGestibNom()+" "+usuariAutoritzat.getGestibCognom1()+" "+usuariAutoritzat.getGestibCognom2());
            usuarisAutoritzats.add(usuariAutoritzat);
        }
        return new ResponseEntity<>(usuarisAutoritzats, HttpStatus.OK);
    }

    @PostMapping("/documents")
    public ResponseEntity<List<DocumentDto>> getDocumentsByPath(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String path = jsonObject.get("path").getAsString();
        String email = jsonObject.get("email").getAsString();

        List<File> driveFiles = googleDriveService.getFilesInFolder(path,email);
        List<DocumentDto> documents = new ArrayList<>();
        for(File driveFile: driveFiles){

            System.out.println(driveFile);

            DocumentDto document = documentService.getDocumentByIdDriveGoogleDrive(driveFile.getId());

            if(document == null){
                document = documentService.getDocumentByGoogleDriveFile(driveFile);

                documents.add(document);
                //documentService.save(document);
            }
        }
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @PostMapping("/documents-no-traspassats")
    public ResponseEntity<List<DocumentDto>> getDocumentsNoTraspassatsByPath(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String path = jsonObject.get("path").getAsString();
        String email = jsonObject.get("email").getAsString();

        List<File> driveFiles = googleDriveService.getFilesInFolder(path,email);
        List<DocumentDto> documents = new ArrayList<>();
        for(File driveFile: driveFiles){

            System.out.println(driveFile);

            DocumentDto document = documentService.getDocumentByIdDriveGoogleDrive(driveFile.getId());

            if(document == null){
                document = documentService.getDocumentByGoogleDriveFile(driveFile);

                documents.add(document);
                //documentService.save(document);
            }
        }

        List<DocumentDto> documentsTraspassats = documentService.findAll();

        //Esborrem els documents trobats
        for(DocumentDto documentDto: documentsTraspassats){
            documents.removeIf(documentDto1 -> documentDto1.getNomOriginal().equals(documentDto.getNomOriginal()));
        }

        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @GetMapping("/documents-grup/{grupCodi}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByGrup(@PathVariable String grupCodi) throws Exception {
        List<DocumentDto> documents = documentService.findAllByGrupCodi(grupCodi);

        for(DocumentDto documentDto: documents){
            List<DocumentSignaturaDto> documentSignaturaDtos = documentSignaturaService.findByDocument(documentDto);
            documentDto.setDocumentSignatures(new HashSet<>(documentSignaturaDtos));
        }

        return new ResponseEntity<>(documents, HttpStatus.OK);
    }




    @PostMapping("/documents/crear-document")
    public ResponseEntity<DocumentDto> createDocument(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String idFile = jsonObject.get("idFile").getAsString();
        String path = jsonObject.get("path").getAsString();
        String emailUser = jsonObject.get("email").getAsString();
        String tipus = jsonObject.get("tipus").getAsString();
        String originalName = jsonObject.get("originalName").getAsString();
        String codiGrup = jsonObject.get("codiGrup").getAsString();

        JsonObject jsonObjectTipusDocument = jsonObject.get("tipusDocument").getAsJsonObject();
        Long idTipusDocument = jsonObjectTipusDocument.get("id").getAsLong();

        Long idUsuari = null;

        if(jsonObject.get("usuari")!=null && !jsonObject.get("usuari").isJsonNull()) {
            JsonObject jsonObjectUsuari = jsonObject.get("usuari").getAsJsonObject();
            idUsuari = jsonObjectUsuari.get("id").getAsLong();
        }


        File file = googleDriveService.getFileById(idFile,emailUser);

        DocumentDto document = documentService.getDocumentByOriginalName(originalName);

        if(document == null){
            document = new DocumentDto();
            document.setNomOriginal(originalName);

        }

        document.setTipusDocument(tipusDocumentService.getTipusDocumentById(idTipusDocument));
        document.setIdUsuari(idUsuari);
        document.setGrupCodi(codiGrup);
        document.setIdGoogleDrive(file.getId());
        document.setIdDriveGoogleDrive(file.getDriveId());
        document.setPathGoogleDrive(path);
        if(file.getOwners()!=null && !file.getOwners().isEmpty()) {
            document.setOwnerGoogleDrive(file.getOwners().get(0).getEmailAddress());
        }

        document.setMimeTypeGoogleDrive(file.getMimeType());

        if(file.getCreatedTime()!=null) {
            document.setCreatedTimeGoogleDrive(file.getCreatedTime().toString());
        }

        if(file.getModifiedTime()!=null) {
            document.setModifiedTimeGoogleDrive(file.getModifiedTime().toString());
        }

        TipusDocumentDto tipusDocumentDto = tipusDocumentService.getTipusDocumentByNom(tipus);
        if(tipusDocumentDto!=null) {
            document.setTipusDocument(tipusDocumentDto);
        }
        DocumentDto documentSaved = documentService.save(document);


        return new ResponseEntity<>(documentSaved, HttpStatus.OK);
    }


    @PostMapping("/crear-carpeta")
    public ResponseEntity<File> createFolder(@RequestBody String json){
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String folderName = jsonObject.get("folderName").getAsString();
        String email = jsonObject.get("email").getAsString();

        String parentFolderId = "root";
        if(jsonObject.get("parentFolderId")!=null && !jsonObject.get("parentFolderId").isJsonNull()) {
            parentFolderId = jsonObject.get("parentFolderId").getAsString();
        }

        File file = this.googleDriveService.createFolder(folderName,email,parentFolderId);

        if(jsonObject.get("administrators")!=null && !jsonObject.get("administrators").isJsonNull()) {
            JsonArray administrators = jsonObject.get("administrators").getAsJsonArray();
            for(JsonElement administrador: administrators){
                this.googleDriveService.assignPermission(file, PermissionType.USER, PermissionRole.WRITER, administrador.getAsString(),email);
            }
        }

        if(jsonObject.get("editors")!=null && !jsonObject.get("editors").isJsonNull()) {
            JsonArray editors = jsonObject.get("editors").getAsJsonArray();
            for (JsonElement editor : editors) {
                this.googleDriveService.assignPermission(file, PermissionType.USER, PermissionRole.FILE_ORGANIZER, editor.getAsString(), email);
            }
        }

        return new ResponseEntity<>(file, HttpStatus.OK);
    }

    @PostMapping("/copy")
    public ResponseEntity<File> copyFile(@RequestBody String json){
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String idFile = jsonObject.get("idFile").getAsString();
        String email = jsonObject.get("email").getAsString();
        String filename = jsonObject.get("filename").getAsString();
        String originalName = jsonObject.get("originalName").getAsString();

        String parentFolderId = "root";

        if(jsonObject.get("parentFolderId")!=null && !jsonObject.get("parentFolderId").isJsonNull()) {
            parentFolderId = jsonObject.get("parentFolderId").getAsString();
        }

        //Comprovem si ja existeix
        System.out.println("nom original: "+originalName);
        boolean existDocumentByOriginalName = this.documentService.existDocumentByOriginalName(originalName);

        if(existDocumentByOriginalName){
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

        //Si no existeix el copiem
        File file = this.googleDriveService.getFileById(idFile,email);

        if(jsonObject.get("administrators")!=null && !jsonObject.get("administrators").isJsonNull()) {
            JsonArray administrators = jsonObject.get("administrators").getAsJsonArray();
            for(JsonElement administrador: administrators){
                this.googleDriveService.assignPermission(file, PermissionType.USER, PermissionRole.WRITER, administrador.getAsString(),email);
            }
        }

        if(jsonObject.get("editors")!=null && !jsonObject.get("editors").isJsonNull()) {
            JsonArray editors = jsonObject.get("editors").getAsJsonArray();
            for (JsonElement editor : editors) {
                this.googleDriveService.assignPermission(file, PermissionType.USER, PermissionRole.FILE_ORGANIZER, editor.getAsString(), email);
            }
        }

        File fileCopy = this.googleDriveService.copy(file,email,filename,parentFolderId);

        return new ResponseEntity<>(fileCopy, HttpStatus.OK);
    }

    @GetMapping("/tipusdocument/list")
    public ResponseEntity<List<TipusDocumentDto>> getTipusDocumentList() throws Exception {
        List<TipusDocumentDto> tipusDocumentList = tipusDocumentService.findAll();
        return new ResponseEntity<>(tipusDocumentList, HttpStatus.OK);
    }

    @GetMapping("/signatura/list")
    public ResponseEntity<List<SignaturaDto>> getSignaturaList() throws Exception {
        List<SignaturaDto> signatures = signaturaService.findAll();
        return new ResponseEntity<>(signatures, HttpStatus.OK);
    }

    @PostMapping("/document/signar")
    public ResponseEntity<Notificacio> signarDocument(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        Long idDocument = jsonObject.get("idDocument").getAsLong();
        Long idSignatura = jsonObject.get("idSignatura").getAsLong();
        boolean signat = jsonObject.get("signat").getAsBoolean();

        DocumentDto document = documentService.getDocumentById(idDocument);
        SignaturaDto signatura = signaturaService.getSignaturaById(idSignatura);
        documentSignaturaService.signarDocument(document,signatura,signat);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Document signat correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }
}
