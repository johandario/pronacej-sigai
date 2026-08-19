package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaAsistenciaPostEgresoCarpeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaAsistenciaPostEgresoCarpetaRepository extends JpaRepository<FichaAsistenciaPostEgresoCarpeta, Long> {
    FichaAsistenciaPostEgresoCarpeta findFirstByFichaAsistenciaPostEgresoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaAsistencia, Boolean removido);

}
