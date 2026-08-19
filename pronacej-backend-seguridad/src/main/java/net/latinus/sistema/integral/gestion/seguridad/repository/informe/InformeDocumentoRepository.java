package net.latinus.sistema.integral.gestion.seguridad.repository.informe;

import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.EvaluacionDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.informe.InformeDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformeDocumentoRepository extends JpaRepository<InformeDocumento, Long> {
    Page<InformeDocumento> findByInformeTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);
}
