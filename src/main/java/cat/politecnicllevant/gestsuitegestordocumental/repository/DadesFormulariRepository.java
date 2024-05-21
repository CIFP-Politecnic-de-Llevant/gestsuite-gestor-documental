package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.DadesFormulari;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DadesFormulariRepository extends MongoRepository<DadesFormulari,String> {

    DadesFormulari findByNomAlumneAndLlinatgesAlumne(String nom, String llinatges);
}
