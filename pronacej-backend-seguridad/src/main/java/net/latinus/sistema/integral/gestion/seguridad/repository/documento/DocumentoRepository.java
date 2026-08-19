package net.latinus.sistema.integral.gestion.seguridad.repository.documento;

import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    /**
     * Devuelve un Documento por el token identificador
     *
     * @param tokenIdentificador String token identificador del objeto.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Documento
     */
    Documento findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    /**
     * Cuanta la cantidad de documentos que contiene una carpeta
     *
     * @param tokenIdentificadorCarpeta String token identificador de la carpeta.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Long
     */
    Long countByCarpetaTokenIdentificadorAndRemovido(String tokenIdentificadorCarpeta, Boolean removido);

    /**
     * Lista los documentos por carpeta
     *
     * @param tokenIdentificadorCarpeta String token identificador de la carpeta.
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return List<Documento>
     */
    List<Documento> findByCarpetaTokenIdentificadorAndRemovido(String tokenIdentificadorCarpeta, Boolean removido);

    Page<Documento> findByCarpetaTokenIdentificadorAndRemovido(
            String tokenIdentificadorCarpeta,
            Boolean removido,
            Pageable pageable
    );
}
