package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

public @Data class TutorEmpresaDto {
    private Long idTutorEmpresa;
    private String nom;
    private String cognom1;
    private String cognom2;
    private String nacionalitat;
    private String dni;
    private String telefon;
    private String email;
    private String carrec;
    private EmpresaDto empresa;
}
