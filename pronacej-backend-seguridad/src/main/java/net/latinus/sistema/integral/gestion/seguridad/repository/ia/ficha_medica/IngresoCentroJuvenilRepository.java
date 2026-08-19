package net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica;

import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.IngresoCentroJuvenil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;



public interface IngresoCentroJuvenilRepository extends JpaRepository<IngresoCentroJuvenil, Long> {

    /**
     * Devuelve una lista de objetos IngresoCentroJuvenil por token identificador de le ficha medica y removido
     *
     * @param tokenId token identificador de la ficha medica a la que pertenecen los ingresos a centros
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return IngresoCentroJuvenil lista de centros juveniles encontrados
     */
    IngresoCentroJuvenil findByTokenIdentificadorAndRemovido(String tokenId, Boolean removido);

    /**
     * Devuelve una lista de objetos IngresoCentroJuvenil por token identificador de le ficha medica y removido
     *
     * @param tokenId token identificador de la ficha medica a la que pertenecen los ingresos a centros
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return List<IngresoCentroJuvenil> lista de centros juveniles encontrados
     */
    Page<IngresoCentroJuvenil> findByFichaIdentificacion_TokenIdentificadorAndRemovido(String tokenId, Boolean removido, Pageable pageable);
}
