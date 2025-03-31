package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.dto.CursAcademicDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DadesFormulariDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import cat.politecnicllevant.gestsuitegestordocumental.service.DadesFormulariService;
import cat.politecnicllevant.gestsuitegestordocumental.service.GoogleDriveService;
import cat.politecnicllevant.gestsuitegestordocumental.service.GrupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FormulariFCTController {

    private final GoogleDriveService googleDriveService;
    private final DadesFormulariService dadesFormulariService;
    private final CoreRestClient coreRestClient;
    private final GrupService grupService;

    //FORMULARI FCT
    @GetMapping("/formulari/llistat")
    public ResponseEntity<List<DadesFormulariDto>> getFormularis(){
        List<DadesFormulariDto> formularis = dadesFormulariService.findAll();
        return new ResponseEntity<>(formularis, HttpStatus.OK);
    }

    @PostMapping("/formulari/save-formulari")
    public ResponseEntity<Notificacio> saveForm(@RequestBody DadesFormulariDto form, @RequestParam String email) throws NoSuchMethodException, GeneralSecurityException, IOException, InvocationTargetException, IllegalAccessException {

        CursAcademicDto cursAcademic = this.coreRestClient.getActualCursAcademic().getBody();

        form.setIdCursAcademic(cursAcademic.getIdcursAcademic());

        dadesFormulariService.save(form);
        googleDriveService.writeData(getGettersDataForm(form, email));

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Formulari guardat correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @PostMapping("/formulari/save-formulari-fempo")
    public ResponseEntity<Notificacio> saveFormFEMPO(@RequestBody DadesFormulariDto form, @RequestParam String email) throws NoSuchMethodException, GeneralSecurityException, IOException, InvocationTargetException, IllegalAccessException {

        CursAcademicDto cursAcademic = this.coreRestClient.getActualCursAcademic().getBody();

        form.setIdCursAcademic(cursAcademic.getIdcursAcademic());

        dadesFormulariService.save(form);

        System.out.println("Configurant grups...<<<"+form.getGrup()+">>>");
        ResponseEntity<GrupDto> responseEntity = coreRestClient.getByCodigrup(form.getGrup());
        if(responseEntity != null && responseEntity.getBody() != null){
            GrupDto grupDtoCore = responseEntity.getBody();
            GrupDto grupDtoGestorDocumental = grupService.getByIdGrupCore(grupDtoCore.getIdgrup());

            System.out.println(grupDtoCore);
            System.out.println(grupDtoGestorDocumental);
        }

        googleDriveService.writeDataPosition(getGettersDataFormPosition(form, email));

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Formulari FEMPO guardat correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    private static Map<String,String> getGettersDataFormPosition(DadesFormulariDto form, String email) {
        DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDateTime now = LocalDateTime.now();
        String nowFormat = now.format(formatterDateTime);

        Map<String, String> getterDataForm = new LinkedHashMap<>();
        getterDataForm.put("MAIL", email);
        getterDataForm.put("Marca_temps", nowFormat);
        getterDataForm.put("CURS_ESCOLAR", (form.getAnyCurs()!=null)?form.getAnyCurs():"");
        getterDataForm.put("Nom_alumne", (form.getNomAlumne()!=null)?form.getNomAlumne():"");
        getterDataForm.put("Llinatges_alumne", (form.getLlinatgesAlumne()!=null)?form.getLlinatgesAlumne():"");
        getterDataForm.put("Telf_alumne", (form.getTelefonAlumne()!=null)?form.getTelefonAlumne():"Sense telèfon");
        getterDataForm.put("mail_alumne", (form.getEmailAlumne()!=null)?form.getEmailAlumne():"Sense correu electrònic");
        getterDataForm.put("Poblacio_alumne", (form.getPoblacioAlumne()!=null)?form.getPoblacioAlumne():"");
        getterDataForm.put("DNI_alumne", (form.getDniAlumne()!=null)?form.getDniAlumne():"");
        getterDataForm.put("expedient_alumne", (form.getNumeroExpedient()!=null)?form.getNumeroExpedient():"");
        getterDataForm.put("Menor_edat", form.getMenorEdat()?"Si":"No");
        getterDataForm.put("Edat_alumne", form.getEdat());
        getterDataForm.put("Estudis", (form.getEstudis()!=null)?form.getEstudis():"");
        getterDataForm.put("Cicle_Formatiu", (form.getCicleFormatiu()!=null)?form.getCicleFormatiu():"");
        getterDataForm.put("Grup", (form.getGrup()!=null)?form.getGrup():"");
        getterDataForm.put("Durada_cicle", (form.getDuradaCicle()!=null)?form.getDuradaCicle():"");
        getterDataForm.put("Periode", (form.getPeriode()!=null)?form.getPeriode():"");
        getterDataForm.put("Hores_FEMPO", (form.getTotalHoresProposadesFct()!=null)?form.getTotalHoresProposadesFct():"");
        getterDataForm.put("Hores_diaries", (form.getHoresDiaries()!=null)?form.getHoresDiaries():"");
        getterDataForm.put("Data_inicial", (form.getDataInici()!=null)?form.getDataInici().format(formatterDate):"");
        getterDataForm.put("Data_final", (form.getDataFi()!=null)?form.getDataFi().format(formatterDate):"");
        getterDataForm.put("Jornada", (form.getTipusJornada()!=null)?form.getTipusJornada():"");
        getterDataForm.put("Horari_jornada", (form.getHorari()!=null)?form.getHorari():"");
        getterDataForm.put("Nom_tutor_centre", (form.getNomTutor()!=null)?form.getNomTutor():"");
        getterDataForm.put("Llinatges_tutor_centre", (form.getLlinatgesTutor()!=null)?form.getLlinatgesTutor():"");
        getterDataForm.put("Telf_tutor_centre", (form.getTelefonTutor()!=null)?form.getTelefonTutor():"Sense telèfon");
        getterDataForm.put("mail_tutor_centre", (form.getEmailTutor()!=null)?form.getEmailTutor():"Sense correu electrònic");
        getterDataForm.put("Admin_publica", form.getEmpresaAdministracioPublica()? "Si":"No");
        getterDataForm.put("N_conveni", (form.getNumeroConveni()!=null)?form.getNumeroConveni():"");
        getterDataForm.put("Nom_empresa", (form.getNomEmpresa()!=null)?form.getNomEmpresa():"");
        getterDataForm.put("CIF_empresa", (form.getCif()!=null)?form.getCif():"");
        getterDataForm.put("CP_empresa", (form.getCpempresa()!=null)?form.getCpempresa():"");
        getterDataForm.put("Adreca_empresa", (form.getAdrecaEmpresa()!=null)?form.getAdrecaEmpresa():"");
        getterDataForm.put("Poblacio_empresa", (form.getPoblacioEmpresa()!=null)?form.getPoblacioEmpresa():"");
        getterDataForm.put("Provincia_empresa", (form.getProvinciaEmpresa()!=null)?form.getProvinciaEmpresa():"Illes Balears");
        getterDataForm.put("Telf_empresa", (form.getTelefonEmpresa()!=null)?form.getTelefonEmpresa():"Sense telèfon");
        getterDataForm.put("mail_empresa", (form.getEmailEmpresa()!=null)?form.getEmailEmpresa():"Sense correu electrònic");
        getterDataForm.put("Nom_representant_legal", (form.getNomRepresentantLegal()!=null)?form.getNomRepresentantLegal():"");
        getterDataForm.put("Llinatges_representant_legal", (form.getLlinatgesRepresentantLegal()!=null)?form.getLlinatgesRepresentantLegal():"");
        getterDataForm.put("NIF_representant_legal", (form.getNifRepresentantLegal()!=null)?form.getNifRepresentantLegal():"");
        getterDataForm.put("Nom_centre_treball", (form.getNomLlocTreball()!=null)?form.getNomLlocTreball():"");
        getterDataForm.put("Adreca_centre_treball", (form.getAdrecaLlocTreball()!=null)?form.getAdrecaLlocTreball():"");
        getterDataForm.put("CP_centre_treball", (form.getCpLlocTreball()!=null)?form.getCpLlocTreball():"");
        getterDataForm.put("Poblacio_centre_treball", (form.getPoblacioLlocTreball()!=null)?form.getPoblacioLlocTreball():"");
        getterDataForm.put("Telf_centre_treball", (form.getTelefonLlocTreball()!=null)?form.getTelefonLlocTreball():"");
        getterDataForm.put("Activitat_centre_treball", (form.getActivitatLlocTreball()!=null)?form.getActivitatLlocTreball():"");
        getterDataForm.put("Nom_tutor_empresa", (form.getNomTutorEmpresa()!=null)?form.getNomTutorEmpresa():"");
        getterDataForm.put("Llinatges_tutor_empresa", (form.getLlinatgesTutorEmpresa()!=null)?form.getLlinatgesTutorEmpresa():"");
        getterDataForm.put("NIF_tutor_empresa", (form.getNifTutorEmpresa()!=null)?form.getNifTutorEmpresa():"");
        getterDataForm.put("Telf_tutor_empresa", (form.getTelefonTutorEmpresa()!=null)?form.getTelefonTutorEmpresa():"");
        getterDataForm.put("mail_tutor_empresa", (form.getEmailTutorEmpresa()!=null)?form.getEmailTutorEmpresa():"");
        getterDataForm.put("Nacionalitat_tutor_empresa", (form.getNacionalitatTutorEmpresa()!=null)?form.getNacionalitatTutorEmpresa():"");
        getterDataForm.put("Municipi_DNI_tutor_empresa", (form.getMunicipiTutorEmpresa()!=null)?form.getMunicipiTutorEmpresa():"");
        getterDataForm.put("Carrec_tutor_empresa", (form.getCarrecTutorEmpresa()!=null)?form.getCarrecTutorEmpresa():"");
        getterDataForm.put("Mesures_educatives", form.getIsMesuresEducatives()? "Si":"No");
        getterDataForm.put("Especifica_mesures_educatives", (form.getMesuresEducativesDescripcio()!=null)?form.getMesuresEducativesDescripcio():"");
        getterDataForm.put("Autoritzacio_extra", form.getIsAutoritzacioExtraordinaria()? "Si":"No");
        getterDataForm.put("motiu", (form.getMotiu()!=null)?form.getMotiu():"");
        //TODO A PARTIR D'AQUI
        /*getterDataForm.put("Km centre treball-població alumne (posa només el número. Exemple: 14)", form.getKm());
        getterDataForm.put("És una empresa nova?", form.getEmpresaNova()?"Si":"No");
        getterDataForm.put("Número d'annex ( si el sabeu)", "");
        getterDataForm.put("Data màxima acabament", form.getDataAcabament().format(formatterDate));
        getterDataForm.put("Dia seguiment centre educatiu", form.getDiaSeguimentCentreEducatiu());
        getterDataForm.put("Hora seguiment centre educatiu", form.getHoraSeguimentCentreEducatiu());
        getterDataForm.put("Dia seguiment amb responsable FCT", form.getDiaSeguimentResponsableFct());
        getterDataForm.put("Hora seguiment amb responsable FCT", form.getHoraSeguimentResponsableFct());
        getterDataForm.put("Tipus de flexibilització", "");
        getterDataForm.put("Hi ha algun tipus de flexibilització en el mòdul de FCT?", form.getFlexibilitzacioModulFct()? "Si":"No");
        getterDataForm.put("Si realitza horari nocturn indicau quin horari té", "");
        */

        return getterDataForm;
    }


    private static Map<String,String> getGettersDataForm(DadesFormulariDto form, String email) throws NoSuchMethodException {
        DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDateTime now = LocalDateTime.now();
        String nowFormat = now.format(formatterDateTime);

        Map<String, String> getterDataForm = new LinkedHashMap<>();
        getterDataForm.put("Marca temporal", nowFormat);
        getterDataForm.put("Dirección de correo", email);
        getterDataForm.put("Nom", form.getNomAlumne());
        getterDataForm.put("Llinatges", form.getLlinatgesAlumne());
        getterDataForm.put("Població", form.getPoblacioAlumne());
        getterDataForm.put("DNI", form.getDniAlumne());
        getterDataForm.put("Nombre expedient", form.getNumeroExpedient());
        getterDataForm.put("Cicle Formatiu", form.getCicleFormatiu());
        getterDataForm.put("Grup", form.getGrup());
        getterDataForm.put("Durada del cicle", form.getDuradaCicle());
        getterDataForm.put("Número d'hores totals proposades per FCT (posa només el número. Exemple: 240)", form.getTotalHoresProposadesFct());
        getterDataForm.put("Número d'hores diàries (posa només el número. Exemple: 7)", form.getHoresDiaries());
        getterDataForm.put("Km centre treball-població alumne (posa només el número. Exemple: 14)", form.getKm());
        getterDataForm.put("Període", form.getPeriode());
        getterDataForm.put("Data inicial", form.getDataInici().format(formatterDate));
        getterDataForm.put("Data final", form.getDataFi().format(formatterDate));
        getterDataForm.put("Tipus de jornada", form.getTipusJornada());
        getterDataForm.put("Horari (Exemple jornada continua: 8:00-13:00 i jornada partida: 8.00-12:00 ; 15:00-18:00)", form.getHorari());
        getterDataForm.put("Nom professor/tutor", form.getNomTutor());
        getterDataForm.put("Llinatges professor/tutor", form.getLlinatgesTutor());
        getterDataForm.put("Telèfon mòbil professor/tutor", form.getTelefonTutor());
        getterDataForm.put("És una empresa nova?", form.getEmpresaNova()?"Si":"No");
        getterDataForm.put("Número de conveni ( si el sabeu)", form.getNumeroConveni());
        getterDataForm.put("Número d'annex ( si el sabeu)", "");
        getterDataForm.put("Nom de l'empresa", form.getNomEmpresa());
        getterDataForm.put("CIF", form.getCif());
        getterDataForm.put("Adreça Empresa", form.getAdrecaEmpresa());
        getterDataForm.put("Còdig postal CP Empresa", form.getCpempresa());
        getterDataForm.put("Població Empresa", form.getPoblacioEmpresa());
        getterDataForm.put("Telèfon Empresa", form.getTelefonEmpresa());
        getterDataForm.put("Fax Empresa", "");
        getterDataForm.put("Nom Centre de treball (on ha d'anar l'alumne)", form.getNomLlocTreball());
        getterDataForm.put("Adreça Centre de treball ", form.getAdrecaLlocTreball());
        getterDataForm.put("Codi postal CP Centre de treball ", form.getCpLlocTreball());
        getterDataForm.put("Població Centre de treball ", form.getPoblacioLlocTreball());
        getterDataForm.put("Telèfon Centre de treball ", form.getTelefonLlocTreball());
        getterDataForm.put("Activitat Centre de treball ", form.getActivitatLlocTreball());
        getterDataForm.put("Nom representant legal", form.getNomCompletRepresentantLegal());
        getterDataForm.put("Llinatges representant legal", ""); //TODO: mirar si es necessita
        getterDataForm.put("NIF representant legal", form.getNifRepresentantLegal());
        getterDataForm.put("Nom tutor empresa", form.getNomCompletTutorEmpresa());
        getterDataForm.put("Llinatges tutor empresa", "");
        getterDataForm.put("NIF tutor empresa", form.getNifTutorEmpresa());
        getterDataForm.put("Municipi (que consta al DNI del tutor empresa)", form.getMunicipiTutorEmpresa());
        getterDataForm.put("Càrrec del tutor dins l'empresa", form.getCarrecTutorEmpresa());
        getterDataForm.put("Correu electrònic de l'empresa", form.getEmailTutorEmpresa());
        getterDataForm.put("CURS ESCOLAR (Exemple: 23/24)", form.getAnyCurs());
        getterDataForm.put("Data màxima acabament", form.getDataAcabament().format(formatterDate));
        getterDataForm.put("Dia seguiment centre educatiu", form.getDiaSeguimentCentreEducatiu());
        getterDataForm.put("Hora seguiment centre educatiu", form.getHoraSeguimentCentreEducatiu());
        getterDataForm.put("Dia seguiment amb responsable FCT", form.getDiaSeguimentResponsableFct());
        getterDataForm.put("Hora seguiment amb responsable FCT", form.getHoraSeguimentResponsableFct());
        getterDataForm.put("Estudis", form.getEstudis());
        getterDataForm.put("Nacionalitat tutor empresa", form.getNacionalitatTutorEmpresa());
        getterDataForm.put("Es menor d'edat en el moment de començar la FCT?", form.getMenorEdat()?"Si":"No");
        getterDataForm.put("És una empresa de l'Administració Pública?", form.getEmpresaAdministracioPublica()? "Si":"No");
        getterDataForm.put("Tipus de flexibilització", "");
        getterDataForm.put("Hi ha algun tipus de flexibilització en el mòdul de FCT?", form.getFlexibilitzacioModulFct()? "Si":"No");
        getterDataForm.put("Si realitza horari nocturn indicau quin horari té", "");
        getterDataForm.put("Edat de l'alumne (només número)", form.getEdat());

        return getterDataForm;
    }
}
