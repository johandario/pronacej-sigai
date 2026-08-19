package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    /**
     * Devuelve un lista de menu por la opcion de removido y de mostrar en el front
     *
     * @param mostrarEnFront Boolean especifica si el menu se muestra en el front o no.
     * @param removido       boolean que especifica si esta removido o no.
     * @return List<Menu>
     */
    List<Menu> findByMostrarEnElFrontAndRemovidoOrderByOrden(Boolean mostrarEnFront, Boolean removido);

    /**
     * Devuelve un lista de menu por la opcion de removido, de mostrar en el front e id del padre
     *
     * @param mostrarEnFront Boolean especifica si el menu se muestra en el front o no.
     * @param removido       boolean que especifica si esta removido o no.
     * @param idPadre        Long id del menu padre
     * @return List<Menu>
     */
    List<Menu> findByMostrarEnElFrontAndMenuPadreIdMenuAndRemovidoOrderByOrden(Boolean mostrarEnFront, Long idPadre, Boolean removido);

    /**
     * Devuelve un lista de menu por la opcion de removido e id del padre
     *
     * @param removido boolean que especifica si esta removido o no.
     * @return List<Menu>
     * @para idPadre Long id del menu padre
     */
    List<Menu> findByMenuPadreIdMenuAndRemovidoOrderByOrden(Long idPadre, Boolean removido);

    /**
     * Devuelve un menu unico por el token unico de menu
     *
     * @param tokenIdentificador Boolean especifica si el menu se muestra en el front o no.
     * @param removido           boolean que especifica si esta removido o no.
     * @return Menu
     */
    Menu findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);


    /**
     * Devuelve un lista de menu por la opcion de removido y nemonico
     *
     * @param removido boolean que especifica si esta removido o no.
     * @param nemonico String nemonico
     * @return List<Menu>
     */
    List<Menu> findByNemonicoAndRemovidoOrderByOrden(String nemonico, Boolean removido);

    /**
     * Devuelve una page de menu por la opcion de removido y nemonico
     *
     * @param removido                  boolean que especifica si esta removido o no.
     * @param tokenIdentificadorEmpresa String token de la empresa
     * @param nemonico                  String nemonico
     * @return Page<Menu>
     */
    Page<Menu> findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(String nemonico, String tokenIdentificadorEmpresa, Boolean removido,
                                                                     Pageable pageable);


    /**
     * Devuelve un lista de menu ordenados por el id de manera descendente
     *
     * @param tokenIdentificadorEmpresa String Token identificador de la empresa.
     * @param menuPadre                 Menu menu padre del menu.
     * @param removido                  Boolean indica si el menu esta removido o no
     * @return List<Menu>
     */
    List<Menu> findByEmpresaTokenIdentificadorAndMenuPadreAndRemovidoOrderByIdMenuDesc(String tokenIdentificadorEmpresa,
                                                                                       Menu menuPadre, Boolean removido);

    /**
     * Devuelve una lista de todos los menus pertenecientes a una empresa sin importar la jerarquia
     *
     * @param tokenIdentificadorEmpresa String Token identificador de la empresa.
     * @param removido                  Boolean indica si el menu esta removido o no
     * @return List<Menu>
     */
    List<Menu> findByEmpresaTokenIdentificadorAndRemovidoOrderByIdMenuDesc(String tokenIdentificadorEmpresa, Boolean removido);

    /**
     * Devuelve una lista de todos los menus pertenecientes a una empresa sin importar la jerarquia
     *
     * @param tokenIdentificadorEmpresa String Token identificador de la empresa.
     * @param menuPadre                 menu padre
     * @param removido                  Boolean indica si el menu esta removido o no
     * @return List<Menu>
     */
    List<Menu> findByEmpresaTokenIdentificadorAndMenuPadreAndRemovidoOrderByOrden(String tokenIdentificadorEmpresa,
                                                                                  Menu menuPadre, Boolean removido);

    /**
     * Devuelve una lista de todos los menus pertenecientes a una empresa sin importar la jerarquia
     *
     * @param tokenIdentificadorEmpresa String Token identificador de la empresa.
     * @param mostrarEnPermisos         Boolean indica si el menu debe mostrarse en el módulo de permisos
     * @param removido                  Boolean indica si el menu esta removido o no
     * @return List<Menu>
     */
    List<Menu> findByEmpresaTokenIdentificadorAndMostrarEnPermisosAndRemovidoOrderByOrdenAsc(String tokenIdentificadorEmpresa, Boolean mostrarEnPermisos, Boolean removido);

    /**
     * Devuelve una objteto Page por el ide de la empresa, removido y un objeto Pageable
     *
     * @param idEmpresa Long id de la empresa.
     * @param removido  Bolean
     * @param pageable  Pageable Objeto pageable.
     * @return Page<Menu>
     */
    Page<Menu> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);

    /**
     * Devuelve una lista de todos los menus por tipo pertenecientes a una empresa sin importar la jerarquia
     *
     * @param tokenIdentificadorEmpresa String Token identificador de la empresa.
     * @param removido                  Boolean indica si el menu esta removido o no
     * @return List<Menu>
     */
    List<Menu> findByEmpresaTokenIdentificadorAndTipoAndRemovidoOrderByIdMenuDesc(String tokenIdentificadorEmpresa, String tipo, Boolean removido);


    /**
     * Devuelve un menu por el filtro de empresa, nemonico y removido
     *
     * @param tokenIdentificadorEmpresa String Token identificador de la empresa.
     * @param nemonico                  String nemonico del menu
     * @param removido                  Boolean indica si el menu esta removido o no
     * @return Menu
     */
    Menu findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
            String tokenIdentificadorEmpresa, String nemonico, Boolean removido
    );

}
