package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pll_grup")
public @Data class Grup {

    @Id
    @Column(name = "idGrup")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGrup;

    @Column(name = "id_google_spreadsheet",nullable = true, length = 128)
    private String idGoogleSpreadsheet;

    @Column(name = "folder_google_drive",nullable = true, length = 255)
    private String folderGoogleDrive;

    @Column(name = "idgrup_core",nullable = false)
    private Long idGrupCore;

}
