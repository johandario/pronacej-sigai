package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.SancionDisciplinariaCarpeta;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SancionDisciplinariaCarpetaRepository extends JpaRepository<SancionDisciplinariaCarpeta, Long> {
    Page<SancionDisciplinariaCarpeta> findBySancionDisciplinariaTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);
    SancionDisciplinariaCarpeta findFirstBySancionDisciplinariaTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
