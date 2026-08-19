package net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica;

import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedicaCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaMedicaCarpetaRepository extends JpaRepository<FichaMedicaCarpeta, Long> {
    Page<FichaMedicaCarpeta> findByFichaMedicaTokenIdentificadorAndRemovido(String tokenIdentificadorFichaMedica, Boolean removido, Pageable pageable);

    FichaMedicaCarpeta findFirstByFichaMedicaTokenIdentificadorAndRemovido(String tokenIdentificadorFichaMedica, Boolean removido);

}
