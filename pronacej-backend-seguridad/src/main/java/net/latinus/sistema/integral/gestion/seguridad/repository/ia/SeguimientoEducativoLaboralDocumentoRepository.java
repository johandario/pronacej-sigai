package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoEducativoLaboralDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguimientoEducativoLaboralDocumentoRepository extends JpaRepository<SeguimientoEducativoLaboralDocumento, Long> {
    Page<SeguimientoEducativoLaboralDocumento> findBySeguimientoEducativoLaboralOtrosTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);
    SeguimientoEducativoLaboralDocumento findFirstBySeguimientoEducativoLaboralOtrosTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorSeguimiento, String tokenIdentificadorDocumento, Boolean removido);
}
