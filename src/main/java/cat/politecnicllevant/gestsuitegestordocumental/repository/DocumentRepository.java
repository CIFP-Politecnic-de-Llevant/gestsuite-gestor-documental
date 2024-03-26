package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByIdDriveGoogleDrive(String idDrive);
    Optional<Document> findByIdGoogleDrive(String id);
    Optional<Document> findByNomOriginal(String nom);
    List<Document> findAllByGrupCodi(String grupCodi);
    void deleteAllByIdUsuari(Long idusuari);
}
