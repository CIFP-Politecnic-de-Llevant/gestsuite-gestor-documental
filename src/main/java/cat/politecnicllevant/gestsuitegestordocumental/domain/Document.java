package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pll_document")
public class Document {
    @Id
    @Column(name = "iddocument")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocument;

    @Column(name = "nom", nullable = false, length = 2048)
    private String nom;

    //GOOGLE DRIVE
    @Column(name = "id_google_drive", nullable = true, length = 128)
    private String idGoogleDrive;

    @Column(name = "iddrive_google_drive", nullable = true, length = 128)
    private String idDriveGoogleDrive;

    @Column(name = "path_google_drive", nullable = false, length = 2048)
    private String pathGoogleDrive;

    @Column(name = "owner_google_drive", nullable = true, length = 128)
    private String ownerGoogleDrive;

    @Column(name = "mimetype_google_drive",nullable = true, length = 128)
    private String mimeTypeGoogleDrive;

    @Column(name = "createdTime_google_drive", nullable = true, length = 64)
    private String createdTimeGoogleDrive;

    @Column(name = "modifiedTime_google_drive", nullable = true, length = 64)
    private String modifiedTimeGoogleDrive;


    //MS CORE
    @Column(name = "idusuari", nullable = true)
    private Long idUsuari;


    //FK
    @ManyToOne
    @JoinColumn(
            name = "tipusdocument_idtipusdocument",
            nullable = true)
    //@JsonBackReference
    private TipusDocument tipusDocument;
}
