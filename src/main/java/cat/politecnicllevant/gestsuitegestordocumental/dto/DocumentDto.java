package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

import java.util.Set;

@Data
public class DocumentDto {
    private Long idDocument;
    private String nomOriginal;

    //GOOGLE DRIVE
    private String idGoogleDrive;
    private String idGoogleSharedDrive;
    private String idDriveGoogleDrive;
    private String pathGoogleDrive;
    private String ownerGoogleDrive;
    private String mimeTypeGoogleDrive;
    private String createdTimeGoogleDrive;
    private String modifiedTimeGoogleDrive;
    private String grupCodi;

    //VISIBILITAT
    private Boolean visibilitat;
    private Boolean traspassat;

    //ESTAT
    private DocumentEstatDto estat;

    //OBSERVACIONS
    private String observacions;

    //MS CORE
    private Long idUsuari;
    private Long idFitxer;

    //FK
    private TipusDocumentDto tipusDocument;
    private ConvocatoriaDto convocatoria;
    private Set<DocumentSignaturaDto> documentSignatures;

}
