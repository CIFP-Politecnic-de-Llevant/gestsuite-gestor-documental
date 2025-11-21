package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "pll_grup")
public @Data class Grup {

    @Id
    @Column(name = "idGrup")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGrupGestorDocumental;

    @Column(name = "id_google_spreadsheet", length = 128)
    private String idGoogleSpreadsheet;

    @Column(name = "folder_google_drive", length = 512)
    private String folderGoogleDrive;

    @Column(name = "curs_grup", length = 16)
    private String cursGrup;

    @Column(name = "idgrup_core",nullable = false)
    private Long coreIdGrup;

    @Column(name = "actiu", nullable = false)
    @ColumnDefault("true")
    private Boolean actiu;

    @PrePersist
    public void prePersist() {
        if(actiu == null) {
            actiu = true;
        }
    }
}
