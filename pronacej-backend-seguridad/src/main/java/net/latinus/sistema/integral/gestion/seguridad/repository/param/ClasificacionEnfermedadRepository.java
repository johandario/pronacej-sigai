package net.latinus.sistema.integral.gestion.seguridad.repository.param;

import net.latinus.sistema.integral.gestion.seguridad.entities.ClasificacionEnfermedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClasificacionEnfermedadRepository extends JpaRepository<ClasificacionEnfermedad, Long> {
    ClasificacionEnfermedad findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    @Query("""
        SELECT c FROM ClasificacionEnfermedad c
        WHERE c.removido = false
            AND (
                (:valor IS NULL OR trim(:valor) = "") OR
                LOWER(c.nombre) LIKE LOWER(CONCAT('%', :valor, '%')) OR
                LOWER(c.codigo) LIKE LOWER(CONCAT('%', :valor, '%'))
   
            )
            AND (
                (:sexo IS NULL OR trim(:sexo) = "") OR
                (:sexo = 'MASCULINO' AND c.aplicaHombre = true) OR
                (:sexo = 'FEMENINO' AND c.aplicaMujer = true)
            )
        ORDER BY c.codigo
        LIMIT 100
        """)
    List<ClasificacionEnfermedad> obtenerClasificacionEnfermedades(@Param("valor") String valor, @Param("sexo") String sexo);

}
