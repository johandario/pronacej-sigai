package net.latinus.sistema.integral.gestion.seguridad.repository.ubicacion;

import net.latinus.sistema.integral.gestion.seguridad.entities.ubicacion.UbicacionJerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad UbicacionJerarquia
 * Proporciona acceso a datos de ubicaciones jerárquicas del sistema
 */
@Repository
public interface UbicacionJerarquiaRepository extends JpaRepository<UbicacionJerarquia, Long> {

    /**
     * Obtiene una ubicación jerárquica por su token identificador
     *
     * @param tokenIdentificador String token identificador
     * @param removido boolean que especifica si está removido o no
     * @return UbicacionJerarquia
     */
    UbicacionJerarquia findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    /**
     * Obtiene una ubicación jerárquica por su ID
     *
     * @param idUbicacionJerarquia Long id de la ubicacion
     * @param removido boolean que especifica si está removido o no
     * @return UbicacionJerarquia
     */
    UbicacionJerarquia findByIdUbicacionJerarquiaAndRemovido(Long idUbicacionJerarquia, Boolean removido);

    /**
     * Obtiene una lista paginada de ubicaciones jerárquicas
     *
     * @param removido boolean que especifica si está removido o no
     * @param pageable configuración de paginación
     * @return Page de UbicacionJerarquia
     */
    Page<UbicacionJerarquia> findByRemovido(Boolean removido, Pageable pageable);

    /**
     * Obtiene todas las ubicaciones jerárquicas no removidas
     *
     * @param removido boolean que especifica si está removido o no
     * @return Lista de UbicacionJerarquia
     */
    List<UbicacionJerarquia> findByRemovido(Boolean removido);

    /**
     * Obtiene ubicaciones jerárquicas hijas de una ubicación padre
     *
     * @param ubicacionPadre UbicacionJerarquia padre
     * @param removido boolean que especifica si está removido o no
     * @return Lista de UbicacionJerarquia
     */
    List<UbicacionJerarquia> findByUbicacionJerarquiaPadreAndRemovido(UbicacionJerarquia ubicacionPadre, Boolean removido);

    /**
     * Obtiene ubicaciones jerárquicas hijas por token del padre
     *
     * @param tokenPadre String token del padre
     * @param removido boolean que especifica si está removido o no
     * @return Lista de UbicacionJerarquia
     */
    List<UbicacionJerarquia> findByUbicacionJerarquiaPadre_TokenIdentificadorAndRemovido(String tokenPadre, Boolean removido);

    /**
     * Obtiene ubicaciones jerárquicas hijas de forma paginada
     *
     * @param tokenPadre String token del padre
     * @param removido boolean que especifica si está removido o no
     * @param pageable configuración de paginación
     * @return Page de UbicacionJerarquia
     */
    Page<UbicacionJerarquia> findByUbicacionJerarquiaPadre_TokenIdentificadorAndRemovido(String tokenPadre, Boolean removido, Pageable pageable);

    /**
     * Cuenta cuántas ubicaciones jerárquicas hijas existen
     *
     * @param ubicacionPadre UbicacionJerarquia padre
     * @param removido boolean que especifica si está removido o no
     * @return cantidad de hijos
     */
    Long countByUbicacionJerarquiaPadreAndRemovido(UbicacionJerarquia ubicacionPadre, Boolean removido);

    /**
     * Obtiene ubicaciones jerárquicas por empresa
     *
     * @param empresa Empresa
     * @param removido boolean que especifica si está removido o no
     * @param pageable configuración de paginación
     * @return Page de UbicacionJerarquia
     */
    Page<UbicacionJerarquia> findByEmpresaAndRemovido(Empresa empresa, Boolean removido, Pageable pageable);

    /**
     * Obtiene todas las ubicaciones jerárquicas por empresa
     *
     * @param empresa Empresa
     * @param removido boolean que especifica si está removido o no
     * @return Lista de UbicacionJerarquia
     */
    List<UbicacionJerarquia> findByEmpresaAndRemovido(Empresa empresa, Boolean removido);

    /**
     * Obtiene ubicaciones jerárquicas por jerarquía centro y empresa activas
     *
     * @param tokenIdentificadorCentro token identificador del centro
     * @param empresa Empresa
     * @param removido estado removido
     * @return Lista de UbicacionJerarquia
     */
    List<UbicacionJerarquia> findByJerarquiaCentro_TokenIdentificadorAndEmpresaAndRemovido(
            String tokenIdentificadorCentro,
            Empresa empresa,
            Boolean removido
    );
}


