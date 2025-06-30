package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.TutorEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutorEmpresaRepository extends JpaRepository<TutorEmpresa,Long> {

    void deleteAllByEmpresa_IdEmpresa(Long id);

    List<TutorEmpresa> findAllByEmpresa_IdEmpresa(Long id);

    void deleteByIdTutorEmpresa(Long id);

    boolean existsByEmpresa_IdEmpresa(Long id);

    @Query(
            "SELECT t FROM TutorEmpresa t WHERE t.empresa.idEmpresa = :idEmpresa AND (t.validat = true OR t.emailCreator = :email)"
    )
    List<TutorEmpresa> findAllByEmpresaIdAndValidatTrueOrEmailCreator(
            @Param("idEmpresa") Long idEmpresa,
            @Param("email") String email
    );

    List<TutorEmpresa> findAllByValidatFalse();

}

