package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.ProgramaFormatiu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramaFormatiuRepository extends JpaRepository<ProgramaFormatiu,Long> {

    void deleteByIdPFormatiu(Long id);
    List<ProgramaFormatiu> findAllByIdGrup(Long id);

}
