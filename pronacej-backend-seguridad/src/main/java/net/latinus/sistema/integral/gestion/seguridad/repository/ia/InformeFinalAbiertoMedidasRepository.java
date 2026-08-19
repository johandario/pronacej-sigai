package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.InformeFinalAbiertoMedidas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InformeFinalAbiertoMedidasRepository extends JpaRepository<InformeFinalAbiertoMedidas, Long> {
    InformeFinalAbiertoMedidas findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    List<InformeFinalAbiertoMedidas> findByInformeFinalAbiertoTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
