package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PertenenciaDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PertenenciaDocumentoRepository extends JpaRepository<PertenenciaDocumento, Long> {
    Page<PertenenciaDocumento> findByPertenenciaTokenIdentificadorAndRemovido(String tokenIdentificadorPertenencia, Boolean removido, Pageable pageable);

    PertenenciaDocumento findFirstByPertenenciaTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorPertenencia, String tokenIdentificadorDocumento, Boolean removido);
}
