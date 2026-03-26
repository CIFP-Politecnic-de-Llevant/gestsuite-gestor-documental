package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Convocatoria;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ConvocatoriaCreateRequestDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ConvocatoriaDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.ConvocatoriaRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;
    private final ModelMapper modelMapper;

    public List<ConvocatoriaDto> findAll(){
        return convocatoriaRepository.findAll().stream()
                .sorted(Comparator.comparing(Convocatoria::getIdConvocatoria).reversed())
                .map(e -> modelMapper.map(e,ConvocatoriaDto.class))
                .collect(Collectors.toList());
    }

    public ConvocatoriaDto findConvocatoriaById(Long id){
        Convocatoria c = convocatoriaRepository.findById(id).orElse(null);

        if(c == null){
            return null;
        }
        return modelMapper.map(c,ConvocatoriaDto.class);
    }

    public ConvocatoriaDto findConvocatoriaActual(){
        Convocatoria e = convocatoriaRepository.findByIsActualTrue();
        return modelMapper.map(e,ConvocatoriaDto.class);
    }

    public ConvocatoriaDto save(ConvocatoriaDto empresa){

        Convocatoria e = modelMapper.map(empresa, Convocatoria.class);
        if(Boolean.TRUE.equals(empresa.getIsActual())) {
            convocatoriaRepository.findAll().forEach(convocatoria -> {
                convocatoria.setIsActual(false);
                convocatoriaRepository.save(convocatoria);
            });
        }
        Convocatoria empresaSaved = convocatoriaRepository.save(e);

        return modelMapper.map(empresaSaved,ConvocatoriaDto.class);
    }

    @Transactional
    public ConvocatoriaDto create(ConvocatoriaCreateRequestDto request){
        ConvocatoriaDto convocatoriaDto = request.getConvocatoria();

        if(Boolean.TRUE.equals(convocatoriaDto.getIsActual())) {
            convocatoriaRepository.findAll().forEach(convocatoria -> {
                convocatoria.setIsActual(false);
                convocatoriaRepository.save(convocatoria);
            });
        }

        if(request.getPreviousConvocatoriaId() != null) {
            Convocatoria previousConvocatoria = convocatoriaRepository.findById(request.getPreviousConvocatoriaId()).orElse(null);
            if(previousConvocatoria != null) {
                previousConvocatoria.setPathDesti(request.getPreviousPathDesti());
                convocatoriaRepository.save(previousConvocatoria);
            }
        }

        Convocatoria convocatoria = new Convocatoria();
        convocatoria.setNom(convocatoriaDto.getNom());
        convocatoria.setPathOrigen("FCT");
        convocatoria.setIsUnitatOrganitzativaOrigen(false);
        convocatoria.setPathDesti(convocatoriaDto.getPathDesti());
        convocatoria.setIsUnitatOrganitzativaDesti(true);
        convocatoria.setIsActual(Boolean.TRUE.equals(convocatoriaDto.getIsActual()));
        convocatoria.setIdCursAcademic(convocatoriaDto.getIdCursAcademic());

        Convocatoria convocatoriaSaved = convocatoriaRepository.save(convocatoria);
        return modelMapper.map(convocatoriaSaved, ConvocatoriaDto.class);
    }

    @Transactional
    public void delete(Long id){
        convocatoriaRepository.deleteById(id);
    }
}
