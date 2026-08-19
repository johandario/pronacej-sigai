package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.InformeFinalAbierto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InformeFinalAbiertoRepository extends JpaRepository<InformeFinalAbierto, Long> {
    InformeFinalAbierto findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<InformeFinalAbierto> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido);

}
