package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioSistemaRepository extends JpaRepository<UsuarioSistema, Long> {

    List<UsuarioSistema> findByUserNameAndRemovido(String username, Boolean removido);

    List<UsuarioSistema> findByEmailAndRemovido(String email, Boolean removido);

    UsuarioSistema findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    UsuarioSistema findByTokenIdentificador(String tokenIdentificador);

    UsuarioSistema findByNumeroDeDocumentoAndRemovido(String numeroDeDocumento, Boolean removido);
}
