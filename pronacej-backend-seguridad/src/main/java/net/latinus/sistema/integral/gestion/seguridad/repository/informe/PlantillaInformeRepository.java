package net.latinus.sistema.integral.gestion.seguridad.repository.informe;

import net.latinus.sistema.integral.gestion.seguridad.entities.informe.PlantillaInforme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantillaInformeRepository extends JpaRepository<PlantillaInforme, Long> {
    List<PlantillaInforme> findByRemovido(boolean removido);
    List<PlantillaInforme> findByTipoCentroNemonicoAndRemovido(String nemonico, boolean removido);
    PlantillaInforme findByIdPlantillaInformeAndRemovido(long idPlantillaInforme, boolean removido);
    PlantillaInforme findByTokenIdentificadorAndRemovido(String tokenIdentificador, boolean removido);
    PlantillaInforme findByCatalogo_IdCatalogoAndRemovido(long idCatalogo, boolean removido);
    PlantillaInforme findByCatalogoNemonicoAndRemovido(String nemonico, boolean removido);
}
