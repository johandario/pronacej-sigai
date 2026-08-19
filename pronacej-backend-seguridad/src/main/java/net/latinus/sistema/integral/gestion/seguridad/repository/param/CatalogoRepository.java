package net.latinus.sistema.integral.gestion.seguridad.repository.param;

import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogoRepository extends JpaRepository<Catalogo, Long> {

    /**
     * Devuelve un objeto catalogo por el token identificador y removido
     *
     * @param tokenIdentificador string token identificador.
     * @param removido           boolean que especifica si esta removido o no.
     * @return Catalogo
     */
    Catalogo findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    /**
     * Devuelve un objeto catalogo por el token identificador y removido
     *
     * @param idCatalogo Long id del catalogo en la db.
     * @param removido   boolean que especifica si esta removido o no.
     * @return Catalogo
     */
    Catalogo findByIdCatalogoAndRemovido(Long idCatalogo, Boolean removido);

    /**
     * Devuelve un objeto catalogo por el token identificador y removido
     *
     * @param nemonico                  String nemonico catalogo.
     * @param tokenIdentificadorEmpresa string token identificador de la empresa.
     * @param removido                  Boolean true o false
     * @return Catalogo
     */
    Catalogo findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(String nemonico, String tokenIdentificadorEmpresa, Boolean removido);

    /**
     * Devuelve un objeto catalogo por el nemonico y removido
     *
     * @param nemonico String nemonico catalogo.
     * @param removido Boolean true o false
     * @return Catalogo
     */
    Catalogo findByNemonicoAndRemovido(String nemonico, Boolean removido);


    /**
     * Devuelve una lista de catalogos por el token catalogo padre, token empresa y removido
     *
     * @param nemonicoPadre             String nemonico del catalogo padre.
     * @param tokenIdentificadorEmpresa string token identificador de la empresa.
     * @param removido                  Boolean true o false
     * @return Catalogo
     */
    List<Catalogo> findByCatalogoPadreNemonicoAndEmpresaTokenIdentificadorAndRemovidoOrderByIdCatalogoDesc(String nemonicoPadre,
                                                                                                           String tokenIdentificadorEmpresa,
                                                                                                           Boolean removido);

    /**
     * Devuelve una lista de catalogos en base al removido
     *
     * @param removido Boolean true o false
     * @return List<Catalogo>
     */
    List<Catalogo> findByRemovido(Boolean removido);

    /**
     * Devuelve una objeto Page de sub catalogos por, token padre, removido y un objeto Pageable
     *
     * @param removido   Bolean
     * @param pageable   Pageable Objeto pageable.
     * @param tokenPadre token del padre para buscar
     * @return Page<Catalogo>
     */
    Page<Catalogo> findByCatalogoPadre_TokenIdentificadorAndRemovido(String tokenPadre, Boolean removido, Pageable pageable);

    /**
     * Devuelve una objeto Page de catalogos por removido y un objeto Pageable
     *
     * @param removido Bolean
     * @param pageable Pageable Objeto pageable.
     * @return Page<Catalogo>
     */
    Page<Catalogo> findByCatalogoPadreIsNullAndRemovido(Boolean removido, Pageable pageable);

    /**
     * Devuelve una objeto Page de catalogos, buscando por nombre
     *
     * @param removido Bolean
     * @param pageable Pageable Objeto pageable.
     * @return Page<Catalogo>
     */
    Page<Catalogo> findByNombreContainingIgnoreCaseAndCatalogoPadreIsNullAndRemovido(String nombre, Boolean removido, Pageable pageable);

    /**
     * Devuelve una objeto Page de catalogos, buscando por nombre
     *
     * @param removido Bolean
     * @param pageable Pageable Objeto pageable.
     * @return Page<Catalogo>
     */
    Page<Catalogo> findByCatalogoPadre_TokenIdentificadorAndNombreContainingIgnoreCaseAndRemovido(String token, String nombre, Boolean removido, Pageable pageable);

    /**
     * Devuelve un objeto Page con los catalogos que coincidan por el nemonico y por la empresa y removido
     *
     * @param removido     Bolean
     * @param tokenEmpresa String token identificador de la empresa.
     * @param pageable     Pageable configuraciones para la paginación.
     * @return Page<Catalogo>
     */
    Page<Catalogo> findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(String nemonico,
                                                                         String tokenEmpresa,
                                                                         Boolean removido,
                                                                         Pageable pageable);

    /**
     * Devuelve una lista los catalogos que coincidan por el nemonico padre y removido
     *
     * @param nemonico String con el nemonico del padre
     * @param removido Boolean inidica si se busca un objeto removido o no.
     * @return List<Catalogo>
     */
    List<Catalogo> findByCatalogoPadre_NemonicoAndRemovido(String nemonico, Boolean removido);


    /**
     * Devuelve una lista los catalogos
     *
     * @param catalogo Catalogo
     * @param removido Boolean inidica si se busca un objeto removido o no.
     * @return List<Catalogo>
     */
    List<Catalogo> findByCatalogoPadreAndRemovidoOrderByIdCatalogo(Catalogo catalogo, Boolean removido);

    /**
     * Devuelve una lista los catalogos
     *
     * @param catalogo Catalogo
     * @param empresa  Empresa
     * @param removido Boolean inidica si se busca un objeto removido o no.
     * @return List<Catalogo>
     */
    List<Catalogo> findByCatalogoPadreAndEmpresaAndRemovidoOrderByNombre(Catalogo catalogo,
                                                                         Empresa empresa, Boolean removido);

    /**
     * Devuelve una lista los catalogos
     *
     * @param tokenIdentificadorPadre String
     * @param empresa                 Empresa
     * @param removido                Boolean inidica si se busca un objeto removido o no.
     * @return List<Catalogo>
     */
    List<Catalogo> findByCatalogoPadreTokenIdentificadorAndEmpresaAndRemovidoOrderByNombre(String tokenIdentificadorPadre,
                                                                                           Empresa empresa, Boolean removido);

    /**
     * Devuelve un page los catalogos hijos
     *
     * @param catalogo Catalogo
     * @param empresa  Empresa
     * @param removido Boolean inidica si se busca un objeto removido o no.
     * @return List<Catalogo>
     */
    Page<Catalogo> findByCatalogoPadreAndEmpresaAndRemovidoOrderByNombre(Catalogo catalogo,
                                                                         Empresa empresa, Boolean removido,
                                                                         Pageable pageable);


    /**
     * Cuenta cuantos catalogos existen relacionados a un padre
     *
     * @param catalogoPadre Catalogo
     * @param empresa       Empresa
     * @param removido      Boolean inidica si se busca un objeto removido o no.
     * @return Long
     */
    Long countByCatalogoPadreAndEmpresaAndRemovido(Catalogo catalogoPadre, Empresa empresa, Boolean removido);

    @Query(value = "SELECT cat.* FROM par_catalogo as cat"
            + " INNER JOIN seg_empresa empresa ON cat.id_empresa = empresa.id_empresa"

            + " WHERE :filtro is null"
            + " or lower(cat.nombre) like %:filtro%"
            + " and empresa.id_empresa = :idEmpresa"
            + " and cat.removido = false"
            + " order by cat.nombre",
            nativeQuery = true
    )
    List<Catalogo> obtenerPorFiltro(
            @Param("filtro") String filtro,
            @Param("idEmpresa") Long idEmpresa
    );


    /**
     * Obten una lista de catlaogo por los filtros
     *
     * @param nemonicoPadre String
     * @param empresa       Empresa
     * @param removido      Boolean inidica si se busca un objeto removido o no.
     * @return List<Catalogo>
     */
    List<Catalogo> findByCatalogoPadreNemonicoAndEmpresaAndRemovidoOrderByNombre(
            String nemonicoPadre, Empresa empresa, Boolean removido
    );
    
    /**
     * Devuelve un objeto catalogo por el nombre y removido
     *
     * @param nombre string nombre del catalogo
     * @param removido           boolean que especifica si esta removido o no.
     * @return Catalogo
     */
    List<Catalogo> findByNombreIgnoreCaseAndRemovido(String nombre, Boolean removido);
}
