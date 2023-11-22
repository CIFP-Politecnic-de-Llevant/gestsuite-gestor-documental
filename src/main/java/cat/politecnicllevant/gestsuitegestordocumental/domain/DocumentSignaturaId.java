package cat.politecnicllevant.gestsuitegestordocumental.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class DocumentSignaturaId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long document;
    private Long signatura;
}
