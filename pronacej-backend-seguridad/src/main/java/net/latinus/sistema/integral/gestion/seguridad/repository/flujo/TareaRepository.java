package net.latinus.sistema.integral.gestion.seguridad.repository.flujo;

import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.Tarea;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.TareaUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
    //List<Tarea> findByInstanciaProcesoIdInstanciaProcesoAndRemovido(Long idInstanciaProceso, Boolean removido);

    @Query("SELECT ft FROM Tarea ft " +
            "JOIN ft.paso fp " +
            "WHERE ft.estado = 'En curso' and " +
            "ft.instanciaProceso.idInstanciaProceso = :idInstanciaProceso " +
            "ORDER BY fp.orden ASC LIMIT 1")
    Tarea obtenerTareaEnCursoPorIdInstanciaProceso(@Param("idInstanciaProceso") Long idInstanciaProceso);

    @Query("SELECT ft FROM Tarea ft " +
            "JOIN ft.paso fp " +
            "WHERE ft.estado = 'Pendiente' and " +
            "ft.instanciaProceso.idInstanciaProceso = :idInstanciaProceso " +
            "ORDER BY fp.orden ASC LIMIT 1")
    Tarea obtenerTareaPendientePorIdInstanciaProceso(@Param("idInstanciaProceso") Long idInstanciaProceso);

    @Query("SELECT ft FROM Tarea ft " +
            "WHERE (ft.rolUsuarioEnvia = :usuario " +
            "OR ft.rolUsuarioEnvia = :rol) " +
            "AND ft.estado <> 'Pendiente'" +
            "AND ft.removido = false " +
            "ORDER BY ft.fechaEdicion DESC")
    Page<Tarea> obtenerTareasEnviadasPorUsuarioRol(@Param("usuario") String usuario, @Param("rol") String rol, Pageable pageable);

    @Query("SELECT ft FROM Tarea ft " +
            "WHERE ft.usuarioSistemaEdita.tokenIdentificador = :tokenUsuario " +
            "AND ft.paso.proceso.nombre LIKE CONCAT('%',:tipo,'%') " +
            "AND ft.estado in ('Completada', 'Rechazada') " +
            "AND ft.removido = false " +
            "ORDER BY ft.fechaEdicion DESC")
    Page<Tarea> obtenerTareasEnviadasPorTokenUsuario(@Param("tokenUsuario") String tokenUsuario, @Param("tipo") String tipo, Pageable pageable);

    @Query("SELECT ft FROM Tarea ft " +
            "WHERE (ft.rolUsuarioRecibe = :usuario " +
            "OR ft.rolUsuarioRecibe = :rol) " +
            "AND ft.estado <> 'Pendiente'" +
            "AND ft.removido = false " +
            "ORDER BY ft.fechaEdicion DESC")
    Page<Tarea> obtenerTareasRecibidasPorUsuarioRol(@Param("usuario") String usuario, @Param("rol") String rol, Pageable pageable);

    /*@Query("SELECT ft FROM Tarea ft " +
            "WHERE ft.estado = 'En curso' " +
            "OR ft.estado = 'Completada' " +
            "AND ft.removido = false" +
            "ORDER BY fp.fechaCreacion")
    Page<Tarea> obtenerTodasTareasEnCursoCompletadas(Pageable pageable);*/

    @Query(
            value = """
                    select distinct ft.* from flu_tarea ft
                    join seg_usuario_sistema sus on sus.id_usuario_sistema = ft.id_usuario_edita
                    left join flu_paso fp ON fp.id_paso = ft.id_paso
                    left join flu_proceso fpr ON fpr.id_proceso = fp.id_proceso
                    left join tras_traslado tt on tt.id_instancia_proceso = ft.id_instancia_proceso
                    left join gest_evento_fuga gef on gef.id_instancia_proceso = ft.id_instancia_proceso
                    left join seg_jerarquia sj1 on sj1.id_jerarquia = tt.id_centro_origen
                    left join seg_jerarquia sj2 on sj2.id_jerarquia = gef.id_centro
                    where ft.estado in :estados and ft.removido = :removido
                    and (sj1.token_identificador = :tokenCentro or sj2.token_identificador = :tokenCentro)
                    and sus.token_identificador = :tokenUsuario
                    and (:tipo is null or fpr.nombre = :tipo)
                    """,
            nativeQuery = true,
            countQuery = """
                    select count(distinct ft.id_tarea) from flu_tarea ft
                    join seg_usuario_sistema sus on sus.id_usuario_sistema = ft.id_usuario_edita
                    left join flu_paso fp ON fp.id_paso = ft.id_paso
                    left join flu_proceso fpr ON fpr.id_proceso = fp.id_proceso
                    left join tras_traslado tt on tt.id_instancia_proceso = ft.id_instancia_proceso
                    left join gest_evento_fuga gef on gef.id_instancia_proceso = ft.id_instancia_proceso
                    left join seg_jerarquia sj1 on sj1.id_jerarquia = tt.id_centro_origen
                    left join seg_jerarquia sj2 on sj2.id_jerarquia = gef.id_centro
                    where ft.estado in :estados and ft.removido = :removido
                    and (sj1.token_identificador = :tokenCentro or sj2.token_identificador = :tokenCentro)
                    and sus.token_identificador = :tokenUsuario
                    and (:tipo is null or fpr.nombre = :tipo)
                    """
    )
    Page<Tarea> obtenerTareasPorTokenUsuarioEditaYEstadosYTokenCentroYTipoProcesoYRemovido(
            @Param("tokenUsuario") String tokenUsuario,
            @Param("estados") List<String> estados,
            @Param("tokenCentro") String tokenCentro,
            @Param("tipo") String tipo,
            @Param("removido") Boolean removido,
            Pageable pageable
    );

    @Query(
            value = """
                    select distinct ft.* from flu_tarea ft
                    join seg_usuario_sistema sus on sus.id_usuario_sistema = ft.id_usuario_edita
                    left join tras_traslado tt on tt.id_instancia_proceso = ft.id_instancia_proceso
                    left join gest_evento_fuga gef on gef.id_instancia_proceso = ft.id_instancia_proceso
                    left join seg_jerarquia sj1 on sj1.id_jerarquia = tt.id_centro_origen
                    left join seg_jerarquia sj2 on sj2.id_jerarquia = gef.id_centro
                    where ft.estado in :estados and ft.removido = :removido
                    and (sj1.token_identificador = :tokenCentro or sj2.token_identificador = :tokenCentro)
                    and sus.token_identificador = :tokenUsuario
                    """,
            nativeQuery = true,
            countQuery = """
                    select count(distinct ft.id_tarea) from flu_tarea ft
                    join seg_usuario_sistema sus on sus.id_usuario_sistema = ft.id_usuario_edita
                    left join tras_traslado tt on tt.id_instancia_proceso = ft.id_instancia_proceso
                    left join gest_evento_fuga gef on gef.id_instancia_proceso = ft.id_instancia_proceso
                    left join seg_jerarquia sj1 on sj1.id_jerarquia = tt.id_centro_origen
                    left join seg_jerarquia sj2 on sj2.id_jerarquia = gef.id_centro
                    where ft.estado in :estados and ft.removido = :removido
                    and (sj1.token_identificador = :tokenCentro or sj2.token_identificador = :tokenCentro)
                    and sus.token_identificador = :tokenUsuario
                    """
    )
    Page<Tarea> obtenerTareasPorTokenUsuarioEditaYEstadosYTokenCentroYRemovido(
            @Param("tokenUsuario") String tokenUsuario,
            @Param("estados") List<String> estados,
            @Param("tokenCentro") String tokenCentro,
            @Param("removido") Boolean removido,
            Pageable pageable
    );

    Tarea findByPasoIdPasoAndInstanciaProcesoIdInstanciaProcesoAndRemovido(Long idPaso, Long idInstanciaProceso, Boolean removido);

    Tarea findByEstadoAndRemovido(String estado, Boolean removido);

    Page<Tarea> findByRemovidoAndEstado(Boolean removido, String estado, Pageable pageable);

    List<Tarea> findByInstanciaProcesoTokenIdentificadorOrderByOrdenAsc(String tokenInstanciaProceso);

    Tarea findByTokenIdentificador(String tokenTarea);

    Tarea findByTokenIdentificadorAndRemovido(String tokenTarea, Boolean removido);

    Tarea findByInstanciaProcesoTokenIdentificadorAndOrdenAndEstadoAndRemovido(String tokenInstancia, Integer orden, String estado, Boolean removido);

    List<Tarea> findByInstanciaProcesoTokenIdentificador(String tokenIdentificadorInstancia);

    Page<Tarea> findByUsuarioSistemaEditaTokenIdentificadorAndEstadoInAndRemovido(String tokenUsuario, List<String> estadoTarea, Boolean removido, Pageable pageable);
}
