package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.InformeFinalAsistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InformeFinalAsistenciaRepository extends JpaRepository<InformeFinalAsistencia, Long> {
    List<InformeFinalAsistencia> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido);

    InformeFinalAsistencia findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<InformeFinalAsistencia> findByFichaIdentificacionTokenIdentificador(String tokenIdentificadorFichaIdentificacion);

    List<InformeFinalAsistencia> findByPlanAsistenciaPostEgresoTokenIdentificadorAndRemovido(String tokenIdentificadorPlanAsistenciaPostEgreso, Boolean removido);
}

