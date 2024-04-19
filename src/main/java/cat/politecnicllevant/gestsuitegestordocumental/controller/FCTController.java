package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionRole;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionType;
import cat.politecnicllevant.gestsuitegestordocumental.dto.*;
import cat.politecnicllevant.gestsuitegestordocumental.dto.google.FitxerBucketDto;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import cat.politecnicllevant.gestsuitegestordocumental.service.*;
import com.ctc.wstx.evt.WstxEventReader;
import com.google.api.services.drive.model.File;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Slf4j
public class FCTController {

    private final CoreRestClient coreRestClient;

    private final GoogleDriveService googleDriveService;

    private final DocumentService documentService;

    private final TipusDocumentService tipusDocumentService;

    private final SignaturaService signaturaService;

    private final DocumentSignaturaService documentSignaturaService;

    private final AlumneService alumneService;

    private final Gson gson;

    @Value("${app.allowed-users}")
    private String[] autoritzats;

    @Value("${server.tmp}")
    private String tmpPath;

    @Value("${gc.storage.bucketnamedata}")
    private String bucketName;

    @Value("${gc.storage.convalidacions.path-files}")
    private String bucketPathFiles;

    @Value("${app.google.drive.user.email}")
    private String userEmail;

    @Value("${app.environment}")
    private String environment;

    @Value("${app.google.drive.shared.id}")
    private String sharedDriveId;

    private String userPath;
    private String sharedDrivePath;

    public FCTController(
            GoogleDriveService googleDriveService,
            CoreRestClient coreRestClient,
            DocumentService documentService,
            TipusDocumentService tipusDocumentService,
            SignaturaService signaturaService,
            DocumentSignaturaService documentSignaturaService,
            AlumneService alumneService,
            Gson gson
    ) {
        this.googleDriveService = googleDriveService;
        this.coreRestClient = coreRestClient;
        this.documentService = documentService;
        this.tipusDocumentService = tipusDocumentService;
        this.signaturaService = signaturaService;
        this.documentSignaturaService = documentSignaturaService;
        this.alumneService = alumneService;
        this.gson = gson;
    }

    @PostConstruct
    private void postConstruct() {
        if(environment.equals("dev")){
            userPath = "FCT JOAN";
            sharedDrivePath = "FCT JOAN RESOLT àèèòñ";
        } else {
            userPath = "FCT";
            sharedDrivePath = "Curs Actual/0206 FCT i FP Dual/Documentació FCT alumnes 23-24/Documentació en tràmit";
        }
    }





    /*
Second Minute Hour Day-of-Month
second, minute, hour, day(1-31), month(1-12), weekday(1-7) SUN-SAT
0 0 2 * * * = a les 2AM de cada dia
 */
    //@Scheduled(cron = "0 0 * * * *")
    @Scheduled(fixedRate = 60*60*1000, initialDelay = 60*1000)
    public void sincronitzaDocumentsAutomatic() throws Exception {
        log.info("Sincronitzant documents...");

        final String path = userPath;
        final String email = userEmail;
        final String FOLDER_BASE = sharedDrivePath;
        final String APP_SHAREDDRIVE_GESTORDOCUMENTAL=sharedDriveId;


        List<File> driveFiles = googleDriveService.getFilesInFolder(path,email);
        List<DocumentDto> documents = new ArrayList<>();
        for(File driveFile: driveFiles){

            //System.out.println(driveFile);

            DocumentDto document = documentService.getDocumentByIdDriveGoogleDrive(driveFile.getId());

            if(document == null){
                document = documentService.getDocumentByGoogleDriveFile(driveFile);
                document.setEstat(DocumentEstatDto.PENDENT_SIGNATURES);

                documents.add(document);
                //documentService.save(document);
            }
        }

        List<DocumentDto> documentsNoTraspassats = documentService.findAll();

        //Esborrem els documents trobats
        for(DocumentDto documentDto: documentsNoTraspassats){
            documents.removeIf(documentDto1 -> documentDto1.getNomOriginal().equals(documentDto.getNomOriginal()));
        }

        //Traspassam els documents
        for(DocumentDto document: documents){
            String[] documentParts = document.getNomOriginal().split("_");

            if(documentParts.length==2){
                String cicle = documentParts[0];
                String nomDocument = documentParts[1];

                TipusDocumentDto tipusDocumentDto = tipusDocumentService.getTipusDocumentByNom(nomDocument);

                if(tipusDocumentDto==null){
                    continue;
                }

                //Permisos
                List<UsuariDto> tutorsFCT = coreRestClient.getTutorFCTByCodiGrup(cicle).getBody();

                JsonObject jsonCarpetaRoot = new JsonObject();
                jsonCarpetaRoot.addProperty("folderName", FOLDER_BASE);
                jsonCarpetaRoot.addProperty("email", email);
                jsonCarpetaRoot.addProperty("parentFolderId", APP_SHAREDDRIVE_GESTORDOCUMENTAL);

                File carpetaRoot = this.createFolder(gson.toJson(jsonCarpetaRoot)).getBody();

                JsonObject jsonCarpetaCicle = new JsonObject();
                //jsonCarpetaRoot.addProperty("path", FOLDER_BASE);
                jsonCarpetaCicle.addProperty("folderName", cicle);
                jsonCarpetaCicle.addProperty("email", email);

                JsonArray jsonEditorsCarpetaRoot = new JsonArray();
                tutorsFCT.forEach(usuariDto -> {
                    jsonEditorsCarpetaRoot.add(usuariDto.getGsuiteEmail());
                });
                jsonCarpetaCicle.add("editors", jsonEditorsCarpetaRoot);

                jsonCarpetaCicle.addProperty("parentFolderId", carpetaRoot.getId());

                File carpetaCicle = this.createFolder(gson.toJson(jsonCarpetaCicle)).getBody();

                JsonObject jsonFitxer = new JsonObject();
                jsonFitxer.addProperty("idFile", document.getIdGoogleDrive());
                jsonFitxer.addProperty("email", email);
                jsonFitxer.addProperty("filename", nomDocument);
                jsonFitxer.addProperty("parentFolderId", carpetaCicle.getId());
                jsonFitxer.addProperty("originalName", document.getNomOriginal());

                File fitxer = this.copyFile(gson.toJson(jsonFitxer)).getBody();

                //Desem el fitxer NOMÉS si no existeix
                DocumentDto documentSaved = documentService.getDocumentByOriginalName(document.getNomOriginal());
                if(documentSaved == null) {
                    JsonObject tipusFitxer = new JsonObject();
                    tipusFitxer.addProperty("id", tipusDocumentDto.getIdTipusDocument());

                    JsonObject jsonFitxerDesat = new JsonObject();
                    jsonFitxerDesat.addProperty("idFile", document.getIdGoogleDrive());
                    jsonFitxerDesat.addProperty("path", FOLDER_BASE + "/" + cicle + "/" + nomDocument);
                    jsonFitxerDesat.addProperty("email", email);
                    jsonFitxerDesat.addProperty("tipus", nomDocument);
                    jsonFitxerDesat.addProperty("originalName", document.getNomOriginal());
                    jsonFitxerDesat.add("tipusDocument", tipusFitxer);
                    jsonFitxerDesat.addProperty("codiGrup", cicle);

                    this.createDocument(gson.toJson(jsonFitxerDesat));
                }

            } else if(documentParts.length==5){
                String cicle = documentParts[0];
                String cognoms = documentParts[1];
                String nom = documentParts[2];
                String numExpedient = documentParts[3];
                String nomDocument = documentParts[4];

                TipusDocumentDto tipusDocumentDto = tipusDocumentService.getTipusDocumentByNom(nomDocument);
                UsuariDto alumne = coreRestClient.getUsuariByNumExpedient(numExpedient).getBody();

                if(tipusDocumentDto==null || alumne==null){
                    continue;
                }

                //Permisos
                List<UsuariDto> tutorsFCT = this.coreRestClient.getTutorFCTByCodiGrup(cicle).getBody();

                //Creem l'estructura de carpetes
                JsonObject jsonCarpetaRoot = new JsonObject();
                jsonCarpetaRoot.addProperty("folderName", FOLDER_BASE);
                jsonCarpetaRoot.addProperty("email", email);
                jsonCarpetaRoot.addProperty("parentFolderId", APP_SHAREDDRIVE_GESTORDOCUMENTAL);

                File carpetaRoot = this.createFolder(gson.toJson(jsonCarpetaRoot)).getBody();

                JsonObject jsonCarpetaCicle = new JsonObject();
                //jsonCarpetaRoot.addProperty("path", FOLDER_BASE);
                jsonCarpetaCicle.addProperty("folderName", cicle);
                jsonCarpetaCicle.addProperty("email", email);

                JsonArray jsonEditorsCarpetaRoot = new JsonArray();
                tutorsFCT.forEach(usuariDto -> {
                    jsonEditorsCarpetaRoot.add(usuariDto.getGsuiteEmail());
                });
                jsonCarpetaCicle.add("editors", jsonEditorsCarpetaRoot);

                jsonCarpetaCicle.addProperty("parentFolderId", carpetaRoot.getId());

                File carpetaCicle = this.createFolder(gson.toJson(jsonCarpetaCicle)).getBody();

                JsonObject jsonCarpetaAlumne = new JsonObject();
                jsonCarpetaAlumne.addProperty("folderName", cognoms+" "+nom);
                jsonCarpetaAlumne.addProperty("email", email);
                jsonCarpetaAlumne.addProperty("parentFolderId", carpetaCicle.getId());

                File carpetaAlumne = this.createFolder(gson.toJson(jsonCarpetaAlumne)).getBody();

                //Fer còpia
                JsonObject jsonFitxer = new JsonObject();
                jsonFitxer.addProperty("idFile", document.getIdGoogleDrive());
                jsonFitxer.addProperty("email", email);
                jsonFitxer.addProperty("filename", cognoms+" "+nom+"_"+nomDocument);
                jsonFitxer.addProperty("parentFolderId", carpetaAlumne.getId());
                jsonFitxer.addProperty("originalName", document.getNomOriginal());

                File fitxer = this.copyFile(gson.toJson(jsonFitxer)).getBody();

                //Desem el fitxer
                JsonObject tipusFitxer = new JsonObject();
                tipusFitxer.addProperty("id", tipusDocumentDto.getIdTipusDocument());


                JsonObject jsonFitxerDesat = new JsonObject();
                jsonFitxerDesat.addProperty("idFile", document.getIdGoogleDrive());
                jsonFitxerDesat.addProperty("path", FOLDER_BASE+"/"+cicle+"/"+cognoms+" "+nom+"/"+cognoms+" "+nom+"_"+nomDocument);
                jsonFitxerDesat.addProperty("email", email);
                jsonFitxerDesat.addProperty("tipus", nomDocument);
                jsonFitxerDesat.addProperty("originalName", document.getNomOriginal());
                jsonFitxerDesat.add("tipusDocument", tipusFitxer);
                jsonFitxerDesat.addProperty("idusuari", alumne.getIdusuari());
                jsonFitxerDesat.addProperty("codiGrup", cicle);

                this.createDocument(gson.toJson(jsonFitxerDesat));

                //Si és un document d'empresa avisem al coordinador FCT
                if(tipusDocumentDto.getNom().contains("Dades alumne empresa")){
                    /** TODO - Avisar al coordinador/s FCT **/
                    JsonObject jsonNotificacio = new JsonObject();
                    jsonNotificacio.addProperty("assumpte", "Document d'empresa pujat a "+cicle+" per "+cognoms+" "+nom);
                    jsonNotificacio.addProperty("missatge", "Document d'empresa pujat a "+cicle+" per "+cognoms+" "+nom+" amb el número d'expedient "+numExpedient);
                    jsonNotificacio.addProperty("to", "ppulido@politecnicllevant.cat");

                    coreRestClient.sendEmail(gson.toJson(jsonNotificacio));

                    /*List<UsuariDto> coordinadorsFCT = this.coreRestClient.getCoordinadorFCT().getBody();
                    if(coordinadosrFCT!=null){
                        Notificacio notificacio = new Notificacio();
                        notificacio.setNotifyMessage("S'ha pujat un document de dades d'empresa de l'alumne "+cognoms+" "+nom+" amb el número d'expedient "+numExpedient);
                        notificacio.setNotifyType(NotificacioTipus.INFO);
                        this.coreRestClient.sendNotification(notificacio, coordinadorFCT.getIdusuari());
                    }*/
                }
            }
        }
    }

    @PostMapping("/documents/saveDocumentExtra")
    public ResponseEntity<DocumentDto> saveDocument(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idusuari = null;
        if(jsonObject.get("idusuari")!=null && !jsonObject.get("idusuari").isJsonNull()){
            idusuari = jsonObject.get("idusuari").getAsLong();
        }
        String curs = jsonObject.get("curs").getAsString();
        String tipusDocument = jsonObject.get("tipusDocument").getAsString();

        JsonObject documentJson = jsonObject.get("document").getAsJsonObject();
        DocumentDto document = gson.fromJson(documentJson, DocumentDto.class);

        document.setIdUsuari(idusuari);
        document.setGrupCodi(curs);

        if(document.getEstat()==null){
            document.setEstat(DocumentEstatDto.PENDENT_SIGNATURES);
        }
        if(document.getPathGoogleDrive()==null){
            document.setPathGoogleDrive("");
        }
        if(document.getIdGoogleDrive()==null){
            document.setIdGoogleDrive("");
        }
        TipusDocumentDto tipusDocumentDto = tipusDocumentService.getTipusDocumentByNom(tipusDocument);
        document.setTipusDocument(tipusDocumentDto);

        /*//Comprovem si el document ja existeix el nom, en posem  un altre d'únic
        int i = 1;
        while(documentService.findByNomOriginal(document.getNomOriginal()) != null){
            document.setNomOriginal(document.getNomOriginal()+"_"+i);
            i++;
        }*/

        DocumentDto documentSaved = documentService.save(document);
        return new ResponseEntity<>(documentSaved, HttpStatus.OK);
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


    @GetMapping("/documents/{id}")
    public ResponseEntity<DocumentDto> getDocumentById(@PathVariable Long id) throws Exception {
        DocumentDto document = documentService.getDocumentById(id);
        return new ResponseEntity<>(document, HttpStatus.OK);
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
        } else if(jsonObject.get("idusuari")!=null && !jsonObject.get("idusuari").isJsonNull()){
            idUsuari = jsonObject.get("idusuari").getAsLong();
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
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        document.setEstat(DocumentEstatDto.PENDENT_SIGNATURES);
        DocumentDto documentSaved = documentService.save(document);

        //Creem les signatures
        Set<SignaturaDto> signatures = documentSaved.getTipusDocument().getSignatures();
        for(SignaturaDto signatura: signatures){
            DocumentSignaturaDto documentSignaturaDto = new DocumentSignaturaDto();
            documentSignaturaDto.setDocument(documentSaved);
            documentSignaturaDto.setSignatura(signatura);
            documentSignaturaDto.setSignat(false);
            documentSignaturaService.save(documentSignaturaDto);
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

    @PostMapping("/document/canviarEstatDocument")
    public ResponseEntity<Notificacio> canviarEstatDocument(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        Long idDocument = jsonObject.get("idDocument").getAsLong();
        String estat = jsonObject.get("estat").getAsString();

        DocumentDto document = documentService.getDocumentById(idDocument);
        document.setEstat(DocumentEstatDto.valueOf(estat));
        documentService.save(document);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Estat canviat correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @PostMapping("/document/get-url")
    public ResponseEntity<String> getURL(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        Long idDocument = jsonObject.get("idFile").getAsLong();

        DocumentDto document = documentService.getDocumentById(idDocument);

        Long idFitxer = document.getIdFitxer();
        if(idFitxer!=null){
            String url = documentService.getURLBucket(idFitxer);
            if(url != null){
                return new ResponseEntity<>(url, HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/document/uploadFile")
    public ResponseEntity<Notificacio> uploadFile(@RequestParam("id") Long idDocument, HttpServletRequest request) throws Exception {
        DocumentDto document = documentService.getDocumentById(idDocument);
        Part filePart = request.getPart("arxiu");

        InputStream is = filePart.getInputStream();

        // Reads the file into memory
        /*
         * Path path = Paths.get(audioPath); byte[] data = Files.readAllBytes(path);
         * ByteString audioBytes = ByteString.copyFrom(data);
         */
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] readBuf = new byte[4096];
        while (is.available() > 0) {
            int bytesRead = is.read(readBuf);
            os.write(readBuf, 0, bytesRead);
        }

        // Passam l'arxiu a dins una carpeta
        String pathArxiu = this.tmpPath + "/arxiu-fct-"+document.getIdDocument()+".pdf";

        OutputStream outputStream = new FileOutputStream(pathArxiu);
        os.writeTo(outputStream);

        java.io.File f = new java.io.File(pathArxiu);

        //Upload to Core
        String remotePath = "";
        try {
            byte[] fileContent = Files.readAllBytes(f.toPath());
            System.out.println("Name: "+f.getName());
            FileUploadDto fileUploadDTO = new FileUploadDto(f.getName(), fileContent);
            ResponseEntity<String> response = coreRestClient.handleFileUpload2(fileUploadDTO);
            remotePath = response.getBody();
        } catch (IOException e) {
            // Handle the IOException
            //System.out.println(e.getMessage());
        }

        ResponseEntity<FitxerBucketDto> fitxerBucketResponse = coreRestClient.uploadObject(bucketPathFiles + "/fct/"+ document.getGrupCodi()+"/"+ document.getNomOriginal()+".pdf", remotePath, bucketName);
        FitxerBucketDto fitxerBucket = fitxerBucketResponse.getBody();

        //Save the file
        ResponseEntity<FitxerBucketDto> fitxerBucketSavedResponse = coreRestClient.save(fitxerBucket);
        FitxerBucketDto fitxerBucketSaved = fitxerBucketSavedResponse.getBody();

        if(fitxerBucketSaved!=null && fitxerBucketSaved.getIdfitxer()!=null) {
            document.setIdFitxer(fitxerBucketSaved.getIdfitxer());
            documentService.save(document);

            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("Arxiu pujat amb èxit.");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);

            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }
        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Error pujant el fitxer."+bucketPathFiles + "/fct/"+ document.getGrupCodi()+"/"+ document.getNomOriginal());
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);

        return new ResponseEntity<>(notificacio, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/alumnes/get-students-from-file")
    public ResponseEntity<List<AlumneDto>> getStudentsFromFile(@RequestParam("file") MultipartFile file) throws Exception {

        try(InputStream inpSt = file.getInputStream()){
            Workbook workbook = new HSSFWorkbook(inpSt);
            Sheet sheet = workbook.getSheetAt(0);

            List<AlumneDto> alumnes = new ArrayList<AlumneDto>();

            Map<String, Method> setterStudent = getSettersStudent();

            //Conseguir el headers del fitxer
            Row headerRow = sheet.getRow(4);
            List<String> headers = getHeaders(headerRow);

            Iterator<Row> rowIterator = sheet.iterator();
            //Botas fins després dels headers(si estan sempre a la mateixa fila)
            for (int i = 0; i < 4; i++) {
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                AlumneDto alumne = new AlumneDto();

                Iterator<Cell> cellIterator = row.cellIterator();
                int index = 0;

                while (cellIterator.hasNext() && index < headers.size()) {

                    Cell cell = cellIterator.next();
                    String cellValue = cell.getStringCellValue().trim();
                    String header = headers.get(index);

                    DateTimeFormatter formatoFechanacimineto = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    // Cercar els setters corresponents i amb el nom de la capçalera
                    Method setter = setterStudent.get(header);
                    if (setter != null) {
                        try {
                            if(header.equals("Data de naixement")) {
                                LocalDate fechaNacimiento = LocalDate.parse(cellValue, formatoFechanacimineto);
                                setter.invoke(alumne, fechaNacimiento);
                            }else if(header.equals("Exp.")){
                                setter.invoke(alumne,Long.parseLong(cellValue));
                                UsuariDto user = coreRestClient.getUsuariByNumExpedient(cellValue).getBody();
                                alumne.setIdUsuari(Objects.requireNonNull(user).getIdusuari());

                            }else if(header.equals("Llinatges i nom")){

                                String[] partesNombre = cellValue.split(",\\s+");
                                String[] apellidos = partesNombre[0].split("\\s+");
                                String apellido1 = apellidos[0];
                                String apellido2 = apellidos.length > 1 ? apellidos[1] : "";
                                String nombre = partesNombre[1];

                                setterStudent.get("cognom1").invoke(alumne, apellido1);
                                setterStudent.get("cognom2").invoke(alumne, apellido2);
                                setterStudent.get("Llinatges i nom").invoke(alumne, nombre);

                            }else if(header.equals("Tel. fix")){

                                String[] telefonos = cellValue.split("Tel");

                                for (int i = 0; i < telefonos.length; i++) {

                                    if(telefonos[i].contains("fix")){

                                        String[] fix = telefonos[i].split(":\\s+");
                                        setterStudent.get("Tel. fix").invoke(alumne, fix[1].trim());

                                    }else if(telefonos[i].contains("mòbil")) {
                                        String[] mobil = telefonos[i].split(":\\s+");
                                        setterStudent.get("mobil").invoke(alumne, mobil[1].trim());

                                    }
                                }
                            }
                            else {
                                setter.invoke(alumne, cellValue);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    index++;
                }

                alumnes.add(alumne);
                //alumneService.save(alumne);

            }
            return new ResponseEntity<>(alumnes, HttpStatus.OK);
        }
    }

    @GetMapping("/alumnes/delete-student/{nExp}")
    public ResponseEntity<Notificacio> deleteStudent(@PathVariable Long nExp){

        boolean eliminado = alumneService.delete(nExp);
        Notificacio notificacio = new Notificacio();

        if(eliminado) {
            notificacio.setNotifyMessage("Alumne eliminat correctament");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }else {
            notificacio.setNotifyMessage("Aquest alumne no té número d'expedient");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping("/alumnes/update-student")
    public ResponseEntity<Notificacio> updateStudent(@RequestBody AlumneDto alumne){

        Notificacio notificacio = new Notificacio();

        alumneService.save(alumne);

        notificacio.setNotifyMessage("Alumne actualitzat");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }
    @PostMapping("/alumnes/save-student")
    public ResponseEntity<Notificacio> saveStudent(@RequestBody List<AlumneDto> alumnes){

        Notificacio notificacio = new Notificacio();

        for (AlumneDto alumne:alumnes) {

            alumneService.save(alumne);
        }

        notificacio.setNotifyMessage("Alumne actualitzat");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @GetMapping("/alumnes/all-students")
    public ResponseEntity<List<AlumneDto>>findAllStudents(){

        List<AlumneDto> alumnes = alumneService.findAll();
        return new ResponseEntity<>(alumnes,HttpStatus.OK);
    }

    private static Map<String,Method> getSettersStudent() throws NoSuchMethodException {

        Map<String, Method> setterStudent = new HashMap<>();
        setterStudent.put("Llinatges i nom", AlumneDto.class.getMethod("setNom", String.class));
        setterStudent.put("cognom1", AlumneDto.class.getMethod("setCognom1", String.class));
        setterStudent.put("cognom2", AlumneDto.class.getMethod("setCognom2", String.class));
        setterStudent.put("Ensenyament", AlumneDto.class.getMethod("setEnsenyament", String.class));
        setterStudent.put("Estudis", AlumneDto.class.getMethod("setEstudis", String.class));
        setterStudent.put("Grup", AlumneDto.class.getMethod("setGrup", String.class));
        setterStudent.put("Exp.", AlumneDto.class.getMethod("setNumeroExpedient", Long.class));
        setterStudent.put("Sexe", AlumneDto.class.getMethod("setSexe", String.class));
        setterStudent.put("Data de naixement", AlumneDto.class.getMethod("setDataNaixement", LocalDate.class));
        setterStudent.put("Nacionalitat", AlumneDto.class.getMethod("setNacionalitat", String.class));
        setterStudent.put("País naixement", AlumneDto.class.getMethod("setPaisNaixement", String.class));
        setterStudent.put("Província naixement", AlumneDto.class.getMethod("setProvinciaNaixement", String.class));
        setterStudent.put("Localitat naixement", AlumneDto.class.getMethod("setLocalitatNaixement", String.class));
        setterStudent.put("DNI", AlumneDto.class.getMethod("setDni", String.class));
        setterStudent.put("Targeta sanitària", AlumneDto.class.getMethod("setTargetaSanitaria", String.class));
        setterStudent.put("CIP", AlumneDto.class.getMethod("setCIP", String.class));
        setterStudent.put("Adreça (Corresp.)", AlumneDto.class.getMethod("setAdrecaCompleta", String.class));
        setterStudent.put("Municipi", AlumneDto.class.getMethod("setMinucipi", String.class));
        setterStudent.put("Localitat", AlumneDto.class.getMethod("setLocalitat", String.class));
        setterStudent.put("CP.", AlumneDto.class.getMethod("setCP", String.class));
        setterStudent.put("mobil", AlumneDto.class.getMethod("setTelefon", String.class));
        setterStudent.put("Tel. fix", AlumneDto.class.getMethod("setTelefonFix", String.class));
        setterStudent.put("E-mail", AlumneDto.class.getMethod("setEmail", String.class));
        setterStudent.put("Tutor/a", AlumneDto.class.getMethod("setTutor", String.class));
        setterStudent.put("Tel. tutor/a", AlumneDto.class.getMethod("setTelefonTutor", String.class));
        setterStudent.put("E-mail tutor/a", AlumneDto.class.getMethod("setEmailTutor", String.class));
        setterStudent.put("DNI tutor/a", AlumneDto.class.getMethod("setDniTutor", String.class));
        setterStudent.put("Adreça pares o tutors (Corresp.)", AlumneDto.class.getMethod("setAdrecaTutor", String.class));
        setterStudent.put("Nacionalitat pares o tutors", AlumneDto.class.getMethod("setNacionalitatTutor", String.class));

        return setterStudent;
    }

    private static List<String> getHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        if (headerRow != null) {
            for (Cell cell : headerRow) {
                String header = cell.getStringCellValue();
                headers.add(header);
            }
        }
        return headers;
    }
}
