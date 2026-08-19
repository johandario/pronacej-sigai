package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ia.HistorialDeFotosFichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.AuditoriaAccionesSistema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface HistorialDeFotosFichaIdentificacionRepository extends JpaRepository<HistorialDeFotosFichaIdentificacion, Long> {

    /**
     * Devuelve un objeto HistorialDeFotosFichaIdentificacion segun el token identificador y removido
     *
     * @param tokenIdentificador String token identificador.
     * @param removido           Boolean removido
     * @return HistorialDeFotosFichaIdentificacion
     */
    HistorialDeFotosFichaIdentificacion findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    /**
     * Devuelve un objeto Page que cumple con los filtros
     *
     * @param filtro String texto de busqueda.
     * @param tokenIdentificadorFichaIdentificacion String
     * @return Page<HistorialDeFotosFichaIdentificacion>
     */
    @Query(value = "SELECT hFFI.* FROM ia_historial_foto_ficha_identificacion as hFFI"
            + " LEFT JOIN ia_ficha_identificacion fi ON hFFI.id_ficha_de_identificacion = fi.id_ficha_identificacion"
            + " LEFT JOIN par_catalogo tipo ON hFFI.id_tipo = tipo.id_catalogo"
            + " LEFT JOIN doc_documento doc ON hFFI.id_documento = doc.id_documento"


            + " WHERE (fi.token_identificador = :tokenIdentificadorFichaIdentificacion)"
            + " and (:filtro is null or tipo.nombre like %:filtro%"
            + " or tipo.nombre like %:filtro%"
            + " or tipo.descripcion like %:filtro%"
            + " or doc.nombre_real like %:filtro%"
            + " or doc.tipo_de_documento_sistema_otro like %:filtro%)"
            + " and hFFI.removido = false and doc.removido = false and fi.removido = false",
            nativeQuery = true
    )
    Page<HistorialDeFotosFichaIdentificacion> encontrarPorFiltroDeBusqueda(
            @Param("filtro") String filtro,
            @Param("tokenIdentificadorFichaIdentificacion") String tokenIdentificadorFichaIdentificacion,
            Pageable pageable
    );

    HistorialDeFotosFichaIdentificacion findFirstByFichaIdentificacionTokenIdentificadorAndTipoNemonicoAndRemovidoOrderByFechaCreacionDesc
            (String tokenIdentificador,String nemonico, Boolean removido);
}
