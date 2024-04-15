package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Alumne;
import cat.politecnicllevant.gestsuitegestordocumental.dto.AlumneDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumneRepository extends JpaRepository<Alumne,Long> {

    //void deleteAlumneByNumeroExpedientAndNomAndCognom1AndCognom2(Long numero_expedient, String nom, String cognom1,String cognom2);
    void deleteAlumneByNumeroExpedient(Long exp);

    boolean existsByNumeroExpedient(Long exp);
}
