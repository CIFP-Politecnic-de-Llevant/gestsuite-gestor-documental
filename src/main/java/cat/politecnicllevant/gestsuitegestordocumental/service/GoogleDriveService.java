package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.MimeType;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionRole;
import cat.politecnicllevant.gestsuitegestordocumental.domain.PermissionType;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {
    @Value("${gc.keyfile}")
    private String keyFile;

    @Value("${gc.adminUser}")
    private String adminUser;

    @Value("${gc.nomprojecte}")
    private String nomProjecte;

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

    public List<File> getFilesInFolder(String path) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            //GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated("qualitat@politecnicllevant.cat");
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);

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
                        .setFields("files(id, name)")
                        .execute();

                List<File> files = query.getFiles();
                String pageToken = query.getNextPageToken();

                if(files!=null) {
                    result.addAll(files);

                    while (pageToken != null) {
                        FileList query2 = service.files().list()
                                .setQ("'" + folderId + "' in parents and not trashed")
                                .setFields("files(id, name)")
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
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public File createFolder(String path, String folderName) {
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            String[] folders = path.split("/");
            String folderId = "root";

            if(!path.isEmpty()) {
                for (String folder : folders) {
                    folderId = getFolderIdByNameAndIdParent(service, folder, folderId);
                }
            }

            String folderIdTarget = getFolderIdByNameAndIdParent(service, folderName, folderId);

            // Get the folder ID by querying for the folder with the given name.
            //String folderId = getFolderIdByName(service, folderName);
            System.out.println("folderId: "+folderId);
            if (folderId != null && (folderIdTarget == null)) {
                File fileMetadata = new File();
                fileMetadata.setName(folderName);
                fileMetadata.setParents(Collections.singletonList(folderId));
                fileMetadata.setMimeType(MimeType.FOLDER.toString());

                File file = service.files().create(fileMetadata)
                        .setFields("id")
                        .execute();

                return file;
            } else {
                System.out.println("Folder not created. Folder already exists in this path: " + path);
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateFile(File file){
        try {
            String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE};
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

            Permission permission = new Permission();
            permission.setEmailAddress("csorell@politecnicllevant.cat");
            permission.setType(PermissionType.USER.toString());
            permission.setRole(PermissionRole.WRITER.toString());

            service.permissions().create(file.getId(),permission).execute();

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private static String getFolderIdByName(Drive service, String folderName) throws IOException {
        System.out.println("getFolderIdByName: "+folderName);

        FileList result = service.files().list()
                .setQ("mimeType='"+MimeType.FOLDER+"' and name='" + folderName + "' and not trashed")
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
        System.out.println("getFolderIdByName: "+folderName);

        FileList result = service.files().list()
                .setQ("mimeType='"+MimeType.FOLDER+"' and name='" + folderName + "' and '"+idParent+"' in parents and not trashed")
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

}
