package net.latinus.sistema.integral.gestion.seguridad.repository;

import jakarta.persistence.QueryHint;
import net.latinus.sistema.integral.gestion.seguridad.entities.HistoricoEntradaSalida;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HistoricoEntradaSalidaRepository extends JpaRepository<HistoricoEntradaSalida, Long> {

    @Query("SELECT h FROM HistoricoEntradaSalida h WHERE h.numeroIdentificacion = :numeroIdentificacion AND h.registroActivo = true")
    Optional<HistoricoEntradaSalida> findByNumeroIdentificacionAndRegistroActivo(@Param("numeroIdentificacion") String numeroIdentificacion);

    @Query("SELECT h FROM HistoricoEntradaSalida h WHERE h.fichaIdentificacion.tokenIdentificador = :tokenFicha AND h.removido = false ORDER BY h.fechaEntrada DESC")
    List<HistoricoEntradaSalida> findAllByFichaIdentificacion(@Param("tokenFicha") String tokenFicha);

    @Query("SELECT h FROM HistoricoEntradaSalida h WHERE h.numeroIdentificacion = :numeroIdentificacion ORDER BY h.fechaEntrada DESC")
    List<HistoricoEntradaSalida> findHistoricoByNumeroIdentificacion(@Param("numeroIdentificacion") String numeroIdentificacion);

    List<HistoricoEntradaSalida> findByNumeroIdentificacionAndRegistroActivo(String numeroIdentificacion, boolean registroActivo);

    @Query("SELECT h FROM HistoricoEntradaSalida h WHERE h.numeroIdentificacion = :numeroIdentificacion " +
            " AND h.tipoRegistro.nemonico = 'TIPO_HISTORICO_SALIDA' AND h.registroActivo = true " +
            " ORDER BY h.fechaEntrada DESC")
    List<HistoricoEntradaSalida> findHistoricoSalidasByNumeroIdentificacion(@Param("numeroIdentificacion") String numeroIdentificacion);

    @Query("SELECT h FROM HistoricoEntradaSalida h " +
            "WHERE h.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND h.registroActivo = true " +
            "ORDER BY h.fechaEntrada DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1"))
    Optional<HistoricoEntradaSalida> findLastByFichaIdentificacionAndRegistroActivo(@Param("tokenIdentificador") String tokenIdentificador);

    @Query("SELECT h FROM HistoricoEntradaSalida h " +
            "WHERE h.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND h.motivoSalida.nemonico = :motivoSalida " +
            "AND h.registroActivo = true " +
            "ORDER BY h.fechaEntrada DESC")
    Optional<HistoricoEntradaSalida> findLastByFichaIdentificacionAndMotivoSalidaAndRegistroActivo
            (@Param("tokenIdentificador") String tokenIdentificador, @Param("motivoSalida") String motivoSalida);

    @Query("SELECT h FROM HistoricoEntradaSalida h " +
            "WHERE h.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND h.registroActivo = true " +
            "ORDER BY h.fechaEntrada DESC")
    List<HistoricoEntradaSalida> findByFichaIdentificacionAndRegistroActivo(@Param("tokenIdentificador") String tokenIdentificador);

    @Query("SELECT h FROM HistoricoEntradaSalida h " +
            "WHERE h.trasladoAdolescente.tokenIdentificador = :tokenIdentificador")
    Optional<HistoricoEntradaSalida> findByTrasladoAdolescenteTokenIdentificador(@Param("tokenIdentificador") String tokenIdentificador);

    @Query("SELECT h FROM HistoricoEntradaSalida h " +
            "WHERE h.externamiento.tokenIdentificador = :tokenIdentificador")
    Optional<HistoricoEntradaSalida> findByExternamientoTokenIdentificador(@Param("tokenIdentificador") String tokenIdentificador);

    @Query("SELECT h FROM HistoricoEntradaSalida h " +
            "WHERE h.permisoSalida.tokenIdentificador = :tokenIdentificador")
    Optional<HistoricoEntradaSalida> findByPermisoSalidaTokenIdentificador(@Param("tokenIdentificador") String tokenIdentificador);

    @Query("SELECT h FROM HistoricoEntradaSalida h " +
            "WHERE h.eventoFuga.tokenIdentificador = :tokenIdentificador")
    Optional<HistoricoEntradaSalida> findByEventoFugaTokenIdentificador(@Param("tokenIdentificador") String tokenIdentificador);

    @Query("SELECT h FROM HistoricoEntradaSalida h " +
            "WHERE h.informeFinalAbierto.tokenIdentificador = :tokenIdentificador")
    Optional<HistoricoEntradaSalida> findByInformeFinalTokenIdentificador(@Param("tokenIdentificador") String tokenIdentificador);

    @Query("SELECT h FROM HistoricoEntradaSalida h " +
            "WHERE h.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND h.motivoSalida.nemonico = :motivoSalida " +
            "AND h.registroActivo = true ")
    List<HistoricoEntradaSalida> findByFichaIdentificacionAndMotivoSalidaAndRegistroActivo(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("motivoSalida") String motivoSalida);

    @Query("SELECT h FROM HistoricoEntradaSalida h " +
            "WHERE h.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND h.motivoSalida.nemonico IN :motivosSalida " +
            "AND h.registroActivo = true " +
            "ORDER BY h.fechaEntrada DESC")
    List<HistoricoEntradaSalida> findByFichaIdentificacionAndMotivoSalidaAndRegistroActivo(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("motivosSalida") List<String> motivosSalida
    );

    @Query(value = "SELECT h FROM historico_entrada_salida h WHERE h.inf_permiso_salida.token_identificador = :tokenIdentificador LIMIT 1", nativeQuery = true)
    Optional<HistoricoEntradaSalida> findFirstByPermisoSalidaTokenIdentificador(@Param("tokenIdentificador") String tokenIdentificador);


    @Query("SELECT h FROM HistoricoEntradaSalida h WHERE h.permisoSalida.tokenIdentificador = :tokenIdentificador")
    List<HistoricoEntradaSalida> findByPermisoSalidaTemporalTokenIdentificador(@Param("tokenIdentificador") String tokenIdentificador);

}
