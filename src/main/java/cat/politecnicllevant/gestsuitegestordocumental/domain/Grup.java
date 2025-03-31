package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pll_grup")
public @Data class Grup {

    @Id
    @Column(name = "idGrup")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGrupGestorDocumental;

    @Column(name = "id_google_spreadsheet",nullable = true, length = 128)
    private String idGoogleSpreadsheet;

    @Column(name = "folder_google_drive",nullable = true, length = 512)
    private String folderGoogleDrive;

    @Column(name = "curs_grup",nullable = true, length = 16)
    private String cursGrup;

    @Column(name = "idgrup_core",nullable = false)
    private Long coreIdGrup;

}
