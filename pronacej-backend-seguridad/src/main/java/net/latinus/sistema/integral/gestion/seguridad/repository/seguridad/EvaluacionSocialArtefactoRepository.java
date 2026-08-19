package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionSocialArtefacto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluacionSocialArtefactoRepository extends JpaRepository<EvaluacionSocialArtefacto, Long> {
    
    /**
     * Busca artefactos por estado de removido
     */
    List<EvaluacionSocialArtefacto> findByRemovido(boolean removido);
    
    /**
     * Busca artefacto por ID
     */
    EvaluacionSocialArtefacto findByIdEvaluacionSocialArtefacto(Long idEvaluacionSocialArtefacto);
    
    /**
     * Busca artefacto por token identificador y estado de removido
     */
    EvaluacionSocialArtefacto findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    /**
     * Busca artefactos por empresa y estado de removido con paginación
     */
    Page<EvaluacionSocialArtefacto> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    /**
     * Busca artefactos por token de evaluación social, empresa y estado de removido con paginación
     * Query principal para obtener artefactos por evaluación social
     */
    @Query("SELECT esa FROM EvaluacionSocialArtefacto esa " +
           "WHERE esa.evaluacionSocial.tokenIdentificador = :tokenIdentificadorEvaluacionSocial " +
           "AND esa.empresa.idEmpresa = :idEmpresa " +
           "AND esa.removido = :removido " +
           "ORDER BY esa.idEvaluacionSocialArtefacto ASC")
    Page<EvaluacionSocialArtefacto> findByEvaluacionSocialTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
            @Param("tokenIdentificadorEvaluacionSocial") String tokenIdentificadorEvaluacionSocial,
            @Param("idEmpresa") Long idEmpresa,
            @Param("removido") Boolean removido,
            Pageable pageable);
    
    /**
     * Query alternativa con join explícito en caso de que la anterior no funcione
     */
    @Query("SELECT esa FROM EvaluacionSocialArtefacto esa " +
           "INNER JOIN esa.evaluacionSocial es " +
           "WHERE es.tokenIdentificador = :tokenIdentificadorEvaluacionSocial " +
           "AND esa.empresa.idEmpresa = :idEmpresa " +
           "AND esa.removido = :removido " +
           "ORDER BY esa.idEvaluacionSocialArtefacto ASC")
    Page<EvaluacionSocialArtefacto> findByEvaluacionSocialTokenWithJoin(
            @Param("tokenIdentificadorEvaluacionSocial") String tokenIdentificadorEvaluacionSocial,
            @Param("idEmpresa") Long idEmpresa,
            @Param("removido") Boolean removido,
            Pageable pageable);
    
    /**
     * Query nativa como última alternativa
     */
    @Query(value = "SELECT * FROM evaluacion_social_artefacto esa " +
                  "INNER JOIN evaluacion_social es ON esa.id_evaluacion_social = es.id_evaluacion_social " +
                  "WHERE es.token_identificador = :tokenEvaluacionSocial " +
                  "AND esa.id_empresa = :idEmpresa " +
                  "AND esa.removido = :removido " +
                  "ORDER BY esa.id_evaluacion_social_artefacto ASC",
           nativeQuery = true)
    Page<EvaluacionSocialArtefacto> findByEvaluacionSocialTokenNative(
            @Param("tokenEvaluacionSocial") String tokenIdentificadorEvaluacionSocial,
            @Param("idEmpresa") Long idEmpresa,
            @Param("removido") Boolean removido,
            Pageable pageable);
    
    /**
     * Obtiene lista sin paginación para casos específicos
     */
    @Query("SELECT esa FROM EvaluacionSocialArtefacto esa " +
           "WHERE esa.evaluacionSocial.tokenIdentificador = :tokenIdentificadorEvaluacionSocial " +
           "AND esa.empresa.idEmpresa = :idEmpresa " +
           "AND esa.removido = :removido " +
           "ORDER BY esa.idEvaluacionSocialArtefacto ASC")
    List<EvaluacionSocialArtefacto> findAllByEvaluacionSocialTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
            @Param("tokenIdentificadorEvaluacionSocial") String tokenIdentificadorEvaluacionSocial,
            @Param("idEmpresa") Long idEmpresa,
            @Param("removido") Boolean removido);
    
    /**
     * Cuenta artefactos por evaluación social
     */
    @Query("SELECT COUNT(esa) FROM EvaluacionSocialArtefacto esa " +
           "WHERE esa.evaluacionSocial.tokenIdentificador = :tokenIdentificadorEvaluacionSocial " +
           "AND esa.empresa.idEmpresa = :idEmpresa " +
           "AND esa.removido = :removido")
    Long countByEvaluacionSocialTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
            @Param("tokenIdentificadorEvaluacionSocial") String tokenIdentificadorEvaluacionSocial,
            @Param("idEmpresa") Long idEmpresa,
            @Param("removido") Boolean removido);
    
    /**
     * Verifica si existen artefactos para una evaluación social
     */
    @Query("SELECT CASE WHEN COUNT(esa) > 0 THEN true ELSE false END FROM EvaluacionSocialArtefacto esa " +
           "WHERE esa.evaluacionSocial.tokenIdentificador = :tokenIdentificadorEvaluacionSocial " +
           "AND esa.empresa.idEmpresa = :idEmpresa " +
           "AND esa.removido = :removido")
    Boolean existsByEvaluacionSocialTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
            @Param("tokenIdentificadorEvaluacionSocial") String tokenIdentificadorEvaluacionSocial,
            @Param("idEmpresa") Long idEmpresa,
            @Param("removido") Boolean removido);
}
