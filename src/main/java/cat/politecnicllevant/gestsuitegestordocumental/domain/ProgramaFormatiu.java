package cat.politecnicllevant.gestsuitegestordocumental.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

@Entity
@Table(name = "pll_programa_formatiu")
public @Data class ProgramaFormatiu {

    @Id
    @Column(name = "idPFormatiu")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPFormatiu;

    @Column(name = "descripcio", nullable = true, length = 255)
    @JdbcTypeCode(Types.LONGVARCHAR)
    private String descripcio;

    @Column(name = "idGrup", nullable = true)
    private Long idGrup;
}
