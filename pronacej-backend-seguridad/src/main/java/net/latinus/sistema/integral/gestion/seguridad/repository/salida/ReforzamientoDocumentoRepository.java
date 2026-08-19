package net.latinus.sistema.integral.gestion.seguridad.repository.salida;

import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.ReforzamientoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReforzamientoDocumentoRepository extends JpaRepository<ReforzamientoDocumento, Long> {
    List<ReforzamientoDocumento> findByReforzamientoTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("""
    SELECT d 
    FROM Documento d
    JOIN d.tipoDeDocumentoSistema tds
    JOIN d.carpeta c
    JOIN FichaIdentificacionCarpeta fic ON fic.carpeta = c
    JOIN fic.fichaIdentificacion fi
    WHERE d.removido = false
    AND tds.nemonico = 'TIPO_DOCUMENTO_ACTA_CONSENTIMIENTO'
    AND fi.tokenIdentificador = :tokenIdentificador
""")
    List<Documento> buscarDocumentosActaConsentimientoPorFicha(
            @Param("tokenIdentificador") String tokenIdentificador
    );


}
