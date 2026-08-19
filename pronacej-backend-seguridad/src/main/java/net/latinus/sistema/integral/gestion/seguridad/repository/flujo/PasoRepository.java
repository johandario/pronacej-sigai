package net.latinus.sistema.integral.gestion.seguridad.repository.flujo;

import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.Paso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasoRepository extends JpaRepository<Paso, Long> {
    Paso findByProcesoIdProcesoAndRemovidoAndOrden(Long id, Boolean removido, Integer orden);

    Paso findByTokenIdentificadorAndRemovido(String tokenPaso, Boolean removido);

    List<Paso> findByProcesoTokenIdentificadorAndRemovido(String tokenProceso, Boolean removido);

    List<Paso> findByProcesoIdProcesoAndRemovido(Long idProceso, Boolean removido);
}
