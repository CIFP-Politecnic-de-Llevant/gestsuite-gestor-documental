package cat.politecnicllevant.gestsuitegestordocumental.dto;

import lombok.Data;

import java.time.LocalDate;

public @Data class AlumneDto {
     private Long idAlumne;
     private String nom;
     private String cognom1;
     private String cognom2;
     private String ensenyament;
     private String estudis;
     private String grup;
     private String numeroExpedient;
     private String sexe;
     private LocalDate dataNaixement;
     private String nacionalitat;
     private String paisNaixement;
     private String provinciaNaixement;
     private String localitatNaixement;
     private String dni;
     private String targetaSanitaria;
     private String CIP;
     private String adrecaCompleta;
     private String municipi;
     private String localitat;
     private String CP;
     private String telefon;
     private String telefonFix;
     private String email;
     private String tutor;
     private String telefonTutor;
     private String emailTutor;
     private String dniTutor;
     private String adrecaTutor;
     private String nacionalitatTutor;
     private Long idUsuari;
     private Boolean isFCT;
}
