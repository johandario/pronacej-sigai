package net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico;

import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionadaEnfermedad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonaRelacionadaEnfermedadRepository extends JpaRepository<PersonaRelacionadaEnfermedad, Long> {

    PersonaRelacionadaEnfermedad findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("SELECT o FROM PersonaRelacionadaEnfermedad o WHERE o.idFichaMedica.tokenIdentificador = :tokenIdentificadorFicha and o.removido = false")
    Page<PersonaRelacionadaEnfermedad> encontrarEnfermedadesPersonaFicha(@Param("tokenIdentificadorFicha") String tokenIdentificadorFicha, Pageable pageable);

}
