package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.SancionDisciplinariaDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SancionDisciplinariaDocumentoRepository extends JpaRepository<SancionDisciplinariaDocumento, Long>{
    Page<SancionDisciplinariaDocumento> findBySancionDisciplinariaTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido, Pageable pageable);

    SancionDisciplinariaDocumento findFirstBySancionDisciplinariaTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorSancion, String tokenIdentificadorDocumento, Boolean removido);
}
