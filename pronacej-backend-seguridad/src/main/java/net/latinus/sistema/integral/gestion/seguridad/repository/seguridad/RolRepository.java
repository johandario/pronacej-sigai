package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Rol findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<Rol> findByEmpresaTokenIdentificadorAndRemovidoOrderByIdRolDesc(String tokenIdentificadorEmpresa, Boolean removido);
        
    Page<Rol> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);

    @Query("SELECT r FROM Rol r WHERE " +
            "(LOWER(r.nombre) LIKE LOWER(CONCAT('%', :param, '%')) OR " +
            "LOWER(r.codigo) LIKE LOWER(CONCAT('%', :param, '%')) OR " +
            "LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :param, '%'))) " +
            "AND r.removido = false")
    Page<Rol> buscarPorValor(@Param("param") String param, Pageable pageable);
    
    List<Rol> findByNombreIgnoreCaseAndRemovido(String nombre, Boolean removido);

}
