package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.InformacionUbicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface InformacionUbicacionRepository extends JpaRepository<InformacionUbicacion, Long> {
    
    InformacionUbicacion findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    // Consulta optimizada con JOIN FETCH para evitar el problema N+1
    @Query("SELECT o FROM InformacionUbicacion o JOIN FETCH o.tipoInformacionUbicacion " +
           "WHERE o.idPersonasRelacionadas.idPersonasRelacionadas = :idPersonaRelacionada AND o.removido = false")
    List<InformacionUbicacion> encontrarInformacionUbicaciones(@Param("idPersonaRelacionada") Long idPersonaRelacionada);
    
    List<InformacionUbicacion> findByIdPersonasRelacionadasIdPersonasRelacionadasAndRemovido(Long idPersonasRelacionadas, boolean removido);
}