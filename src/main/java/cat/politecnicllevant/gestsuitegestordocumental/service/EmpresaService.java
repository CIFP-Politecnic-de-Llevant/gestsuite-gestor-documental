package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Empresa;
import cat.politecnicllevant.gestsuitegestordocumental.dto.EmpresaDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.EmpresaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpresaService {

    public final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository){
        this.empresaRepository = empresaRepository;
    }

    public List<EmpresaDto> findAll(){

        ModelMapper modelMapper = new ModelMapper();
        return empresaRepository.findAll().stream().map(e -> modelMapper.map(e,EmpresaDto.class)).collect(Collectors.toList());
    }
    public EmpresaDto save(EmpresaDto empresa){

        ModelMapper modelMapper =  new ModelMapper();
        Empresa e = modelMapper.map(empresa, Empresa.class);
        Empresa empresaSaved = empresaRepository.save(e);

        return modelMapper.map(empresaSaved,EmpresaDto.class);
    }

    public boolean delete(Long id){

        empresaRepository.deleteByIdEmpresa(id);

        return !empresaRepository.existsByIdEmpresa(id) ? true: false;
    }
}
