package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Convocatoria;
import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByConvocatoria(Convocatoria convocatoria);
    Optional<Document> findByIdDriveGoogleDriveAndConvocatoria(String idDrive, Convocatoria convocatoria);
    Optional<Document> findByIdGoogleDriveAndConvocatoria(String id, Convocatoria convocatoria);
    Optional<Document> findByNomOriginalAndConvocatoria(String nom, Convocatoria convocatoria);
    Optional<Document> findByIdDocumentAndConvocatoria(Long id, Convocatoria convocatoria);
    List<Document> findAllByGrupCodiAndConvocatoria(String grupCodi, Convocatoria convocatoria);
    void deleteByIdDocumentAndConvocatoria(Long idDocument, Convocatoria convocatoria);
    void deleteAllByIdUsuariAndConvocatoria(Long idusuari, Convocatoria convocatoria);
}
