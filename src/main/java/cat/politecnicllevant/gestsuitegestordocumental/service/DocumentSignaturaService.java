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
import jakarta.transaction.Transactional;
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

    public List<DocumentSignaturaDto> findByDocument(DocumentDto documentDto){
        ModelMapper modelMapper = new ModelMapper();
        return documentSignaturaRepository.findAllByDocument(modelMapper.map(documentDto,Document.class)).stream().map(d->modelMapper.map(d,DocumentSignaturaDto.class)).collect(Collectors.toList());
    }

    public DocumentSignaturaDto findByDocumentAndSignatura(DocumentDto documentDto, SignaturaDto signaturaDto){
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(documentSignaturaRepository.findByDocumentAndSignatura(modelMapper.map(documentDto,Document.class),modelMapper.map(signaturaDto,Document.class)).orElse(null),DocumentSignaturaDto.class);
    }

    @Transactional
    public DocumentSignaturaDto save(DocumentSignaturaDto documentSignaturaDto){
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(documentSignaturaRepository.save(modelMapper.map(documentSignaturaDto,DocumentSignatura.class)),DocumentSignaturaDto.class);
    }

    @Transactional
    public void deleteSignaturaByDocumentIdDocument(DocumentDto documentDto) {
        ModelMapper modelMapper = new ModelMapper();
        Document document = modelMapper.map(documentDto, Document.class);
        this.documentSignaturaRepository.deleteDocumentSignaturaByDocument(document);
    }

}
