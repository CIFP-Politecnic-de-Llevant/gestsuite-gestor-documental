package cat.politecnicllevant.gestsuitegestordocumental.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"llocsTreball", "tutorsEmpresa"})
@ToString(exclude = {"llocsTreball", "tutorsEmpresa"})
public class EmpresaDto {
    private Long idEmpresa;
    private String numeroConveni;
    private String nomRepresentantLegal;
    private String cognom1RepresentantLegal;
    private String cognom2RepresentantLegal;
    private String dniRepresentantLegal;
    private String emailEmpresa;
    private String nom;
    private String cif;
    private String adreca;
    private String codiPostal;
    private String poblacio;
    private String provincia;
    private String telefon;
    @JsonManagedReference
    private Set<LlocTreballDto> llocsTreball = new HashSet<>();
    @JsonManagedReference
    private Set<TutorEmpresaDto> tutorsEmpresa = new HashSet<>();
}
