package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ia.SeguimientoConductual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeguimientoConductualRepository extends JpaRepository<SeguimientoConductual, Long> {
    List<SeguimientoConductual> findByEvaluacionFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificador, boolean removido);

    List<SeguimientoConductual> findByEvaluacionTokenIdentificadorAndRemovido(String tokenIdentificador, boolean removido);

    SeguimientoConductual findByIdSeguimientoConductualAndRemovido(Long idSeguimientoConductual, boolean removido);
}
