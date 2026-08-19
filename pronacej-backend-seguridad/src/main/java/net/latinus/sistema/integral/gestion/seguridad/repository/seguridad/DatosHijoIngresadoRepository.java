package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.DatosHijoIngresado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatosHijoIngresadoRepository extends JpaRepository<DatosHijoIngresado, Long> {
    List<DatosHijoIngresado> findByRemovido(boolean removido);
    
    DatosHijoIngresado findByIdDatosHijoIngresado(Long idDatosHijoIngresado);
        
    DatosHijoIngresado findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    
    DatosHijoIngresado findByFichaIngresoTokenIdentificador(String tokenIdentificadorFichaIngreso);
}