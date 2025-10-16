package cat.politecnicllevant.gestsuitegestordocumental.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "pll_lloc_treball")
@Data
@EqualsAndHashCode(exclude = {"empresa"})
@ToString(exclude = {"empresa"})
public class LlocTreball {

    @Id
    @Column(name = "idLlocEmpresa")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLlocTreball;

    @Column(name = "nom",nullable = true, length = 255)
    private String nom;

    @Column(name = "adreca",nullable = true, length = 255)
    private String adreca;

    @Column(name = "codi_potal",nullable = true)
    private String codiPostal;

    @Column(name = "telefon",nullable = true, length = 128)
    private String telefon;

    @Column(name = "poblacio",nullable = true, length = 128)
    private String poblacio;

    @Column(name = "activitat",nullable = true, length = 128)
    private String activitat;

    @Column(name = "municipi",nullable = true, length = 128)
    private String municipi;

    @Column(name = "validat", nullable = false)
    private boolean validat;

    @Column(name = "email_creator",nullable = true, length = 255)
    private String emailCreator;

    // Dades contacte Lloc de Treball
    @Column(name = "nom_contacte", nullable = true, length = 128)
    private String nomContacte;

    @Column(name = "cognom1_contacte", nullable = true, length = 128)
    private String cognom1Contacte;

    @Column(name = "cognom2_contacte", nullable = true, length = 128)
    private String cognom2Contacte;

    @Column(name = "telefon_contacte",nullable = true, length = 128)
    private String telefonContacte;

    @Column(name = "email_contacte",nullable = true, length = 255)
    private String emailContacte;
    /////////////

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn( name = "idEmpresa")
    private Empresa empresa;
}
