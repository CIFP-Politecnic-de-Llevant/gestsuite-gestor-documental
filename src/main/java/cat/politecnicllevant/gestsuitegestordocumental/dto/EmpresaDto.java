package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

import java.util.List;

public @Data class EmpresaDto {

    private Long idEmpresa;
    private String numeroConveni;
    private String emailEmpresa;
    private String nom;
    private String cif;
    private String adreca;
    private String codiPostal;
    private String poblacio;
    private String provincia;
    private String telefon;
    private List<LlocTreballDto> llocsTreball;
}
