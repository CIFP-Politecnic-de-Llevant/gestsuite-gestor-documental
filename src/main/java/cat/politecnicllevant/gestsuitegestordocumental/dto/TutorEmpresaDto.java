package cat.politecnicllevant.gestsuitegestordocumental.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = "empresa")
@ToString(exclude = "empresa")
public class TutorEmpresaDto {
    private Long idTutorEmpresa;
    private String nom;
    private String cognom1;
    private String cognom2;
    private String nacionalitat;
    private String dni;
    private String telefon;
    private String email;
    private String carrec;
    private boolean validat;
    private String emailCreator;
    @JsonBackReference
    private EmpresaDto empresa;
    @Transient
    private Long idEmpresa;
}
