package net.latinus.sistema.integral.gestion.seguridad.repository.flujo;

import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.TareaUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TareaUsuarioRepository extends JpaRepository<TareaUsuario, Long> {
    Page<TareaUsuario> findByUsuarioSistemaTokenIdentificadorAndTareaEstadoInAndTareaRemovidoAndRemovido(String tokenUsuario, List<String> estadoTarea, Boolean removidoTarea, Boolean removido, Pageable pageable);

    List<TareaUsuario> findByTareaTokenIdentificadorAndRemovido(String tokenTarea, Boolean removido);

    @Query(
            value = """
                    select distinct ftu.* from flu_tarea_usuario ftu
                    join flu_tarea ft on ft.id_tarea = ftu.id_tarea
                    join seg_usuario_sistema sus on sus.id_usuario_sistema = ftu.id_usuario
                    left join tras_traslado tt on tt.id_instancia_proceso = ft.id_instancia_proceso
                    left join gest_evento_fuga gef on gef.id_instancia_proceso = ft.id_instancia_proceso
                    left join seg_jerarquia sj1 on sj1.id_jerarquia = tt.id_centro_origen
                    left join seg_jerarquia sj2 on sj2.id_jerarquia = gef.id_centro
                    where ft.estado in :estados and ft.removido = :removido and ftu.removido = :removido
                    and (sj1.token_identificador = :tokenCentro or sj2.token_identificador = :tokenCentro)
                    and sus.token_identificador = :tokenUsuario
                    """,
            nativeQuery = true,
            countQuery = """
                    select count(distinct ftu.id_tarea_usuario) from flu_tarea_usuario ftu
                    join flu_tarea ft on ft.id_tarea = ftu.id_tarea
                    join seg_usuario_sistema sus on sus.id_usuario_sistema = ftu.id_usuario
                    left join tras_traslado tt on tt.id_instancia_proceso = ft.id_instancia_proceso
                    left join gest_evento_fuga gef on gef.id_instancia_proceso = ft.id_instancia_proceso
                    left join seg_jerarquia sj1 on sj1.id_jerarquia = tt.id_centro_origen
                    left join seg_jerarquia sj2 on sj2.id_jerarquia = gef.id_centro
                    where ft.estado in :estados and ft.removido = :removido and ftu.removido = :removido
                    and (sj1.token_identificador = :tokenCentro or sj2.token_identificador = :tokenCentro)
                    and sus.token_identificador = :tokenUsuario
                    """
    )
    Page<TareaUsuario> obtenerTareasPorEstadosYTokenCentroYTokenUsuarioYRemovido(
            @Param("estados") List<String> estados,
            @Param("tokenCentro") String tokenCentro,
            @Param("tokenUsuario") String tokenUsuario,
            @Param("removido") Boolean removido,
            Pageable pageable
    );
}
