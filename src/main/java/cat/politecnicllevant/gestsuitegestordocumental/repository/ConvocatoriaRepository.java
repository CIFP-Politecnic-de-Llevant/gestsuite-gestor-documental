package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Convocatoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria,Long> {
}
