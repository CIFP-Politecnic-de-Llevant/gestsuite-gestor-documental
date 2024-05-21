package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.DadesFormulari;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DadesFormulariDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DadesFormulariRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DadesFormulariService{

    private final DadesFormulariRepository dadesFormulariRepository;

    public DadesFormulariService(DadesFormulariRepository dadesFormulariRepository) {
        this.dadesFormulariRepository = dadesFormulariRepository;
    }

    public DadesFormulariDto save(DadesFormulariDto dadesFormulariDto){

        ModelMapper modelMapper= new ModelMapper();
        DadesFormulari df = modelMapper.map(dadesFormulariDto, DadesFormulari.class);
        DadesFormulari dfSave = dadesFormulariRepository.save(df);

        return modelMapper.map(dfSave, DadesFormulariDto.class);
    }

    public void delete(DadesFormulariDto dadesFormulariDto){

        ModelMapper modelMapper = new ModelMapper();
        DadesFormulari df = modelMapper.map(dadesFormulariDto, DadesFormulari.class);

        dadesFormulariRepository.delete(df);
    }

    public List<DadesFormulariDto> findAll(){

        ModelMapper modelMapper = new ModelMapper();

        return dadesFormulariRepository.findAll().stream().map(df -> modelMapper.map(df, DadesFormulariDto.class)).collect(Collectors.toList());
    }
}
