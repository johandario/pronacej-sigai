package net.latinus.sistema.integral.gestion.seguridad.repository.salida;

import net.latinus.sistema.integral.gestion.seguridad.entities.salida.Reforzamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReforzamientoRepository extends JpaRepository<Reforzamiento, Long> {
    List<Reforzamiento> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificador, boolean removido);
    Reforzamiento findByTokenIdentificadorAndRemovido(String tokenIdentificador, boolean removido);
}
