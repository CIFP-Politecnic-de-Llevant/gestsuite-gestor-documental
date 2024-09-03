package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Convocatoria;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ConvocatoriaDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.ConvocatoriaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConvocatoriaService {

    public final ConvocatoriaRepository convocatoriaRepository;

    public ConvocatoriaService(ConvocatoriaRepository convocatoriaRepository){
        this.convocatoriaRepository = convocatoriaRepository;
    }

    public List<ConvocatoriaDto> findAll(){
        ModelMapper modelMapper = new ModelMapper();
        return convocatoriaRepository.findAll().stream().map(e -> modelMapper.map(e,ConvocatoriaDto.class)).collect(Collectors.toList());
    }

    public ConvocatoriaDto findConvocatoriaById(Long id){
        ModelMapper modelMapper =  new ModelMapper();
        Convocatoria e = convocatoriaRepository.findById(id).orElse(null);

        return modelMapper.map(e,ConvocatoriaDto.class);
    }

    public ConvocatoriaDto save(ConvocatoriaDto empresa){
        ModelMapper modelMapper =  new ModelMapper();

        Convocatoria e = modelMapper.map(empresa, Convocatoria.class);
        Convocatoria empresaSaved = convocatoriaRepository.save(e);

        return modelMapper.map(empresaSaved,ConvocatoriaDto.class);
    }

    @Transactional
    public void delete(Long id){
        convocatoriaRepository.deleteById(id);
    }
}
