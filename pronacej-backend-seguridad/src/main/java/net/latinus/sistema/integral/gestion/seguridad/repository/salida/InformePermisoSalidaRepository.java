package net.latinus.sistema.integral.gestion.seguridad.repository.salida;

import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;
import net.latinus.sistema.integral.gestion.seguridad.entities.fuga.EventoFuga;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.InformePermisoSalidaAdolescente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InformePermisoSalidaRepository extends JpaRepository<InformePermisoSalidaAdolescente, Long>{
    Page<InformePermisoSalidaAdolescente> findByRemovido(Boolean removido, Pageable pageable);
    InformePermisoSalidaAdolescente findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    List<InformePermisoSalidaAdolescente> findAllByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    @Query("SELECT r FROM InformePermisoSalidaAdolescente r WHERE r.tokenFichaIdentificacion.tokenIdentificador = :tokenIdentificador AND r.removido = :removido")
    List<InformePermisoSalidaAdolescente> findAllByFichaIdentificacionTokenIdentificadorAndRemovido(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("removido") Boolean removido
    );
    List<InformePermisoSalidaAdolescente> findByTokenFichaIdentificacionIdFichaIdentificacionAndRemovido(Long idFichaIdentificacion, Boolean removido);
    @Query("SELECT r FROM InformePermisoSalidaAdolescente r " +
            "LEFT JOIN r.frecuenciaSalida f " +
            "LEFT JOIN r.tipoSalida t " +
            "WHERE r.removido = false " +
            "AND r.tokenFichaIdentificacion.tokenIdentificador = :tokenIdentificador " + // Filtrar por token
            "AND (" +
            "LOWER(r.usuarioSalida) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(r.nroDocumento) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(f.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            ")")
    Page<InformePermisoSalidaAdolescente> buscarPorTokenYFiltro(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("filter") String filter,
            Pageable pageable);


}
