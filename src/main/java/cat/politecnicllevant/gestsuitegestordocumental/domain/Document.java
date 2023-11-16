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

    @Column(name = "gestib_nom", nullable = false, length = 2048)
    private String gestibNom;
}
