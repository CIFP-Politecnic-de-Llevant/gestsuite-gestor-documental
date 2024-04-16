package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import cat.politecnicllevant.gestsuitegestordocumental.domain.DocumentSignatura;
import cat.politecnicllevant.gestsuitegestordocumental.domain.DocumentSignaturaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.print.Doc;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentSignaturaRepository extends JpaRepository<DocumentSignatura, DocumentSignaturaId> {
    List<DocumentSignatura> findAllByDocument(Document document);
    Optional<DocumentSignatura> findByDocumentAndSignatura(Document document, Document signatura);
    void deleteDocumentSignaturaByDocument(Document document);
}
