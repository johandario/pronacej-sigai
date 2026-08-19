package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.AuditoriaAccionesSistema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Date;

@Repository
public interface AuditoriaAccionesSistemaRepository extends JpaRepository<AuditoriaAccionesSistema, Long> {

    /**
     * Devuelve un objeto AuditoriaAccionesSistema por el token identificador y removido
     *
     * @param tokenIdentificador String token identificador.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return AuditoriaAccionesSistema
     */
    AuditoriaAccionesSistema findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    /**
     * Devuelve un objeto paginacion de AuditoriaAccionesSistema por los filtros aplicados
     *
     * @param fechaInicio Date fecha de inicio.
     * @param fechaFin Date fecha fin.
     * @param userName String username del usuario.
     * @param tokenRol String identificador del rol.
     * @param tokenAccion String identificador de la acción.
     * @param tokenMenu String identificador del menu.
     * @param pageable Pageable objecto de paginacion request.
     *
     * @return Page<AuditoriaAccionesSistema
     */

    @Query(value = "SELECT audAS.* FROM seg_auditoria_acciones_sistema as audAS"
            + " LEFT JOIN seg_usuario_sistema userS ON audAS.id_usuario_que_realiza_la_accion = userS.id_usuario_sistema"
            + " LEFT JOIN par_catalogo accion ON audAS.id_accion = accion.id_catalogo"
            + " LEFT JOIN seg_rol rol ON audAS.id_rol = rol.id_rol"
            + " LEFT JOIN seg_menu menu ON audAS.id_menu = menu.id_menu"


            + " WHERE ((cast(:fechaInicio as date) is null) or (audAS.fecha_inicio_accion >= :fechaInicio))"
            + " and ((cast(:fechaFin as date) is null) or (audAS.fecha_fin_accion < :fechaFin))"
            + " and ((:userName is null) or (userS.user_name = :userName))"
            + " and ((:tokenRol is null) or (rol.token_identificador = :tokenRol))"
            + " and ((:tokenAccion is null) or (accion.token_identificador = :tokenAccion))"
            + " and ((:tokenMenu is null) or (menu.token_identificador = :tokenMenu))"
            + " and audAS.removido = false",
            nativeQuery = true
    )
    Page<AuditoriaAccionesSistema> encontrarPorFiltrosApp2(
            @Param("fechaInicio") Date fechaInicio,
            @Param("fechaFin") Date fechaFin,
            @Param("userName") String userName,
            @Param("tokenRol") String tokenRol,
            @Param("tokenAccion") String tokenAccion,
            @Param("tokenMenu") String tokenMenu,
            Pageable pageable
    );


    /**
     * Devuelve una lista de AuditoriaAccionesSistema por los filtros aplicados
     *
     * @param fechaInicio Date fecha de inicio.
     * @param fechaFin Date fecha fin.
     * @param userName String username del usuario.
     * @param tokenRol String identificador del rol.
     * @param tokenAccion String identificador de la acción.
     * @param tokenMenu String identificador del menu.
     *
     * @return Page<AuditoriaAccionesSistema
     */

    @Query(value = "SELECT audAS.* FROM seg_auditoria_acciones_sistema as audAS"
            + " LEFT JOIN seg_usuario_sistema userS ON audAS.id_usuario_que_realiza_la_accion = userS.id_usuario_sistema"
            + " LEFT JOIN par_catalogo accion ON audAS.id_accion = accion.id_catalogo"
            + " LEFT JOIN seg_rol rol ON audAS.id_rol = rol.id_rol"
            + " LEFT JOIN seg_menu menu ON audAS.id_menu = menu.id_menu"


            + " WHERE ((cast(:fechaInicio as date) is null) or (audAS.fecha_inicio_accion >= :fechaInicio))"
            + " and ((cast(:fechaFin as date) is null) or (audAS.fecha_fin_accion < :fechaFin))"
            + " and ((:userName is null) or (userS.user_name = :userName))"
            + " and ((:tokenRol is null) or (rol.token_identificador = :tokenRol))"
            + " and ((:tokenAccion is null) or (accion.token_identificador = :tokenAccion))"
            + " and ((:tokenMenu is null) or (menu.token_identificador = :tokenMenu))"
            + " and audAS.removido = false ORDER BY id_auditoria_acciones_sistema desc",
            nativeQuery = true
    )
    List<AuditoriaAccionesSistema> encontrarPorFiltrosAppTodos(
            @Param("fechaInicio") Date fechaInicio,
            @Param("fechaFin") Date fechaFin,
            @Param("userName") String userName,
            @Param("tokenRol") String tokenRol,
            @Param("tokenAccion") String tokenAccion,
            @Param("tokenMenu") String tokenMenu
    );

}
