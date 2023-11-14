package cat.politecnicllevant.gestsuitegestordocumental.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class GoogleDriveService {
    @Value("${gc.keyfile}")
    private String keyFile;

    @Value("${gc.adminUser}")
    private String adminUser;

    @Value("${gc.nomprojecte}")
    private String nomProjecte;

    public void prova() throws IOException, GeneralSecurityException {
        String[] scopes = {DriveScopes.DRIVE_METADATA_READONLY};

        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Drive service = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }
}
