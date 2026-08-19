package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.DatosFamiliaresDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatosFamiliaresDocumentoRepository extends JpaRepository<DatosFamiliaresDocumento, Long> {
    Page<DatosFamiliaresDocumento> findByDatosFamiliaresTokenIdentificadorAndRemovido(String tokenIdentificadorDatosFamiliares, Boolean removido, Pageable pageable);

    DatosFamiliaresDocumento findFirstByDatosFamiliaresTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorDatosFamiliares, String tokenIdentificadorDocumento, Boolean removido);
}
