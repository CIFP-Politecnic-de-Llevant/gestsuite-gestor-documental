package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.gestsuitegestordocumental.service.GoogleDriveService;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class FCTController {

    private final GoogleDriveService googleDriveService;

    public FCTController(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    @GetMapping("/prova")
    public void prova() throws GeneralSecurityException, IOException {
        this.googleDriveService.prova();
    }
}
