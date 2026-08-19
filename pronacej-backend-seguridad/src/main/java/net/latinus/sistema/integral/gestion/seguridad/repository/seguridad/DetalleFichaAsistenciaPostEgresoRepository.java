package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.DetalleFichaAsistenciaPostEgreso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleFichaAsistenciaPostEgresoRepository extends JpaRepository<DetalleFichaAsistenciaPostEgreso, Long> {

    Page<DetalleFichaAsistenciaPostEgreso> findByFichaAsistenciaPostEgresoTokenIdentificadorAndRemovido(
            String tokenIdentificadorFichaAsistencia, Boolean removido, Pageable pageable);

    // Buscar un detalle específico por su token
    DetalleFichaAsistenciaPostEgreso findByTokenIdentificadorAndRemovido(
            String tokenIdentificador, Boolean removido);

}
