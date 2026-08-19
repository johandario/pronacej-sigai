package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionDomiciliaria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluacionDomiciliariaRepository extends JpaRepository<EvaluacionDomiciliaria, Long> {

    /**
     * Busca una evaluación domiciliaria por token identificador y estado de eliminación
     */
    EvaluacionDomiciliaria findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    /**
     * Busca evaluaciones domiciliarias con filtro por centro del usuario (multi-jerárquico)
     * Aplica filtros de texto en múltiples campos y filtra por centro específico
     */
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.centro.idJerarquia = :centroId " +
           "AND e.removido = false " +
           "AND (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(CAST(e.fechaEntrevista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CAST(e.duracionVista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.motivoNoVisita) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.objetivoGeneral) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.desarrolloVisitaDomiciliaria) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.conclusiones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.recomendaciones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.otraPersonaRelacionada) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CONCAT(pr.primerNombre, ' ', pr.segundoNombre, ' ', pr.primerApellido, ' ', pr.segundoApellido)) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<EvaluacionDomiciliaria> buscarPorFiltroYCentro(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("centroId") Long centroId,
            @Param("filtro") String filtro,
            Pageable pageable);

    /**
     * Busca evaluaciones ordenadas por persona entrevistada ASC con filtro por centro
     */
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.centro.idJerarquia = :centroId " +
           "AND e.removido = false " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END ASC")
    Page<EvaluacionDomiciliaria> buscarOrdenadoPorPersonaEntrevistadaAsc(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("centroId") Long centroId,
            Pageable pageable);

    /**
     * Busca evaluaciones ordenadas por persona entrevistada DESC con filtro por centro
     */
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.centro.idJerarquia = :centroId " +
           "AND e.removido = false " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END DESC")
    Page<EvaluacionDomiciliaria> buscarOrdenadoPorPersonaEntrevistadaDesc(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("centroId") Long centroId,
            Pageable pageable);

    /**
     * Busca evaluaciones con filtro de texto y ordenadas por persona entrevistada ASC con filtro por centro
     */
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.centro.idJerarquia = :centroId " +
           "AND e.removido = false " +
           "AND (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(CAST(e.fechaEntrevista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CAST(e.duracionVista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.motivoNoVisita) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.objetivoGeneral) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.desarrolloVisitaDomiciliaria) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.conclusiones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.recomendaciones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.otraPersonaRelacionada) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CONCAT(pr.primerNombre, ' ', pr.segundoNombre, ' ', pr.primerApellido, ' ', pr.segundoApellido)) LIKE LOWER(CONCAT('%', :filtro, '%'))) " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END ASC")
    Page<EvaluacionDomiciliaria> buscarConFiltroOrdenadoPorPersonaEntrevistadaAsc(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("centroId") Long centroId,
            @Param("filtro") String filtro,
            Pageable pageable);

    /**
     * Busca evaluaciones con filtro de texto y ordenadas por persona entrevistada DESC con filtro por centro
     */
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.centro.idJerarquia = :centroId " +
           "AND e.removido = false " +
           "AND (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(CAST(e.fechaEntrevista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CAST(e.duracionVista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.motivoNoVisita) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.objetivoGeneral) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.desarrolloVisitaDomiciliaria) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.conclusiones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.recomendaciones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.otraPersonaRelacionada) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CONCAT(pr.primerNombre, ' ', pr.segundoNombre, ' ', pr.primerApellido, ' ', pr.segundoApellido)) LIKE LOWER(CONCAT('%', :filtro, '%'))) " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END DESC")
    Page<EvaluacionDomiciliaria> buscarConFiltroOrdenadoPorPersonaEntrevistadaDesc(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("centroId") Long centroId,
            @Param("filtro") String filtro,
            Pageable pageable);

    /**
     * @deprecated Usar buscarPorFiltroYCentro() para enfoque multi-jerárquico
     */
    @Deprecated
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.removido = false " +
           "AND (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(CAST(e.fechaEntrevista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CAST(e.duracionVista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.motivoNoVisita) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.objetivoGeneral) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.desarrolloVisitaDomiciliaria) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.conclusiones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.recomendaciones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.otraPersonaRelacionada) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CONCAT(pr.primerNombre, ' ', pr.segundoNombre, ' ', pr.primerApellido, ' ', pr.segundoApellido)) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<EvaluacionDomiciliaria> buscarPorFiltro(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("filtro") String filtro,
            Pageable pageable);

    /**
     * @deprecated Usar buscarOrdenadoPorPersonaEntrevistadaAsc() con centro para enfoque multi-jerárquico
     */
    @Deprecated
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.removido = false " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END ASC")
    Page<EvaluacionDomiciliaria> buscarOrdenadoPorPersonaEntrevistadaAsc(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            Pageable pageable);

    /**
     * @deprecated Usar buscarOrdenadoPorPersonaEntrevistadaDesc() con centro para enfoque multi-jerárquico
     */
    @Deprecated
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.removido = false " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END DESC")
    Page<EvaluacionDomiciliaria> buscarOrdenadoPorPersonaEntrevistadaDesc(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            Pageable pageable);

    /**
     * @deprecated Usar buscarConFiltroOrdenadoPorPersonaEntrevistadaAsc() con centro para enfoque multi-jerárquico
     */
    @Deprecated
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.removido = false " +
           "AND (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(CAST(e.fechaEntrevista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CAST(e.duracionVista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.motivoNoVisita) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.objetivoGeneral) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.desarrolloVisitaDomiciliaria) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.conclusiones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.recomendaciones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.otraPersonaRelacionada) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CONCAT(pr.primerNombre, ' ', pr.segundoNombre, ' ', pr.primerApellido, ' ', pr.segundoApellido)) LIKE LOWER(CONCAT('%', :filtro, '%'))) " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END ASC")
    Page<EvaluacionDomiciliaria> buscarConFiltroOrdenadoPorPersonaEntrevistadaAsc(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("filtro") String filtro,
            Pageable pageable);

    /**
     * @deprecated Usar buscarConFiltroOrdenadoPorPersonaEntrevistadaDesc() con centro para enfoque multi-jerárquico
     */
    @Deprecated
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.removido = false " +
           "AND (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(CAST(e.fechaEntrevista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CAST(e.duracionVista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.motivoNoVisita) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.objetivoGeneral) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.desarrolloVisitaDomiciliaria) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.conclusiones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.recomendaciones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.otraPersonaRelacionada) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CONCAT(pr.primerNombre, ' ', pr.segundoNombre, ' ', pr.primerApellido, ' ', pr.segundoApellido)) LIKE LOWER(CONCAT('%', :filtro, '%'))) " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END DESC")
    Page<EvaluacionDomiciliaria> buscarConFiltroOrdenadoPorPersonaEntrevistadaDesc(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("filtro") String filtro,
            Pageable pageable);

    /**
     * Busca evaluaciones domiciliarias filtrando por jerarquía padre (todos los centros de la misma jerarquía)
     * Aplica filtros de texto en múltiples campos y filtra por jerarquía padre
     */
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.centro.jerarquiaPadre.idJerarquia = :jerarquiaPadreId " +
           "AND e.removido = false " +
           "AND (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(CAST(e.fechaEntrevista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CAST(e.duracionVista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.motivoNoVisita) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.objetivoGeneral) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.desarrolloVisitaDomiciliaria) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.conclusiones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.recomendaciones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.otraPersonaRelacionada) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CONCAT(pr.primerNombre, ' ', pr.segundoNombre, ' ', pr.primerApellido, ' ', pr.segundoApellido)) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<EvaluacionDomiciliaria> buscarPorFiltroYJerarquiaPadre(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("jerarquiaPadreId") Long jerarquiaPadreId,
            @Param("filtro") String filtro,
            Pageable pageable);

    /**
     * Busca evaluaciones ordenadas por persona entrevistada ASC filtrando por jerarquía padre
     */
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.centro.jerarquiaPadre.idJerarquia = :jerarquiaPadreId " +
           "AND e.removido = false " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END ASC")
    Page<EvaluacionDomiciliaria> buscarOrdenadoPorPersonaEntrevistadaAscPorJerarquiaPadre(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("jerarquiaPadreId") Long jerarquiaPadreId,
            Pageable pageable);

    /**
     * Busca evaluaciones ordenadas por persona entrevistada DESC filtrando por jerarquía padre
     */
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.centro.jerarquiaPadre.idJerarquia = :jerarquiaPadreId " +
           "AND e.removido = false " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END DESC")
    Page<EvaluacionDomiciliaria> buscarOrdenadoPorPersonaEntrevistadaDescPorJerarquiaPadre(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("jerarquiaPadreId") Long jerarquiaPadreId,
            Pageable pageable);

    /**
     * Busca evaluaciones con filtro de texto y ordenadas por persona entrevistada ASC filtrando por jerarquía padre
     */
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.centro.jerarquiaPadre.idJerarquia = :jerarquiaPadreId " +
           "AND e.removido = false " +
           "AND (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(CAST(e.fechaEntrevista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CAST(e.duracionVista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.motivoNoVisita) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.objetivoGeneral) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.desarrolloVisitaDomiciliaria) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.conclusiones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.recomendaciones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.otraPersonaRelacionada) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CONCAT(pr.primerNombre, ' ', pr.segundoNombre, ' ', pr.primerApellido, ' ', pr.segundoApellido)) LIKE LOWER(CONCAT('%', :filtro, '%'))) " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END ASC")
    Page<EvaluacionDomiciliaria> buscarConFiltroOrdenadoPorPersonaEntrevistadaAscPorJerarquiaPadre(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("jerarquiaPadreId") Long jerarquiaPadreId,
            @Param("filtro") String filtro,
            Pageable pageable);

    /**
     * Busca evaluaciones con filtro de texto y ordenadas por persona entrevistada DESC filtrando por jerarquía padre
     */
    @Query("SELECT e FROM EvaluacionDomiciliaria e " +
           "LEFT JOIN e.personaRelacionada pr " +
           "WHERE e.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND e.empresa.idEmpresa = :empresaId " +
           "AND e.centro.jerarquiaPadre.idJerarquia = :jerarquiaPadreId " +
           "AND e.removido = false " +
           "AND (:filtro IS NULL OR :filtro = '' OR " +
           "LOWER(CAST(e.fechaEntrevista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CAST(e.duracionVista AS string)) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.motivoNoVisita) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.objetivoGeneral) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.desarrolloVisitaDomiciliaria) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.conclusiones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.recomendaciones) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.otraPersonaRelacionada) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(CONCAT(pr.primerNombre, ' ', pr.segundoNombre, ' ', pr.primerApellido, ' ', pr.segundoApellido)) LIKE LOWER(CONCAT('%', :filtro, '%'))) " +
           "ORDER BY " +
           "CASE WHEN e.otraPersonaRelacionada IS NOT NULL AND e.otraPersonaRelacionada != '' " +
           "THEN e.otraPersonaRelacionada " +
           "ELSE CONCAT(COALESCE(pr.primerNombre, ''), ' ', COALESCE(pr.segundoNombre, ''), ' ', COALESCE(pr.primerApellido, ''), ' ', COALESCE(pr.segundoApellido, '')) " +
           "END DESC")
    Page<EvaluacionDomiciliaria> buscarConFiltroOrdenadoPorPersonaEntrevistadaDescPorJerarquiaPadre(
            @Param("tokenFicha") String tokenFicha,
            @Param("empresaId") Long empresaId,
            @Param("jerarquiaPadreId") Long jerarquiaPadreId,
            @Param("filtro") String filtro,
            Pageable pageable);
}
