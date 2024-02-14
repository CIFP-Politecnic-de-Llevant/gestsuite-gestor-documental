package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

import java.util.Set;

@Data
public class TipusDocumentDto {
        private Long idTipusDocument;
        private String nom;
        private Set<SignaturaDto> signatures;
        private TipusDocumentPropietariDto propietari;
}
