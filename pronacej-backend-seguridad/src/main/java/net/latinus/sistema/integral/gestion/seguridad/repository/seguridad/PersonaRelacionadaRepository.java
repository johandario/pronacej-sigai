package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacionPersonaRelacionada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PersonaRelacionadaRepository extends JpaRepository<PersonaRelacionada, Long> {
    
    List<PersonaRelacionada> findByRemovido(boolean removido);
    
    PersonaRelacionada findByIdPersonasRelacionadas(Long idPersonasRelacionadas);
    
    PersonaRelacionada findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    Page<PersonaRelacionada> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);
    
    Page<PersonaRelacionada> findByEmpresaIdEmpresaAndEvaluacionSocialTokenIdentificadorAndRemovido(Long idEmpresa, String tokenIdentificador, Boolean removido, Pageable pageable);
    
    PersonaRelacionada findByIdPersonasRelacionadasAndRemovido(Long idPersonasRelacionadas, Boolean removido);
    
    // Método para buscar por token identificador con condición de que comience con un prefijo (para tokens temporales)
    @Query("SELECT p FROM PersonaRelacionada p WHERE p.tokenIdentificador LIKE CONCAT(:prefix, '%') AND p.removido = :removido")
    List<PersonaRelacionada> findByTokenIdentificadorStartingWithAndRemovido(@Param("prefix") String prefix, @Param("removido") Boolean removido);

    @Query("SELECT o.idPersonasRelacionadas FROM FichaIdentificacionPersonaRelacionada o WHERE o.idFichaIdentificacion.tokenIdentificador = :tokenIdentificado and o.idPersonasRelacionadas.removido = false")
    Page<PersonaRelacionada> encontrarPersonaRelacionadaPorUiid(@Param("tokenIdentificado") String tokenIdentificado, Pageable pageable);

    @Query("SELECT f.idPersonasRelacionadas FROM FichaIdentificacionPersonaRelacionada f " +
            "WHERE f.idFichaIdentificacion.idFichaIdentificacion = :idFichaIdentificacion " +
            "AND f.removido = :removido")
    List<PersonaRelacionada> encontrarPersonaRelacionadaPorIdFicha(
            @Param("idFichaIdentificacion") Long idFichaIdentificacion,
            @Param("removido") boolean removido);


    @Query("SELECT f.idPersonasRelacionadas FROM FichaIdentificacionPersonaRelacionada f " +
            "WHERE f.idFichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND f.removido = :removido")
    List<PersonaRelacionada> encontrarPersonaRelacionadaPorTokenFicha(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("removido") boolean removido);
    
    /**
    * Busca personas relacionadas por número de identificación
    * 
    * @param identificacion número de identificación o documento
    * @param removido indica si está eliminado lógicamente
    * @return lista de personas relacionadas
    */
   List<PersonaRelacionada> findByIdentificacionAndRemovido(String identificacion, Boolean removido);

   /**
    * Busca relaciones por el ID de la persona relacionada
    * 
    * @param idPersonasRelacionadas ID de la persona relacionada
    * @param removido indica si está eliminado lógicamente
    * @return lista de relaciones
    */
   List<FichaIdentificacionPersonaRelacionada> findByIdPersonasRelacionadasAndRemovido(
       PersonaRelacionada idPersonasRelacionadas, Boolean removido);
   
    /**
     * Busca personas relacionadas por el número de identificación exacto
     * 
     * @param identificacion número de identificación o documento (se busca coincidencia exacta)
     * @param removido indica si está eliminado lógicamente
     * @return lista de personas relacionadas
     */
    @Query("SELECT p FROM PersonaRelacionada p WHERE p.identificacion = :identificacion AND p.removido = :removido")
    List<PersonaRelacionada> findByIdentificacionExactaAndRemovido(
        @Param("identificacion") String identificacion, 
        @Param("removido") Boolean removido);
}
