package net.latinus.sistema.integral.gestion.seguridad.repository.param;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;
import net.latinus.sistema.integral.gestion.seguridad.entities.PlantillaFormulario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantillaFormularioRepository extends JpaRepository<PlantillaFormulario, Long> {

    /**
     * Devuelve una plantilla formulario por el nemonico token de empresa y el boolean removido
     *
     * @param tokenEmpresa String token identificador de la empresa.
     * @param nemonico String nemonico del parametro del sistema.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return PlantillaFormulario
     */
    PlantillaFormulario findByNemonicoAndEmpresaTokenIdentificadorAndRemovido(
            String nemonico, String tokenEmpresa, Boolean removido
    );
    
    PlantillaFormulario findByNemonicoAndRemovido(String Nemonico, Boolean removido);

    PlantillaFormulario findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<PlantillaFormulario> findByEmpresaIdEmpresaAndRemovido(Long idEmpresa, Boolean removido);
    
    List<PlantillaFormulario> findByNemonicoAndEmpresaIdEmpresaAndRemovido(String nemonico, Long idEmpresa, Boolean removido);
}
