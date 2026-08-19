package net.latinus.sistema.integral.gestion.seguridad.repository.salida;

import net.latinus.sistema.integral.gestion.seguridad.entities.salida.SesionReforzamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SesionReforzamientoRepository extends JpaRepository<SesionReforzamiento, Long> {
    List<SesionReforzamiento> findByReforzamientoTokenIdentificadorAndRemovido(String tokenIdentificador, boolean removido);
}
