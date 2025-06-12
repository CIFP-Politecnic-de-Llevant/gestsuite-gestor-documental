package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.TutorEmpresa;
import cat.politecnicllevant.gestsuitegestordocumental.dto.TutorEmpresaDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.TutorEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TutorEmpresaService {
    public final TutorEmpresaRepository tutorEmpresaRepository;

    public List<TutorEmpresaDto> findAll(){
        ModelMapper modelMapper = new ModelMapper();
        return tutorEmpresaRepository.findAll().stream().map(t -> modelMapper.map(t, TutorEmpresaDto.class)).collect(Collectors.toList());
    }
    
    public TutorEmpresaDto save(TutorEmpresaDto tutorEmpresa){

        ModelMapper modelMapper =  new ModelMapper();
        TutorEmpresa t = modelMapper.map(tutorEmpresa, TutorEmpresa.class);
        TutorEmpresa tutorEmpresaSaved = tutorEmpresaRepository.save(t);

        return modelMapper.map(tutorEmpresaSaved,TutorEmpresaDto.class);
    }
    public List<TutorEmpresaDto> finaAllWorkspabeByIdCompany(Long id){

        ModelMapper modelMapper = new ModelMapper();
        return tutorEmpresaRepository.findAllByEmpresa_IdEmpresa(id).stream().map(t -> modelMapper.map(t, TutorEmpresaDto.class)).collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteById(Long id){

        tutorEmpresaRepository.deleteByIdTutorEmpresa(id);

        return !tutorEmpresaRepository.existsById(id);
    }
    @Transactional
    public boolean deleteByIdEmpresa(Long id){

        tutorEmpresaRepository.deleteAllByEmpresa_IdEmpresa(id);

        return !tutorEmpresaRepository.existsByEmpresa_IdEmpresa(id);
    }
}
