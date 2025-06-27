package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.dto.*;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import cat.politecnicllevant.gestsuitegestordocumental.service.EmpresaService;
import cat.politecnicllevant.gestsuitegestordocumental.service.LlocTreballService;
import cat.politecnicllevant.gestsuitegestordocumental.service.TokenManager;
import cat.politecnicllevant.gestsuitegestordocumental.service.TutorEmpresaService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@Slf4j
public class EmpresaController {
    //EMPRESA

    private final EmpresaService empresaService;
    private final LlocTreballService llocTreballService;
    private final TutorEmpresaService tutorEmpresaService;
    public final CoreRestClient coreRestClient;
    private final TokenManager tokenManager;

    @PostMapping("/empresa/save-company")
    public ResponseEntity<Notificacio> saveCompany(@RequestBody EmpresaDto empresa){

        Notificacio notificacio = new Notificacio();

        empresaService.save(empresa);

        notificacio.setNotifyMessage("Empresa creada");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }
    @PostMapping("/empresa/update-company")
    public ResponseEntity<Notificacio> updateCompany(@RequestBody EmpresaDto empresa){

        Notificacio notificacio = new Notificacio();

        empresaService.save(empresa);

        notificacio.setNotifyMessage("Empresa actualitzada");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @GetMapping("/empresa/all-companies")
    public ResponseEntity<List<EmpresaDto>> allCompanies(HttpServletRequest request) throws Exception {

        UsuariDto usuari = getUsuariFromRequest(request);

        List<EmpresaDto> companies = empresaService.findAll();

        for (EmpresaDto company:companies){

            List<LlocTreballDto> llocsTreball = llocTreballService.finaAllWorkspaceByIdCompany(company.getIdEmpresa(), usuari);
            if(llocsTreball != null){
                company.setLlocsTreball(new HashSet<>(llocsTreball));
            }

            List<TutorEmpresaDto> tutorsEmpresa = tutorEmpresaService.finaAllTutorEmpresaByIdCompany(company.getIdEmpresa(), usuari);
            if(tutorsEmpresa != null){
                company.setTutorsEmpresa(new HashSet<>(tutorsEmpresa));
            }
        }
        return new ResponseEntity<>(companies,HttpStatus.OK);
    }

    @PostMapping("/empresa/company/{id}")
    public ResponseEntity<EmpresaDto> getCompany(@PathVariable Long id, HttpServletRequest request) throws Exception {

        UsuariDto usuari = getUsuariFromRequest(request);

        EmpresaDto empresa = empresaService.findCompanyById(id);

        List<LlocTreballDto> llocsTreball = llocTreballService.finaAllWorkspaceByIdCompany(empresa.getIdEmpresa(), usuari);
        if(llocsTreball != null){
            empresa.setLlocsTreball(new HashSet<>(llocsTreball));
        }

        List<TutorEmpresaDto> tutorsEmpresa = tutorEmpresaService.finaAllTutorEmpresaByIdCompany(empresa.getIdEmpresa(), usuari);
        if(tutorsEmpresa != null){
            empresa.setTutorsEmpresa(new HashSet<>(tutorsEmpresa));
        }

        return new ResponseEntity<>(empresa,HttpStatus.OK);
    }

    @GetMapping("/empresa/delete-company/{id}")
    public ResponseEntity<Notificacio> deleteCompany(@PathVariable Long id){

        llocTreballService.deleteByIdEmpresa(id);
        tutorEmpresaService.deleteByIdEmpresa(id);

        boolean empresaEliminada = empresaService.delete(id);

        Notificacio notificacio = new Notificacio();

        if(empresaEliminada) {
            notificacio.setNotifyMessage("Empresa eliminada correctament");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }else {
            notificacio.setNotifyMessage("Aquest empresa no s'ha pogut eliminar");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    // LLOCS DE TREBALL
    @GetMapping("/empresa/lloc-treball/all-workspaces-not-validated")
    public ResponseEntity<?> getWorkspacesNoValid() {
        List<LlocTreballDto> llocsTreball = llocTreballService.findAllNotValidated();

        if(llocsTreball == null || llocsTreball.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(llocsTreball,HttpStatus.OK);
    }

    @GetMapping("/empresa/lloc-treball/all-workspaces/{idEmpresa}")
    public ResponseEntity<?> getWorkspacesByCompany(@PathVariable Long idEmpresa, HttpServletRequest request) throws Exception {
        List<LlocTreballDto> llocsTreball = llocTreballService.finaAllWorkspaceByIdCompany(idEmpresa, getUsuariFromRequest(request));

        if(llocsTreball == null || llocsTreball.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(llocsTreball,HttpStatus.OK);
    }

    @PostMapping("/empresa/lloc-treball/save-workspace")
    public ResponseEntity<Notificacio> saveWorkspace(@RequestBody LlocTreballDto llocTreball, HttpServletRequest request) throws Exception {
        Claims claims = tokenManager.getClaims(request);
        String myEmail = (String) claims.get("email");
        Notificacio notificacio = new Notificacio();

        ResponseEntity<UsuariDto> usuariResponse = coreRestClient.getUsuariByEmail(myEmail);
        UsuariDto usuariDto = usuariResponse.getBody();

        if (usuariDto == null) {
            notificacio.setNotifyMessage("Usuari no trobat");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.NOT_FOUND);
        }

        Set<RolDto> rolsUsuari = usuariDto.getRols();
        llocTreball.setValidat(rolsUsuari.contains(RolDto.ADMINISTRADOR) || rolsUsuari.contains(RolDto.ADMINISTRADOR_FCT));
        llocTreball.setEmailCreator(myEmail);

        llocTreballService.save(llocTreball);

        notificacio.setNotifyMessage("Lloc de treball creat");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @PostMapping("/empresa/lloc-treball/update-workspace")
    public ResponseEntity<Notificacio> updateWorkspace(@RequestBody LlocTreballDto llocTreball){

        Notificacio notificacio = new Notificacio();

        System.out.println(llocTreball);
        llocTreballService.save(llocTreball);

        notificacio.setNotifyMessage("Lloc de treball actualitzat");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @GetMapping("/empresa/lloc-treball/delete/{id}")
    public ResponseEntity<Notificacio> deleteWorkspace(@PathVariable Long id){

        boolean eliminado = llocTreballService.deleteById(id);
        Notificacio notificacio = new Notificacio();

        if(eliminado) {
            notificacio.setNotifyMessage("Lloc de treball eliminat correctament");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }else {
            notificacio.setNotifyMessage("El lloc de treball no s'ha pogut eliminar");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    // TUTORS D'EMPRESA
    @GetMapping("/empresa/tutor-empresa/all-tutors/{idEmpresa}")
    public ResponseEntity<List<TutorEmpresaDto>> getTutorsByCompany(@PathVariable Long idEmpresa, HttpServletRequest request) throws Exception {

        List<TutorEmpresaDto> tutorsEmpresa = tutorEmpresaService.finaAllTutorEmpresaByIdCompany(idEmpresa, getUsuariFromRequest(request));

        if(tutorsEmpresa == null || tutorsEmpresa.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(tutorsEmpresa,HttpStatus.OK);
    }

    @PostMapping("/empresa/tutor-empresa/save-tutor")
    public ResponseEntity<Notificacio> saveTutorEmpresa(@RequestBody TutorEmpresaDto tutorEmpresa, HttpServletRequest request) throws Exception {
        Claims claims = tokenManager.getClaims(request);
        String myEmail = (String) claims.get("email");
        Notificacio notificacio = new Notificacio();

        tutorEmpresa.setEmailCreator(myEmail);

        ResponseEntity<UsuariDto> usuariResponse = coreRestClient.getUsuariByEmail(myEmail);
        UsuariDto usuariDto = usuariResponse.getBody();

        if (usuariDto == null) {
            notificacio.setNotifyMessage("Usuari no trobat");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.NOT_FOUND);
        }

        Set<RolDto> rolsUsuari = usuariDto.getRols();
        tutorEmpresa.setValidat(rolsUsuari.contains(RolDto.ADMINISTRADOR) || rolsUsuari.contains(RolDto.ADMINISTRADOR_FCT));

        tutorEmpresaService.save(tutorEmpresa);

        notificacio.setNotifyMessage("Tutor d'empresa creat correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @PostMapping("/empresa/tutor-empresa/update-tutor")
    public ResponseEntity<Notificacio> updateTutorEmpresa(@RequestBody TutorEmpresaDto tutorEmpresa){

        Notificacio notificacio = new Notificacio();

        TutorEmpresaDto tutorEmrpesaDtoOld = tutorEmpresaService.findTutorById(tutorEmpresa.getIdTutorEmpresa());

        // Map old values to new tutorEmpresaDto
        tutorEmpresa.setEmailCreator(tutorEmrpesaDtoOld.getEmailCreator());
//        tutorEmpresa.setValidat(tutorEmrpesaDtoOld.isValidat());

        tutorEmpresaService.save(tutorEmpresa);

        notificacio.setNotifyMessage("Tutor d'empresa actualitzat correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @GetMapping("/empresa/tutor-empresa/delete/{id}")
    public ResponseEntity<Notificacio> deleteTutorEmpresa(@PathVariable Long id){

        boolean eliminado = tutorEmpresaService.deleteById(id);
        Notificacio notificacio = new Notificacio();


        if(eliminado) {
            notificacio.setNotifyMessage("Tutor d'empresa eliminat correctament");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }else {
            notificacio.setNotifyMessage("El tutor d'empresa no s'ha pogut eliminar");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private UsuariDto getUsuariFromRequest(HttpServletRequest request) throws Exception {
        Claims claims = tokenManager.getClaims(request);
        String myEmail = (String) claims.get("email");
        ResponseEntity<UsuariDto> usuariResponse = coreRestClient.getUsuariByEmail(myEmail);
        UsuariDto usuariDto = usuariResponse.getBody();
        if (usuariDto == null) {
            throw new Exception("Usuari no trobat");
        }
        return usuariDto;
    }
}
