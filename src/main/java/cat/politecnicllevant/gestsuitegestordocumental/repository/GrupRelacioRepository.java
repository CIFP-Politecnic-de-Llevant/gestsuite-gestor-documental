package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.GrupRelacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrupRelacioRepository extends JpaRepository<GrupRelacio, Long> {

    List<GrupRelacio> findAllByGrup_IdGrupGestorDocumental(Long grupId);

    void deleteByGrup_IdGrupGestorDocumentalAndGrupRelacionat_IdGrupGestorDocumental(Long grupId, Long grupRelacionatId);

    boolean existsByGrup_IdGrupGestorDocumentalAndGrupRelacionat_IdGrupGestorDocumental(Long grupId, Long grupRelacionatId);
}
