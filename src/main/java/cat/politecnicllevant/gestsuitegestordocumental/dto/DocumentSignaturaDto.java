package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

@Data
public class DocumentSignaturaDto {
    private DocumentDto document;
    private SignaturaDto signatura;
    private Boolean signat;
}
