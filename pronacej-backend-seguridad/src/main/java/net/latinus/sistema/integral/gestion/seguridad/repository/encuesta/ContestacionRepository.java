package net.latinus.sistema.integral.gestion.seguridad.repository.encuesta;

import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.Contestacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestacionRepository extends JpaRepository<Contestacion, Long> {
    List<Contestacion> findByEncabezadoIdEncabezadoAndRemovido(Long idEncabezado, Boolean removido);
}
