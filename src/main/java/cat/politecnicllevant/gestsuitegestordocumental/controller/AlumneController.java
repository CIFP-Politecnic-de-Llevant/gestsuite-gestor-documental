package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.dto.AlumneDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.CursDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.UsuariDto;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import cat.politecnicllevant.gestsuitegestordocumental.service.AlumneService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@Slf4j
public class AlumneController {
    private final AlumneService alumneService;
    private final CoreRestClient coreRestClient;

    public AlumneController(AlumneService alumneService, CoreRestClient coreRestClient) {
        this.alumneService = alumneService;
        this.coreRestClient = coreRestClient;
    }

    //ALUMNE
    @PostMapping("/alumnes/get-students-from-file")
    public ResponseEntity<List<AlumneDto>> getStudentsFromFile(@RequestParam("file") MultipartFile file) throws Exception {

        try(InputStream inpSt = file.getInputStream()){
            Workbook workbook = new HSSFWorkbook(inpSt);
            Sheet sheet = workbook.getSheetAt(0);

            List<AlumneDto> alumnes = new ArrayList<AlumneDto>();

            Map<String, Method> setterStudent = getSettersStudent();

            //Conseguir el headers del fitxer
            Row headerRow = sheet.getRow(4);
            List<String> headers = getHeaders(headerRow);

            Iterator<Row> rowIterator = sheet.iterator();
            //Botas fins després dels headers(si estan sempre a la mateixa fila)
            for (int i = 0; i < 4; i++) {
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                AlumneDto alumne = new AlumneDto();

                Iterator<Cell> cellIterator = row.cellIterator();
                int index = 0;

                while (cellIterator.hasNext() && index < headers.size()) {

                    Cell cell = cellIterator.next();
                    String cellValue = cell.getStringCellValue().trim();
                    String header = headers.get(index);

                    DateTimeFormatter formatoFechanacimineto = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    // Cercar els setters corresponents i amb el nom de la capçalera
                    Method setter = setterStudent.get(header);
                    if (setter != null) {
                        try {
                            if(header.equals("Data de naixement")) {
                                LocalDate fechaNacimiento = LocalDate.parse(cellValue, formatoFechanacimineto);
                                setter.invoke(alumne, fechaNacimiento);
                            }else if(header.equals("Exp.")){
                                setter.invoke(alumne,cellValue);
                                UsuariDto user = coreRestClient.getUsuariByNumExpedient(cellValue).getBody();
                                if(user!=null) {
                                    alumne.setIdUsuari(user.getIdusuari());
                                }
                            }else if(header.equals("Llinatges i nom")){

                                String[] partesNombre = cellValue.split(",\\s+");
                                String[] apellidos = partesNombre[0].split("\\s+");
                                String apellido1 = apellidos[0];
                                String apellido2 = apellidos.length > 1 ? apellidos[1] : "";
                                String nombre = partesNombre[1];

                                setterStudent.get("cognom1").invoke(alumne, apellido1);
                                setterStudent.get("cognom2").invoke(alumne, apellido2);
                                setterStudent.get("Llinatges i nom").invoke(alumne, nombre);

                            }else if(header.equals("Tel. fix")){

                                String[] telefonos = cellValue.split("Tel");

                                for (int i = 0; i < telefonos.length; i++) {

                                    if(telefonos[i].contains("fix")){

                                        String[] fix = telefonos[i].split(":\\s+");
                                        setterStudent.get("Tel. fix").invoke(alumne, fix[1].trim());

                                    }else if(telefonos[i].contains("mòbil")) {
                                        String[] mobil = telefonos[i].split(":\\s+");
                                        setterStudent.get("mobil").invoke(alumne, mobil[1].trim());

                                    }
                                }
                            }else {
                                setter.invoke(alumne, cellValue);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    index++;
                }
                if(alumne.getIdUsuari()!=null) {
                    alumnes.add(alumne);
                }
            }
            return new ResponseEntity<>(alumnes, HttpStatus.OK);
        }
    }

    @GetMapping("/alumnes/delete-student/{nExp}")
    public ResponseEntity<Notificacio> deleteStudent(@PathVariable String nExp){

        boolean eliminado = alumneService.delete(nExp);
        Notificacio notificacio = new Notificacio();

        if(eliminado) {
            notificacio.setNotifyMessage("Alumne eliminat correctament");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }else {
            notificacio.setNotifyMessage("Aquest alumne no té número d'expedient");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping("/alumnes/update-student")
    public ResponseEntity<Notificacio> updateStudent(@RequestBody AlumneDto alumne){

        Notificacio notificacio = new Notificacio();
        UsuariDto user = coreRestClient.getUsuariByNumExpedient(String.valueOf(alumne.getNumeroExpedient())).getBody();
        alumne.setIdUsuari(Objects.requireNonNull(user).getIdusuari());
        alumneService.save(alumne);

        notificacio.setNotifyMessage("Alumne actualitzat");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }
    @PostMapping("/alumnes/save-student")
    public ResponseEntity<Notificacio> saveStudent(@RequestBody AlumneDto[] alumnes){

        Notificacio notificacio = new Notificacio();

        for (AlumneDto alumne:alumnes) {

            UsuariDto user = coreRestClient.getUsuariByNumExpedient(alumne.getNumeroExpedient()).getBody();
            if(user!=null) {

                AlumneDto alumneDB = alumneService.getByNumeroExpedient(alumne.getNumeroExpedient());
                if(alumneDB == null){
                    alumneDB = new AlumneDto();
                    alumneDB.setIdUsuari(user.getIdusuari());
                    alumneDB.setIsFCT(false);
                }

                alumneDB.setNumeroExpedient(user.getGestibExpedient());
                alumneDB.setNom(user.getGestibNom());
                alumneDB.setCognom1(user.getGestibCognom1());
                alumneDB.setCognom2(user.getGestibCognom2());
                //alumneDB.setGrup(user.getGestibGrup());

                alumneDB.setCIP(alumne.getCIP());
                alumneDB.setCP(alumne.getCP());
                alumneDB.setAdrecaCompleta(alumne.getAdrecaCompleta());
                alumneDB.setLocalitat(alumne.getLocalitat());
                alumneDB.setMunicipi(alumne.getMunicipi());
                alumneDB.setTelefon(alumne.getTelefon());
                alumneDB.setTelefonFix(alumne.getTelefonFix());
                alumneDB.setEmail(alumne.getEmail());
                alumneDB.setSexe(alumne.getSexe());
                alumneDB.setDataNaixement(alumne.getDataNaixement());
                alumneDB.setNacionalitat(alumne.getNacionalitat());
                alumneDB.setPaisNaixement(alumne.getPaisNaixement());
                alumneDB.setProvinciaNaixement(alumne.getProvinciaNaixement());
                alumneDB.setLocalitatNaixement(alumne.getLocalitatNaixement());
                alumneDB.setDni(alumne.getDni());
                alumneDB.setTargetaSanitaria(alumne.getTargetaSanitaria());
                alumneDB.setTutor(alumne.getTutor());
                alumneDB.setTelefonTutor(alumne.getTelefonTutor());
                alumneDB.setEmailTutor(alumne.getEmailTutor());
                alumneDB.setDniTutor(alumne.getDniTutor());
                alumneDB.setAdrecaTutor(alumne.getAdrecaTutor());
                alumneDB.setNacionalitatTutor(alumne.getNacionalitatTutor());

                String ensenyament = alumne.getEnsenyament();
                if(ensenyament.equals("GS")){
                    ensenyament = "CF Grau Superior";
                } else if(ensenyament.equals("GM")) {
                    ensenyament = "CF Grau Mitjà";
                } else if(ensenyament.equals("FPGB")) {
                    ensenyament = "FP Bàsica";
                }
                alumneDB.setEnsenyament(ensenyament);

                alumneDB.setEstudis(alumne.getEstudis());
                alumneDB.setGrup(alumne.getGrup());

                alumneService.save(alumneDB);
            }
        }

        notificacio.setNotifyMessage("Alumnes guardats i/o actualitzats");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @GetMapping("/alumnes/all-students")
    public ResponseEntity<List<AlumneDto>>findAllStudents(){

        List<AlumneDto> alumnes = alumneService.findAll();
        return new ResponseEntity<>(alumnes,HttpStatus.OK);
    }

    private static Map<String,Method> getSettersStudent() throws NoSuchMethodException {

        Map<String, Method> setterStudent = new HashMap<>();
        setterStudent.put("Llinatges i nom", AlumneDto.class.getMethod("setNom", String.class));
        setterStudent.put("cognom1", AlumneDto.class.getMethod("setCognom1", String.class));
        setterStudent.put("cognom2", AlumneDto.class.getMethod("setCognom2", String.class));
        setterStudent.put("Ensenyament", AlumneDto.class.getMethod("setEnsenyament", String.class));
        setterStudent.put("Estudis", AlumneDto.class.getMethod("setEstudis", String.class));
        setterStudent.put("Grup", AlumneDto.class.getMethod("setGrup", String.class));
        setterStudent.put("Exp.", AlumneDto.class.getMethod("setNumeroExpedient", String.class));
        setterStudent.put("Sexe", AlumneDto.class.getMethod("setSexe", String.class));
        setterStudent.put("Data de naixement", AlumneDto.class.getMethod("setDataNaixement", LocalDate.class));
        setterStudent.put("Nacionalitat", AlumneDto.class.getMethod("setNacionalitat", String.class));
        setterStudent.put("País naixement", AlumneDto.class.getMethod("setPaisNaixement", String.class));
        setterStudent.put("Província naixement", AlumneDto.class.getMethod("setProvinciaNaixement", String.class));
        setterStudent.put("Localitat naixement", AlumneDto.class.getMethod("setLocalitatNaixement", String.class));
        setterStudent.put("DNI", AlumneDto.class.getMethod("setDni", String.class));
        setterStudent.put("Targeta sanitària", AlumneDto.class.getMethod("setTargetaSanitaria", String.class));
        setterStudent.put("CIP", AlumneDto.class.getMethod("setCIP", String.class));
        setterStudent.put("Adreça (Corresp.)", AlumneDto.class.getMethod("setAdrecaCompleta", String.class));
        setterStudent.put("Municipi", AlumneDto.class.getMethod("setMunicipi", String.class));
        setterStudent.put("Localitat", AlumneDto.class.getMethod("setLocalitat", String.class));
        setterStudent.put("CP.", AlumneDto.class.getMethod("setCP", String.class));
        setterStudent.put("mobil", AlumneDto.class.getMethod("setTelefon", String.class));
        setterStudent.put("Tel. fix", AlumneDto.class.getMethod("setTelefonFix", String.class));
        setterStudent.put("E-mail", AlumneDto.class.getMethod("setEmail", String.class));
        setterStudent.put("Tutor/a", AlumneDto.class.getMethod("setTutor", String.class));
        setterStudent.put("Tel. tutor/a", AlumneDto.class.getMethod("setTelefonTutor", String.class));
        setterStudent.put("E-mail tutor/a", AlumneDto.class.getMethod("setEmailTutor", String.class));
        setterStudent.put("DNI tutor/a", AlumneDto.class.getMethod("setDniTutor", String.class));
        setterStudent.put("Adreça pares o tutors (Corresp.)", AlumneDto.class.getMethod("setAdrecaTutor", String.class));
        setterStudent.put("Nacionalitat pares o tutors", AlumneDto.class.getMethod("setNacionalitatTutor", String.class));

        return setterStudent;
    }

    private static List<String> getHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        if (headerRow != null) {
            for (Cell cell : headerRow) {
                String header = cell.getStringCellValue();
                headers.add(header);
            }
        }
        return headers;
    }
}
