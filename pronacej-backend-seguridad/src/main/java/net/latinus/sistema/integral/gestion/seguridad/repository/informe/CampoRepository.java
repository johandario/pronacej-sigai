package net.latinus.sistema.integral.gestion.seguridad.repository.informe;

import net.latinus.sistema.integral.gestion.seguridad.entities.informe.CampoInforme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampoRepository extends JpaRepository<CampoInforme, Long> {
    CampoInforme findByIdCampoAndRemovido(long IdCampo, boolean removido);
    List<CampoInforme> findByRemovido(boolean removido);
    List<CampoInforme> findByPlantillaInforme_IdPlantillaInformeAndRemovido(long idPlantillaInforme, boolean removido);
}
