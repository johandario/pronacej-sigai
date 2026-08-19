package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionTipoDeDocumento;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichaIdentificacionTipoDeDocumentoRepository extends JpaRepository<FichaIdentificacionTipoDeDocumento, Long> {

    /**
     * Devuelve una lista de FichaIdentificacionTipoDeDocumento por el nemonico de la seccion de la ficha principal
     *
     * @param nemonico String nemonico del tipo de seccion de la ficha principal.
     * @param removido Boolean removido
     *
     * @return List<FichaIdentificacionTipoDeDocumento>
     */
    List<FichaIdentificacionTipoDeDocumento> findBySeccionFichaDeIdentificacionNemonicoAndRemovido(
            String nemonico, Boolean removido
    );

    /**
     * Devuelve una lista de FichaIdentificacionTipoDeDocumento por el nemonico de la seccion de la ficha principal
     *
     * @param nemonico String nemonico del tipo de seccion de la ficha principal.
     * @param removido Boolean removido
     *
     * @return List<FichaIdentificacionTipoDeDocumento>
     */
    List<FichaIdentificacionTipoDeDocumento> findBySeccionFichaDeIdentificacionNemonicoAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
            String nemonico, Boolean removido,
            Boolean removidoSeccionFicha,
            Boolean removidoTipoArchivo
    );

    /**
     * Cuenta cuantos valores existen de una seccion de ficha ppl
     *
     * @param seccionFichaPrincipal Catalogo
     * @param empresa Empresa
     * @param removido Boolean removido
     *
     * @return Long
     */
    Long countBySeccionFichaDeIdentificacionAndEmpresaAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
            Catalogo seccionFichaPrincipal,
            Empresa empresa,
            Boolean removido,
            Boolean removidoSeccionFicha,
            Boolean removidoTipoArchivo
    );


    /**
     * Cuenta cuantos valores existen de una seccion de ficha ppl
     *
     * @param seccionFichaPrincipal Catalogo
     * @param empresa Empresa
     * @param removido Boolean removido
     *
     * @return FichaIdentificacionTipoDeDocumento
     */
    FichaIdentificacionTipoDeDocumento findFirstBySeccionFichaDeIdentificacionAndEmpresaAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
            Catalogo seccionFichaPrincipal,
            Empresa empresa,
            Boolean removido,
            Boolean removidoSeccionFicha,
            Boolean removidoTipoArchivo
    );

    /**
     * Cuenta cuantos valores existen de una seccion de ficha ppl
     *
     * @param seccionFichaPrincipal Catalogo
     * @param empresa Empresa
     * @param removido Boolean removido
     *
     * @return FichaIdentificacionTipoDeDocumento
     */
    FichaIdentificacionTipoDeDocumento findFirstBySeccionFichaDeIdentificacionAndTipoArchivoSistemaAndEmpresaAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
            Catalogo seccionFichaPrincipal,
            Catalogo tipoDeDocumento,
            Empresa empresa,
            Boolean removido,
            Boolean removidoSeccionFicha,
            Boolean removidoTipoArchivo
    );


    /**
     * Devuelve una lista de FichaIdentificacionTipoDeDocumento
     *
     * @param tokenSeccionFichaPrincipal String.
     * @param removido Boolean removido
     *
     * @return List<FichaIdentificacionTipoDeDocumento>
     */
    List<FichaIdentificacionTipoDeDocumento> findBySeccionFichaDeIdentificacionTokenIdentificadorAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
            String tokenSeccionFichaPrincipal, Boolean removido,
            Boolean removidoSeccionFicha,
            Boolean removidoTipoArchivo
    );


    /**
     * Devuelve un FichaIdentificacionTipoDeDocumento
     *
     * @param tokenIdentificador String nemonico del tipo de seccion de la ficha principal.
     * @param removido Boolean removido
     *
     * @return List<FichaIdentificacionTipoDeDocumento>
     */
    FichaIdentificacionTipoDeDocumento findByTokenIdentificadorAndRemovidoAndSeccionFichaDeIdentificacionRemovidoAndTipoArchivoSistemaRemovido(
            String tokenIdentificador, Boolean removido,
            Boolean removidoSeccionFicha,
            Boolean removidoTipoArchivo
    );
}
