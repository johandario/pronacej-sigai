package net.latinus.sistema.integral.gestion.seguridad.repository.param;

import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.Localidad;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Long> {

    /**
     * Devuelve una lista las localidades que coincidan por el nemonico padre y removido
     *
     * @param nemonico String con el nemonico del padre
     * @param removido Boolean inidica si se busca un objeto removido o no.
     *
     * @return List<Localidad>
     */
    List<Localidad> findByLocalidadPadre_NemonicoAndRemovido(String nemonico, Boolean removido);

    /**
     * Devuelve una lista las localidades que coincidan por el nemonico del tipo y removido
     *
     * @param nemonico String con el nemonico del tipo localidad
     * @param removido Boolean inidica si se busca un objeto removido o no.
     *
     * @return List<Localidad>
     */
    List<Localidad> findByTipoLocalidad_NemonicoAndRemovido(String nemonico, Boolean removido);

    /**
     * Devuelve una lista las localidades que coincidan por el nemonico del tipo y removido
     *
     * @param nemonico String con el nemonico de la localidad
     * @param removido Boolean inidica si se busca un objeto removido o no.
     *
     * @return Localidad
     */
    Localidad findByNemonicoAndRemovido(String nemonico, Boolean removido);

    /**
     * Devuelve una lista las localidades que coincidan por el nemonico del tipo y removido
     *
     * @param ubigeo String con el ubigeo de la localidad
     * @param removido Boolean inidica si se busca un objeto removido o no.
     *
     * @return Localidad
     */
    Localidad findByCodigoUbigeoAndRemovido(String ubigeo, Boolean removido);

    @Query("SELECT l FROM Localidad l WHERE l.localidadPadre = :localidadPadre")
    List<Localidad> findByLocalidadPadre(@Param("localidadPadre") Localidad localidadPadre);

    @Query("SELECT l FROM Localidad l LEFT JOIN FETCH l.localidadPadre WHERE l.nemonico = :nemonico")
    Optional<Localidad> findLocalidadWithChildren(@Param("nemonico") String nemonico);

    @Query("SELECT COUNT(l) > 0 FROM Localidad l WHERE l.localidadPadre.idLocalidad = :idLocalidad")
    boolean existsByLocalidadPadre(Long idLocalidad);

    Localidad findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    Long countByLocalidadPadreAndEmpresaAndRemovido(Localidad catalogoPadre, Empresa empresa, Boolean removido);

    Optional<Localidad> findByCodigoUbigeoAndRemovidoFalse(String codigoUbigeo);

    Optional<Localidad> findByNemonicoAndRemovidoFalse(String nemonico);


}
