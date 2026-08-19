package net.latinus.sistema.integral.gestion.seguridad.repository.param;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametroDelSistemaRepository extends JpaRepository<ParametroDelSistema, Long> {
    /**
     * Devuelve un objeto page de parametro del sistema
     *
     * @param nemonico String token identificador.
     * @param removido boolean que especifica si esta removido o no.
     * @param idEmpresa boolean que especifica si esta removido o no.
     * @param pageable boolean que especifica si esta removido o no.
     *
     * @return Page<ParametroDelSistema>
     */
    Page<ParametroDelSistema> findByNemonicoAndRemovidoAndEmpresaIdEmpresa(String nemonico, Boolean removido, Long idEmpresa, Pageable pageable);

    /**
     * Devuelve un objeto parametro del sistema
     *
     * @param tokenIdentificador String token identificador.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return ParametroDelSistema
     */
    ParametroDelSistema findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);


    /**
     * Devuelve un objeto parametro del sistema
     *
     * @param idParametroDelSistema Long id parametro del sistema en la db.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return ParametroDelSistema
     */
    ParametroDelSistema findByIdParametroDelSistemaAndRemovido(Long idParametroDelSistema, Boolean removido);

    /**
     * Devuelve un objeto parametro del sistema
     *
     * @param nemonico String nemonico parametro del sistema
     * @param empresa Empresa empresa.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return ParametroDelSistema
     */
    ParametroDelSistema findByNemonicoAndEmpresaAndRemovido(String nemonico, Empresa empresa, Boolean removido);


    /**
     * Devuelve un objeto parametro del sistema
     *
     * @param tokenEmpresa String token identificador de la empresa.
     * @param nemonico String nemonico del parametro del sistema.
     * @param removido boolean que especifica si esta removido o no.
     * @param idEmpresa Long id de la empresa.
     *
     * @return ParametroDelSistema
     */
    ParametroDelSistema findByEmpresaTokenIdentificadorOrEmpresaIdEmpresaAndNemonicoAndRemovido(
            String tokenEmpresa, Long idEmpresa, String nemonico, Boolean removido);


    /**
     * Devuelve un objeto parametro del sistema
     *
     * @param tokenEmpresa String token identificador de la empresa.
     * @param nemonico String nemonico del parametro del sistema.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return ParametroDelSistema
     */
    ParametroDelSistema findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
            String tokenEmpresa, String nemonico, Boolean removido);


    /**
     * Devuelve una lista de parametros del sistema por el token de empresa, nemonico del padre y removido
     *
     * @param tokenEmpresa String token identificador de la empresa.
     * @param nemonicoPadre String nemonico del parametro del sistema padre.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return ParametroDelSistema
     */
    List<ParametroDelSistema> findByEmpresaTokenIdentificadorAndParametroDelSistemaPadreNemonicoAndRemovido(
            String tokenEmpresa, String nemonicoPadre, Boolean removido);


    /**
     * Devuelve una lista de parametros del sistema por el token del padre y removido
     *
     * @param tokenPadre String identificador del parametro del sistema padre.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return ParametroDelSistema
     */
    List<ParametroDelSistema> findByParametroDelSistemaPadreTokenIdentificadorAndRemovido(
            String tokenPadre, Boolean removido);

    /**
     * Devuelve una lista de parametros del sistema por el nemonico del padre y removido
     *
     * @param nemonico String nemonico del padre.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return List<ParametroDelSistema>
     */
    List<ParametroDelSistema> findByParametroDelSistemaPadreNemonicoAndRemovido(
            String nemonico, Boolean removido
    );


    /**
     * Devuelve un page de parametros del sistema por el nemonico y removido
     *
     * @param nemonico String nemonico.
     * @param removido boolean que especifica si esta removido o no.
     * @param pageable Pageable
     *
     * @return List<ParametroDelSistema>
     */
    Page<ParametroDelSistema> findByNemonicoAndRemovido(
            String nemonico, Boolean removido, Pageable pageable
    );
}
