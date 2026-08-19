package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.SancionDisciplinaria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface SancionDisciplinariaRepository extends JpaRepository<SancionDisciplinaria, Long>{

    SancionDisciplinaria findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<SancionDisciplinaria> findAllByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("SELECT s FROM SancionDisciplinaria s WHERE s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador AND s.removido = :removido")
    List<SancionDisciplinaria> findAllByFichaIdentificacionTokenIdentificadorAndRemovido(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("removido") Boolean removido
    );

    List<SancionDisciplinaria> findByFichaIdentificacionIdFichaIdentificacionAndRemovido(Long idFichaIdentificacion, Boolean removido);

    @Query("SELECT s FROM SancionDisciplinaria s " +
            "LEFT JOIN s.tipificacionFalta t " +
            "WHERE s.removido = false " +
            "AND s.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND (" +
            "LOWER(s.nroResolucion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.falta) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.sancion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.motivo) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            ")")
    Page<SancionDisciplinaria> buscarPorTokenYFiltro(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("filter") String filter,
            Pageable pageable
    );

    Page<SancionDisciplinaria> findByFichaIdentificacionTokenIdentificadorAndRemovido(
            String tokenIdentificador,
            Boolean removido,
            Pageable pageable
    );
}
