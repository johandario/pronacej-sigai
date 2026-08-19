package net.latinus.sistema.integral.gestion.seguridad.repository.documento;

import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.EvaluacionDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluacionDocumentoRepository extends JpaRepository<EvaluacionDocumento, Long> {
    Page<EvaluacionDocumento> findByEncabezadoFichaIdentificacionTokenIdentificadorAndCatalogoCarpetaNemonicoAndRemovido(String tokenIdentificador, String nemonico, Boolean removido, Pageable pageable);

    Page<EvaluacionDocumento> findByEncabezadoTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);
}
