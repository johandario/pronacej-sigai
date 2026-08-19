package net.latinus.sistema.integral.gestion.seguridad.repository.encuesta;

import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.Encabezado;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EncabezadoRepository extends JpaRepository<Encabezado, Long> {
    Encabezado findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<Encabezado> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<Encabezado> findByEncuestaCatalogoIdCatalogoAndFichaIdentificacionTokenIdentificadorAndRemovido(Long idCatalogo, String tokenIdentificador, Boolean removido);

    List<Encabezado> findByEncuestaCategoriaNemonicoAndFichaIdentificacionTokenIdentificadorAndRemovido(String nemonico, String tokenIdentificador, Boolean removido);

    @Query("SELECT e FROM Encabezado e " +
            "WHERE e.encuesta.categoria.nemonico = :nemonicoCategoria " +
            "AND (e.encuesta.tipoCentro.nemonico = :nemonicoTipoCentro OR e.encuesta.tipoCentro.nemonico = '" + EtiquetaNemonico.TIPO_CENTRO_TODOS + "') " +
            "AND e.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND e.removido = :removido")
    List<Encabezado> findByEncuestaCategoriaNemonicoAndEncuestaTipoCentroNemonicoAndFichaIdentificacionTokenIdentificadorAndRemovido(@Param("nemonicoCategoria") String nemonicoCategoria,
                                                                                                                                     @Param("nemonicoTipoCentro") String nemonicoTipoCentro,
                                                                                                                                     @Param("tokenIdentificador") String tokenIdentificador,
                                                                                                                                     @Param("removido") Boolean removido);
    @Query("SELECT e FROM Encabezado e " +
            "WHERE e.encuesta.catalogo.idCatalogo = :idCatalogo " +
            "AND e.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND e.removido = :removido")
    Page<Encabezado> findByEncuestaCatalogoIdCatalogoAndFichaIdentificacionTokenIdentificadorAndRemovido(
            @Param("idCatalogo") Long idCatalogo,
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("removido") Boolean removido,
            Pageable pageable
    );


    @Query("SELECT e FROM Encabezado e " +
            "WHERE e.encuesta.catalogo.idCatalogo = :idCatalogo " +
            "AND e.fichaIdentificacion.tokenIdentificador = :tokenIdentificador " +
            "AND e.removido = false " +
            "AND (LOWER(e.nombre) LIKE LOWER(:nombre) OR LOWER(e.descripcion) LIKE LOWER(:descripcion))")
    Page<Encabezado> buscarPorFiltros(
            @Param("idCatalogo") Long idCatalogo,
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("nombre") String nombre,
            @Param("descripcion") String descripcion,
            Pageable pageable
    );
}
