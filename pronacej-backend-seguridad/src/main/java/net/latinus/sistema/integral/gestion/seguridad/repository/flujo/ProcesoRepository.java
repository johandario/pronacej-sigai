package net.latinus.sistema.integral.gestion.seguridad.repository.flujo;

import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.Proceso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcesoRepository extends JpaRepository<Proceso, Long> {
    Page<Proceso> findByRemovido(Boolean removido, Pageable pageable);

    @Query("SELECT p FROM Proceso p " +
            "JOIN FETCH p.pasos pa " +
            "WHERE pa.removido = false AND p.removido = false " +
            "ORDER BY p.nombre ASC"
            )
    //Page<Proceso> obtenerProcesosValido(Pageable pageable);
    List<Proceso> obtenerProcesosValido();

    Proceso findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("SELECT p FROM Proceso p " +
            "JOIN FETCH p.pasos pa " +
            "WHERE p.tokenIdentificador = :tokenIdentificador and pa.removido = false AND p.removido = false")
    Proceso obtenerProcesoValidoPorTokenIdentificador(@Param("tokenIdentificador") String tokenIdentificador);

    Proceso findByNemonicoAndRemovido(String nemonicoProceso, Boolean removido);

    @Query("SELECT p.nombre FROM Proceso p " +
            "WHERE p.removido = false " +
            "ORDER BY p.nombre ASC")
    List<String> obtenerListaDeProcesosHabilitados();
}
