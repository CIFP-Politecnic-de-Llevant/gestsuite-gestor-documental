package cat.politecnicllevant.gestsuitegestordocumental.dto;

import cat.politecnicllevant.gestsuitegestordocumental.domain.TipusDocument;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class DocumentDto {
    private Long idDocument;
    private String nom;
    private Long idUsuari;
    private TipusDocument tipusDocument;
}
