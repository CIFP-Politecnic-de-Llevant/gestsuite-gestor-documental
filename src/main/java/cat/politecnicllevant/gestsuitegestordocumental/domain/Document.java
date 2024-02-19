package cat.politecnicllevant.gestsuitegestordocumental.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "pll_document")
public class Document {
    @Id
    @Column(name = "iddocument")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocument;

    @Column(name = "nom_original", unique = true, nullable = false, length = 2048)
    private String nomOriginal;

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

    @Column(name = "grup_codi", nullable = true, length = 10)
    private String grupCodi;

    @Column(name = "estat", nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentEstat estat;


    //MS CORE
    @Column(name = "idusuari", nullable = true)
    private Long idUsuari;

    @Column(name = "idfitxer", nullable = true)
    private Long idFitxer;


    //FK
    @ManyToOne
    @JoinColumn(
            name = "tipusdocument_idtipusdocument",
            nullable = true)
    //@JsonBackReference
    private TipusDocument tipusDocument;

    @OneToMany(mappedBy="document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<DocumentSignatura> documentSignatures = new HashSet<>();
}
