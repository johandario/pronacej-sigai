package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ActividadIntervencionSeguimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadIntervencionSeguimientoRepository extends JpaRepository<ActividadIntervencionSeguimiento, Long> {

    Page<ActividadIntervencionSeguimiento> findByActividadIdActividadIntervencionAndRemovido(
            Long idActividadIntervencion, Boolean removido, Pageable pageable);

}
