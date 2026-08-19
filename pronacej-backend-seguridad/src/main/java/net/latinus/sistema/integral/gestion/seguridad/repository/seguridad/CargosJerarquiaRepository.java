package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.CargosJerarquia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CargosJerarquiaRepository extends JpaRepository<CargosJerarquia, Long> {

    List<CargosJerarquia> findByRemovido(Boolean removido);
    CargosJerarquia findCargosJerarquiaByIdCargosJerarquia(Long idCargosJerarquia);
    CargosJerarquia findCargosJerarquiaByTokenIdentificador(String tokenIdentificador);
    List<CargosJerarquia> findByEmpresaTokenIdentificadorAndRemovidoOrderByIdCargosJerarquiaDesc(String tokenIdentificadorEmpresa,
                                                                                                 Boolean removido);
    
    Page<CargosJerarquia> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    CargosJerarquia findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<CargosJerarquia> findByNombreIgnoreCaseAndRemovido(String nombre, Boolean removido);

    @Query("SELECT c FROM CargosJerarquia c " +
            "WHERE c.removido = false AND " +
            "(LOWER(FUNCTION('REPLACE', c.nombre, ' ', '')) LIKE LOWER(FUNCTION('REPLACE', CONCAT('%', :param, '%'), ' ', '')))")
    Page<CargosJerarquia> buscarPorValor(@Param("param") String param, Pageable pageable);

}
