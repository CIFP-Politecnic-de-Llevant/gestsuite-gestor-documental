package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Empresa;
import cat.politecnicllevant.gestsuitegestordocumental.dto.EmpresaDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EmpresaService {

    public final EmpresaRepository empresaRepository;

    public List<EmpresaDto> findAll(){
        ModelMapper modelMapper = new ModelMapper();
        return empresaRepository.findAll().stream().map(e -> modelMapper.map(e,EmpresaDto.class)).collect(Collectors.toList());
    }

    public EmpresaDto findCompanyById(Long id){
        ModelMapper modelMapper =  new ModelMapper();
        Empresa e = empresaRepository.findByIdEmpresa(id);

        return modelMapper.map(e,EmpresaDto.class);
    }

    public EmpresaDto save(EmpresaDto empresa){
        if(empresa.getLlocsTreball() != null){
            empresa.getLlocsTreball().clear();
        }
        if(empresa.getTutorsEmpresa() != null){
            empresa.getTutorsEmpresa().clear();
        }
        ModelMapper modelMapper =  new ModelMapper();
        
        Empresa e = modelMapper.map(empresa, Empresa.class);
        Empresa empresaSaved = empresaRepository.save(e);

        return modelMapper.map(empresaSaved,EmpresaDto.class);
    }

    @Transactional
    public boolean delete(Long id){

        empresaRepository.deleteByIdEmpresa(id);

        return !empresaRepository.existsByIdEmpresa(id) ? true: false;
    }
}
