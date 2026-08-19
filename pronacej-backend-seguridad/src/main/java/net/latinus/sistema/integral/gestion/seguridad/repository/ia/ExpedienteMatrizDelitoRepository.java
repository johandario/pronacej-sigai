package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.ExpedienteMatrizDelito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpedienteMatrizDelitoRepository extends JpaRepository<ExpedienteMatrizDelito, Long> {

    @Query("SELECT c.nombre, COALESCE(COUNT(e), 0) " +
            "FROM Catalogo c " +
            "LEFT JOIN ExpedienteMatrizDelito e ON e.delitoGenerico = c " +
            "WHERE c.removido = false " +
            "and c.catalogoPadre IS NOT NULL AND c.catalogoPadre.nemonico = 'LISTADO_DELITOS_EXPEDIENTE' " +
            "GROUP BY c.nombre " +
            "ORDER BY COUNT(e) DESC")
    List<Object[]> countDelitosGenericos();

    @Query("SELECT c.nombre, COALESCE(COUNT(e), 0) " +
            "FROM Catalogo c " +
            "LEFT JOIN ExpedienteMatrizDelito e ON e.delitoGenerico = c " +
            "LEFT JOIN e.expedienteMatrizDetalle d " +
            "LEFT JOIN d.expedienteMatriz m " +
            "LEFT JOIN m.fichaIdentificacion f " +
            "LEFT JOIN f.tipoSexo s " +
            "WHERE c.removido = false " +
            "AND c.catalogoPadre IS NOT NULL " +
            "AND c.catalogoPadre.nemonico = 'LISTADO_DELITOS_EXPEDIENTE' " +
            "AND (s.nemonico = :nemonicoTipoSexo OR :nemonicoTipoSexo IS NULL) " +
            "GROUP BY c.nombre " +
            "ORDER BY COUNT(e) DESC")
    List<Object[]> countDelitosGenericos(@Param("nemonicoTipoSexo") String nemonicoTipoSexo);

    @Query("SELECT c.nombre, COALESCE(COUNT(e), 0) " +
            "FROM Catalogo c " +
            "LEFT JOIN ExpedienteMatrizDelito e ON e.delitoGenerico = c " +
            "LEFT JOIN e.expedienteMatrizDetalle d " +
            "LEFT JOIN d.expedienteMatriz m " +
            "LEFT JOIN m.fichaIdentificacion f " +
            "LEFT JOIN f.tipoSexo s " +
            "LEFT JOIN f.centroIngreso ci " +
            "WHERE c.removido = false " +
            "AND c.catalogoPadre IS NOT NULL " +
            "AND c.catalogoPadre.nemonico = 'LISTADO_DELITOS_EXPEDIENTE' " +
            "AND (:nemonicoTipoSexo IS NULL OR s.nemonico = :nemonicoTipoSexo) " +
            "AND (:tokenIdentificadorCentro IS NULL OR ci.tokenIdentificador = :tokenIdentificadorCentro) " +
            "AND (:nemonicoCentro IS NULL OR (ci.jerarquiaPadre IS NOT NULL AND ci.jerarquiaPadre.nemonico = :nemonicoCentro)) " +
            "GROUP BY c.nombre " +
            "ORDER BY COUNT(e) DESC")
    List<Object[]> countDelitosGenericos(@Param("nemonicoTipoSexo") String nemonicoTipoSexo,
                                         @Param("tokenIdentificadorCentro") String tokenIdentificadorCentro,
                                         @Param("nemonicoCentro") String nemonicoCentro);

    List<ExpedienteMatrizDelito> findByExpedienteMatrizDetalleTokenIdentificadorAndRemovido(String tokenDetalle, Boolean removido);
}
