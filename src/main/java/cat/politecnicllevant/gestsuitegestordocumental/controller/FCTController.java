package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionRole;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionType;
import cat.politecnicllevant.gestsuitegestordocumental.domain.TipusDocument;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.UsuariDto;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import cat.politecnicllevant.gestsuitegestordocumental.service.DocumentService;
import cat.politecnicllevant.gestsuitegestordocumental.service.GoogleDriveService;
import com.google.api.services.drive.model.File;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class FCTController {

    @Value("${app.allowed-users}")
    private String[] autoritzats;

    private final CoreRestClient coreRestClient;

    private final GoogleDriveService googleDriveService;

    private final DocumentService documentService;

    private final Gson gson;

    public FCTController(
            GoogleDriveService googleDriveService,
            CoreRestClient coreRestClient,
            DocumentService documentService,
            Gson gson
    ) {
        this.googleDriveService = googleDriveService;
        this.coreRestClient = coreRestClient;
        this.documentService = documentService;
        this.gson = gson;
    }

    @GetMapping("/prova")
    public void prova() throws GeneralSecurityException, IOException {
        //this.googleDriveService.prova();
        this.googleDriveService.createFolder("","Test","jgalmes1@politecnicllevant.cat");
        this.googleDriveService.createFolder("Test","prova inside","jgalmes1@politecnicllevant.cat");
        this.googleDriveService.createFolder("Test/prova inside","prova inside 2","jgalmes1@politecnicllevant.cat");
        this.googleDriveService.createFolder("Test/prova inside","prova inside 3","jgalmes1@politecnicllevant.cat");
        List<File> files = this.googleDriveService.getFilesInFolder("Test/prova inside/prova inside 2","jgalmes1@politecnicllevant.cat");
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:"+files.size());
            for (File file : files) {
                this.googleDriveService.assignPermission(file, PermissionType.USER, PermissionRole.WRITER, "jgalmes@politecnicllevant.cat","jgalmes1@politecnicllevant.cat");
                System.out.println("name: "+file.getName());
                System.out.println("id: "+file.getId());
            }
        }
    }


    @GetMapping("/autoritzats")
    public List<UsuariDto> getAutoritzats() throws Exception {
        List<UsuariDto> usuarisAutoritzats = new ArrayList<>();

        for(String autoritzat: autoritzats){
            UsuariDto usuariAutoritzat = coreRestClient.getUsuariByEmail(autoritzat).getBody();
            System.out.println(usuariAutoritzat.getGsuiteEmail()+usuariAutoritzat.getGestibNom()+" "+usuariAutoritzat.getGestibCognom1()+" "+usuariAutoritzat.getGestibCognom2());
            usuarisAutoritzats.add(usuariAutoritzat);
        }
        return usuarisAutoritzats;
    }

    @PostMapping("/documents")
    public List<DocumentDto> getDocumentsByPath(@RequestBody String json){
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String path = jsonObject.get("path").getAsString();
        String email = jsonObject.get("email").getAsString();

        List<File> driveFiles = googleDriveService.getFilesInFolder(path,email);
        List<DocumentDto> documents = new ArrayList<>();
        for(File driveFile: driveFiles){

            DocumentDto document = documentService.getDocumentByIdDrive(driveFile.getId());

            if(document == null){
                document = new DocumentDto();
                document.setIdDrive(driveFile.getId());
                document.setNom(driveFile.getName());
                document.setMimeType(driveFile.getMimeType());
                document.setModifiedTime(driveFile.getModifiedTime().toString());
                document.setCreatedTime(driveFile.getCreatedTime().toString());
                document.setOwner(driveFile.getOwners().get(0).getDisplayName());
                document.setPath(path);
                documentService.save(document);
            }
        }
        return documents;
    }
}
