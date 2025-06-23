package cat.politecnicllevant.gestsuitegestordocumental.service;


import cat.politecnicllevant.gestsuitegestordocumental.domain.LlocTreball;
import cat.politecnicllevant.gestsuitegestordocumental.dto.LlocTreballDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.RolDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.UsuariDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.LlocTreballRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LlocTreballService {

    public final LlocTreballRepository llocTreballRepository;

    public LlocTreballService(LlocTreballRepository llocTreballRepository){
        this.llocTreballRepository = llocTreballRepository;
    }

    public List<LlocTreballDto> findAll(){

        ModelMapper modelMapper = new ModelMapper();
        return llocTreballRepository.findAll().stream().map(ll -> modelMapper.map(ll, LlocTreballDto.class)).collect(Collectors.toList());
    }
    public LlocTreballDto save(LlocTreballDto llocTreball){

        ModelMapper modelMapper =  new ModelMapper();
        LlocTreball ll = modelMapper.map(llocTreball, LlocTreball.class);
        LlocTreball llocTreballSaved = llocTreballRepository.save(ll);

        return modelMapper.map(llocTreballSaved,LlocTreballDto.class);
    }
    public List<LlocTreballDto> finaAllWorkspaceByIdCompany(Long id, UsuariDto usuari){

        ModelMapper modelMapper = new ModelMapper();

        if (usuari.getRols().contains(RolDto.ADMINISTRADOR) || usuari.getRols().contains(RolDto.ADMINISTRADOR_FCT))
            return llocTreballRepository.findAllByEmpresa_IdEmpresa(id).stream().map(ll -> modelMapper.map(ll, LlocTreballDto.class)).collect(Collectors.toList());
        else
            return llocTreballRepository.findAllByEmpresaIdAndValidatTrueOrEmailCreator(id, usuari.getGsuiteEmail()).stream().map(ll -> modelMapper.map(ll, LlocTreballDto.class)).collect(Collectors.toList());

    }

    @Transactional
    public boolean deleteById(Long id){

        llocTreballRepository.deleteByIdLlocTreball(id);

        return !llocTreballRepository.existsById(id);
    }
    @Transactional
    public boolean deleteByIdEmpresa(Long id){

        llocTreballRepository.deleteAllByEmpresa_IdEmpresa(id);

        return !llocTreballRepository.existsByEmpresa_IdEmpresa(id);
    }
}
