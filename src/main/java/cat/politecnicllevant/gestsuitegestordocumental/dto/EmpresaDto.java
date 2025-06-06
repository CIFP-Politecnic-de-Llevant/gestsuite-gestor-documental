package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

public @Data class EmpresaDto {
    private Long idEmpresa;
    private String numeroConveni;
    private String nomRepresentantLegal;
    private String cognomsRepresentantLegal;
    private String dniRepresentantLegal;
    private String emailEmpresa;
    private String nom;
    private String cif;
    private String adreca;
    private String codiPostal;
    private String poblacio;
    private String provincia;
    private String telefon;
    private Set<LlocTreballDto> llocsTreball = new HashSet<>();
    private Set<TutorEmpresaDto> tutorsEmpresa = new HashSet<>();
}
