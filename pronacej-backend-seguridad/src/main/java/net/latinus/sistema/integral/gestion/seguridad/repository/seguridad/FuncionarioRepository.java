package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    Funcionario findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    Page<Funcionario> findByRemovido(Boolean removido, Pageable pageable);

    @Query("""
        SELECT f from UsuarioSistema u
        JOIN u.funcionario f
        WHERE u.removido = :removido AND f.removido = :removido
    """)
    List<Funcionario> obtenerPorRemovido(@Param("removido") Boolean removido);
//    Page<Funcionario> findByEmpresaIdEmpresaRemovido(Long idEmpresa, Boolean removido, Pageable pageable);

    List<Funcionario> findByNumeroDeDocumento(String numDocumento);

    List<Funcionario> findByEmailAndRemovido(String email, Boolean removido);

    Funcionario findByNumeroDeDocumentoAndRemovido(String numDocumento, Boolean removido);

    Funcionario findByNumeroDeDocumentoAndRemovidoAndBloqueado(String numDocumento, Boolean removido, Boolean bloqueado);

    @Query("SELECT f FROM Funcionario f WHERE (" +
            "LOWER(f.nombres) LIKE LOWER(CONCAT('%', :param, '%')) OR " +
            "LOWER(f.apellidos) LIKE LOWER(CONCAT('%', :param, '%')) OR " +
            "LOWER(f.email) LIKE LOWER(CONCAT('%', :param, '%')) OR " +
            "LOWER(f.telefono) LIKE LOWER(CONCAT('%', :param, '%')) OR " +
            "LOWER(f.numeroDeCelular) LIKE LOWER(CONCAT('%', :param, '%')) OR " +
            "LOWER(f.numeroDeDocumento) LIKE LOWER(CONCAT('%', :param, '%'))) AND f.removido = false")
    Page<Funcionario> buscarPorValor(@Param("param") String param, Pageable pageable);

    @Query("SELECT f FROM Funcionario f " +
            "JOIN f.cargo cj " +
            "JOIN cj.jerarquia j " +
            "WHERE j.nombre = :nombreJerarquia " +
            "AND (cj.nombre = 'Director/a' OR cj.nombre = 'Director') " +
            "AND cj.esJefe = true " +
            "ORDER BY f.idFuncionario ASC")
    List<Funcionario> findDirectorsByJerarquia(@Param("nombreJerarquia") String nombreJerarquia);

    /**
     * Busca todos los funcionarios que, en la tabla FuncionarioJerarquiaRol,
     * tienen asignada la jerarquía dada y cuyo rol/etiqueta es "Director/a".
     */
    @Query("""
        SELECT DISTINCT f
        FROM Funcionario f
        JOIN f.asignaciones fr
        WHERE fr.jerarquia.idJerarquia       = :idDepartamento
          AND f.cargo.nombre                  = 'Director/a'
          AND f.cargo.removido                 = false
          AND fr.removido                     = false
          AND f.removido                      = false
          AND f.cargo.esJefe                  = true
        """)
    List<Funcionario> findDirectoresByDepartamento(@Param("idDepartamento") Long idDepartamento);

}
