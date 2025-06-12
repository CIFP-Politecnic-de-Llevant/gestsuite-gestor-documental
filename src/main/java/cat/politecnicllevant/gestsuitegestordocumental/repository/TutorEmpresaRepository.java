package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.TutorEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutorEmpresaRepository extends JpaRepository<TutorEmpresa,Long> {

    void deleteAllByEmpresa_IdEmpresa(Long id);

    List<TutorEmpresa> findAllByEmpresa_IdEmpresa(Long id);

    void deleteByIdTutorEmpresa(Long id);

    boolean existsByEmpresa_IdEmpresa(Long id);
}

