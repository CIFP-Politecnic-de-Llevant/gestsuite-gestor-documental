package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.gestsuitegestordocumental.service.GoogleDriveService;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
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
        this.googleDriveService.createFolder("root","Test");
        this.googleDriveService.createFolder("Test","prova inside");
        this.googleDriveService.createFolder("Test/prova inside","prova inside 2");
        List<File> files = this.googleDriveService.getFilesInFolder("Test/prova inside/prova inside 2");
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:"+files.size());
            for (File file : files) {
                User user = new User();
                user.setEmailAddress("jgalmes@politecnicllevant.cat");

                User user2 = new User();
                user.setEmailAddress("csorell@politecnicllevant.cat");

                List<User> propietaris = new ArrayList<>();
                propietaris.add(user);
                propietaris.add(user2);

                file.setOwners(propietaris);
                System.out.println("name: "+file.getName());
                System.out.println("id: "+file.getId());

            }
        }
    }
}
