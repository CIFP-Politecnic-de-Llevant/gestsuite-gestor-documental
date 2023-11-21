package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.UsuariDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DocumentRepository;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    public final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public DocumentDto getDocumentById(Long id) {
        ModelMapper modelMapper = new ModelMapper();
        Document document = documentRepository.findById(id).orElse(null);
        return modelMapper.map(document,DocumentDto.class);
    }

    public DocumentDto getDocumentByIdDrive(String idDrive) {
        ModelMapper modelMapper = new ModelMapper();
        Document document =  documentRepository.findByIdDrive(idDrive);
        return modelMapper.map(document,DocumentDto.class);
    }

    public void save(DocumentDto documentDto){
        ModelMapper modelMapper = new ModelMapper();
        Document document = modelMapper.map(documentDto,Document.class);
        documentRepository.save(document);
    }


}
