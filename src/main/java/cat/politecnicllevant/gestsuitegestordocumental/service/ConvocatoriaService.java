package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Convocatoria;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ConvocatoriaDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.ConvocatoriaRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;
    private final ModelMapper modelMapper;

    public List<ConvocatoriaDto> findAll(){
        return convocatoriaRepository.findAll().stream().map(e -> modelMapper.map(e,ConvocatoriaDto.class)).collect(Collectors.toList());
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
        Convocatoria empresaSaved = convocatoriaRepository.save(e);

        return modelMapper.map(empresaSaved,ConvocatoriaDto.class);
    }

    @Transactional
    public void delete(Long id){
        convocatoriaRepository.deleteById(id);
    }
}
