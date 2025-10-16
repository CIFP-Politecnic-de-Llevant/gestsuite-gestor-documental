package cat.politecnicllevant.gestsuitegestordocumental.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = "empresa")
@ToString(exclude = "empresa")
public class LlocTreballDto {

    private Long idLlocTreball;
    private String nom;
    private String adreca;
    private String codiPostal;
    private String telefon;
    private String poblacio;
    private String activitat;
    private String municipi;
    private boolean validat;
    private String emailCreator;

    // Dades contacte Lloc de Treball
    private String nomContacte;
    private String cognom1Contacte;
    private String cognom2Contacte;
    private String telefonContacte;
    private String emailContacte;
    /////////////

    @JsonBackReference
    private EmpresaDto empresa;
    private Long idEmpresa;
}
