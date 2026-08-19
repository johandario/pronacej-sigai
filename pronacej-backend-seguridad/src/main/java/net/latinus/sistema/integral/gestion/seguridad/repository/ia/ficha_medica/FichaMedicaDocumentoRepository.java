package net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica;

import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedicaDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaMedicaDocumentoRepository extends JpaRepository<FichaMedicaDocumento, Long> {
    @Query("""
            SELECT fmd
            FROM FichaMedicaDocumento fmd
            JOIN fmd.fichaMedica fm
            JOIN fmd.documento doc
            JOIN doc.tipoDeDocumentoSistema tdoc
            WHERE
                fm.tokenIdentificador = :tokenIdentificadorFichaMedica
                AND fmd.removido = :removido
                AND (
                        LOWER(TRIM(doc.nombreReal)) LIKE %:filtro% OR
                        LOWER(TRIM(doc.descripcion)) LIKE %:filtro% OR
                        LOWER(TRIM(tdoc.nombre)) LIKE %:filtro% OR
                        LOWER(TRIM(doc.tipoDeDocumentoSistemaOtro)) LIKE %:filtro%
                )
            ORDER BY fmd.fechaCreacion DESC
        """)
    Page<FichaMedicaDocumento> obtenerDocumentosConFiltro(String tokenIdentificadorFichaMedica, String filtro, Boolean removido, Pageable pageable);

    Page<FichaMedicaDocumento> findByFichaMedicaTokenIdentificadorAndRemovidoOrderByFechaCreacionDesc(String tokenIdentificadorFichaMedica, Boolean removido, Pageable pageable);

    FichaMedicaDocumento findFirstByFichaMedicaTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(String tokenIdentificadorFichaMedica, String tokenIdentificadorDocumento, Boolean removido);
}
