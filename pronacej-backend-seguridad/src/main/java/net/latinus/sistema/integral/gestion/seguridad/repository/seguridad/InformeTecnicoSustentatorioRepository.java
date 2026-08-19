package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.InformeTecnicoSustentatorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InformeTecnicoSustentatorioRepository extends JpaRepository<InformeTecnicoSustentatorio, Long> {
    
    /**
     * Busca un informe técnico por su token identificador que no haya sido removido
     *
     * @param tokenIdentificador String token identificador
     * @param removido boolean indicador de si fue removido
     * @return InformeTecnicoSustentatorio
     */
    InformeTecnicoSustentatorio findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    /**
     * Obtiene los informes técnicos por token identificador de ficha 
     * de identificación, empresa y que no hayan sido removidos
     *
     * @param tokenIdentificador String token identificador de la ficha de identificación
     * @param idEmpresa Long id de la empresa
     * @param removido boolean indicador de si fue removido
     * @param pageable Pageable objeto para la paginación
     * @return Page<InformeTecnicoSustentatorio>
     */
    Page<InformeTecnicoSustentatorio> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
            String tokenIdentificador, Long idEmpresa, Boolean removido, Pageable pageable);
    
    /**
     * Búsqueda con filtrado de texto para informes técnicos sustentatorios
     * El filtro se aplica a varios campos de texto relevantes
     * 
     * @param tokenIdentificador String token identificador de la ficha de identificación
     * @param idEmpresa Long id de la empresa
     * @param filtro String texto a buscar
     * @param pageable Pageable objeto para la paginación
     * @return Page<InformeTecnicoSustentatorio>
     */
    @Query("SELECT i FROM InformeTecnicoSustentatorio i " +
           "WHERE i.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND i.empresa.idEmpresa = :idEmpresa " +
           "AND i.removido = false " +
           "AND (" +
           "  :filtro = '' OR " +
           "  LOWER(COALESCE(i.motivo, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(i.criteriosSeleccion, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(i.analisisPsicologico, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(i.analisisSocial, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(i.analisisConductual, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(i.analisisFamiliar, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(i.propuestaActividadFormativa, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(i.importanciaParticipacionAdolescente, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(i.objetivosConseguir, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(i.conclusiones, '')) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "  LOWER(COALESCE(i.recomendaciones, '')) LIKE LOWER(CONCAT('%', :filtro, '%'))" +
           ")")
    Page<InformeTecnicoSustentatorio> buscarPorFiltro(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("filtro") String filtro,
        Pageable pageable
    );

    /**
     * Búsqueda general con filtrado que incluye campos del usuario y información adicional
     * Similar al patrón de EvaluacionSocialRepository
     * 
     * @param tokenIdentificador String token identificador de la ficha de identificación
     * @param idEmpresa Long id de la empresa
     * @param filtro String texto a buscar (debe venir con % ya incluido)
     * @param pageable Pageable objeto para la paginación
     * @return Page<InformeTecnicoSustentatorio>
     */
    @Query("SELECT i FROM InformeTecnicoSustentatorio i " +
           "WHERE i.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
           "AND i.empresa.idEmpresa = :idEmpresa " +
           "AND i.removido = false " +
           "AND (" +
           "  LOWER(COALESCE(i.motivo, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.criteriosSeleccion, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.analisisPsicologico, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.analisisSocial, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.analisisConductual, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.analisisFamiliar, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.propuestaActividadFormativa, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.importanciaParticipacionAdolescente, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.objetivosConseguir, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.conclusiones, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.recomendaciones, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.usuarioSistemaCrea.nombres, '')) LIKE :filtro OR " +
           "  LOWER(COALESCE(i.usuarioSistemaCrea.apellidos, '')) LIKE :filtro" +
           ")")
    Page<InformeTecnicoSustentatorio> buscarPorFiltroGeneral(
        @Param("tokenIdentificador") String tokenIdentificador,
        @Param("idEmpresa") Long idEmpresa,
        @Param("filtro") String filtro,
        Pageable pageable
    );
}