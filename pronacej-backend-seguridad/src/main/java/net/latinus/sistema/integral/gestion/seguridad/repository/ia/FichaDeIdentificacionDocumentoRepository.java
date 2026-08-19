package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaDeIdentificacionDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaDeIdentificacionDocumentoRepository extends JpaRepository<FichaDeIdentificacionDocumento, Long> {

    /**
     * Devuelve un objeto paginacion con la data obtenida
     *
     * @param tokenIdentificadorFichaIdentintificacion String token identificador de la ficha de identificacion.
     * @param busqueda String filtro de busqueda.
     * @param pageable Pagebale
     *
     * @return Page<AuditoriaAccionesSistema>
     */
    @Query(value = "SELECT fichaIDoc.* FROM ia_ficha_identificacion_documento as fichaIDoc"

            //+ " LEFT JOIN doc_carpeta carp ON fichaIDoc.id_carpeta = carp.id_carpeta"
            + " LEFT JOIN ia_ficha_identificacion fichaI ON fichaIDoc.id_ficha_de_identificacion = fichaI.id_ficha_identificacion"
            + " LEFT JOIN doc_documento doc ON doc.id_documento = fichaIDoc.id_documento"
            + " LEFT JOIN par_catalogo cat ON cat.id_catalogo = doc.id_tipo_de_documento_sistema"


            + " WHERE (fichaI.token_identificador = :tokenIdentificadorFichaIdentintificacion)"
            + " and (:busqueda is null or ( doc.descripcion like %:busqueda%"
            + " or doc.mime_type like %:busqueda%"
            + " or doc.nombre_real like %:busqueda%"
            + " or cat.nombre like %:busqueda%"
            + " or doc.mime_type like %:busqueda% ))"

            + " and fichaIDoc.removido = false and doc.removido = false",
            nativeQuery = true
    )
    Page<FichaDeIdentificacionDocumento> encontrarPorFichaIdentificacionYFiltroBuscar(
            @Param("tokenIdentificadorFichaIdentintificacion") String tokenIdentificadorFichaIdentintificacion,
            @Param("busqueda") String busqueda,
            Pageable pageable
    );

    /**
     * Devuelve un objeto FichaDeIdentificacionDocumento por el token de la ficha de identificacion y del documento y removido
     *
     * @param tokenIdentificadorFichaIdentificacion String token identificador de la ficha de identificacion.
     * @param tokenIdentificadorDocumento String token identificador del documento.
     * @param removido Boolean
     *
     * @return Page<FichaDeIdentificacionDocumento>
     */
    Page<FichaDeIdentificacionDocumento> findByFichaIdentificacionTokenIdentificadorAndDocumentoTokenIdentificadorAndRemovido(
            String tokenIdentificadorFichaIdentificacion, String tokenIdentificadorDocumento, Boolean removido,
            Pageable pageable
    );

    /**
     * Devuelve un objeto FichaDeIdentificacionDocumento
     *
     * @param tokenIdentificadorDocumento String token identificador
     * @param removido Boolean
     *
     * @return Page<FichaDeIdentificacionDocumento>
     */
    FichaDeIdentificacionDocumento findByTokenIdentificadorAndRemovido(String tokenIdentificadorDocumento, Boolean removido);
}
