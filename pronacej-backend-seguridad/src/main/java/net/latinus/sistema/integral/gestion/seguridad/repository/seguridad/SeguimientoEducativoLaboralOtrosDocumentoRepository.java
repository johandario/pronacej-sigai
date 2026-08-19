package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoEducativoLaboralOtrosDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguimientoEducativoLaboralOtrosDocumentoRepository extends JpaRepository<SeguimientoEducativoLaboralOtrosDocumento, Long> {
    
    /**
     * Busca documentos asociados a un seguimiento educativo laboral otros específico con paginación
     * @param tokenIdentificador Token del seguimiento educativo laboral otros
     * @param removido Estado de eliminación
     * @param pageable Configuración de paginación
     * @return Página de documentos encontrados
     */
    Page<SeguimientoEducativoLaboralOtrosDocumento> findBySeguimientoEducativoLaboralOtrosTokenIdentificadorAndRemovido(
            String tokenIdentificador, 
            Boolean removido, 
            Pageable pageable
    );
    
    /**
     * Busca un documento específico asociado a un seguimiento educativo laboral otros
     * @param tokenIdentificadorSeguimiento Token del seguimiento educativo laboral otros
     * @param tokenIdentificadorDocumento Token del documento
     * @param removido Estado de eliminación
     * @return Documento encontrado o null si no existe
     */
    SeguimientoEducativoLaboralOtrosDocumento findFirstBySeguimientoEducativoLaboralOtrosTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
            String tokenIdentificadorSeguimiento, 
            String tokenIdentificadorDocumento, 
            Boolean removido
    );
}
