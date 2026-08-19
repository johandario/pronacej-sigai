package net.latinus.sistema.integral.gestion.seguridad.repository.informe;

import net.latinus.sistema.integral.gestion.seguridad.entities.informe.ValorInforme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValorRepository extends JpaRepository<ValorInforme, Long> {
    List<ValorInforme> findByRemovido(boolean removido);
    ValorInforme findByIdValorAndRemovido(long IdValor, boolean removido);
    ValorInforme findByInforme_IdInformeAndCampoInforme_IdCampoAndRemovido(long IdInforme, long IdCampo, boolean removido);
}
