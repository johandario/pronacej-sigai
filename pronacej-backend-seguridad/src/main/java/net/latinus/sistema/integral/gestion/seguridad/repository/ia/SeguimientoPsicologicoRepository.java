package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ia.SeguimientoPsicologico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeguimientoPsicologicoRepository extends JpaRepository<SeguimientoPsicologico, Long> {
    List<SeguimientoPsicologico> findByEvaluacionFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificador, boolean removido);
    List<SeguimientoPsicologico> findByEvaluacionTokenIdentificadorAndRemovido(String tokenIdentificador, boolean removido);
    SeguimientoPsicologico findByIdSeguimientoPsicologicoAndRemovido (Long idSeguimientoPsicologico, boolean removido);
}
