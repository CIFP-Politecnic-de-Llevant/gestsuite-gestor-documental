package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "pll_document")
public class Document {
    @Id
    @Column(name = "iddocument")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocument;

    @Column(name = "nom", unique = true, nullable = false, length = 2048)
    private String nom;

    //GOOGLE DRIVE
    @Column(name = "iddrive", unique = true, nullable = false, length = 2048)
    private String idDrive;

    @Column(name = "path", nullable = false, length = 2048)
    private String path;

    @Column(name = "owner", nullable = false, length = 2048)
    private String owner;

    @Column(name = "mimetype",nullable = false, length = 2048)
    private String mimeType;

    @Column(name = "createdTime", nullable = false, length = 2048)
    private String createdTime;

    @Column(name = "modifiedTime", nullable = false, length = 2048)
    private String modifiedTime;


    //MS CORE
    @Column(name = "idusuari", nullable = false, length = 2048)
    private Long idUsuari;


    //FK
    @ManyToOne
    @JoinColumn(
            name = "tipusdocument_idtipusdocument",
            nullable = true)
    //@JsonBackReference
    private TipusDocument tipusDocument;
}
