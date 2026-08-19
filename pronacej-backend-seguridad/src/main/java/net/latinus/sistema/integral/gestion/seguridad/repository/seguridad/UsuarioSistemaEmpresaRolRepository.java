package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistemaEmpresaRol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioSistemaEmpresaRolRepository extends JpaRepository<UsuarioSistemaEmpresaRol, Long> {

    /**
     * Devuelve un objeto por el token identificador y removido
     *
     * @param tokenIdentificador String token identificador.
    ¿     * @param removido Bolean
     *
     * @return UsuarioSistemaEmpresaRol
     */
    UsuarioSistemaEmpresaRol findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);


    /**
     * Devuelve una lista de objetos por el token de empresa, de usuario y removido
     *
     * @param tokenEmpresa String token de empresa.
     * @param tokenUsuario String token del usuario
     * @param removido Bolean
     *
     * @return List<UsuarioSistemaEmpresaRol>
     */
    List<UsuarioSistemaEmpresaRol> findByEmpresaTokenIdentificadorAndUsuarioSistemaTokenIdentificadorAndRemovido(String tokenEmpresa,
                                                                                                                 String tokenUsuario,
                                                                                                                 Boolean removido);

    /**
     * Devuelve una lista de objetos por el id de usuario y removido
     *
     * @param idUsuarioSistema Long id del usuario.
     * @param removido Bolean
     *
     * @return List<UsuarioSistemaEmpresaRol>
     */
    List<UsuarioSistemaEmpresaRol> findByUsuarioSistemaIdUsuarioSistemaAndRemovido(Long idUsuarioSistema, Boolean removido);

    /**
     * Devuelve una objteto Page por el ide de la empresa, removido y un objeto Pageable
     *
     * @param idEmpresa Long id de la empresa.
     * @param removido Bolean
     * @param pageable Pageable Objeto pageable.
     *
     * @return Page<UsuarioSistemaEmpresaRol>
     */
    Page<UsuarioSistemaEmpresaRol> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido, Pageable pageable);

    /**
     * Devuelve una objteto Page por el ide de la empresa, removido y un objeto Pageable
     *
     * @param idEmpresa Long id de la empresa.
     * @param removido Bolean
     * @param removidoUsuario Boolean.
     *
     * @return Page<UsuarioSistemaEmpresaRol>
     */
    List<UsuarioSistemaEmpresaRol> findByEmpresaIdEmpresaAndRemovidoAndUsuarioSistemaRemovido(Long idEmpresa, Boolean removido, Boolean removidoUsuario);

    /**
     * Devuelve una lista de UsuarioSistemaEmpresaRol por el id de la empresa y removido
     *
     * @param idEmpresa Long id de la empresa.
     * @param removido Bolean
     *
     * @return Page<UsuarioSistemaEmpresaRol>
     */
    List<UsuarioSistemaEmpresaRol> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido);


    /**
     * Devuelve una objteto por el token de emprsa, de usuario, de rol y si esta removido o no
     *
     * @param tokenIdentificadorEmpresa string token de empresa.
     * @param tokenIdentificadorUsuario String token del usuario a ser buscado
     * @param tokenIdentificadorRol String token de rol
     * @param removido Bolean
     *
     * @return UsuarioSistemaEmpresaRol
     */
    UsuarioSistemaEmpresaRol findByEmpresaTokenIdentificadorAndUsuarioSistemaTokenIdentificadorAndRolTokenIdentificadorAndRemovido(String tokenIdentificadorEmpresa,
                                                                                                                                   String tokenIdentificadorUsuario,
                                                                                                                                   String tokenIdentificadorRol,
                                                                                                                                   Boolean removido);

    /**
     * Devuelve una lista de objetos por el rol, el token de usuario y si esta o no removido
     *
     * @param roles List<Rol> lista de roles a ser buscados.
     * @param tokenUsuario String token del usuario a ser buscado
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return List<UsuarioSistemaEmpresaRol>
     */
    List<UsuarioSistemaEmpresaRol> findByRolInAndUsuarioSistemaTokenIdentificadorAndRemovido(List<Rol> roles,
                                                                                             String tokenUsuario,
                                                                                             Boolean removido);


    /**
     * Devuelve una lista de objetos por el token de usuario y si esta o no removido
     *
     * @param tokenUsuario String token del usuario a ser buscado
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return List<UsuarioSistemaEmpresaRol>
     */
    List<UsuarioSistemaEmpresaRol> findByUsuarioSistemaTokenIdentificadorAndRemovido(
            String tokenUsuario,
            Boolean removido);
}