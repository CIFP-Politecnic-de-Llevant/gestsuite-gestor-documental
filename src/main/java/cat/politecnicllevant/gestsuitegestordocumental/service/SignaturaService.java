package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.TipusDocument;
import cat.politecnicllevant.gestsuitegestordocumental.dto.SignaturaDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.TipusDocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.SignaturaRepository;
import cat.politecnicllevant.gestsuitegestordocumental.repository.TipusDocumentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SignaturaService {

    public final SignaturaRepository signaturaRepository;


    public SignaturaService(
            SignaturaRepository signaturaRepository
    ) {
        this.signaturaRepository = signaturaRepository;
    }

    public List<SignaturaDto> findAll() {
        ModelMapper modelMapper = new ModelMapper();
        return signaturaRepository.findAll().stream().map(s->modelMapper.map(s,SignaturaDto.class)).collect(Collectors.toList());
    }

    public SignaturaDto getSignaturaById(Long id) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(signaturaRepository.findById(id).orElse(null),SignaturaDto.class);
    }

}
