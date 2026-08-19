package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaAsistenciaPostEgresoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaAsistenciaPostEgresoDocumentoRepository extends JpaRepository <FichaAsistenciaPostEgresoDocumento, Long> {
    Page<FichaAsistenciaPostEgresoDocumento> findByFichaAsistenciaPostEgresoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaAsistencia, Boolean removido, Pageable pageable);

    FichaAsistenciaPostEgresoDocumento findFirstByFichaAsistenciaPostEgresoTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaAsistencia, String tokenIdentificadorDocumento, Boolean removido);
}
