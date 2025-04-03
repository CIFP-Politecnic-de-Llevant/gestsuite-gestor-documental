package cat.politecnicllevant.gestsuitegestordocumental.service;

import cat.politecnicllevant.gestsuitegestordocumental.domain.TipusDocument;
import cat.politecnicllevant.gestsuitegestordocumental.dto.TipusDocumentDto;
import cat.politecnicllevant.gestsuitegestordocumental.repository.TipusDocumentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TipusDocumentService {

    public final TipusDocumentRepository tipusDocumentRepository;


    public TipusDocumentService(
            TipusDocumentRepository tipusDocumentRepository
    ) {
        this.tipusDocumentRepository = tipusDocumentRepository;
    }

    public List<TipusDocumentDto> findAll() {
        ModelMapper modelMapper = new ModelMapper();
        return tipusDocumentRepository.findAll().stream().map(td->modelMapper.map(td,TipusDocumentDto.class)).collect(Collectors.toList());
    }

    public TipusDocumentDto getTipusDocumentByNom(String nom) {
        ModelMapper modelMapper = new ModelMapper();
        //No podem fer findTipusDocumentByNom perquè el caràcter guió (-) no el cerca bé
        //TipusDocument tipusDocument = tipusDocumentRepository.findTipusDocumentByNom(nom);
        TipusDocument tipusDocument = tipusDocumentRepository.findAll()
                .stream()
                .filter(td->td.getNom() != null && (!td.getNom().isEmpty()) && removeIllegalCharacters(td.getNom()).equals(removeIllegalCharacters(nom)))
                .findFirst()
                .orElse(null);

        if(tipusDocument == null && nom != null && !nom.isEmpty()) {
            tipusDocument = tipusDocumentRepository.findAll()
                    .stream()
                    .filter(td -> td.getNom() != null && (!td.getNom().isEmpty()) && (removeIllegalCharacters(td.getNom()).equals(removeIllegalCharacters(nom)) || removeIllegalCharacters(nom).contains(removeIllegalCharacters(td.getNom()))))
                    .findFirst()
                    .orElse(null);
        }

        if(tipusDocument!=null) {
            return modelMapper.map(tipusDocument, TipusDocumentDto.class);
        }
        return null;
    }

    private String removeIllegalCharacters(String nom) {
        return nom.replaceAll("[^a-zA-Z0-9]", "");
    }

    public TipusDocumentDto getTipusDocumentById(Long id) {
        ModelMapper modelMapper = new ModelMapper();
        TipusDocument tipusDocument = tipusDocumentRepository.findById(id).orElse(null);
        if(tipusDocument!=null) {
            return modelMapper.map(tipusDocument, TipusDocumentDto.class);
        }
        return null;
    }

}
