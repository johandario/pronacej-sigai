package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.SeguimientoEducativoLaboralOtrosCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguimientoEducativoLaboralOtrosCarpetaRepository extends JpaRepository<SeguimientoEducativoLaboralOtrosCarpeta, Long> {
    
    /**
     * Busca carpetas asociadas a un seguimiento educativo laboral otros específico con paginación
     * @param tokenIdentificador Token del seguimiento educativo laboral otros
     * @param removido Estado de eliminación
     * @param pageable Configuración de paginación
     * @return Página de carpetas encontradas
     */
    Page<SeguimientoEducativoLaboralOtrosCarpeta> findBySeguimientoEducativoLaboralOtrosTokenIdentificadorAndRemovido(
            String tokenIdentificador, 
            Boolean removido, 
            Pageable pageable
    );
    
    /**
     * Busca la primera carpeta asociada a un seguimiento educativo laboral otros específico
     * @param tokenIdentificador Token del seguimiento educativo laboral otros
     * @param removido Estado de eliminación
     * @return Primera carpeta encontrada o null si no existe
     */
    SeguimientoEducativoLaboralOtrosCarpeta findFirstBySeguimientoEducativoLaboralOtrosTokenIdentificadorAndRemovido(
            String tokenIdentificador, 
            Boolean removido
    );
}
