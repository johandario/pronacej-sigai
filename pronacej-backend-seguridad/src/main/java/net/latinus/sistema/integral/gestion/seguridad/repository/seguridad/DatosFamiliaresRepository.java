package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.DatosFamiliares;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DatosFamiliaresRepository extends JpaRepository<DatosFamiliares, Long> {
    
    // Consulta optimizada con JOIN FETCH para cargar relaciones en una sola consulta
    @Query("SELECT o FROM DatosFamiliares o " +
           "LEFT JOIN FETCH o.tipoFamilia " +
           "LEFT JOIN FETCH o.organizacionFamiliar " +
           "LEFT JOIN FETCH o.tipoSacramento " +
           "WHERE o.fichaIdentificacion.tokenIdentificador = :tokenIdentificador AND o.removido = false")
    DatosFamiliares encontrarDatosPersonales(@Param("tokenIdentificador") String tokenIdentificador);
    
    DatosFamiliares findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
