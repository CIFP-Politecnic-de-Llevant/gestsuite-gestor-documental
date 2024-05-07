package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.ProgramaFormatiu;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ProgramaFormatiuDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.ProgramaFormatiuRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgramaFormatiuService {

    public final ProgramaFormatiuRepository programaFormatiuRepository;


    public ProgramaFormatiuService(ProgramaFormatiuRepository programaFormatiuRepository) {
        this.programaFormatiuRepository = programaFormatiuRepository;
    }

    public List<ProgramaFormatiuDto> findAll(){

        ModelMapper modelMapper =  new ModelMapper();
        return programaFormatiuRepository.findAll().stream().map(pf->modelMapper.map(pf, ProgramaFormatiuDto.class)).collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteById (Long id){

        programaFormatiuRepository.deleteByIdPFormatiu(id);
        return programaFormatiuRepository.existsById(id);
    }

    public ProgramaFormatiuDto save(ProgramaFormatiuDto programaFormatiuDto){

        ModelMapper modelMapper = new ModelMapper();
        ProgramaFormatiu pf = modelMapper.map(programaFormatiuDto, ProgramaFormatiu.class);
        ProgramaFormatiu pfSave = programaFormatiuRepository.save(pf);

        return modelMapper.map(pfSave, ProgramaFormatiuDto.class);
    }

}
