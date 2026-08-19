package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacionPersonaRelacionada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionada;

public interface FichaIdentificacionPersonaRelacionadaRepository extends JpaRepository<FichaIdentificacionPersonaRelacionada, Long> {

    // Método alternativo que usa la convención de nombres de Spring Data JPA
    // Este método buscará todas las relaciones donde la ficha tenga el tokenIdentificador especificado
    // y no estén marcadas como removidas
    List<FichaIdentificacionPersonaRelacionada> findByIdFichaIdentificacionTokenIdentificadorAndRemovido(
            String tokenIdentificador, boolean removido);

    // Los métodos originales se mantienen para compatibilidad

    @Query("SELECT o FROM FichaIdentificacionPersonaRelacionada o WHERE o.idFichaIdentificacion.tokenIdentificador = :tokenIdentificado " +
            "and o.idPersonasRelacionadas.tokenIdentificador = :tokenIdentificadorP")
    FichaIdentificacionPersonaRelacionada encontrarPorTokenFichaYIdPersonaRelacionada(
            @Param("tokenIdentificado") String tokenIdentificado,
            @Param("tokenIdentificadorP") String tokenIdentificadorP);

    /**
     * Busca relaciones por persona relacionada y estado de eliminación
     * 
     * @param idPersonasRelacionadas Objeto PersonaRelacionada
     * @param removido estado de eliminación lógica
     * @return Lista de relaciones encontradas
     */
    List<FichaIdentificacionPersonaRelacionada> findByIdPersonasRelacionadasAndRemovido(
        PersonaRelacionada idPersonasRelacionadas, boolean removido);
    
    /**
     * Busca relaciones por el ID numérico de la persona relacionada
     * 
     * @param idPersonasRelacionadas ID numérico de la persona relacionada
     * @param removido indica si está eliminado lógicamente
     * @return lista de relaciones
     */
    List<FichaIdentificacionPersonaRelacionada> findByIdPersonasRelacionadas_IdPersonasRelacionadasAndRemovido(
        Long idPersonasRelacionadas, boolean removido);
}
