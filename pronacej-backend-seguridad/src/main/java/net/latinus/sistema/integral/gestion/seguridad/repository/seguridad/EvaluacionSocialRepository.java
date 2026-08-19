package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionSocial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluacionSocialRepository extends JpaRepository<EvaluacionSocial, Long> {
    
    List<EvaluacionSocial> findByRemovido(boolean removido);
    
    EvaluacionSocial findByIdEvaluacionSocial(Long idEvaluacionSocial);
    
    EvaluacionSocial findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<EvaluacionSocial> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<EvaluacionSocial> findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
            String tokenIdentificadorFichaIdentificacion, 
            Long idEmpresa, 
            Boolean removido, 
            Pageable pageable);
    
    @Query("SELECT es FROM EvaluacionSocial es " +
           "WHERE es.fichaIdentificacion.tokenIdentificador = :tokenFicha " +
           "AND es.empresa.idEmpresa = :idEmpresa " +
           "AND es.removido = false " +
           "AND (" +
           "    LOWER(CONCAT(COALESCE(es.usuarioSistemaCrea.nombres, ''), ' ', COALESCE(es.usuarioSistemaCrea.apellidos, ''))) LIKE LOWER(:filtro) OR " +
           "    (es.grupoAmical IS NOT NULL AND LOWER(es.grupoAmical) LIKE LOWER(:filtro)) OR " +
           "    (es.factorRiesgoMedio IS NOT NULL AND LOWER(es.factorRiesgoMedio) LIKE LOWER(:filtro))" +
           ")")
    Page<EvaluacionSocial> buscarPorFiltroGeneral(
            @Param("tokenFicha") String tokenFicha,
            @Param("idEmpresa") Long idEmpresa,
            @Param("filtro") String filtro,
            Pageable pageable);
}
