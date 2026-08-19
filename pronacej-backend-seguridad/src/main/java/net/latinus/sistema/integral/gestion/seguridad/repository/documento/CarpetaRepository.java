package net.latinus.sistema.integral.gestion.seguridad.repository.documento;

import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarpetaRepository extends JpaRepository<Carpeta, Long> {

    /**
     * Devuelve una carpeta asociada al token de identificador
     *
     * @param tokenIdentificador String token identificador del objeto.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Carpeta
     */
    Carpeta findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    /**
     * Devuelve una carpeta asociada al token de identificador de alfresco
     *
     * @param identificadorAlfresco String token identificador de la carpeta en alfresco.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Carpeta
     */
    Carpeta findByIdentificadorAlfrescoAndRemovido(String identificadorAlfresco, Boolean removido);

    /**
     * Cuenta la cantidad de carpetas hijos
     *
     * @param tokenIdentificadorPadre String token identificador de la carpeta padre.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Long
     */
    Long countByCarpetaPadreTokenIdentificadorAndRemovido(String tokenIdentificadorPadre, Boolean removido);

    /**
     * Lista la cantidad de carpeta hijas
     *
     * @param tokenIdentificadorPadre String token identificador de la carpeta padre.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return List<Carpeta>
     */
    List<Carpeta> findByCarpetaPadreTokenIdentificadorAndRemovidoOrderByNombreCliente(String tokenIdentificadorPadre, Boolean removido);

}
