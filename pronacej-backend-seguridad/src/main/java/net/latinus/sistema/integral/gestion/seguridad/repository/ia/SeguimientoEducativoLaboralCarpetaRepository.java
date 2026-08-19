package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoEducativoLaboralCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguimientoEducativoLaboralCarpetaRepository extends JpaRepository<SeguimientoEducativoLaboralCarpeta, Long> {
    Page<SeguimientoEducativoLaboralCarpeta> findBySeguimientoEducativoLaboralOtrosTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);
    SeguimientoEducativoLaboralCarpeta findFirstBySeguimientoEducativoLaboralOtrosTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
