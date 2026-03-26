package cat.politecnicllevant.gestsuitegestordocumental.controller;

import cat.politecnicllevant.gestsuitegestordocumental.dto.ConvocatoriaCreateRequestDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ConvocatoriaDto;
import cat.politecnicllevant.gestsuitegestordocumental.service.ConvocatoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConvocatoriaController {

    private final ConvocatoriaService convocatoriaService;

    @GetMapping("/admin/convocatories")
    public ResponseEntity<List<ConvocatoriaDto>> findAllConvocatories() {
        return new ResponseEntity<>(convocatoriaService.findAll(), HttpStatus.OK);
    }

    @PostMapping("/admin/convocatories")
    public ResponseEntity<ConvocatoriaDto> createConvocatoria(@RequestBody ConvocatoriaCreateRequestDto request) {
        return new ResponseEntity<>(convocatoriaService.create(request), HttpStatus.OK);
    }
}
