package net.latinus.sistema.integral.gestion.seguridad.repository.param;

import net.latinus.sistema.integral.gestion.seguridad.entities.CorreoTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorreoTemplateRepository extends JpaRepository<CorreoTemplate, Long> {

    /**
     * Devuelve un correo template por el nemonico token de empresa y el boolean removido
     *
     * @param tokenEmpresa String token identificador de la empresa.
     * @param nemonico String nemonico del parametro del sistema.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return CorreoTemplate
     */
    CorreoTemplate findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
            String nemonico, String tokenEmpresa, Boolean removido
    );
}
