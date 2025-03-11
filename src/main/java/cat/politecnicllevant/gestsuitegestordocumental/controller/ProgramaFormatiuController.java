package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ProgramaFormatiuDto;
import cat.politecnicllevant.gestsuitegestordocumental.service.ProgramaFormatiuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class ProgramaFormatiuController {
    //PROGRAMA FORMATIU
    private final ProgramaFormatiuService programaFormatiuService;

    public ProgramaFormatiuController(ProgramaFormatiuService programaFormatiuService) {
        this.programaFormatiuService = programaFormatiuService;
    }

    @PostMapping("/programa-formacio/save-task")
    public ResponseEntity<Notificacio> saveTask(@RequestBody ProgramaFormatiuDto programaFormatiuDto){

        Notificacio notificacio = new Notificacio();
        System.out.println(programaFormatiuDto);
        programaFormatiuService.save(programaFormatiuDto);

        if(programaFormatiuDto.getIdPFormatiu() == null){
            notificacio.setNotifyMessage("Tasca guardada");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }else{
            notificacio.setNotifyMessage("Tasca actualitzada");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }

    }

    @GetMapping("/programa-formacio/delete-task/{id}")
    public ResponseEntity<Notificacio> deleteTask(@PathVariable Long id){

        programaFormatiuService.deleteById(id);
        Notificacio notificacio = new Notificacio();

        notificacio.setNotifyMessage("Tasca eliminada");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @GetMapping("/programa-formacio/all-PFormatius")
    public ResponseEntity<List<ProgramaFormatiuDto>>findAllPFormatius(){

        List<ProgramaFormatiuDto> pf = programaFormatiuService.findAll();
        return new ResponseEntity<>(pf,HttpStatus.OK);
    }
    @GetMapping("/programa-formacio/all-PFormatiusById/{id}")
    public ResponseEntity<List<ProgramaFormatiuDto>>findAllPFormatiusById(@PathVariable Long id){

        List<ProgramaFormatiuDto> pf = programaFormatiuService.findAllById(id);
        return new ResponseEntity<>(pf,HttpStatus.OK);
    }
}
