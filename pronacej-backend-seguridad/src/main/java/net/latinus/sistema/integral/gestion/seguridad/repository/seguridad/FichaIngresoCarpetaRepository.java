package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIngresoCarpeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaIngresoCarpetaRepository extends JpaRepository<FichaIngresoCarpeta, Long> {

    FichaIngresoCarpeta findFirstByFichaIngresoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIngreso, Boolean removido);

}
