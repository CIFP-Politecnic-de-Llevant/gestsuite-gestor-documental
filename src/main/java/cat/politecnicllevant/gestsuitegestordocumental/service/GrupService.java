package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Grup;
import cat.politecnicllevant.gestsuitegestordocumental.dto.GrupDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.GrupRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GrupService {

    private final GrupRepository grupRepository;
    private final ModelMapper modelMapper;


    public List<GrupDto> findAll() {
        List<GrupDto> grupDtos = new ArrayList<>();
        List<Grup> grups = grupRepository.findAll();

        for (Grup grup : grups) {
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

    public List<GrupDto> findAllWithFempo() {
        List<GrupDto> grupWithFempoDtos = new ArrayList<>();
        List<Grup> grupsWithFempo = grupRepository.findByIdGoogleSpreadsheetIsNotNull();
        for (Grup grup : grupsWithFempo) {
            GrupDto grupDto = new GrupDto();
            grupDto.setIdGoogleSpreadsheet(grup.getIdGoogleSpreadsheet());
            grupDto.setFolderGoogleDrive(grup.getFolderGoogleDrive());
            grupDto.setCursGrup(grup.getCursGrup());

            grupWithFempoDtos.add(grupDto);
        }

        return grupWithFempoDtos;
    }

    public GrupDto getById(long id) {
        return modelMapper.map(grupRepository.findById(id).orElse(null), GrupDto.class);
    }

    public GrupDto getByIdGrupCore(long id) {
        Grup grup = grupRepository.findByCoreIdGrup(id);
        if (grup != null) {
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
