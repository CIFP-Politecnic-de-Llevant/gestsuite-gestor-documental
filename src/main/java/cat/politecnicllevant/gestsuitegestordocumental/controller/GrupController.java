package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.service.GrupRelacioService;
import cat.politecnicllevant.gestsuitegestordocumental.service.GrupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/grup")
public class GrupController {

    private final GrupService grupService;
    private final GrupRelacioService grupRelacioService;

    @GetMapping
    public ResponseEntity<List<GrupDto>> getGrups() {
        return new ResponseEntity<>(grupService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}/relacions")
    public ResponseEntity<List<GrupDto>> getRelacions(@PathVariable Long id) {
        List<GrupDto> grupsRelacionats = grupRelacioService.getGrupsRelacionats(id);

        if (grupsRelacionats.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(grupsRelacionats, HttpStatus.OK);
    }

    @PostMapping("/{id}/relacions")
    public ResponseEntity<List<GrupDto>> actualitzarRelacions(@PathVariable Long id, @RequestBody List<Long> grupsRelacionats) {
        List<GrupDto> relacions = grupRelacioService.actualitzarRelacions(id, grupsRelacionats);
        return new ResponseEntity<>(relacions, HttpStatus.OK);
    }
}
