package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.DadesFormulari;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DadesFormulariDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DadesFormulariRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DadesFormulariService{

    private final DadesFormulariRepository dadesFormulariRepository;
    private final ModelMapper modelMapper;

    public DadesFormulariDto save(DadesFormulariDto dadesFormulariDto){
        DadesFormulari df = modelMapper.map(dadesFormulariDto, DadesFormulari.class);
        DadesFormulari dfSave = dadesFormulariRepository.save(df);

        return modelMapper.map(dfSave, DadesFormulariDto.class);
    }

    public void delete(DadesFormulariDto dadesFormulariDto){
        DadesFormulari df = modelMapper.map(dadesFormulariDto, DadesFormulari.class);
        dadesFormulariRepository.delete(df);
    }

    public List<DadesFormulariDto> findAll(){
        return dadesFormulariRepository.findAll().stream().map(df -> modelMapper.map(df, DadesFormulariDto.class)).collect(Collectors.toList());
    }
}
