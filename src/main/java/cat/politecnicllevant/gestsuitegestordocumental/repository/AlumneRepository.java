package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Alumne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumneRepository extends JpaRepository<Alumne,Long> {

    //void deleteAlumneByNumeroExpedientAndNomAndCognom1AndCognom2(Long numero_expedient, String nom, String cognom1,String cognom2);
    void deleteAlumneByNumeroExpedient(String exp);

    boolean existsByNumeroExpedient(String exp);
    Alumne findByNumeroExpedient(String exp);
}
