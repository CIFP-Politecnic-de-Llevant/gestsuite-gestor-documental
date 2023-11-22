package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pll_signatura")
public class Signatura {
    @Id
    @Column(name = "idsignatura")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSignatura;

    @Column(name = "nom", unique = true, nullable = false, length = 2048)
    private String nom;

}
