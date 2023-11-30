package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionRole;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionType;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.TipusDocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.UsuariDto;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import cat.politecnicllevant.gestsuitegestordocumental.service.DocumentService;
import cat.politecnicllevant.gestsuitegestordocumental.service.GoogleDriveService;
import cat.politecnicllevant.gestsuitegestordocumental.service.TipusDocumentService;
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
import java.util.List;

@RestController
public class FCTController {

    @Value("${app.allowed-users}")
    private String[] autoritzats;

    private final CoreRestClient coreRestClient;

    private final GoogleDriveService googleDriveService;

    private final DocumentService documentService;

    private final TipusDocumentService tipusDocumentService;

    private final Gson gson;

    public FCTController(
            GoogleDriveService googleDriveService,
            CoreRestClient coreRestClient,
            DocumentService documentService,
            TipusDocumentService tipusDocumentService,
            Gson gson
    ) {
        this.googleDriveService = googleDriveService;
        this.coreRestClient = coreRestClient;
        this.documentService = documentService;
        this.tipusDocumentService = tipusDocumentService;
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

            DocumentDto document = documentService.getDocumentByIdDrive(driveFile.getId());

            if(document == null){
                document = documentService.getDocumentByGoogleDriveFile(driveFile);

                documents.add(document);
                //documentService.save(document);
            }
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

        File file = googleDriveService.getFileById(idFile,emailUser);

        DocumentDto document = documentService.getDocumentByGoogleDriveFile(file);
        DocumentDto documentSaved = null;
        if(document!=null) {
            document.setIdGoogleDrive(file.getId());
            document.setIdDriveGoogleDrive(file.getDriveId());
            document.setPathGoogleDrive(path);


            TipusDocumentDto tipusDocumentDto = tipusDocumentService.getTipusDocumentByNom(tipus);
            if(tipusDocumentDto!=null) {
                document.setTipusDocument(tipusDocumentDto);
            }
            documentSaved = documentService.save(document);
        }

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

        String parentFolderId = "root";

        if(jsonObject.get("parentFolderId")!=null && !jsonObject.get("parentFolderId").isJsonNull()) {
            parentFolderId = jsonObject.get("parentFolderId").getAsString();
        }

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
}
