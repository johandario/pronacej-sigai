package net.latinus.sistema.integral.gestion.seguridad.repository.param;

import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.PlantillaVariable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantillaVariableRepository extends JpaRepository<PlantillaVariable, Long> {

    /**
     * Devuelve una plantilla formulario por el nemonico token de empresa y el boolean removido
     *
     * @param tokenEmpresa String token identificador de la empresa.
     * @param tokenPlantillaFormulario String token identificador de la plantilla formulario.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return List<PlantillaVariable>
     */
    List<PlantillaVariable> findByPlantillaFormularioTokenIdentificadorAndEmpresaTokenIdentificadorAndRemovido(
            String tokenPlantillaFormulario, String tokenEmpresa, Boolean removido
    );

    List<PlantillaVariable> findByPlantillaFormularioIdPlantillaFormularioAndRemovido(
            Long idPlantillaFormulario, Boolean removido
    );
    
    PlantillaVariable findByTokenIdentificadorAndRemovido(
            String tokenIdentificador, Boolean removido
    );
}
