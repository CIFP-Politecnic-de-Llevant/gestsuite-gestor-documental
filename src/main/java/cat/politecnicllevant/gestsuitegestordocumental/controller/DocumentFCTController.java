package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionRole;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionType;
import cat.politecnicllevant.gestsuitegestordocumental.dto.*;
import cat.politecnicllevant.gestsuitegestordocumental.dto.google.FitxerBucketDto;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import cat.politecnicllevant.gestsuitegestordocumental.service.*;
import com.google.api.services.drive.model.File;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
public class DocumentFCTController {

    private final CoreRestClient coreRestClient;

    private final GoogleDriveService googleDriveService;

    private final DocumentService documentService;

    private final TipusDocumentService tipusDocumentService;

    private final Gson gson;
    private final ConvocatoriaService convocatoriaService;
    private final GrupService grupService;

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

    @Value("${app.google.drive.user.pathdocdefinitiva}")
    private String userPathDocDefinitiva;

    @Value("${app.google.drive.user.pathdocsmigrats}")
    private String userPathDocsMigrats;

    private String pathOrigen = "FCT_DEVELOPMENT";
    private Boolean isUnitatOrganitzativaOrigen = false;
    private String pathDesti = "FCT_DEVELOPMENT";
    private Boolean isUnitatOrganitzativaDesti = true;

    public DocumentFCTController(
            GoogleDriveService googleDriveService,
            CoreRestClient coreRestClient,
            DocumentService documentService,
            TipusDocumentService tipusDocumentService,
            Gson gson,
            ConvocatoriaService convocatoriaService, GrupService grupService) {
        this.googleDriveService = googleDriveService;
        this.coreRestClient = coreRestClient;
        this.documentService = documentService;
        this.tipusDocumentService = tipusDocumentService;
        this.gson = gson;
        this.convocatoriaService = convocatoriaService;
        this.grupService = grupService;
    }

    @PostConstruct
    private void postConstruct() {
        if (!environment.equals("dev")) {
            ConvocatoriaDto convocatoria = convocatoriaService.findConvocatoriaActual();
            pathOrigen = convocatoria.getPathOrigen();
            pathDesti = convocatoria.getPathDesti();
        }

        /*if(environment.equals("dev")){
            userPath = "FCT JOAN";
            sharedDrivePath = "FCT JOAN RESOLT àèèòñ";
        } else {
            userPath = "FCT";
            sharedDrivePath = "Curs Actual/0206 FCT i FP Dual/Documentació FCT alumnes 23-24/Documentació en tràmit";
        }*/
    }

    @Scheduled(cron = "0 0 * * * *")
    public void sincronitzaDocumentsAutomaticFEMPO() throws Exception {
        if (environment.equals("dev")) {
            log.warn("No es sincronitzen els documents en entorn dev");
            return;
        }

        log.info("Sincronitzant documents FEMPO...");

        //String path = pathOrigen;
        final String email = userEmail;
        String FOLDER_BASE = pathDesti;
        final String APP_SHAREDDRIVE_GESTORDOCUMENTAL = sharedDriveId;

        ConvocatoriaDto convocatoria = convocatoriaService.findConvocatoriaActual();
        List<DocumentDto> documents = new ArrayList<>();
        List<GrupDto> grups = grupService.findAll();

        for (GrupDto grup : grups) {
            String path = "FEMPO/" + grup.getCursGrup() + "_Q_FEMPO";
            List<File> driveFiles = googleDriveService.getFilesInFolder(path, email);

            if (driveFiles == null || driveFiles.isEmpty()) {
                log.info("No s'han trobat fitxers a la carpeta {}", path);
                continue;
            }

            for (File driveFile : driveFiles) {

                //System.out.println(driveFile);
                //log.info("Document {} in folder {}", driveFile.getName(), path);

                DocumentDto document = documentService.getDocumentByIdDriveGoogleDrive(driveFile.getId(), convocatoria);

                if (document == null) {
                    document = documentService.getDocumentByGoogleDriveFile(driveFile, convocatoria);
                    document.setEstat(DocumentEstatDto.PENDENT);

                    documents.add(document);
                }
            }
        }

        List<DocumentDto> documentsNoTraspassats = documentService.findAll(convocatoria);

        //Esborrem els documents trobats
        for (DocumentDto documentDto : documentsNoTraspassats) {
            //log.info("Esborrant document {} de la llista de documents a traspassar... Id documentDto: {}", documentDto.getNomOriginal(), documentDto.getIdGoogleDrive());
            documents.removeIf(documentDto1 -> documentDto1.getNomOriginal().equals(documentDto.getNomOriginal()));
        }

        //Traspassam els documents
        for (DocumentDto document : documents) {
            log.info("Traspassant document {}...", document.getNomOriginal());
            try {
                String[] documentParts = document.getNomOriginal().split("_");

                if (documentParts.length == 2) {
                    String cicle = documentParts[0];
                    String nomDocument = documentParts[1];

                    TipusDocumentDto tipusDocumentDto = tipusDocumentService.getTipusDocumentByNom(nomDocument);

                    if (tipusDocumentDto == null) {
                        log.info("No s'ha trobat el tipus de document {}. Número de parts del document: 2", nomDocument);
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
                    DocumentDto documentSaved = documentService.getDocumentByOriginalName(document.getNomOriginal(), convocatoria);
                    if (documentSaved == null) {
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

                } else if (documentParts.length == 5) {
                    String cicle = documentParts[0];
                    String cognoms = documentParts[1];
                    String nom = documentParts[2];
                    String numExpedient = documentParts[3];
                    String nomDocument = documentParts[4];

                    TipusDocumentDto tipusDocumentDto = tipusDocumentService.getTipusDocumentByNom(nomDocument);
                    ResponseEntity<UsuariDto> alumneResponse = coreRestClient.getUsuariByNumExpedient(numExpedient);

                    if (tipusDocumentDto == null || alumneResponse == null) {
                        log.info("No s'ha trobat el tipus de document {}. Número de parts del document: 5", nomDocument);
                        continue;
                    }

                    UsuariDto alumne = alumneResponse.getBody();

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
                    jsonCarpetaAlumne.addProperty("folderName", cognoms + " " + nom);
                    jsonCarpetaAlumne.addProperty("email", email);
                    jsonCarpetaAlumne.addProperty("parentFolderId", carpetaCicle.getId());

                    File carpetaAlumne = this.createFolder(gson.toJson(jsonCarpetaAlumne)).getBody();

                    //Fer còpia
                    JsonObject jsonFitxer = new JsonObject();
                    jsonFitxer.addProperty("idFile", document.getIdGoogleDrive());
                    jsonFitxer.addProperty("email", email);
                    jsonFitxer.addProperty("filename", cognoms + " " + nom + "_" + nomDocument);
                    jsonFitxer.addProperty("parentFolderId", carpetaAlumne.getId());
                    jsonFitxer.addProperty("originalName", document.getNomOriginal());

                    File fitxer = this.copyFile(gson.toJson(jsonFitxer)).getBody();

                    //Desem el fitxer
                    JsonObject tipusFitxer = new JsonObject();
                    tipusFitxer.addProperty("id", tipusDocumentDto.getIdTipusDocument());


                    JsonObject jsonFitxerDesat = new JsonObject();
                    jsonFitxerDesat.addProperty("idFile", document.getIdGoogleDrive());
                    jsonFitxerDesat.addProperty("path", FOLDER_BASE + "/" + cicle + "/" + cognoms + " " + nom + "/" + cognoms + " " + nom + "_" + nomDocument);
                    jsonFitxerDesat.addProperty("email", email);
                    jsonFitxerDesat.addProperty("tipus", nomDocument);
                    jsonFitxerDesat.addProperty("originalName", document.getNomOriginal());
                    jsonFitxerDesat.add("tipusDocument", tipusFitxer);
                    jsonFitxerDesat.addProperty("idusuari", alumne.getIdusuari());
                    jsonFitxerDesat.addProperty("codiGrup", cicle);

                    this.createDocument(gson.toJson(jsonFitxerDesat));

                    //Si és un document d'empresa avisem al coordinador FCT
                    if (tipusDocumentDto.getNom().contains("Dades alumne empresa")) {
                        /** TODO - Avisar al coordinador/s FCT **/
                        JsonObject jsonNotificacio = new JsonObject();
                        jsonNotificacio.addProperty("assumpte", "Document d'empresa pujat a " + cicle + " per " + cognoms + " " + nom);
                        jsonNotificacio.addProperty("missatge", "Document d'empresa pujat a " + cicle + " per " + cognoms + " " + nom + " amb el número d'expedient " + numExpedient);
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
                log.info("Document traspassat {}", document.getNomOriginal());
            } catch (Exception e) {
                log.error("Error traspassant document", e);
            }
        }

        //Traspassar documents del bucket
        log.info("Traspassant documents del bucket...");
        List<ConvocatoriaDto> convocatories = convocatoriaService.findAll();
        for (ConvocatoriaDto convocatoriaDto : convocatories) {
            List<DocumentDto> documentsBucketNoTraspassats = documentService.findAllDocumentsBucketNoTraspassats(convocatoriaDto);
            for (DocumentDto doc : documentsBucketNoTraspassats) {
                try {
                    if (!doc.getEstat().equals(DocumentEstatDto.ACCEPTAT)) {
                        continue;
                    }
                    //System.out.println("Doc no traspassat: " + doc.getNomOriginal());
                    ResponseEntity<FitxerBucketDto> responseEntity = coreRestClient.getFitxerBucketById(doc.getIdFitxer());
                    FitxerBucketDto fitxerBucket = responseEntity.getBody();

                    JsonObject jsonFitxerBucket = new JsonObject();
                    jsonFitxerBucket.addProperty("idfitxer", fitxerBucket.getIdfitxer());
                    jsonFitxerBucket.addProperty("nom", fitxerBucket.getNom());
                    jsonFitxerBucket.addProperty("bucket", fitxerBucket.getBucket());
                    jsonFitxerBucket.addProperty("path", fitxerBucket.getPath());

                    ResponseEntity<String> urlResponse = coreRestClient.generateSignedURL(jsonFitxerBucket.toString());
                    String url = urlResponse.getBody();

                    //Split / and get last part
                    String[] parts = fitxerBucket.getNom().split("/");
                    String nomFitxerCleaned = parts[parts.length - 1];

                    //Get file from URL
                    InputStream in = new URL(url).openStream();
                    Files.copy(in, Paths.get("/tmp/" + nomFitxerCleaned), StandardCopyOption.REPLACE_EXISTING);
                    //log.info("Fitxer copiat a /tmp/" + nomFitxerCleaned);

                    String basePathGoogleDrive = userPathDocDefinitiva;
                    ConvocatoriaDto convocatoriaDocument = convocatoriaService.findConvocatoriaById(doc.getConvocatoria().getIdConvocatoria());

                    String[] pathDoc = doc.getNomOriginal().split("_");

                    String nomFitxer = nomFitxerCleaned;
                    List<String> pathDocList = new ArrayList<String>();
                    if (pathDoc.length == 5) {
                        String numExpedient = pathDoc[3];
                        UsuariDto alumne = coreRestClient.getUsuariByNumExpedient(numExpedient).getBody();

                        if (alumne == null) {
                            log.error("No s'ha pogut traspassar el fitxer " + nomFitxerCleaned);
                            continue;
                        }

                        nomFitxer = pathDoc[4]; //Nom fitxer original

                        pathDocList.add(basePathGoogleDrive); // Base
                        pathDocList.add(convocatoriaDocument.getNom()); // Convocatòria del document
                        pathDocList.add(pathDoc[0]); // Curs + Grup. IFC33B
                        pathDocList.add(alumne.getGestibCognom1() + " " + alumne.getGestibCognom2() + ", " + alumne.getGestibNom()); //Cognoms + Nom
                    } else if (pathDoc.length == 2) {
                        nomFitxer = pathDoc[1];//Nom fitxer original

                        pathDocList.add(basePathGoogleDrive); // Base
                        pathDocList.add(convocatoriaDocument.getNom()); // Convocatòria del document
                        pathDocList.add(pathDoc[0]); // Curs + Grup. IFC33B

                    } else if (pathDoc.length == 4 && pathDoc[0].equals("CUSTOM") && pathDoc[1].equals("Altra documentació grup")) {
                        nomFitxer = pathDoc[3]; //Nom fitxer original

                        pathDocList.add(basePathGoogleDrive); // Base
                        pathDocList.add(convocatoriaDocument.getNom()); // Convocatòria del document
                        pathDocList.add(pathDoc[2]); //Curs + Grup. IFC33B
                    } else if (pathDoc.length == 4 && pathDoc[0].equals("CUSTOM") && pathDoc[1].equals("Annex 4")) {
                        nomFitxer = pathDoc[3]; //Nom fitxer original
                        String numExpedient = pathDoc[3];
                        UsuariDto alumne = coreRestClient.getUsuariByNumExpedient(numExpedient).getBody();

                        if (alumne == null) {
                            log.error("No s'ha pogut traspassar el fitxer " + nomFitxerCleaned);
                            continue;
                        }

                        pathDocList.add(basePathGoogleDrive); // Base
                        pathDocList.add(convocatoriaDocument.getNom()); // Convocatòria del document
                        pathDocList.add(pathDoc[2]); //Curs + Grup. IFC33B
                        pathDocList.add(alumne.getGestibCognom1() + " " + alumne.getGestibCognom2() + ", " + alumne.getGestibNom()); //Cognoms + Nom
                    } else {
                        log.error("No s'ha pogut traspassar el fitxer " + nomFitxerCleaned);
                        continue;
                    }


                    //Crear carpetes si no existeixen
                    File parent = googleDriveService.getFolder(basePathGoogleDrive, "qualitat@politecnicllevant.cat", "root");
                    if (parent == null) {
                        log.error("No s'ha trobat la carpeta base " + basePathGoogleDrive);
                    } else {
                        log.info("Carpeta base trobada: " + basePathGoogleDrive);
                    }
                    for (int i = 1; i < pathDocList.size(); i++) {
                        if (parent != null && parent.getId() != null) {
                            log.info("Cercant la carpeta " + pathDocList.get(i));
                            File folder = googleDriveService.getFolder(pathDocList.get(i), "qualitat@politecnicllevant.cat", parent.getId());
                            if (folder == null) {
                                folder = googleDriveService.createFolder(pathDocList.get(i), "qualitat@politecnicllevant.cat", parent.getId());
                            }

                            if (pathDocList.size() - 1 == i) {
                                //Get file from folder
                                java.io.File fileToUpload = new java.io.File("/tmp/" + nomFitxerCleaned);
                                googleDriveService.uploadFile(folder.getId(), "qualitat@politecnicllevant.cat", fileToUpload, nomFitxer);

                                doc.setTraspassat(true);
                                documentService.save(doc, convocatoriaDocument);
                            }

                            parent = googleDriveService.getFolder(pathDocList.get(i), "qualitat@politecnicllevant.cat", parent.getId());
                        }
                    }


                } catch (Exception e) {
                    log.error("Error traspassant fitxer de bucket de " + doc.getNomOriginal(), e);
                }
            }
        }
    }


    /*
Second Minute Hour Day-of-Month
second, minute, hour, day(1-31), month(1-12), weekday(1-7) SUN-SAT
0 0 2 * * * = a les 2AM de cada dia
 */
    @Deprecated
    @Scheduled(cron = "0 30 * * * *")
    //@Scheduled(fixedRate = 60*60*1000, initialDelay = 60*1000)
    public void sincronitzaDocumentsAutomatic() throws Exception {
        if (environment.equals("dev")) {
            log.warn("No es sincronitzen els documents en entorn dev");
            return;
        }

        log.info("Sincronitzant documents...");

        String path = pathOrigen;
        final String email = userEmail;
        String FOLDER_BASE = pathDesti;
        final String APP_SHAREDDRIVE_GESTORDOCUMENTAL = sharedDriveId;

        /*if (environment.equals("dev")) {
            path = envUserPath;
            FOLDER_BASE = envSharedDrivePath;
        }*/

        ConvocatoriaDto convocatoria = convocatoriaService.findConvocatoriaActual();

        List<File> driveFiles = googleDriveService.getFilesInFolder(path, email);
        List<DocumentDto> documents = new ArrayList<>();
        for (File driveFile : driveFiles) {

            //System.out.println(driveFile);
            log.info("Document {} in folder {}", driveFile.getName(), path);

            DocumentDto document = documentService.getDocumentByIdDriveGoogleDrive(driveFile.getId(), convocatoria);

            if (document == null) {
                document = documentService.getDocumentByGoogleDriveFile(driveFile, convocatoria);
                document.setEstat(DocumentEstatDto.PENDENT);

                documents.add(document);
            }
        }

        List<DocumentDto> documentsNoTraspassats = documentService.findAll(convocatoria);

        //Esborrem els documents trobats
        for (DocumentDto documentDto : documentsNoTraspassats) {
            //log.info("Esborrant document {} de la llista de documents a traspassar... Id documentDto: {}", documentDto.getNomOriginal(), documentDto.getIdGoogleDrive());
            documents.removeIf(documentDto1 -> documentDto1.getNomOriginal().equals(documentDto.getNomOriginal()));
        }

        //Traspassam els documents
        for (DocumentDto document : documents) {
            log.info("Traspassant document {}...", document.getNomOriginal());
            try {
                String[] documentParts = document.getNomOriginal().split("_");

                if (documentParts.length == 2) {
                    String cicle = documentParts[0];
                    String nomDocument = documentParts[1];

                    TipusDocumentDto tipusDocumentDto = tipusDocumentService.getTipusDocumentByNom(nomDocument);

                    if (tipusDocumentDto == null) {
                        log.info("No s'ha trobat el tipus de document {}. Número de parts del document: 2", nomDocument);
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
                    DocumentDto documentSaved = documentService.getDocumentByOriginalName(document.getNomOriginal(), convocatoria);
                    if (documentSaved == null) {
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

                } else if (documentParts.length == 5) {
                    String cicle = documentParts[0];
                    String cognoms = documentParts[1];
                    String nom = documentParts[2];
                    String numExpedient = documentParts[3];
                    String nomDocument = documentParts[4];

                    TipusDocumentDto tipusDocumentDto = tipusDocumentService.getTipusDocumentByNom(nomDocument);
                    ResponseEntity<UsuariDto> alumneResponse = coreRestClient.getUsuariByNumExpedient(numExpedient);

                    if (tipusDocumentDto == null || alumneResponse == null) {
                        log.info("No s'ha trobat el tipus de document {}. Número de parts del document: 5", nomDocument);
                        continue;
                    }

                    UsuariDto alumne = alumneResponse.getBody();

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
                    jsonCarpetaAlumne.addProperty("folderName", cognoms + " " + nom);
                    jsonCarpetaAlumne.addProperty("email", email);
                    jsonCarpetaAlumne.addProperty("parentFolderId", carpetaCicle.getId());

                    File carpetaAlumne = this.createFolder(gson.toJson(jsonCarpetaAlumne)).getBody();

                    //Fer còpia
                    JsonObject jsonFitxer = new JsonObject();
                    jsonFitxer.addProperty("idFile", document.getIdGoogleDrive());
                    jsonFitxer.addProperty("email", email);
                    jsonFitxer.addProperty("filename", cognoms + " " + nom + "_" + nomDocument);
                    jsonFitxer.addProperty("parentFolderId", carpetaAlumne.getId());
                    jsonFitxer.addProperty("originalName", document.getNomOriginal());

                    File fitxer = this.copyFile(gson.toJson(jsonFitxer)).getBody();

                    //Desem el fitxer NOMÉS si no existeix
                    DocumentDto documentSaved = documentService.getDocumentByOriginalName(document.getNomOriginal(), convocatoria);
                    if (documentSaved == null) {
                        //Desem el fitxer
                        JsonObject tipusFitxer = new JsonObject();
                        tipusFitxer.addProperty("id", tipusDocumentDto.getIdTipusDocument());


                        JsonObject jsonFitxerDesat = new JsonObject();
                        jsonFitxerDesat.addProperty("idFile", document.getIdGoogleDrive());
                        jsonFitxerDesat.addProperty("path", FOLDER_BASE + "/" + cicle + "/" + cognoms + " " + nom + "/" + cognoms + " " + nom + "_" + nomDocument);
                        jsonFitxerDesat.addProperty("email", email);
                        jsonFitxerDesat.addProperty("tipus", nomDocument);
                        jsonFitxerDesat.addProperty("originalName", document.getNomOriginal());
                        jsonFitxerDesat.add("tipusDocument", tipusFitxer);
                        jsonFitxerDesat.addProperty("idusuari", alumne.getIdusuari());
                        jsonFitxerDesat.addProperty("codiGrup", cicle);

                        this.createDocument(gson.toJson(jsonFitxerDesat));

                        //Si és un document d'empresa avisem al coordinador FCT
                        if (tipusDocumentDto.getNom().contains("Dades alumne empresa")) {
                            /** TODO - Avisar al coordinador/s FCT **/
                            JsonObject jsonNotificacio = new JsonObject();
                            jsonNotificacio.addProperty("assumpte", "Document d'empresa pujat a " + cicle + " per " + cognoms + " " + nom);
                            jsonNotificacio.addProperty("missatge", "Document d'empresa pujat a " + cicle + " per " + cognoms + " " + nom + " amb el número d'expedient " + numExpedient);
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
                log.info("Document traspassat {}", document.getNomOriginal());
            } catch (Exception e) {
                log.error("Error traspassant document", e);
            }
        }

        //Traspassar documents del bucket
        log.info("Traspassant documents del bucket...");
        List<ConvocatoriaDto> convocatories = convocatoriaService.findAll();
        for (ConvocatoriaDto convocatoriaDto : convocatories) {
            List<DocumentDto> documentsBucketNoTraspassats = documentService.findAllDocumentsBucketNoTraspassats(convocatoriaDto);
            for (DocumentDto doc : documentsBucketNoTraspassats) {
                try {
                    if (!doc.getEstat().equals(DocumentEstatDto.ACCEPTAT)) {
                        continue;
                    }
                    System.out.println("Doc no traspassat: " + doc.getNomOriginal());
                    ResponseEntity<FitxerBucketDto> responseEntity = coreRestClient.getFitxerBucketById(doc.getIdFitxer());
                    FitxerBucketDto fitxerBucket = responseEntity.getBody();

                    JsonObject jsonFitxerBucket = new JsonObject();
                    jsonFitxerBucket.addProperty("idfitxer", fitxerBucket.getIdfitxer());
                    jsonFitxerBucket.addProperty("nom", fitxerBucket.getNom());
                    jsonFitxerBucket.addProperty("bucket", fitxerBucket.getBucket());
                    jsonFitxerBucket.addProperty("path", fitxerBucket.getPath());

                    ResponseEntity<String> urlResponse = coreRestClient.generateSignedURL(jsonFitxerBucket.toString());
                    String url = urlResponse.getBody();

                    //Split / and get last part
                    String[] parts = fitxerBucket.getNom().split("/");
                    String nomFitxerCleaned = parts[parts.length - 1];

                    //Get file from URL
                    InputStream in = new URL(url).openStream();
                    Files.copy(in, Paths.get("/tmp/" + nomFitxerCleaned), StandardCopyOption.REPLACE_EXISTING);
                    log.info("Fitxer copiat a /tmp/" + nomFitxerCleaned);

                    String basePathGoogleDrive = userPathDocDefinitiva;
                    ConvocatoriaDto convocatoriaDocument = convocatoriaService.findConvocatoriaById(doc.getConvocatoria().getIdConvocatoria());

                    String[] pathDoc = doc.getNomOriginal().split("_");

                    String nomFitxer = nomFitxerCleaned;
                    List<String> pathDocList = new ArrayList<String>();
                    if (pathDoc.length == 5) {
                        String numExpedient = pathDoc[3];
                        UsuariDto alumne = coreRestClient.getUsuariByNumExpedient(numExpedient).getBody();

                        if (alumne == null) {
                            log.error("No s'ha pogut traspassar el fitxer " + nomFitxerCleaned);
                            continue;
                        }

                        nomFitxer = pathDoc[4]; //Nom fitxer original

                        pathDocList.add(basePathGoogleDrive); // Base
                        pathDocList.add(convocatoriaDocument.getNom()); // Convocatòria del document
                        pathDocList.add(pathDoc[0]); // Curs + Grup. IFC33B
                        pathDocList.add(alumne.getGestibCognom1() + " " + alumne.getGestibCognom2() + ", " + alumne.getGestibNom()); //Cognoms + Nom
                    } else if (pathDoc.length == 2) {
                        nomFitxer = pathDoc[1];//Nom fitxer original

                        pathDocList.add(basePathGoogleDrive); // Base
                        pathDocList.add(convocatoriaDocument.getNom()); // Convocatòria del document
                        pathDocList.add(pathDoc[0]); // Curs + Grup. IFC33B

                    } else if (pathDoc.length == 4 && pathDoc[0].equals("CUSTOM") && pathDoc[1].equals("Altra documentació grup")) {
                        nomFitxer = pathDoc[3]; //Nom fitxer original

                        pathDocList.add(basePathGoogleDrive); // Base
                        pathDocList.add(convocatoriaDocument.getNom()); // Convocatòria del document
                        pathDocList.add(pathDoc[2]); //Curs + Grup. IFC33B
                    } else if (pathDoc.length == 4 && pathDoc[0].equals("CUSTOM") && pathDoc[1].equals("Annex 4")) {
                        nomFitxer = pathDoc[3]; //Nom fitxer original
                        String numExpedient = pathDoc[3];
                        UsuariDto alumne = coreRestClient.getUsuariByNumExpedient(numExpedient).getBody();

                        if (alumne == null) {
                            log.error("No s'ha pogut traspassar el fitxer " + nomFitxerCleaned);
                            continue;
                        }

                        pathDocList.add(basePathGoogleDrive); // Base
                        pathDocList.add(convocatoriaDocument.getNom()); // Convocatòria del document
                        pathDocList.add(pathDoc[2]); //Curs + Grup. IFC33B
                        pathDocList.add(alumne.getGestibCognom1() + " " + alumne.getGestibCognom2() + ", " + alumne.getGestibNom()); //Cognoms + Nom
                    } else {
                        log.error("No s'ha pogut traspassar el fitxer " + nomFitxerCleaned);
                        continue;
                    }


                    //Crear carpetes si no existeixen
                    File parent = googleDriveService.getFolder(basePathGoogleDrive, "qualitat@politecnicllevant.cat", "root");
                    if (parent == null) {
                        log.error("No s'ha trobat la carpeta base " + basePathGoogleDrive);
                    } else {
                        log.info("Carpeta base trobada: " + basePathGoogleDrive);
                    }
                    for (int i = 1; i < pathDocList.size(); i++) {
                        if (parent != null && parent.getId() != null) {
                            log.info("Cercant la carpeta " + pathDocList.get(i));
                            File folder = googleDriveService.getFolder(pathDocList.get(i), "qualitat@politecnicllevant.cat", parent.getId());
                            if (folder == null) {
                                folder = googleDriveService.createFolder(pathDocList.get(i), "qualitat@politecnicllevant.cat", parent.getId());
                            }

                            if (pathDocList.size() - 1 == i) {
                                //Get file from folder
                                java.io.File fileToUpload = new java.io.File("/tmp/" + nomFitxerCleaned);
                                googleDriveService.uploadFile(folder.getId(), "qualitat@politecnicllevant.cat", fileToUpload, nomFitxer);

                                doc.setTraspassat(true);
                                documentService.save(doc, convocatoriaDocument);
                            }

                            parent = googleDriveService.getFolder(pathDocList.get(i), "qualitat@politecnicllevant.cat", parent.getId());
                        }
                    }


                } catch (Exception e) {
                    log.error("Error traspassant fitxer de bucket de " + doc.getNomOriginal(), e);
                }
            }
        }
    }

    @PostMapping("/documents/saveDocumentExtra")
    public ResponseEntity<DocumentDto> saveDocument(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idusuari = null;
        if (jsonObject.get("idusuari") != null && !jsonObject.get("idusuari").isJsonNull()) {
            idusuari = jsonObject.get("idusuari").getAsLong();
        }
        String curs = jsonObject.get("curs").getAsString();
        String tipusDocument = jsonObject.get("tipusDocument").getAsString();

        JsonObject documentJson = jsonObject.get("document").getAsJsonObject();

        ConvocatoriaDto convocatoria = convocatoriaService.findConvocatoriaActual();

        DocumentDto document = gson.fromJson(documentJson, DocumentDto.class);

        document.setIdUsuari(idusuari);
        document.setGrupCodi(curs);

        if (document.getEstat() == null) {
            document.setEstat(DocumentEstatDto.PENDENT);
        }
        if (document.getPathGoogleDrive() == null) {
            document.setPathGoogleDrive("");
        }
        if (document.getIdGoogleDrive() == null) {
            document.setIdGoogleDrive("");
        }
        TipusDocumentDto tipusDocumentDto = tipusDocumentService.getTipusDocumentByNom(tipusDocument);
        document.setTipusDocument(tipusDocumentDto);
        document.setVisibilitat(tipusDocumentDto.getVisibilitatDefecte());

        if (document.getTraspassat() == null) {
            document.setTraspassat(false);
        }

        document.setNomOriginal("CUSTOM_" + tipusDocumentDto.getNom() + "_" + curs + "_" + idusuari);
        //Comprovem si el document ja existeix el nom, en posem  un altre d'únic
        int i = 1;
        while (documentService.findByNomOriginal(document.getNomOriginal(), convocatoria) != null) {
            document.setNomOriginal(document.getNomOriginal() + "_" + i);
            i++;
        }

        DocumentDto documentSaved = documentService.save(document, convocatoria);
        return new ResponseEntity<>(documentSaved, HttpStatus.OK);
    }

    @GetMapping("/autoritzats")
    public ResponseEntity<List<UsuariDto>> getAutoritzats() throws Exception {
        List<UsuariDto> usuarisAutoritzats = new ArrayList<>();

        for (String autoritzat : autoritzats) {
            UsuariDto usuariAutoritzat = coreRestClient.getUsuariByEmail(autoritzat).getBody();
            System.out.println(usuariAutoritzat.getGsuiteEmail() + usuariAutoritzat.getGestibNom() + " " + usuariAutoritzat.getGestibCognom1() + " " + usuariAutoritzat.getGestibCognom2());
            usuarisAutoritzats.add(usuariAutoritzat);
        }
        return new ResponseEntity<>(usuarisAutoritzats, HttpStatus.OK);
    }

    @GetMapping("/convocatories")
    public ResponseEntity<List<ConvocatoriaDto>> findAllConvocatories() throws Exception {
        List<ConvocatoriaDto> convocatories = convocatoriaService.findAll();

        return new ResponseEntity<>(convocatories, HttpStatus.OK);
    }

    @GetMapping("/grups-amb-documentsfct")
    public ResponseEntity<List<GrupDto>> getGrupsAmbDocuments(@RequestParam(required = false) Long idConvocatoria) {
        ConvocatoriaDto convocatoria;
        if (idConvocatoria != null) {
            convocatoria = convocatoriaService.findConvocatoriaById(idConvocatoria);
        } else {
            convocatoria = convocatoriaService.findConvocatoriaActual();
        }
        List<String> grups = documentService.findAll(convocatoria).stream().map(DocumentDto::getGrupCodi).toList();
        Set<String> codis = new HashSet<>(grups);

        List<GrupDto> grupsNoDuplicats = new ArrayList<>();
        for (String codi : codis) {
            try {
                ResponseEntity<GrupDto> responseEntity = coreRestClient.getByCodigrup(codi);
                grupsNoDuplicats.add(responseEntity.getBody());
            } catch (Exception e) {
                log.error("El codi de grup " + codi + " no existeix", e);
            }
        }

        return new ResponseEntity<>(grupsNoDuplicats, HttpStatus.OK);
    }


    @PostMapping("/documents")
    public ResponseEntity<List<DocumentDto>> getDocumentsByPath(@RequestBody String json, @RequestParam(required = false) Long idConvocatoria) throws Exception {
        ConvocatoriaDto convocatoria;
        if (idConvocatoria != null) {
            convocatoria = convocatoriaService.findConvocatoriaById(idConvocatoria);
        } else {
            convocatoria = convocatoriaService.findConvocatoriaActual();
        }

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String path = jsonObject.get("path").getAsString();
        String email = jsonObject.get("email").getAsString();

        List<File> driveFiles = googleDriveService.getFilesInFolder(path, email);
        List<DocumentDto> documents = new ArrayList<>();
        for (File driveFile : driveFiles) {

            //System.out.println(driveFile);
            log.info(driveFile.getId() + " " + driveFile.getName());

            DocumentDto document = documentService.getDocumentByIdDriveGoogleDrive(driveFile.getId(), convocatoria);

            if (document == null) {
                document = documentService.getDocumentByGoogleDriveFile(driveFile, convocatoria);

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

        ConvocatoriaDto convocatoria = convocatoriaService.findConvocatoriaActual();

        List<File> driveFiles = googleDriveService.getFilesInFolder(path, email);
        List<DocumentDto> documents = new ArrayList<>();
        for (File driveFile : driveFiles) {

            System.out.println(driveFile);

            DocumentDto document = documentService.getDocumentByIdDriveGoogleDrive(driveFile.getId(), convocatoria);

            if (document == null) {
                document = documentService.getDocumentByGoogleDriveFile(driveFile, convocatoria);

                documents.add(document);
                //documentService.save(document);
            }
        }

        List<DocumentDto> documentsTraspassats = documentService.findAll(convocatoria);

        //Esborrem els documents trobats
        for (DocumentDto documentDto : documentsTraspassats) {
            documents.removeIf(documentDto1 -> documentDto1.getNomOriginal().equals(documentDto.getNomOriginal()));
        }

        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @GetMapping("/documents-grup/{grupCodi}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByGrup(@PathVariable String grupCodi, @RequestParam(required = false) Long idConvocatoria) throws Exception {
        ConvocatoriaDto convocatoria;
        if (idConvocatoria != null) {
            convocatoria = convocatoriaService.findConvocatoriaById(idConvocatoria);
        } else {
            convocatoria = convocatoriaService.findConvocatoriaActual();
        }

        if (grupCodi == null || convocatoria == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<DocumentDto> documents = documentService.findAllByGrupCodi(grupCodi, convocatoria);

        return new ResponseEntity<>(documents, HttpStatus.OK);
    }


    @GetMapping("/documents/{id}")
    public ResponseEntity<DocumentDto> getDocumentById(@PathVariable Long id, @RequestParam(required = false) Long idConvocatoria) throws Exception {
        ConvocatoriaDto convocatoria;
        if (idConvocatoria != null) {
            convocatoria = convocatoriaService.findConvocatoriaById(idConvocatoria);
        } else {
            convocatoria = convocatoriaService.findConvocatoriaActual();
        }

        DocumentDto document = documentService.getDocumentById(id, convocatoria);
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

        if (jsonObject.get("usuari") != null && !jsonObject.get("usuari").isJsonNull()) {
            JsonObject jsonObjectUsuari = jsonObject.get("usuari").getAsJsonObject();
            idUsuari = jsonObjectUsuari.get("id").getAsLong();
        } else if (jsonObject.get("idusuari") != null && !jsonObject.get("idusuari").isJsonNull()) {
            idUsuari = jsonObject.get("idusuari").getAsLong();
        }


        File file = googleDriveService.getFileById(idFile, emailUser);
        ConvocatoriaDto convocatoria = convocatoriaService.findConvocatoriaActual();

        DocumentDto document = documentService.getDocumentByOriginalName(originalName, convocatoria);

        if (document == null) {
            document = new DocumentDto();
            document.setNomOriginal(originalName);

        }

        document.setTipusDocument(tipusDocumentService.getTipusDocumentById(idTipusDocument));
        document.setIdUsuari(idUsuari);
        document.setGrupCodi(codiGrup);
        document.setIdGoogleDrive(file.getId());
        document.setIdDriveGoogleDrive(file.getDriveId());
        document.setPathGoogleDrive(path);
        if (file.getOwners() != null && !file.getOwners().isEmpty()) {
            document.setOwnerGoogleDrive(file.getOwners().get(0).getEmailAddress());
        }

        document.setMimeTypeGoogleDrive(file.getMimeType());

        if (file.getCreatedTime() != null) {
            document.setCreatedTimeGoogleDrive(file.getCreatedTime().toString());
        }

        if (file.getModifiedTime() != null) {
            document.setModifiedTimeGoogleDrive(file.getModifiedTime().toString());
        }

        TipusDocumentDto tipusDocumentDto = tipusDocumentService.getTipusDocumentByNom(tipus);
        if (tipusDocumentDto != null) {
            document.setTipusDocument(tipusDocumentDto);
            document.setVisibilitat(tipusDocumentDto.getVisibilitatDefecte());
            document.setTraspassat(false);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        document.setEstat(DocumentEstatDto.PENDENT);
        DocumentDto documentSaved = documentService.save(document, convocatoria);

        return new ResponseEntity<>(documentSaved, HttpStatus.OK);
    }

    @PostMapping("/documents/eliminar-document")
    public ResponseEntity<Notificacio> deleteDocument(@RequestBody String json, @RequestParam(required = false) Long idConvocatoria) {
        ConvocatoriaDto convocatoria;
        if (idConvocatoria != null) {
            convocatoria = convocatoriaService.findConvocatoriaById(idConvocatoria);
        } else {
            convocatoria = convocatoriaService.findConvocatoriaActual();
        }

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        Long documentId = jsonObject.get("documentId").getAsLong();
        String email = jsonObject.get("email").getAsString();
        DocumentDto documentDto = documentService.getDocumentById(documentId, convocatoria);

        if (jsonObject.get("fitxerId") != null) {
            Long fitxerId = jsonObject.get("fitxerId").getAsLong();

            ResponseEntity<FitxerBucketDto> responseEntity = coreRestClient.getFitxerBucketById(fitxerId);
            FitxerBucketDto fitxerBucket = responseEntity.getBody();
            coreRestClient.delete(fitxerBucket);
        }

        if (documentDto.getIdGoogleDrive() != null)
            googleDriveService.deleteFileById(documentDto.getIdGoogleDrive(), email);

        if (documentDto.getIdGoogleSharedDrive() != null)
            googleDriveService.deleteFileById(documentDto.getIdGoogleSharedDrive(), email);

        documentService.deleteByIdDocument(documentId, convocatoria);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Document eliminat");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);

        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @PostMapping("/documents/eliminar-documents-alumne")
    public ResponseEntity<Notificacio> deleteDocumentsAlumne(@RequestBody String json, @RequestParam(required = false) Long idConvocatoria) {
        ConvocatoriaDto convocatoria;
        if (idConvocatoria != null) {
            convocatoria = convocatoriaService.findConvocatoriaById(idConvocatoria);
        } else {
            convocatoria = convocatoriaService.findConvocatoriaActual();
        }

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonArray documentIds = jsonObject.get("documentIds").getAsJsonArray();
        String email = jsonObject.get("email").getAsString();
        String folderName = jsonObject.get("folderName").getAsString();
        String parentFolderId = jsonObject.get("parentFolderId").getAsString();

        for (JsonElement id : documentIds) {
            DocumentDto documentDto = this.documentService.getDocumentByIdGoogleDrive(id.getAsString(), convocatoria);

            this.googleDriveService.deleteFileById(id.getAsString(), email);
        }

        Long alumneId = this.documentService.getDocumentByIdGoogleDrive(documentIds.get(0).getAsString(), convocatoria).getIdUsuari();
        this.documentService.deleteAllByIdUsuari(alumneId, convocatoria);

        this.googleDriveService.deleteFolder(folderName, email, parentFolderId);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Documents eliminats");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);

        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @PostMapping("/get-carpeta")
    public ResponseEntity<File> getFolder(@RequestBody String json) {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String folderName = jsonObject.get("folderName").getAsString();
        String email = jsonObject.get("email").getAsString();
        String parentFolderId = "root";

        if (jsonObject.get("parentFolderId") != null && !jsonObject.get("parentFolderId").isJsonNull()) {
            parentFolderId = jsonObject.get("parentFolderId").getAsString();
        }

        File folder = this.googleDriveService.getFolder(folderName, email, parentFolderId);
        return new ResponseEntity<>(folder, HttpStatus.OK);
    }

    @PostMapping("/crear-carpeta")
    public ResponseEntity<File> createFolder(@RequestBody String json) {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String folderName = jsonObject.get("folderName").getAsString();
        String email = jsonObject.get("email").getAsString();

        String parentFolderId = "root";
        if (jsonObject.get("parentFolderId") != null && !jsonObject.get("parentFolderId").isJsonNull()) {
            parentFolderId = jsonObject.get("parentFolderId").getAsString();
        }

        File file = this.googleDriveService.createFolder(folderName, email, parentFolderId);

        if (jsonObject.get("administrators") != null && !jsonObject.get("administrators").isJsonNull()) {
            JsonArray administrators = jsonObject.get("administrators").getAsJsonArray();
            for (JsonElement administrador : administrators) {
                this.googleDriveService.assignPermission(file, PermissionType.USER, PermissionRole.WRITER, administrador.getAsString(), email);
            }
        }

        if (jsonObject.get("editors") != null && !jsonObject.get("editors").isJsonNull() && !environment.equals("dev")) {
            JsonArray editors = jsonObject.get("editors").getAsJsonArray();
            for (JsonElement editor : editors) {
                this.googleDriveService.assignPermission(file, PermissionType.USER, PermissionRole.FILE_ORGANIZER, editor.getAsString(), email);
            }
        }

        return new ResponseEntity<>(file, HttpStatus.OK);
    }

    @PostMapping("/copy")
    public ResponseEntity<File> copyFile(@RequestBody String json) {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String idFile = jsonObject.get("idFile").getAsString();
        String email = jsonObject.get("email").getAsString();
        String filename = jsonObject.get("filename").getAsString();
        String originalName = jsonObject.get("originalName").getAsString();

        String parentFolderId = "root";

        if (jsonObject.get("parentFolderId") != null && !jsonObject.get("parentFolderId").isJsonNull()) {
            parentFolderId = jsonObject.get("parentFolderId").getAsString();
        }

        //Comprovem si ja existeix
        System.out.println("nom original: " + originalName);
        ConvocatoriaDto convocatoria = convocatoriaService.findConvocatoriaActual();
        boolean existDocumentByOriginalName = this.documentService.existDocumentByOriginalName(originalName, convocatoria);

        if (existDocumentByOriginalName) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

        //Si no existeix el copiem
        File file = this.googleDriveService.getFileById(idFile, email);

        if (jsonObject.get("administrators") != null && !jsonObject.get("administrators").isJsonNull()) {
            JsonArray administrators = jsonObject.get("administrators").getAsJsonArray();
            for (JsonElement administrador : administrators) {
                this.googleDriveService.assignPermission(file, PermissionType.USER, PermissionRole.WRITER, administrador.getAsString(), email);
            }
        }

        if (jsonObject.get("editors") != null && !jsonObject.get("editors").isJsonNull() && !environment.equals("dev")) {
            JsonArray editors = jsonObject.get("editors").getAsJsonArray();
            for (JsonElement editor : editors) {
                this.googleDriveService.assignPermission(file, PermissionType.USER, PermissionRole.FILE_ORGANIZER, editor.getAsString(), email);
            }
        }

        File fileCopy = this.googleDriveService.copy(file, email, filename, parentFolderId);

        return new ResponseEntity<>(fileCopy, HttpStatus.OK);
    }

    @GetMapping("/tipusdocument/list")
    public ResponseEntity<List<TipusDocumentDto>> getTipusDocumentList() throws Exception {
        List<TipusDocumentDto> tipusDocumentList = tipusDocumentService.findAll();
        return new ResponseEntity<>(tipusDocumentList, HttpStatus.OK);
    }

    @PostMapping("/document/canviarEstatDocument")
    public ResponseEntity<Notificacio> canviarEstatDocument(@RequestBody String json, @RequestParam(required = false) Long idConvocatoria) throws Exception {
        ConvocatoriaDto convocatoria;
        if (idConvocatoria != null) {
            convocatoria = convocatoriaService.findConvocatoriaById(idConvocatoria);
        } else {
            convocatoria = convocatoriaService.findConvocatoriaActual();
        }

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        Long idDocument = jsonObject.get("idDocument").getAsLong();
        String estat = jsonObject.get("estat").getAsString();

        DocumentDto document = documentService.getDocumentById(idDocument, convocatoria);
        document.setEstat(DocumentEstatDto.valueOf(estat));
        documentService.save(document, convocatoria);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Estat canviat correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @PostMapping("/document/canviarObservacionsDocument")
    public ResponseEntity<Notificacio> canviarObservacionsDocument(@RequestBody String json, @RequestParam(required = false) Long idConvocatoria) throws Exception {
        ConvocatoriaDto convocatoria;
        if (idConvocatoria != null) {
            convocatoria = convocatoriaService.findConvocatoriaById(idConvocatoria);
        } else {
            convocatoria = convocatoriaService.findConvocatoriaActual();
        }

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        Long idDocument = jsonObject.get("idDocument").getAsLong();
        String observacions = jsonObject.get("observacions").getAsString();

        DocumentDto document = documentService.getDocumentById(idDocument, convocatoria);
        document.setObservacions(observacions);
        documentService.save(document, convocatoria);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Observacions desades correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }


    @PostMapping("/document/canviar-visibilitat-document")
    public ResponseEntity<String> canviarVisibilitatDocument(@RequestBody String json, @RequestParam(required = false) Long idConvocatoria) throws Exception {
        ConvocatoriaDto convocatoria;
        if (idConvocatoria != null) {
            convocatoria = convocatoriaService.findConvocatoriaById(idConvocatoria);
        } else {
            convocatoria = convocatoriaService.findConvocatoriaActual();
        }

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        Long idDocument = jsonObject.get("idDocument").getAsLong();
        boolean visibilitat = jsonObject.get("visibilitat").getAsBoolean();

        DocumentDto document = documentService.getDocumentById(idDocument, convocatoria);
        document.setVisibilitat(visibilitat);
        documentService.save(document, convocatoria);

        return new ResponseEntity<>("Canvi fet", HttpStatus.OK);
    }

    @PostMapping("/document/get-url")
    public ResponseEntity<String> getURL(@RequestBody String json, @RequestParam(required = false) Long idConvocatoria) throws Exception {
        ConvocatoriaDto convocatoria;
        if (idConvocatoria != null) {
            convocatoria = convocatoriaService.findConvocatoriaById(idConvocatoria);
        } else {
            convocatoria = convocatoriaService.findConvocatoriaActual();
        }

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        Long idDocument = jsonObject.get("idFile").getAsLong();

        DocumentDto document = documentService.getDocumentById(idDocument, convocatoria);

        Long idFitxer = document.getIdFitxer();
        if (idFitxer != null) {
            String url = documentService.getURLBucket(idFitxer);
            if (url != null) {
                return new ResponseEntity<>(url, HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/document/uploadFile")
    public ResponseEntity<Notificacio> uploadFile(@RequestParam("id") Long idDocument, @RequestParam(required = false) Long idConvocatoria, HttpServletRequest request) throws Exception {
        ConvocatoriaDto convocatoria;
        if (idConvocatoria != null) {
            convocatoria = convocatoriaService.findConvocatoriaById(idConvocatoria);
        } else {
            convocatoria = convocatoriaService.findConvocatoriaActual();
        }

        DocumentDto document = documentService.getDocumentById(idDocument, convocatoria);
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
        String pathArxiu = this.tmpPath + "/arxiu-fct-" + document.getIdDocument() + ".pdf";

        OutputStream outputStream = new FileOutputStream(pathArxiu);
        os.writeTo(outputStream);

        java.io.File f = new java.io.File(pathArxiu);

        //Upload to Core
        String remotePath = "";
        try {
            byte[] fileContent = Files.readAllBytes(f.toPath());
            System.out.println("Name: " + f.getName());
            FileUploadDto fileUploadDTO = new FileUploadDto(f.getName(), fileContent);
            ResponseEntity<String> response = coreRestClient.handleFileUpload2(fileUploadDTO);
            remotePath = response.getBody();
        } catch (IOException e) {
            // Handle the IOException
            //System.out.println(e.getMessage());
        }

        ResponseEntity<FitxerBucketDto> fitxerBucketResponse = coreRestClient.uploadObject(bucketPathFiles + "/fct/" + document.getGrupCodi() + "/" + document.getNomOriginal() + ".pdf", remotePath, "application/pdf", bucketName);
        FitxerBucketDto fitxerBucket = fitxerBucketResponse.getBody();

        //Save the file
        ResponseEntity<FitxerBucketDto> fitxerBucketSavedResponse = coreRestClient.save(fitxerBucket);
        FitxerBucketDto fitxerBucketSaved = fitxerBucketSavedResponse.getBody();

        if (fitxerBucketSaved != null && fitxerBucketSaved.getIdfitxer() != null) {
            document.setIdFitxer(fitxerBucketSaved.getIdfitxer());
            document.setEstat(DocumentEstatDto.PENDENT);
            documentService.save(document, convocatoria);

            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("Arxiu pujat amb èxit.");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);

            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }
        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Error pujant el fitxer." + bucketPathFiles + "/fct/" + document.getGrupCodi() + "/" + document.getNomOriginal());
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);

        return new ResponseEntity<>(notificacio, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/document/migrarDocumentsGenerals")
    public ResponseEntity<Notificacio> migrarDocumentsGenerals() throws Exception {

        List<DocumentGeneralDto> docs = documentService.findAllDocumentsGenerals();

        File parentFolder = googleDriveService.getFolder(userPathDocsMigrats, userEmail, "root");
        System.out.println("parentFolder: " + parentFolder);

        docs.forEach(document -> {
            System.out.println("Provant d'obtenir document amb id: " + document.getIdGoogleDrive() + " amb nom: " + document.getNomOriginal());
            File docFile = googleDriveService.getFileById(document.getIdGoogleDrive(), userEmail);

            List<GrupDto> grups = grupService.findAllWithFempo();

            grups.forEach(grup -> {
                String folderName = grup.getCursGrup() + "_Q_FEMPO";
                File groupFolder = googleDriveService.getFolder(folderName, userEmail, parentFolder.getId());

                if (groupFolder != null) {
                    System.out.println("groupFolder: " + groupFolder.getName());
                    String folderPath = userPathDocsMigrats+"/"+folderName;
                    String nomDocument = grup.getCursGrup() + "_" + docFile.getName();
                    try {
                        List<File> existingFiles = googleDriveService.getFilesInFolder(folderPath,userEmail);
                        if (!fileAlreadyExist(nomDocument, existingFiles)) {
                            googleDriveService.copy(docFile,userEmail,nomDocument,groupFolder.getId());
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Error obtenint documents de la carpeta " + folderPath);
                    }
                }
            });
        });

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Els documents generals s'han migrat a les carpetes de grup FEMPO");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);

        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    private boolean fileAlreadyExist(String fileName, List<File> folderFiles) {
        return folderFiles.stream().anyMatch(folderFile -> folderFile.getName().equals(fileName));
    }
}
