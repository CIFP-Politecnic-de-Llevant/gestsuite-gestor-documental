package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Alumne;
import cat.politecnicllevant.gestsuitegestordocumental.dto.AlumneDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.AlumneRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class AlumneService {

    public final AlumneRepository alumneRepository;

    public AlumneService(AlumneRepository alumneRepository) {
        this.alumneRepository = alumneRepository;
    }


    public List<AlumneDto> findAll() {
        ModelMapper modelMapper = new ModelMapper();
        return alumneRepository.findAll().stream().map(a -> modelMapper.map(a,AlumneDto.class)).collect(Collectors.toList());
    }
    public AlumneDto save(AlumneDto alumne){

        ModelMapper modelMapper = new ModelMapper();
        Alumne a = modelMapper.map(alumne,Alumne.class);
        Alumne alumneSaved = alumneRepository.save(a);

        return modelMapper.map(alumneSaved,AlumneDto.class);
    }

    @Transactional
    public boolean delete(Long exp){

        alumneRepository.deleteAlumneByNumeroExpedient(exp);

        return !alumneRepository.existsByNumeroExpedient(exp) ? true : false;
    }


}
