package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import cat.politecnicllevant.gestsuitegestordocumental.domain.TipusDocument;
import cat.politecnicllevant.gestsuitegestordocumental.dto.DocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.dto.TipusDocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.DocumentRepository;
import cat.politecnicllevant.gestsuitegestordocumental.repository.TipusDocumentRepository;
import cat.politecnicllevant.gestsuitegestordocumental.restclient.CoreRestClient;
import com.google.api.services.drive.model.File;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class TipusDocumentService {

    public final TipusDocumentRepository tipusDocumentRepository;


    public TipusDocumentService(
            TipusDocumentRepository tipusDocumentRepository
    ) {
        this.tipusDocumentRepository = tipusDocumentRepository;
    }

    public TipusDocumentDto getTipusDocumentByNom(String nom) {
        ModelMapper modelMapper = new ModelMapper();
        TipusDocument tipusDocument = tipusDocumentRepository.findTipusDocumentByNom(nom);
        if(tipusDocument!=null) {
            return modelMapper.map(tipusDocument, TipusDocumentDto.class);
        }
        return null;
    }

}
