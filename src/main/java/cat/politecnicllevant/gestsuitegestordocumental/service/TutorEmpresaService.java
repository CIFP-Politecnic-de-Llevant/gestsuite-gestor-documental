package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.TutorEmpresa;
import cat.politecnicllevant.gestsuitegestordocumental.dto.LlocTreballDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.RolDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.TutorEmpresaDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.UsuariDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.TutorEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TutorEmpresaService {
    private final TutorEmpresaRepository tutorEmpresaRepository;
    private final ModelMapper modelMapper;

    public List<TutorEmpresaDto> findAll(){
        return tutorEmpresaRepository.findAll().stream().map(t -> modelMapper.map(t, TutorEmpresaDto.class)).collect(Collectors.toList());
    }

    public TutorEmpresaDto findTutorById(Long id){
        TutorEmpresa t = tutorEmpresaRepository.findById(id).orElse(null);

        return modelMapper.map(t, TutorEmpresaDto.class);
    }
    
    public TutorEmpresaDto save(TutorEmpresaDto tutorEmpresa){
        TutorEmpresa t = modelMapper.map(tutorEmpresa, TutorEmpresa.class);
        TutorEmpresa tutorEmpresaSaved = tutorEmpresaRepository.save(t);

        return modelMapper.map(tutorEmpresaSaved,TutorEmpresaDto.class);
    }

    public List<TutorEmpresaDto> finaAllTutorEmpresaByIdCompany(Long id, UsuariDto usuari){
        modelMapper.getConfiguration().setAmbiguityIgnored(true);

        if (usuari.getRols().contains(RolDto.ADMINISTRADOR) || usuari.getRols().contains(RolDto.ADMINISTRADOR_FCT))
            return tutorEmpresaRepository.findAllByEmpresa_IdEmpresa(id).stream().map(t -> modelMapper.map(t, TutorEmpresaDto.class)).collect(Collectors.toList());
        else
            return tutorEmpresaRepository.findAllByEmpresaIdAndValidatTrueOrEmailCreator(id, usuari.getGsuiteEmail()).stream().map(t -> modelMapper.map(t, TutorEmpresaDto.class)).collect(Collectors.toList());
    }

    public List<TutorEmpresaDto> findAllNotValidated(){
        return tutorEmpresaRepository.findAllByValidatFalse().stream()
                .map(ll -> {
                    TutorEmpresaDto tutorEmpresaDto = modelMapper.map(ll, TutorEmpresaDto.class);
                    tutorEmpresaDto.setIdEmpresa(ll.getEmpresa().getIdEmpresa());
                    return tutorEmpresaDto;
                })
                .collect(Collectors.toList());
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
