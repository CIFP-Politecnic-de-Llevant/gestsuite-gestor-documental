package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Grup;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.GrupRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrupService {

    public final GrupRepository grupRepository;

    public List<GrupDto> findAll(){
        ModelMapper modelMapper =  new ModelMapper();
        return grupRepository.findAll().stream().map(pf->modelMapper.map(pf, GrupDto.class)).collect(Collectors.toList());
    }

    public GrupDto getById(long id){
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(grupRepository.findById(id).orElse(null), GrupDto.class);
    }

    public GrupDto getByIdGrupCore(long id){
        ModelMapper modelMapper = new ModelMapper();
        Grup grup = grupRepository.findByIdGrupCore(id);
        if(grup !=null) {
            return modelMapper.map(grup, GrupDto.class);
        }
        return null;
    }
}
