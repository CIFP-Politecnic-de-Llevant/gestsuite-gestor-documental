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
     private Long numero_expedient;
     private String sexe;
     private LocalDate data_naixament;
     private String nacionalitat;
     private String pais_naixament;
     private String provincia_naixament;
     private String localitat_naixament;
     private String dni;
     private String targeta_sanitaria;
     private String CIP;
     private String adreça_completa;
     private String minucipi;
     private String localitat;
     private Long CP;
     private Long telefon;
     private Long telefon_fix;
     private String email;
     private String tutor;
     private String pare;
     private String mare;
     private Long telefon_tutor;
     private Long telefon_pare;
     private Long telefon_mare;
     private String email_tutor;
     private String email_pare;
     private String email_mare;
     private String dni_tutor;
     private String dni_pare;
     private String dni_mare;
     private String adreça_tutor;
     private String adreça_pare;
     private String adreça_mare;
     private String nacionalitat_tutor;
     private String nacionalitat_pare;
     private String nacionalitat_mare;

}
