package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

import java.util.Set;

@Data
public class TipusDocumentDto {
        private Long idTipusDocument;
        private String nom;
        private String descripcio;
        private TipusDocumentPropietariDto propietari;
        private Boolean visibilitatDefecte;
}
