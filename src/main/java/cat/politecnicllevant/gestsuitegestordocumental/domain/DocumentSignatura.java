package cat.politecnicllevant.gestsuitegestordocumental.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="pll_document_signatura")
@IdClass(DocumentSignaturaId.class)
public class DocumentSignatura {
    @Id
    @ManyToOne
    @JoinColumn(name = "document_iddocument", insertable = false, updatable = false)
    @JsonManagedReference
    private Document document;

    @Id
    @ManyToOne
    @JoinColumn(name = "signatura_idsignatura", insertable = false, updatable = false)
    @JsonBackReference
    private Signatura signatura;


    @Column(name = "bloquejat", nullable = false)
    private Boolean signat;
}
