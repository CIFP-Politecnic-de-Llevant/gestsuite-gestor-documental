package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.TipusDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipusDocumentRepository extends JpaRepository<TipusDocument, Long> {
    TipusDocument findTipusDocumentByNom(String nom);
}
