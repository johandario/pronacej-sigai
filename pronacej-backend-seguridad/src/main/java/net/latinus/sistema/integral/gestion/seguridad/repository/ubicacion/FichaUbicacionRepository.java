package net.latinus.sistema.integral.gestion.seguridad.repository.ubicacion;

import net.latinus.sistema.integral.gestion.seguridad.entities.ubicacion.FichaUbicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichaUbicacionRepository extends JpaRepository<FichaUbicacion, Long> {

    FichaUbicacion findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    Page<FichaUbicacion> findByFichaIdentificacionTokenIdentificadorAndRemovido(
            String tokenIdentificadorFichaIdentificacion,
            Boolean removido,
            Pageable pageable
    );

    List<FichaUbicacion> findByFichaIdentificacionTokenIdentificadorAndRemovidoAndUbicacionActual(
            String tokenIdentificadorFichaIdentificacion,
            Boolean removido,
            Boolean ubicacionActual
    );
}

