package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.InformeSeguimientoPII;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InformeSeguimientoPIIRepository extends JpaRepository<InformeSeguimientoPII, Long> {
    
    /**
     * Busca un informe de seguimiento por su token identificador que no haya sido removido
     *
     * @param tokenIdentificador String token identificador
     * @param removido boolean indicador de si fue removido
     * @return InformeSeguimientoPII
     */
    InformeSeguimientoPII findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    /**
     * Obtiene los informes de seguimiento por token identificador de ficha 
     * de identificación, empresa y que no hayan sido removidos
     *
     * @param tokenIdentificador String token identificador de la ficha de identificación
     * @param idEmpresa Long id de la empresa
     * @param removido boolean indicador de si fue removido
     * @param pageable Pageable objeto para la paginación
     * @return Page<InformeSeguimientoPII>
     */
    Page<InformeSeguimientoPII> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
        String tokenIdentificador, Long idEmpresa, Boolean removido, Pageable pageable);
    
    /**
     * Búsqueda con filtrado de texto para informes de seguimiento PII
     * El filtro se aplica a varios campos de texto relevantes
     * 
     * @param tokenIdentificador String token identificador de la ficha de identificación
     * @param idEmpresa Long id de la empresa
     * @param filtro String texto a buscar
     * @param pageable Pageable objeto para la paginación
     * @return Page<InformeSeguimientoPII>
     */
    @Query("SELECT is FROM InformeSeguimientoPII is " +
           "WHERE is.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND is.empresa.idEmpresa = :idEmpresa " +
           "AND is.removido = false " +
           "AND (" +
           "  :filtro = '' OR " +
           "  LOWER(COALESCE(is.motivoIngreso, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(is.antecedentesOrganicidad, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(is.tecnicasUtilizadas, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(is.observacionConductual, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(is.evaluacionPlanPsicologica, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(is.evaluacionPlanSocial, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(is.evaluacionPlanConductual, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(is.evaluacionPlanFamiliar, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(is.evaluacionPlanEducativa, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(is.evaluacionPlanLaboral, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(is.conclusiones, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(is.recomendaciones, '')) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
           ")")
    Page<InformeSeguimientoPII> buscarPorFiltro(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("filtro") String filtro,
        Pageable pageable
    );
}