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
    Optional<Document> findByIdDriveGoogleDrive(String idDrive);
    Optional<Document> findByIdGoogleDrive(String id);
    Optional<Document> findByNomOriginal(String nom);
    List<Document> findAllByGrupCodiAndConvocatoria(String grupCodi, Convocatoria convocatoria);
    void deleteByIdDocument(Long idDocument);
    void deleteAllByIdUsuari(Long idusuari);
}
