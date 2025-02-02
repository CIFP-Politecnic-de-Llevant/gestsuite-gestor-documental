package cat.politecnicllevant.gestsuitegestordocumental.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pll_document")
public @Data class Document {
    @Id
    @Column(name = "iddocument")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocument;

    @Column(name = "nom_original", nullable = false, length = 2048)
    private String nomOriginal;

    //GOOGLE DRIVE
    @Column(name = "id_google_drive", nullable = true, length = 128)
    private String idGoogleDrive;

    @Column(name = "id_google_shared_drive", nullable = true, length = 128)
    private String idGoogleSharedDrive;

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

    @Column(name = "observacions",nullable = true)
    private String observacions;

    @Column(name = "visibilitat", nullable = false)
    private Boolean visibilitat;

    @Column(name = "traspassat", nullable = false)
    private Boolean traspassat;


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

    @ManyToOne
    @JoinColumn(
            name = "convocatoria_idconvocatoria",
            nullable = true)
    //@JsonBackReference
    private Convocatoria convocatoria;

    @OneToMany(mappedBy="document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<DocumentSignatura> documentSignatures = new HashSet<>();
}
