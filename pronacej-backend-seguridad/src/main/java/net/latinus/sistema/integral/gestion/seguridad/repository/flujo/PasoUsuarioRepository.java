package net.latinus.sistema.integral.gestion.seguridad.repository.flujo;

import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.PasoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasoUsuarioRepository extends JpaRepository<PasoUsuario, Long> {
    List<PasoUsuario> findByPasoTokenIdentificadorAndRemovido(String tokenPaso, Boolean removido);

    PasoUsuario findByTokenIdentificadorAndRemovido(String tokenPasoUsuario, Boolean removido);

    List<PasoUsuario> findByPasoProcesoNemonicoAndPasoOrdenAndRemovido(String nemonicoProceso, Integer numeroPaso, Boolean removido);

    @Query("SELECT pu FROM PasoUsuario pu " +
            "JOIN FETCH pu.usuarioSistema us " +
            "JOIN FETCH pu.paso pa " +
            "WHERE pa.proceso.nemonico = :nemonico " +
            "AND pa.orden = :orden " +
            "AND pu.removido = false")
    List<PasoUsuario> findPasoUsuariosConUsuarioSistema(@Param("nemonico") String nemonico, @Param("orden") int orden);

}
