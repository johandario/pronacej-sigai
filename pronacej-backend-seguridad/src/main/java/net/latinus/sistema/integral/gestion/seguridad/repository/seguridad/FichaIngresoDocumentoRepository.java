package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIngresoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaIngresoDocumentoRepository extends JpaRepository<FichaIngresoDocumento, Long> {

    Page<FichaIngresoDocumento> findByFichaIngresoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIngreso, Boolean removido, Pageable pageable);

    FichaIngresoDocumento findFirstByFichaIngresoTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaIngreso,
                                                                                                             String tokenIdentificadorDocumento, Boolean removido);

    Page<FichaIngresoDocumento> findByFichaIngresoFichaIdentificacionTokenIdentificadorAndRemovido( String tokenIdentificadorFichaIngreso,Boolean removido, Pageable pageable);
}