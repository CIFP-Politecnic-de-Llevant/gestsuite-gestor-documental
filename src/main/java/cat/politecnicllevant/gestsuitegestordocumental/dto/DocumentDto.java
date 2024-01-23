package cat.politecnicllevant.gestsuitegestordocumental.dto;

import cat.politecnicllevant.gestsuitegestordocumental.domain.DocumentSignatura;
import cat.politecnicllevant.gestsuitegestordocumental.domain.TipusDocument;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class DocumentDto {
    private Long idDocument;
    private String nomOriginal;

    //GOOGLE DRIVE
    private String idGoogleDrive;
    private String idDriveGoogleDrive;
    private String pathGoogleDrive;
    private String ownerGoogleDrive;
    private String mimeTypeGoogleDrive;
    private String createdTimeGoogleDrive;
    private String modifiedTimeGoogleDrive;
    private String grupCodi;

    //MS CORE
    private Long idUsuari;

    //FK
    private TipusDocumentDto tipusDocument;
    private Set<DocumentSignaturaDto> documentSignatures;
}
