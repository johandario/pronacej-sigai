package net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica;

import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.AntecedenteFamiliar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AntecedenteFamiliarRepository extends JpaRepository<AntecedenteFamiliar, Long> {

    /**
     * Devuelve una lista de objetos AntecedenteFamiliar por token identificador de le ficha medica y removido
     *
     * @param tokenId token identificador de la ficha medica a la que pertenecen los antecedentes familiares
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return AntecedenteFamiliar antecedente encontrado
     */
    AntecedenteFamiliar findByTokenIdentificadorAndRemovido(String tokenId, Boolean removido);

    /**
     * Devuelve una lista de objetos AntecedenteFamiliar por token identificador de le ficha medica y removido
     *
     * @param tokenId token identificador de la ficha medica a la que pertenecen los antecedentes familiares
     * @param removido boolean que especifica si esta removido o no.
     *
     * @return Page<AntecedenteFamiliar> Page de antecedentes familiares encontrados
     */
    Page<AntecedenteFamiliar> findByFichaIdentificacion_TokenIdentificadorAndRemovido(String tokenId, Boolean removido, Pageable pageable);
}
