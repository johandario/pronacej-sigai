package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.ActividadOcupacional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActividadOcupacionalRepository extends JpaRepository<ActividadOcupacional, Long> {

    Page<ActividadOcupacional> findByFichaIdentificacionTokenIdentificadorAndRemovido(
            String tokenIdentificador,
            Boolean removido,
            Pageable pageable
    );

    @Query("SELECT a FROM ActividadOcupacional a " +
            "LEFT JOIN a.programa p " +
            "WHERE a.removido = false " +
            "AND a.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND (" +
            ":filter IS NULL OR :filter = '' OR " +
            "CAST(a.fechaInicio AS string) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(a.numeroDocumento) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))" +
            ")")
    Page<ActividadOcupacional> buscarPorFiltro(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("filter") String filter,
            Pageable pageable
    );



    Optional<ActividadOcupacional> findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);


}
