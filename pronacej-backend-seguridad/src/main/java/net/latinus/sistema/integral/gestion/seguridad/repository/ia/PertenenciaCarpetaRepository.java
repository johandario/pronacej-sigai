package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.PertenenciaCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PertenenciaCarpetaRepository extends JpaRepository<PertenenciaCarpeta, Long> {
    Page<PertenenciaCarpeta> findByPertenenciaTokenIdentificadorAndRemovido(String tokenIdentificadorPertenencia, Boolean removido, Pageable pageable);

    PertenenciaCarpeta findFirstByPertenenciaTokenIdentificadorAndRemovido(String tokenIdentificadorPertenencia, Boolean removido);

}
