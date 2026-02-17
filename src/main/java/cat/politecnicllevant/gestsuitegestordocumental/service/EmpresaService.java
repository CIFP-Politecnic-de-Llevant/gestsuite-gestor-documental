package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Empresa;
import cat.politecnicllevant.gestsuitegestordocumental.domain.LlocTreball;
import cat.politecnicllevant.gestsuitegestordocumental.dto.EmpresaDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.EmpresaRepository;
import cat.politecnicllevant.gestsuitegestordocumental.repository.LlocTreballRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final LlocTreballRepository llocTreballRepository;
    private final ModelMapper modelMapper;

    public List<EmpresaDto> findAll(){
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        return empresaRepository.findAll().stream().map(e -> modelMapper.map(e,EmpresaDto.class)).collect(Collectors.toList());
    }

    public EmpresaDto findCompanyById(Long id){
        modelMapper.getConfiguration().setAmbiguityIgnored(true);

        Empresa e = empresaRepository.findByIdEmpresa(id);
        return modelMapper.map(e,EmpresaDto.class);
    }

    @Transactional
    public EmpresaDto save(EmpresaDto empresa){
        boolean isNewEmpresa = empresa.getIdEmpresa() == null || !empresaRepository.existsByIdEmpresa(empresa.getIdEmpresa());

        if(empresa.getLlocsTreball() != null){
            empresa.getLlocsTreball().clear();
        }
        if(empresa.getTutorsEmpresa() != null){
            empresa.getTutorsEmpresa().clear();
        }
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        
        Empresa e = modelMapper.map(empresa, Empresa.class);
        Empresa empresaSaved = empresaRepository.save(e);

        if(isNewEmpresa){
            LlocTreball llocTreball = new LlocTreball();
            llocTreball.setEmpresa(empresaSaved);
            llocTreball.setNom(empresaSaved.getNom());
            llocTreball.setAdreca(empresaSaved.getAdreca());
            llocTreball.setCodiPostal(empresaSaved.getCodiPostal());
            llocTreball.setTelefon(empresaSaved.getTelefon());
            llocTreball.setPoblacio(empresaSaved.getPoblacio());
            llocTreball.setMunicipi(empresaSaved.getPoblacio());
            llocTreball.setNomContacte(empresaSaved.getNomRepresentantLegal());
            llocTreball.setCognom1Contacte(empresaSaved.getCognom1RepresentantLegal());
            llocTreball.setCognom2Contacte(empresaSaved.getCognom2RepresentantLegal());
            llocTreball.setTelefonContacte(empresaSaved.getTelefon());
            llocTreball.setEmailContacte(empresaSaved.getEmailEmpresa());
            llocTreballRepository.save(llocTreball);
        }

        return modelMapper.map(empresaSaved,EmpresaDto.class);
    }

    @Transactional
    public boolean delete(Long id){

        empresaRepository.deleteByIdEmpresa(id);

        return !empresaRepository.existsByIdEmpresa(id) ? true: false;
    }
}
