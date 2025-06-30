package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.ProgramaFormatiu;
import cat.politecnicllevant.gestsuitegestordocumental.dto.ProgramaFormatiuDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.ProgramaFormatiuRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProgramaFormatiuService {

    private final ProgramaFormatiuRepository programaFormatiuRepository;
    private final ModelMapper modelMapper;

    public List<ProgramaFormatiuDto> findAll(){
        return programaFormatiuRepository.findAll().stream().map(pf->modelMapper.map(pf, ProgramaFormatiuDto.class)).collect(Collectors.toList());
    }

    public List<ProgramaFormatiuDto> findAllById(long id){
        return programaFormatiuRepository.findAllByIdGrup(id).stream().map(pf->modelMapper.map(pf, ProgramaFormatiuDto.class)).collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteById (Long id){

        programaFormatiuRepository.deleteByIdPFormatiu(id);
        return programaFormatiuRepository.existsById(id);
    }

    public ProgramaFormatiuDto save(ProgramaFormatiuDto programaFormatiuDto){
        ProgramaFormatiu pf = modelMapper.map(programaFormatiuDto, ProgramaFormatiu.class);
        ProgramaFormatiu pfSave = programaFormatiuRepository.save(pf);

        return modelMapper.map(pfSave, ProgramaFormatiuDto.class);
    }

}
