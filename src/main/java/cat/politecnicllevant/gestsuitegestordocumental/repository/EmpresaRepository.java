package cat.politecnicllevant.gestsuitegestordocumental.repository;

import cat.politecnicllevant.gestsuitegestordocumental.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa,Long> {

    Empresa findByIdEmpresa(Long id);
    void deleteByIdEmpresa(Long id);
    boolean existsByIdEmpresa(Long id);
}
