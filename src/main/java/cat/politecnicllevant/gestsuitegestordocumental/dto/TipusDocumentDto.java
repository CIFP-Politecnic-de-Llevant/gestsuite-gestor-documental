package cat.politecnicllevant.gestsuitegestordocumental.dto;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Signatura;
import lombok.Data;

import java.util.Set;

@Data
public class TipusDocumentDto {
        private Long idTipusDocument;
        private String nom;
        private Set<Signatura> signatures;
        private TipusDocumentPropietariDto propietari;
}
