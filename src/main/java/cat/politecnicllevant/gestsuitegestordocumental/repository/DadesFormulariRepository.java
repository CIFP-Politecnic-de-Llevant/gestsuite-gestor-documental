package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.DadesFormulari;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DadesFormulariRepository extends JpaRepository<DadesFormulari,String> {

    DadesFormulari findByNomAlumneAndLlinatgesAlumne(String nom, String llinatges);
}
