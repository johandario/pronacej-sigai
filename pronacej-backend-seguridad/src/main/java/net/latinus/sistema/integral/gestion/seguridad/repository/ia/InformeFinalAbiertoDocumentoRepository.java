package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.InformeFinalAbiertoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InformeFinalAbiertoDocumentoRepository extends JpaRepository<InformeFinalAbiertoDocumento, Long> {
    Page<InformeFinalAbiertoDocumento> findByInformeFinalAbiertoTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);

    InformeFinalAbiertoDocumento findFirstByInformeFinalAbiertoTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorInforme, String tokenIdentificadorDocumento, Boolean removido);
}
