package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.dto.EmpresaDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.LlocTreballDto;
import cat.politecnicllevant.gestsuitegestordocumental.service.EmpresaService;
import cat.politecnicllevant.gestsuitegestordocumental.service.LlocTreballService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class EmpresaController {
    //EMPRESA

    private final EmpresaService empresaService;

    private final LlocTreballService llocTreballService;

    public EmpresaController(EmpresaService empresaService, LlocTreballService llocTreballService) {
        this.empresaService = empresaService;
        this.llocTreballService = llocTreballService;
    }

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
    public ResponseEntity<List<EmpresaDto>> allCompanies(){

        List<EmpresaDto> companies = empresaService.findAll();

        for (EmpresaDto company:companies){

            List<LlocTreballDto> llocsTreball = llocTreballService.finaAllWorkspabeByIdCompany(company.getIdEmpresa());

            if(llocsTreball != null){
                company.setLlocsTreball(llocsTreball);
            }
        }
        return new ResponseEntity<>(companies,HttpStatus.OK);
    }

    @PostMapping("/empresa/company/{id}")
    public ResponseEntity<EmpresaDto> getCompany(@PathVariable Long id){

        EmpresaDto empresa = empresaService.findCompanyById(id);

        List<LlocTreballDto> llocsTreball = llocTreballService.finaAllWorkspabeByIdCompany(id);
        empresa.setLlocsTreball(llocsTreball);

        return new ResponseEntity<>(empresa,HttpStatus.OK);
    }

    @GetMapping("/empresa/delete-company/{id}")
    public ResponseEntity<Notificacio> deleteCompany(@PathVariable Long id){

        llocTreballService.deleteByIdEmpresa(id);
        boolean eliminado = empresaService.delete(id);
        Notificacio notificacio = new Notificacio();


        if(eliminado) {
            notificacio.setNotifyMessage("Empresa eliminada correctament");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }else {
            notificacio.setNotifyMessage("Aquest empresa no s'ha pogut eliminar");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio,HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping("/empresa/lloc-treball/save-workspace")
    public ResponseEntity<Notificacio> saveWorkspace(@RequestBody LlocTreballDto llocTreball){

        Notificacio notificacio = new Notificacio();

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
            notificacio.setNotifyMessage("Aquest llod de treball no s'ha pogut eliminar");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio,HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
