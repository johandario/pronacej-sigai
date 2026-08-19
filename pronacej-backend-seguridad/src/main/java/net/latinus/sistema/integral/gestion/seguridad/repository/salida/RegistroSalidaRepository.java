package net.latinus.sistema.integral.gestion.seguridad.repository.salida;



import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.RegistroSalida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroSalidaRepository extends JpaRepository<RegistroSalida, Long>{

    Page<RegistroSalida> findByRemovido(Boolean removido, Pageable pageable);
    RegistroSalida findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    List<RegistroSalida> findAllByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    @Query("SELECT r FROM RegistroSalida r WHERE r.tokenFichaIdentificacion.tokenIdentificador = :tokenIdentificador AND r.removido = :removido")
    List<RegistroSalida> findAllByFichaIdentificacionTokenIdentificadorAndRemovido(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("removido") Boolean removido
    );
    @Query("SELECT r FROM RegistroSalida r " +
            "JOIN r.tokenFichaIdentificacion f " +
            "LEFT JOIN r.motivoSalida m " +
            "WHERE r.removido = false " +
            "AND r.centroSalida.tokenIdentificador = :tokenIdentificador " +
            "AND (" +
            "LOWER(CONCAT(f.nombres, ' ', f.apellidoPaterno, ' ', f.apellidoMaterno)) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.nroDocumento) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))" +
            ")")
    Page<RegistroSalida> buscarPorFiltroYCentro(@Param("tokenIdentificador") String tokenIdentificador,
                                         @Param("filter") String filter,
                                         Pageable pageable);

    @Query("""
    SELECT r FROM RegistroSalida r
    WHERE r.removido = false
    AND r.tokenFichaIdentificacion.tokenIdentificador = :tokenIdentificador
    AND (
        EXISTS (
            SELECT 1 FROM EventoFuga ef
            WHERE ef = r.eventoFuga
            AND ef.estadoEvento IS NOT NULL
            AND ef.estadoEvento.nemonico = 'ESTADO_SALIDA_INACTIVO'
        )
        OR EXISTS (
            SELECT 1 FROM TrasladoAdolescente ta
            WHERE ta.traslado = r.traslado
            AND ta.fichaIdentificacion.tokenIdentificador = :tokenIdentificador
            AND ta.estadoEvento IS NOT NULL
            AND ta.estadoEvento.nemonico = 'ESTADO_SALIDA_INACTIVO'
        )
    )
""")
    Page<RegistroSalida> obtenerSalidasConFugaYTrasladoInactivos(
            @Param("tokenIdentificador") String tokenIdentificador,
            Pageable pageable
    );

    @Query("""
    SELECT r FROM RegistroSalida r
    WHERE r.removido = false
    AND r.tokenFichaIdentificacion.tokenIdentificador = :tokenIdentificador
    AND (
        EXISTS (
            SELECT 1 FROM EventoFuga ef
            WHERE ef = r.eventoFuga
            AND ef.estadoEvento IS NOT NULL
            AND ef.estadoEvento.nemonico = 'ESTADO_SALIDA_INACTIVO'
        )
        OR EXISTS (
            SELECT 1 FROM TrasladoAdolescente ta
            WHERE ta.traslado = r.traslado
            AND ta.fichaIdentificacion.tokenIdentificador = :tokenIdentificador
            AND ta.estadoEvento IS NOT NULL
            AND ta.estadoEvento.nemonico = 'ESTADO_SALIDA_INACTIVO'
        )
    )
    AND (
        LOWER(r.nroDocumento) LIKE LOWER(CONCAT('%', :textoFiltro, '%')) OR
        LOWER(r.observaciones) LIKE LOWER(CONCAT('%', :textoFiltro, '%')) OR
        LOWER(r.motivoSalida.nombre) LIKE LOWER(CONCAT('%', :textoFiltro, '%')) OR
        CAST(FUNCTION('TO_CHAR', r.fechaHoraSalida, 'YYYY-MM-DD') AS string) LIKE CONCAT('%', :textoFiltro, '%')
    )
""")
    Page<RegistroSalida> obtenerSalidasConFugaYTrasladoInactivosPorFiltro(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("textoFiltro") String textoFiltro,
            Pageable pageable
    );


    @Query("SELECT r FROM RegistroSalida r " +
            "WHERE r.centroSalida.tokenIdentificador = :tokenIdentificador " +
            "AND r.removido = false")
    Page<RegistroSalida> findByCentroTokenIdentificador(
            @Param("tokenIdentificador") String tokenIdentificador,
            Pageable pageable
    );




    @Query("SELECT r FROM RegistroSalida r " +
            "JOIN r.tokenFichaIdentificacion f " +
            "LEFT JOIN r.motivoSalida m " +
            "WHERE r.removido = false " +
            "AND (" +
            "LOWER(CONCAT(f.nombres, ' ', f.apellidoPaterno, ' ', f.apellidoMaterno)) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.nroDocumento) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))" +
            ")")
    Page<RegistroSalida> buscarPorFiltro(
                                         @Param("filter") String filter,
                                         Pageable pageable);



}
