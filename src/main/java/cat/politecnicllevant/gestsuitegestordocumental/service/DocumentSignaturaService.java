package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import cat.politecnicllevant.gestsuitegestordocumental.domain.DocumentSignatura;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentSignaturaDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.SignaturaDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DocumentRepository;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DocumentSignaturaRepository;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import com.google.api.services.drive.model.File;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentSignaturaService {

    public final DocumentSignaturaRepository documentSignaturaRepository;

    public DocumentSignaturaService(
            DocumentSignaturaRepository documentSignaturaRepository
    ) {
        this.documentSignaturaRepository = documentSignaturaRepository;
    }

    public void signarDocument(DocumentDto documentDto, SignaturaDto signaturaDto, boolean signat) {
        ModelMapper modelMapper = new ModelMapper();
        DocumentSignaturaDto documentSignaturaDto = new DocumentSignaturaDto();
        documentSignaturaDto.setDocument(documentDto);
        documentSignaturaDto.setSignatura(signaturaDto);
        documentSignaturaDto.setSignat(signat);

        DocumentSignatura documentSignatura = modelMapper.map(documentSignaturaDto,DocumentSignatura.class);
        documentSignaturaRepository.save(documentSignatura);
    }


}
