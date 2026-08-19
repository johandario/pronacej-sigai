package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.permiso;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso.HistoricoFichaIdentificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricoFichaIdentificacionRepository extends JpaRepository<HistoricoFichaIdentificacion, Long> {
    Optional<HistoricoFichaIdentificacion> findByFichaIdentificacionIdFichaIdentificacionAndCentroIdJerarquiaAndActivoAndRemovido(Long idFichaIdentificacion, Long IdCentro, Boolean activo, Boolean removido);

    Optional<HistoricoFichaIdentificacion> findByFichaIdentificacionTokenIdentificadorAndCentroTokenIdentificadorAndActivoAndRemovido(String tokenFichaIdentificacion, String tokenCentro, Boolean activo, Boolean removido);

    List<HistoricoFichaIdentificacion> findByFichaIdentificacionIdFichaIdentificacionAndActivoAndRemovido(Long idFichaIdentificacion, Boolean activo, Boolean removido);
}
