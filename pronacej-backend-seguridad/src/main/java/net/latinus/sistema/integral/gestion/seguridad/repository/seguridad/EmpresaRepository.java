package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    /**
     * Devuelve un objeto empresa por el token identificador y removido
     *
     * @param tokenIdentificador String token identificador.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Empresa
     */
    Empresa findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    /**
     * Devuelve un objeto empresa por el id de la db y removido
     *
     * @param idEmpresa Long id de la empresa.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Empresa
     */
    Empresa findByIdEmpresaAndRemovido(Long idEmpresa, Boolean removido);


    /**
     * Devuelve una lista de empresa totales del sistema
     *
     * @param removido Boolean removido.
     *
     * @return List<Empresa>
     */
    List<Empresa> findByRemovido(Boolean removido);
}
