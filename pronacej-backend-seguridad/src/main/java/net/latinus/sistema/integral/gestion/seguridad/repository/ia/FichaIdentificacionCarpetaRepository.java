package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichaIdentificacionCarpetaRepository extends JpaRepository<FichaIdentificacionCarpeta, Long> {
    /**
     * Devuelve un objeto page con las FichasIdentificacionCarpeta
     *
     * @param tokenIdentificadorFichaIdenticacion String token identificador de la ficha de identificacion.
     * @param nemonicoTipogestionDeAdolescente    String nemonico de tipo de gestion de adolescente.
     * @param removido                            Boolean removido
     * @return Page<FichaIdentificacionCarpeta>
     */
    Page<FichaIdentificacionCarpeta> findByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
            String tokenIdentificadorFichaIdenticacion, String nemonicoTipogestionDeAdolescente, Boolean removido,
            Pageable pageable
    );

    FichaIdentificacionCarpeta findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
            String tokenIdentificadorFichaIdenticacion, String nemonicoTipogestionDeAdolescente, Boolean removido
    );

    /**
     * Obten una lista de ficha de identficacion carpeta
     *
     * @param tokenIdentificadorFichaPrincipal String.
     * @param tipoDeGestionDeAdolescente       String
     * @param removido                         Boolean
     * @return List<FichaIdentificacionCarpeta>
     */
    List<FichaIdentificacionCarpeta> findByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteAndRemovidoOrderByIdFichaIdentificacionCarpeta(
            String tokenIdentificadorFichaPrincipal, Catalogo tipoDeGestionDeAdolescente,
            Boolean removido
    );

    /**
     * Obten un objeto FichaIdentificacionCarpeta
     *
     * @param tokenIdentificador               String.
     * @param tokenIdentificadorFichaPrincipal String
     * @param removido                         Boolean
     * @return FichaIdentificacionCarpeta
     */
    FichaIdentificacionCarpeta findByTokenIdentificadorAndFichaIdentificacionTokenIdentificadorAndRemovido(
            String tokenIdentificador, String tokenIdentificadorFichaPrincipal, Boolean removido
    );

    /**
     * Obten una lista List<FichaIdentificacionCarpeta>
     *
     * @param carpeta               Carpeta.
     * @param fichaIdentificacion FichaIdentificacion
     * @param removido                         Boolean
     * @return List<FichaIdentificacionCarpeta>
     */
    List<FichaIdentificacionCarpeta> findByCarpetaAndFichaIdentificacionAndRemovidoOrderByIdFichaIdentificacionCarpetaDesc(Carpeta carpeta,
                                                                                                                           FichaIdentificacion fichaIdentificacion,
                                                                                                                           Boolean removido);

    /**
     * Obten una lista List<FichaIdentificacionCarpeta>
     *
     * @return List<FichaIdentificacionCarpeta>
     */
    @Query("SELECT f FROM FichaIdentificacionCarpeta f WHERE f.tipoDeGestionDeAdolescente IS NULL AND f.carpeta IS NOT NULL AND f.removido = false")
    List<FichaIdentificacionCarpeta> findAllCarpetasPadres();

    /**
     * Obten una lista List<FichaIdentificacionCarpeta>
     *
     * @return List<FichaIdentificacionCarpeta>
     */
    @Query("SELECT f FROM FichaIdentificacionCarpeta f WHERE f.fichaIdentificacion.tokenIdentificador = :tokenIdentificador AND f.tipoDeGestionDeAdolescente IS NOT NULL AND f.removido = false")
    List<FichaIdentificacionCarpeta> findSubcarpetasByCarpetaPadre(@Param("tokenIdentificador") String tokenIdentificador);
}
