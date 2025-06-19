package cat.politecnicllevant.gestsuitegestordocumental.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Entity
@Table(name = "pll_tutor_empresa")
@Data
@EqualsAndHashCode(exclude = "empresa")
@ToString(exclude = {"empresa"})
public class TutorEmpresa {
    @Id
    @Column( name = "idTutorEmpresa")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTutorEmpresa;

    @Column(name = "nom", nullable = true, length = 128)
    private String nom;

    @Column(name = "cognom1", nullable = true, length = 128)
    private String cognom1;

    @Column(name = "cognom2", nullable = true, length = 128)
    private String cognom2;

    @Column(name = "nacionalitat", nullable = true, length = 128)
    private String nacionalitat;

    @Column(name = "dni", nullable = true, length = 64)
    private String dni;

    @Column(name = "telefon", nullable = true, length = 64)
    private String telefon;

    @Column(name = "email", nullable = true, length = 255)
    private String email;

    @Column(name = "carrec",nullable = true, length = 128)
    private String carrec;

    @Column(name = "validat", nullable = false)
    private boolean validat;

    @Column(name = "email_creator",nullable = true, length = 255)
    private String emailCreator;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn( name = "idEmpresa")
    private Empresa empresa;
}
