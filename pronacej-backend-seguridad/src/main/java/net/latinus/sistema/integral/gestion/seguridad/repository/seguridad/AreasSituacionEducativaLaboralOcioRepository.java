package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.AreasSituacionEducativaLaboralOcio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreasSituacionEducativaLaboralOcioRepository extends JpaRepository<AreasSituacionEducativaLaboralOcio, Long> {

    AreasSituacionEducativaLaboralOcio findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
