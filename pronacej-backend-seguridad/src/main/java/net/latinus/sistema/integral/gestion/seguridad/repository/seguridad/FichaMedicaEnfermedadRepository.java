package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaMedicaEnfermedad;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionadaEnfermedad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FichaMedicaEnfermedadRepository extends JpaRepository<FichaMedicaEnfermedad, Long> {

    FichaMedicaEnfermedad findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("SELECT o FROM FichaMedicaEnfermedad o WHERE o.idFichaMedica.tokenIdentificador = :tokenIdentificadorFicha and o.removido = false")
    Page<FichaMedicaEnfermedad> encontrarEnfermedadesFichaMedica(@Param("tokenIdentificadorFicha") String tokenIdentificadorFicha, Pageable pageable);
}
