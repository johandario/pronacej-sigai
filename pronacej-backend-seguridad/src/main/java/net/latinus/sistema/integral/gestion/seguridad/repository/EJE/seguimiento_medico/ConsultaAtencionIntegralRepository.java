package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.ConsultaAtencionIntegral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultaAtencionIntegralRepository extends JpaRepository<ConsultaAtencionIntegral, Long> {

    ConsultaAtencionIntegral findByTokenIdentificadorAndRemovido(String tokenId, Boolean removido);

    Page<ConsultaAtencionIntegral> findByFichaMedica_TokenIdentificadorAndRemovidoOrderByFechaInicioDesc(
            String tokenIdFichaMedica, Boolean removido, Pageable pageable);

}
