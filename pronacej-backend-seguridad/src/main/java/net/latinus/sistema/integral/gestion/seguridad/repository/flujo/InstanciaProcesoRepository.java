package net.latinus.sistema.integral.gestion.seguridad.repository.flujo;

import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.InstanciaProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstanciaProcesoRepository extends JpaRepository<InstanciaProceso, Long> {
    List<InstanciaProceso> findByRemovido(Boolean removido);

    InstanciaProceso findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
