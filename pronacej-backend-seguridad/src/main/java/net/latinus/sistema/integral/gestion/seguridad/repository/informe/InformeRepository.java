package net.latinus.sistema.integral.gestion.seguridad.repository.informe;

import net.latinus.sistema.integral.gestion.seguridad.entities.informe.Informe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InformeRepository extends JpaRepository<Informe, Long> {

    List<Informe> findByRemovido(boolean removido);
    Informe findByIdInformeAndRemovido(long idInforme, boolean removido);
    List<Informe> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificador, boolean removido);
}
