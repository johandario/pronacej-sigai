package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.Pertenencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PertenenciaRepository extends JpaRepository<Pertenencia, Long> {
    Page<Pertenencia> findByRemovido(Boolean removido, Pageable pageable);

    List<Pertenencia> findByIdPertenenciaAndRemovido(Long id, Boolean removido);

/*
    Page<Pertenencia> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido, Pageable pageable);
*/
    List<Pertenencia> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion, Boolean removido);

    Pertenencia findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    Long countByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIdentificacion,Boolean removido);
}


