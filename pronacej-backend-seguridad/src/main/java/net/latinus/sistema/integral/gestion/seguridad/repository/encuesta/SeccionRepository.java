package net.latinus.sistema.integral.gestion.seguridad.repository.encuesta;

import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.Encuesta;
import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeccionRepository extends JpaRepository<Seccion, Long> {
    List<Seccion> findByRemovido(boolean removido);
    List<Seccion> findByEncuestaAndRemovido(Encuesta encuesta, boolean b);
}
