package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "pll_document_general")
public class DocumentGeneral {
    @Id
    @Column(name = "id_google_drive", nullable = false, length = 128)
    private String idGoogleDrive;

    @Column(name = "nom_original", nullable = false, length = 2048)
    private String nomOriginal;
}
