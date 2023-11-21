package cat.politecnicllevant.gestsuitegestordocumental.dto;

import cat.politecnicllevant.gestsuitegestordocumental.domain.TipusDocument;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class DocumentDto {
    private Long idDocument;
    private String nom;

    //GOOGLE DRIVE
    private String idDrive;
    private String path;
    private String owner;
    private String mimeType;
    private String createdTime;
    private String modifiedTime;

    //MS CORE
    private Long idUsuari;

    //FK
    private TipusDocument tipusDocument;
}
