package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentGeneralDto;
import cat.politecnicllevant.gestsuitegestordocumental.service.DocumentGeneralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class DocumentGeneralController {

    private final DocumentGeneralService documentGeneralService;

    @GetMapping("/document-general")
    public ResponseEntity<List<DocumentGeneralDto>> findAll() {
        return new ResponseEntity<>(documentGeneralService.findAll(), HttpStatus.OK);
    }

    @PostMapping("/document-general")
    public ResponseEntity<Notificacio> save(@RequestBody DocumentGeneralDto documentGeneralDto) {
        Notificacio notificacio = new Notificacio();

        DocumentGeneralService.CreateResult result = documentGeneralService.create(documentGeneralDto);
        if (result == DocumentGeneralService.CreateResult.CONFLICT) {
            notificacio.setNotifyMessage("Ja existeix un document general amb aquest ID Google Drive");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.CONFLICT);
        }

        notificacio.setNotifyMessage("Document general guardat");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @PutMapping("/document-general/{idGoogleDrive}")
    public ResponseEntity<Notificacio> update(@PathVariable String idGoogleDrive, @RequestBody DocumentGeneralDto documentGeneralDto) {
        Notificacio notificacio = new Notificacio();

        DocumentGeneralService.UpdateResult result = documentGeneralService.update(idGoogleDrive, documentGeneralDto);

        if (result == DocumentGeneralService.UpdateResult.NOT_FOUND) {
            notificacio.setNotifyMessage("No s ha trobat el document general");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.NOT_FOUND);
        }

        if (result == DocumentGeneralService.UpdateResult.CONFLICT) {
            notificacio.setNotifyMessage("Ja existeix un document general amb aquest ID Google Drive");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.CONFLICT);
        }

        notificacio.setNotifyMessage("Document general actualitzat");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @DeleteMapping("/document-general/{idGoogleDrive}")
    public ResponseEntity<Notificacio> delete(@PathVariable String idGoogleDrive) {
        Notificacio notificacio = new Notificacio();

        boolean deleted = documentGeneralService.deleteById(idGoogleDrive);
        if (deleted) {
            notificacio.setNotifyMessage("Document general eliminat");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }

        notificacio.setNotifyMessage("No s ha trobat el document general");
        notificacio.setNotifyType(NotificacioTipus.ERROR);
        return new ResponseEntity<>(notificacio, HttpStatus.NOT_FOUND);
    }
}
