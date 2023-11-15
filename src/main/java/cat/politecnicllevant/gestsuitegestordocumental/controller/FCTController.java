package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.gestsuitegestordocumental.service.GoogleDriveService;
import com.google.api.services.drive.model.File;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
public class FCTController {

    private final GoogleDriveService googleDriveService;

    public FCTController(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    @GetMapping("/prova")
    public void prova() throws GeneralSecurityException, IOException {
        //this.googleDriveService.prova();
        List<File> files = this.googleDriveService.getFilesInFolder("IES Manacor/parcel");
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:"+files.size());
            for (com.google.api.services.drive.model.File file : files) {
                System.out.println("name: "+file.getName());
                System.out.println("id: "+file.getId());

            }
        }
    }
}
