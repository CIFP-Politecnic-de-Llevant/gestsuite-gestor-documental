package cat.politecnicllevant.gestsuitegestordocumental.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @JsonBackReference
    private EmpresaDto empresa;
}
