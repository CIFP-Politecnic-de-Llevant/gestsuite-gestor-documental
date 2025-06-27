package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.LlocTreball;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LlocTreballRepository extends JpaRepository<LlocTreball,Long> {

    void deleteAllByEmpresa_IdEmpresa(Long id);

    List<LlocTreball> findAllByEmpresa_IdEmpresa(Long id);

    void deleteByIdLlocTreball(Long id);

    boolean existsByEmpresa_IdEmpresa(Long id);

    @Query(
        "SELECT l FROM LlocTreball l WHERE l.empresa.idEmpresa = :idEmpresa AND (l.validat = true OR l.emailCreator = :email)"
    )
    List<LlocTreball> findAllByEmpresaIdAndValidatTrueOrEmailCreator(
        @Param("idEmpresa") Long idEmpresa,
        @Param("email") String email
    );

    List<LlocTreball> findAllByValidatFalse();

}

