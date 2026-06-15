package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.MimeType;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionRole;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionType;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GoogleDriveService {
    @Value("${gc.keyfile}")
    private String keyFile;

    @Value("${gc.adminUser}")
    private String adminUser;

    @Value("${gc.nomprojecte}")
    private String nomProjecte;

    @Value("${app.google.drive.shared.id}")
    private String sharedDriveId;

    @Value("${app.google.drive.spreadsheet.shared.id}")
    private String shareSpredaSheetId;

    @Value("${app.google.drive.user.email}")
    private String driveUserEmail;

    public void prova() throws IOException, GeneralSecurityException {
        String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};

        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);
        //GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated("qualitat@politecnicllevant.cat");
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setFields("nextPageToken, files(id, name,webViewLink,fullFileExtension)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.println("name: "+file.getName());
                System.out.println("id: "+file.getId());
                System.out.println("getFullFileExtension: "+file.getFullFileExtension());
                System.out.println("getDriveId: "+file.getDriveId());
                System.out.println("getOriginalFilename: "+file.getOriginalFilename());
                System.out.println("getWebContentLink: "+file.getWebContentLink());
                System.out.println("getWebViewLink: "+file.getWebViewLink());

            }
        }

        //Create folder
        File fileMetadata = new File();
        fileMetadata.setName("Test");
        //fileMetadata.setParents()
        fileMetadata.setMimeType(MimeType.FOLDER.toString());

        File file = service.files().create(fileMetadata)
            .setFields("id")
            .execute();
    }

    public List<File> getFilesInFolder(String path,String user) throws InterruptedException {
        return getFilesInFolder(path, user, 0);
    }

    private List<File> getFilesInFolder(String path,String user, int retry) throws InterruptedException {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);

            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            String[] folders = path.split("/");
            String folderId = "root";

            for (String folder : folders) {
                folderId = getFolderIdByNameAndIdParent(service, folder, folderId);
            }

            // Get the folder ID by querying for the folder with the given name.
            System.out.println("folderId: "+folderId);
            if (folderId != null) {
                List<File> result = new ArrayList<>();

                // List files in the specified folder.
                FileList query = service.files().list()
                        .setQ("'" + folderId + "' in parents and not trashed")
                        .setSupportsAllDrives(true)
                        .setFields("nextPageToken, files(id,name,owners,mimeType,createdTime,modifiedTime,webViewLink,fullFileExtension,driveId,originalFilename,webContentLink)")
                        .setPageSize(1000)
                        .execute();

                List<File> files = query.getFiles();
                String pageToken = query.getNextPageToken();
                log.info("PAGE TOKEN getFilesInFolder: "+pageToken);

                if(files!=null) {
                    result.addAll(files);

                    while (pageToken != null) {
                        FileList query2 = service.files().list()
                                .setQ("'" + folderId + "' in parents and not trashed")
                                .setSupportsAllDrives(true)
                                .setFields("nextPageToken, files(id,name,owners,mimeType,createdTime,modifiedTime,webViewLink,fullFileExtension,driveId,originalFilename,webContentLink)")
                                .setPageToken(pageToken)
                                .execute();
                        List<File> files2 = query2.getFiles();
                        pageToken = query2.getNextPageToken();

                        if (files2 != null) {
                            System.out.println("files2: "+files2.size());
                            result.addAll(files2);
                        }
                    }
                }

                return result;
            } else {
                System.out.println("Folder not found in this path: " + path);
            }
        } catch (GeneralSecurityException | IOException e) {
            if (retry < 5) {
                TimeUnit.MILLISECONDS.sleep(((2 ^ retry) * 1000L) + getRandomMilliseconds());
                return getFilesInFolder(path, user,retry + 1);
            }
            log.error("Error aconseguint documents de la carpeta "+path+" i usuari "+user, e);
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<File> getFilesInFolderById(String folderId, String user) {
        return getFilesInFolderById(folderId, user, 0);
    }

    private List<File> getFilesInFolderById(String folderId, String user, int retry) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);

            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            List<File> result = new ArrayList<>();

            FileList query = service.files().list()
                    .setQ("'" + folderId + "' in parents and not trashed")
                    .setSupportsAllDrives(true)
                    .setIncludeItemsFromAllDrives(true)
                    .setCorpora("drive")
                    .setDriveId(this.sharedDriveId)
                    .setFields("nextPageToken, files(id,name,owners,mimeType,createdTime,modifiedTime,webViewLink,fullFileExtension,driveId,originalFilename,webContentLink)")
                    .setPageSize(1000)
                    .execute();

            List<File> files = query.getFiles();
            String pageToken = query.getNextPageToken();

            if (files != null) {
                result.addAll(files);

                while (pageToken != null) {
                    FileList query2 = service.files().list()
                            .setQ("'" + folderId + "' in parents and not trashed")
                            .setSupportsAllDrives(true)
                            .setIncludeItemsFromAllDrives(true)
                            .setCorpora("drive")
                            .setDriveId(this.sharedDriveId)
                            .setFields("nextPageToken, files(id,name,owners,mimeType,createdTime,modifiedTime,webViewLink,fullFileExtension,driveId,originalFilename,webContentLink)")
                            .setPageToken(pageToken)
                            .execute();
                    List<File> files2 = query2.getFiles();
                    pageToken = query2.getNextPageToken();

                    if (files2 != null) {
                        result.addAll(files2);
                    }
                }
            }

            return result;
        } catch (GeneralSecurityException | IOException e) {
            if (retry < 5) {
                try {
                    TimeUnit.MILLISECONDS.sleep(((2 ^ retry) * 1000L) + getRandomMilliseconds());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return Collections.emptyList();
                }
                return getFilesInFolderById(folderId, user, retry + 1);
            }
            log.error("Error aconseguint documents de la carpeta amb id " + folderId + " i usuari " + user, e);
        }
        return Collections.emptyList();
    }

    public void deleteFileById(String id, String user) {
        deleteFileByIdInternal(id, user);
    }

    public void deleteFileByIdWithOwnerFallback(String id, String user, String ownerEmail) {
        boolean deleted = deleteFileByIdInternal(id, user);
        if (!deleted && ownerEmail != null && !ownerEmail.isBlank() && !ownerEmail.equalsIgnoreCase(user)) {
            log.warn("Retrying delete for file {} as owner {}", id, ownerEmail);
            deleteFileByIdInternal(id, ownerEmail);
        }
    }

    private boolean deleteFileByIdInternal(String id, String user) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            service.files().delete(id)
                    .setSupportsAllDrives(true)
                    .execute();
            return true;
        } catch (GoogleJsonResponseException e) {
            log.error("Error deleting file {} as {}", id, user, e);
            return false;
        } catch (IOException | GeneralSecurityException e) {
            log.error("Error deleting file {} as {}", id, user, e);
            return false;
        }
    }

    public File getFolder(String folderName, String user, String idParent) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            String idFolder = getFolderIdByNameAndIdParent(service, folderName, idParent,this.sharedDriveId);

            if(idFolder==null){
                return null;
            }
            return service.files().get(idFolder).setSupportsAllDrives(true).execute();
        } catch (IOException | GeneralSecurityException e) {
            //e.printStackTrace();
            log.error("Error al obtenir la carpeta", e);
        }
        return null;
    }

    public File createFolder(String folderName, String user, String idParent) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            String idCurrent = getFolderIdByNameAndIdParent(service, folderName, idParent,this.sharedDriveId);

            System.out.println("idCurrent"+idCurrent+"-idParent: "+idParent);

            if (idCurrent == null) {
                File fileMetadata = new File();
                fileMetadata.setName(folderName);
                fileMetadata.setParents(Collections.singletonList(idParent));
                fileMetadata.setMimeType(MimeType.FOLDER.toString());

                System.out.println("Folder created with name "+folderName+" and idParent "+idParent);
                File createdFile = service.files().create(fileMetadata)
                        .setSupportsAllDrives(true)
                        .setFields("id")
                        .execute();
                
                // Afegim la propietat només a l'objecte retornat, no es persisteix a Google Drive
                createdFile.setAppProperties(Collections.singletonMap("isNewFolder", "true"));
                return createdFile;
            } else {
                System.out.println("Folder not created. Folder already exists");
                // Recuperem la carpeta existent i afegim la propietat per a ús intern
                File existingFile = service.files().get(idCurrent).setSupportsAllDrives(true).setFields("id").execute();
                existingFile.setAppProperties(Collections.singletonMap("isNewFolder", "false"));
                return existingFile;
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File createFolderInSharedDriveRoot(String folderName) {
        File folder = createFolder(folderName, this.driveUserEmail, this.sharedDriveId);
        if (folder == null) {
            throw new IllegalStateException("No s'ha pogut crear la carpeta " + folderName + " a la unitat compartida");
        }
        if (folder.getAppProperties() != null && "false".equals(folder.getAppProperties().get("isNewFolder"))) {
            throw new IllegalStateException("Ja existeix una carpeta amb nom " + folderName + " a la unitat compartida");
        }
        return folder;
    }

    public void deleteFolderInSharedDriveRoot(String folderName) {
        deleteFolder(folderName, this.driveUserEmail, this.sharedDriveId);
    }

    public void deleteFileByIdInSharedDrive(String fileId) {
        if (fileId == null || fileId.isBlank()) {
            throw new IllegalStateException("No s'ha proporcionat cap fileId per eliminar a la unitat compartida");
        }
        boolean deleted = deleteFileByIdInternal(fileId, this.driveUserEmail);
        if (!deleted) {
            throw new IllegalStateException("No s'ha pogut eliminar l'element amb id '" + fileId + "' de la unitat compartida");
        }
    }

    public File renameFolderInSharedDriveRoot(String oldFolderName, String newFolderName) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.driveUserEmail);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();
            assertSharedDriveAccessible(service);

            String folderId = getFolderIdByNameAndIdParent(service, oldFolderName, this.sharedDriveId, this.sharedDriveId);
            if (folderId == null) {
                throw new IllegalStateException("No s'ha trobat la carpeta '" + oldFolderName + "' a l'arrel de la unitat compartida '" + this.sharedDriveId + "' amb l'usuari delegat '" + this.driveUserEmail + "'. Comprova que la carpeta existeix a l'arrel i que aquest usuari és membre de la unitat compartida.");
            }

            if (!Objects.equals(oldFolderName, newFolderName)) {
                String existingTargetFolderId = getFolderIdByNameAndIdParent(service, newFolderName, this.sharedDriveId, this.sharedDriveId);
                if (existingTargetFolderId != null) {
                    throw new IllegalStateException("Ja existeix una carpeta amb nom " + newFolderName + " a la unitat compartida");
                }
            }

            File fileMetadata = new File();
            fileMetadata.setName(newFolderName);

            return service.files()
                    .update(folderId, fileMetadata)
                    .setSupportsAllDrives(true)
                    .execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("No s'ha pogut reanomenar la carpeta " + oldFolderName + " a " + newFolderName, e);
        }
    }

    public void clonePermissionsBetweenSharedDriveRootFolders(String sourceFolderName, String targetFolderName) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.driveUserEmail);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();
            assertSharedDriveAccessible(service);

            String sourceFolderId = getFolderIdByNameAndIdParent(service, sourceFolderName, this.sharedDriveId, this.sharedDriveId);
            if (sourceFolderId == null) {
                throw new IllegalStateException("No s'ha trobat la carpeta origen '" + sourceFolderName + "' a l'arrel de la unitat compartida '" + this.sharedDriveId + "' amb l'usuari delegat '" + this.driveUserEmail + "'.");
            }

            String targetFolderId = getFolderIdByNameAndIdParent(service, targetFolderName, this.sharedDriveId, this.sharedDriveId);
            if (targetFolderId == null) {
                throw new IllegalStateException("No s'ha trobat la carpeta destí '" + targetFolderName + "' a l'arrel de la unitat compartida '" + this.sharedDriveId + "' amb l'usuari delegat '" + this.driveUserEmail + "'.");
            }

            PermissionList sourcePermissions = service.permissions().list(sourceFolderId)
                    .setSupportsAllDrives(true)
                    .setFields("permissions(id,type,role,emailAddress,domain,allowFileDiscovery)")
                    .execute();

            PermissionList targetPermissions = service.permissions().list(targetFolderId)
                    .setSupportsAllDrives(true)
                    .setFields("permissions(id,type,role,emailAddress,domain,allowFileDiscovery)")
                    .execute();

            Set<String> targetPermissionKeys = new HashSet<>();
            if (targetPermissions.getPermissions() != null) {
                for (Permission permission : targetPermissions.getPermissions()) {
                    targetPermissionKeys.add(buildPermissionKey(permission));
                }
            }

            if (sourcePermissions.getPermissions() == null) {
                return;
            }

            for (Permission sourcePermission : sourcePermissions.getPermissions()) {
                if ("owner".equals(sourcePermission.getRole())) {
                    continue;
                }

                String permissionKey = buildPermissionKey(sourcePermission);
                if (permissionKey == null || targetPermissionKeys.contains(permissionKey)) {
                    continue;
                }

                Permission newPermission = new Permission();
                newPermission.setType(sourcePermission.getType());
                newPermission.setRole(sourcePermission.getRole());

                if ("user".equals(sourcePermission.getType()) || "group".equals(sourcePermission.getType())) {
                    if (sourcePermission.getEmailAddress() == null || sourcePermission.getEmailAddress().isBlank()) {
                        continue;
                    }
                    newPermission.setEmailAddress(sourcePermission.getEmailAddress());
                } else if ("domain".equals(sourcePermission.getType())) {
                    if (sourcePermission.getDomain() == null || sourcePermission.getDomain().isBlank()) {
                        continue;
                    }
                    newPermission.setDomain(sourcePermission.getDomain());
                    newPermission.setAllowFileDiscovery(sourcePermission.getAllowFileDiscovery());
                } else if ("anyone".equals(sourcePermission.getType())) {
                    newPermission.setAllowFileDiscovery(sourcePermission.getAllowFileDiscovery());
                } else {
                    continue;
                }

                service.permissions()
                        .create(targetFolderId, newPermission)
                        .setSupportsAllDrives(true)
                        .setSendNotificationEmail(false)
                        .execute();
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("No s'han pogut clonar els permisos de la carpeta " + sourceFolderName + " a " + targetFolderName, e);
        }
    }

    public void clonePermissionsBetweenFolderIds(String sourceFolderId, String targetFolderId) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.driveUserEmail);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();
            assertSharedDriveAccessible(service);

            PermissionList sourcePermissions = service.permissions().list(sourceFolderId)
                    .setSupportsAllDrives(true)
                    .setFields("permissions(id,type,role,emailAddress,domain,allowFileDiscovery)")
                    .execute();

            PermissionList targetPermissions = service.permissions().list(targetFolderId)
                    .setSupportsAllDrives(true)
                    .setFields("permissions(id,type,role,emailAddress,domain,allowFileDiscovery)")
                    .execute();

            Set<String> targetPermissionKeys = new HashSet<>();
            if (targetPermissions.getPermissions() != null) {
                for (Permission permission : targetPermissions.getPermissions()) {
                    targetPermissionKeys.add(buildPermissionKey(permission));
                }
            }

            if (sourcePermissions.getPermissions() == null) {
                return;
            }

            for (Permission sourcePermission : sourcePermissions.getPermissions()) {
                if ("owner".equals(sourcePermission.getRole())) {
                    continue;
                }

                String permissionKey = buildPermissionKey(sourcePermission);
                if (permissionKey == null || targetPermissionKeys.contains(permissionKey)) {
                    continue;
                }

                Permission newPermission = new Permission();
                newPermission.setType(sourcePermission.getType());
                newPermission.setRole(sourcePermission.getRole());

                if ("user".equals(sourcePermission.getType()) || "group".equals(sourcePermission.getType())) {
                    if (sourcePermission.getEmailAddress() == null || sourcePermission.getEmailAddress().isBlank()) {
                        continue;
                    }
                    newPermission.setEmailAddress(sourcePermission.getEmailAddress());
                } else if ("domain".equals(sourcePermission.getType())) {
                    if (sourcePermission.getDomain() == null || sourcePermission.getDomain().isBlank()) {
                        continue;
                    }
                    newPermission.setDomain(sourcePermission.getDomain());
                    newPermission.setAllowFileDiscovery(sourcePermission.getAllowFileDiscovery());
                } else if ("anyone".equals(sourcePermission.getType())) {
                    newPermission.setAllowFileDiscovery(sourcePermission.getAllowFileDiscovery());
                } else {
                    continue;
                }

                service.permissions()
                        .create(targetFolderId, newPermission)
                        .setSupportsAllDrives(true)
                        .setSendNotificationEmail(false)
                        .execute();
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("No s'han pogut clonar els permisos de la carpeta origen '" + sourceFolderId + "' a la carpeta destí '" + targetFolderId + "'", e);
        }
    }

    public void deleteFolder(String folderName, String user, String idParent) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            String folderId = getFolderIdByNameAndIdParent(service, folderName, idParent, this.sharedDriveId);

            service.files().delete(folderId).setSupportsAllDrives(true).execute();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public List<File> getSubfoldersInFolder(String parentPath, String suffix, String user) throws InterruptedException {
        return getSubfoldersInFolder(parentPath, suffix, user, 0);
    }

    private List<File> getSubfoldersInFolder(String parentPath, String suffix, String user, int retry) throws InterruptedException {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            String[] folders = parentPath.split("/");
            String folderId = "root";
            for (String folder : folders) {
                folderId = getFolderIdByNameAndIdParent(service, folder, folderId);
            }

            if (folderId == null) {
                log.warn("Parent folder not found: {}", parentPath);
                return Collections.emptyList();
            }

            FileList result = service.files().list()
                    .setQ("mimeType='" + MimeType.FOLDER + "' and '" + folderId + "' in parents and not trashed")
                    .setSupportsAllDrives(true)
                    .setFields("files(id,name)")
                    .setPageSize(1000)
                    .execute();

            List<File> allFolders = result.getFiles();
            if (allFolders == null) {
                return Collections.emptyList();
            }

            if (suffix != null && !suffix.isEmpty()) {
                List<File> filtered = new ArrayList<>();
                for (File f : allFolders) {
                    if (f.getName().endsWith(suffix)) {
                        filtered.add(f);
                    }
                }
                return filtered;
            }
            return allFolders;
        } catch (GeneralSecurityException | IOException e) {
            if (retry < 5) {
                TimeUnit.MILLISECONDS.sleep(((2 ^ retry) * 1000L) + getRandomMilliseconds());
                return getSubfoldersInFolder(parentPath, suffix, user, retry + 1);
            }
            log.error("Error llistant subcarpetes de {} amb suffix {}", parentPath, suffix, e);
        }
        return Collections.emptyList();
    }

    public void deleteAllFilesInFolder(String path, String user) throws InterruptedException {
        List<File> files = getFilesInFolder(path, user);
        for (File file : files) {
            try {
                deleteFileByIdInternal(file.getId(), user);
            } catch (Exception e) {
                log.error("Error esborrant fitxer {} ({}) de la carpeta {}", file.getName(), file.getId(), path, e);
            }
        }
    }

    public void deleteFolderByPath(String path, String user) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            String[] folders = path.split("/");
            String folderId = "root";
            for (String folder : folders) {
                folderId = getFolderIdByNameAndIdParent(service, folder, folderId);
            }

            if (folderId == null) {
                log.warn("Folder not found for deletion: {}", path);
                return;
            }

            service.files().delete(folderId).setSupportsAllDrives(true).execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("No s'ha pogut esborrar la carpeta " + path, e);
        }
    }

    public void assignPermission(File file,PermissionType permissionType, PermissionRole permissionRole, String email, String user){
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            Permission permission = new Permission();
            permission.setEmailAddress(email);
            permission.setType(permissionType.toString());
            permission.setRole(permissionRole.toString());

            //Evitem notificar per correu
            service.permissions().create(file.getId(),permission).setSupportsAllDrives(true).setSendNotificationEmail(false).execute();

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public File getFileById(String id, String user){
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            return service.files().get(id).setSupportsAllDrives(true).execute();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File copy(File file, String user, String newFileName,String parentFolderId) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            //String parentFolderId = getFolderIdByNameAndIdParent(service, "JOAN FCT RESOLT", "root");

            // Create a new file in the destination folder
            File copiedFile = new File();
            copiedFile.setName(newFileName);
            copiedFile.setParents(java.util.Collections.singletonList(parentFolderId));

            // Copy the content of the original file to the new file
            System.out.println("File copied successfully!");
            return service.files().copy(file.getId(), copiedFile).setSupportsAllDrives(true).execute();

           /* service.files()
                    .copy("1vYTHBpr0I9Nh6JoiiaJsPljJZcvVuK2j", file)
                    .setSupportsAllDrives(true)
                    .execute();*/
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getFolderIdByName(Drive service, String folderName) throws IOException {
        System.out.println("getFolderIdByName: "+folderName);

        FileList result = service.files().list()
                .setQ("mimeType='"+MimeType.FOLDER+"' and name='" + folderName + "' and not trashed")
                .setSupportsAllDrives(true)
                .setFields("files(id)")
                .execute();

        List<File> folders = result.getFiles();
        if (folders != null && !folders.isEmpty()) {
            System.out.println("Hi ha resultat");
            return folders.get(0).getId();
        }
        System.out.println("No hi ha resultat");
        return null;
    }

    private static String getFolderIdByNameAndIdParent(Drive service, String folderName, String idParent) throws IOException {
        return getFolderIdByNameAndIdParent(service,folderName,idParent,null);
    }

    private static String getFolderIdByNameAndIdParent(Drive service, String folderName, String idParent, String idSharedDrive) throws IOException {
        System.out.println("getFolderIdByName: "+folderName+"-idParent: "+idParent);

        com.google.api.services.drive.Drive.Files.List resultPartial = service.files().list()
                .setQ("mimeType='"+MimeType.FOLDER+"' and name='" + folderName + "' and '"+idParent+"' in parents and not trashed")
                .setFields("files(id)");

        if(idSharedDrive!=null){
            resultPartial.setCorpora("drive");
            resultPartial.setDriveId(idSharedDrive);
            resultPartial.setIncludeItemsFromAllDrives(true);
            resultPartial.setSupportsAllDrives(true);
        }

        FileList result = resultPartial.execute();

        List<File> folders = result.getFiles();
        if (folders != null && !folders.isEmpty()) {
            System.out.println("Hi ha resultat. Size:"+folders.size());
            return folders.get(0).getId();
        }
        System.out.println("No hi ha resultat");
        return null;
    }

    //SpreadSheet

    public int getLastDataRow(String idSpreadSheet) throws IOException, GeneralSecurityException {

        String range = "A:Z";
        String[] scopes = {SheetsScopes.SPREADSHEETS_READONLY};
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Sheets sheetsService = new Sheets.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer)
                .setApplicationName(this.nomProjecte)
                .build();

        ValueRange response = sheetsService.spreadsheets().values()
                .get(idSpreadSheet, range)
                .execute();


        List<List<Object>> values = response.getValues();
        int lastRowIndex = values != null ? values.size() : 0;

        if (lastRowIndex > 0) {
            for (int i = lastRowIndex - 1; i >= 0; i--) {
                List<Object> row = values.get(i);
                if (row != null && !row.isEmpty()) {
                    lastRowIndex = i + 1;
                    break;
                }
            }
        }
        return lastRowIndex;
    }

    public void uploadFile(String pathParent, String user, java.io.File file, String fileName) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(user);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            File fileMetadata = new File();
            //fileMetadata.setName(file.getName());
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(pathParent));
            String mimeType= URLConnection.guessContentTypeFromName(file.getName());

            fileMetadata.setMimeType(mimeType);

            java.io.File filePath = new java.io.File(file.getAbsolutePath());
            com.google.api.client.http.FileContent mediaContent = new com.google.api.client.http.FileContent(mimeType, filePath);

            service.files().create(fileMetadata, mediaContent)
                    .setSupportsAllDrives(true)
                    .execute();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public void writeData(Map<String, String> gettersDataForm) throws IOException, GeneralSecurityException {

        int startRowIndex = getLastDataRow(this.shareSpredaSheetId) + 1;
        String range = "A" + startRowIndex + ":BZ";

        String[] scopes = {SheetsScopes.SPREADSHEETS};
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Sheets sheetsService = new Sheets.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer)
                .setApplicationName(this.nomProjecte)
                .build();

        List<List<Object>> valuesToWrite = new ArrayList<>();

        //Escriure headers
        if (startRowIndex == 1) {
            List<Object> headerRow = new ArrayList<>(gettersDataForm.keySet());
            valuesToWrite.add(headerRow);
        }
        //Escriure les dades
        List<Object> dataRow = new ArrayList<>();
        for (Map.Entry<String, String> entry : gettersDataForm.entrySet()) {
            String value = "";
            if(entry.getValue()!=null) {
                value = entry.getValue();
            }

            //Aqui dona fallo, arreglar-lo
            if(value.equals("true")){
                value = "Si";
            } else if (value.equals("false")) {
                value ="No";
            }
            System.out.println("Datos en el for =  " + value);
            dataRow.add(value);
        }
        valuesToWrite.add(dataRow);

        ValueRange requestBody = new ValueRange().setValues(valuesToWrite);

        sheetsService.spreadsheets().values()
                .update(this.shareSpredaSheetId, range, requestBody)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }


    public void writeDataPosition(Map<String, String> gettersDataForm, String idSpreadSheet) throws IOException, GeneralSecurityException {

        int startRowIndex = getLastDataRow(idSpreadSheet) + 1;
        String range = "A" + startRowIndex + ":CZ"+startRowIndex;
        String rangeHeader = "A1:CZ1";

        String[] scopes = {SheetsScopes.SPREADSHEETS};
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Sheets sheetsService = new Sheets.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer)
                .setApplicationName(this.nomProjecte)
                .build();

        ValueRange header = sheetsService.spreadsheets().values().get(idSpreadSheet, rangeHeader).execute();
        List<List<Object>> valuesHeader = header.getValues();
        List<Object> headerRow = valuesHeader.get(0);
        int lastColumnIndex = valuesHeader != null ? valuesHeader.get(0).size() : 0;

        ValueRange data = sheetsService.spreadsheets().values().get(idSpreadSheet, range).execute();
        List<List<Object>> valuesData = data.getValues();
        int lastRowIndex = valuesData != null ? valuesData.size() : 0;

        List<List<Object>> valuesToWrite = new ArrayList<>();

        List<Object> dataRow = new ArrayList<>();

        System.out.println("Row Header = " + headerRow);

        // Compare header value with key entry
        for(Object rowHeader: headerRow){
            boolean trobat = false;
            for (Map.Entry<String, String> entry : gettersDataForm.entrySet()) {
                //System.out.println("Values = <" + entry.getKey()+"> --- <"+entry.getValue()+"> - <"+rowHeader+">");
                if(entry.getKey() != null && rowHeader != null && entry.getKey().trim().equals(rowHeader.toString().trim())) {
                    dataRow.add(entry.getValue());
                    trobat = true;
                    break;
                }
            }
            if(!trobat){
                dataRow.add("");
            }
            //System.out.println("Datos en el for22 =  " + dataRow);
        }
        System.out.println("Datos en el dataRow =  " + dataRow);
        System.out.println("Datos en el gettersDataForm =  " + gettersDataForm);

        valuesToWrite.add(dataRow);

        ValueRange requestBody = new ValueRange().setValues(valuesToWrite);

        System.out.println("RequestBody = " + requestBody);

        sheetsService.spreadsheets().values()
                .update(idSpreadSheet, range, requestBody)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    private int getRandomMilliseconds() {
        Random r = new Random();
        int low = 0;
        int high = 1000;
        int result = r.nextInt(high - low) + low;
        return result;
    }

    private void assertSharedDriveAccessible(Drive service) {
        try {
            service.drives()
                    .get(this.sharedDriveId)
                    .execute();
        } catch (GoogleJsonResponseException e) {
            throw new IllegalStateException("No es pot accedir a la unitat compartida '" + this.sharedDriveId + "' amb l'usuari delegat '" + this.driveUserEmail + "'. Comprova que la shared drive existeix i que aquest usuari n'és membre amb permisos suficients.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Error accedint a la unitat compartida '" + this.sharedDriveId + "' amb l'usuari delegat '" + this.driveUserEmail + "'.", e);
        }
    }

    private String buildPermissionKey(Permission permission) {
        if (permission == null || permission.getType() == null || permission.getRole() == null) {
            return null;
        }

        if ("user".equals(permission.getType()) || "group".equals(permission.getType())) {
            if (permission.getEmailAddress() == null) {
                return null;
            }
            return permission.getType() + ":" + permission.getEmailAddress() + ":" + permission.getRole();
        }

        if ("domain".equals(permission.getType())) {
            if (permission.getDomain() == null) {
                return null;
            }
            return permission.getType() + ":" + permission.getDomain() + ":" + permission.getRole();
        }

        if ("anyone".equals(permission.getType())) {
            return permission.getType() + ":" + permission.getRole();
        }

        return null;
    }

}
