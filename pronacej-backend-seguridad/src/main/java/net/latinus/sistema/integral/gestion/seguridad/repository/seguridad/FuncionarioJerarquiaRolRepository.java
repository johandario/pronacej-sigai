package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.FuncionarioJerarquiaRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioJerarquiaRolRepository extends JpaRepository<FuncionarioJerarquiaRol, Long> {


    /**
     * Todas las asignaciones de jerarquía+rol para un funcionario dado,
     * buscando por el tokenIdentificador del Funcionario.
     */
    List<FuncionarioJerarquiaRol>
    findByFuncionario_TokenIdentificador(String tokenIdentificadorFuncionario);


    /**
     * Elimina todas las asignaciones de un funcionario (por ejemplo, al editar),
     * buscando por el tokenIdentificador del Funcionario.
     */
    void deleteByFuncionario_TokenIdentificador(String tokenIdentificadorFuncionario);

    /**
     * Todas las asignaciones que involucran una jerarquía específica,
     * buscando por el tokenIdentificador de la Jerarquía.
     */
    List<FuncionarioJerarquiaRol>
    findByJerarquia_TokenIdentificador(String tokenIdentificadorJerarquia);

    /**
     * Todas las asignaciones que involucran un rol específico,
     * buscando por el tokenIdentificador del Rol.
     */
    List<FuncionarioJerarquiaRol>
    findByRol_TokenIdentificador(String tokenIdentificadorRol);

    /**
     * Busca una asignación concreta por funcionario y rol,
     * usando los tokenIdentificador de cada uno.
     */
    List<FuncionarioJerarquiaRol>
    findByFuncionario_TokenIdentificadorAndRol_TokenIdentificadorAndRemovido(
            String tokenIdentificadorFuncionario,
            String tokenIdentificadorRol,
            Boolean removido
    );

    /**
     * Busca una asignación concreta por funcionario, jerarquía y rol,
     * usando los tokenIdentificador de cada uno.
     */
    Optional<FuncionarioJerarquiaRol>
    findByFuncionario_TokenIdentificadorAndJerarquia_TokenIdentificadorAndRol_TokenIdentificador(
            String tokenIdentificadorFuncionario,
            String tokenIdentificadorJerarquia,
            String tokenIdentificadorRol
    );

    Optional<FuncionarioJerarquiaRol>
    findByFuncionario_TokenIdentificadorAndJerarquia_TokenIdentificadorAndRemovidoFalse(
            String tokenIdentificadorFuncionario,
            String tokenIdentificadorJerarquia
    );

    /**
     * Todas las asignaciones no eliminadas de un funcionario.
     */
    List<FuncionarioJerarquiaRol>
    findByFuncionario_TokenIdentificadorAndRemovidoFalse(String tokenIdentificadorFuncionario);

    Optional<FuncionarioJerarquiaRol>
    findByFuncionario_TokenIdentificadorAndJerarquia_TokenIdentificador(
            String tokenIdentificadorFuncionario,
            String tokenIdentificadorJerarquia
    );

    List<FuncionarioJerarquiaRol>
    findByFuncionario_TokenIdentificadorAndRemovidoFalseAndRolIsNotNull(String tokenIdentificadorFuncionario);

    /**
     * Obtener un objeto por el token identificador y removido.
     */
    Optional<FuncionarioJerarquiaRol> findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
