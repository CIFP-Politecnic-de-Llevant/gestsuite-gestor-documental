package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Grup;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.GrupRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GrupService {

    public final GrupRepository grupRepository;

    public GrupService(GrupRepository grupRepository) {
        this.grupRepository = grupRepository;
    }

    public List<GrupDto> findAll(){
        List<GrupDto> grupDtos = new ArrayList<>();
        List<Grup> grups = grupRepository.findAll();

        for(Grup grup : grups) {
            GrupDto grupDto = new GrupDto();
            grupDto.setCoreIdGrup(grup.getCoreIdGrup());
            grupDto.setIdGoogleSpreadsheet(grup.getIdGoogleSpreadsheet());
            grupDto.setFolderGoogleDrive(grup.getFolderGoogleDrive());
            grupDto.setCursGrup(grup.getCursGrup());
            grupDto.setIdgrup(grup.getIdGrupGestorDocumental());
            grupDtos.add(grupDto);
        }

        return grupDtos;
    }

    public GrupDto getById(long id){
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(grupRepository.findById(id).orElse(null), GrupDto.class);
    }

    public GrupDto getByIdGrupCore(long id){
        Grup grup = grupRepository.findByCoreIdGrup(id);
        if(grup !=null) {
            GrupDto grupDto = new GrupDto();
            grupDto.setCoreIdGrup(grup.getCoreIdGrup());
            grupDto.setIdGoogleSpreadsheet(grup.getIdGoogleSpreadsheet());
            grupDto.setFolderGoogleDrive(grup.getFolderGoogleDrive());
            grupDto.setCursGrup(grup.getCursGrup());
            grupDto.setIdgrup(grup.getIdGrupGestorDocumental());
            return grupDto;
        }
        return null;
    }
}
