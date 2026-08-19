package net.latinus.sistema.integral.gestion.seguridad.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MetadataRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // Obtener las tablas que hacen referencia a ia_ficha_identificacion
    public List<String> obtenerTablasQueUsanFicha() {
        String sql = """
            SELECT DISTINCT k.table_name
            FROM information_schema.key_column_usage k
            JOIN information_schema.constraint_column_usage c
            ON k.constraint_name = c.constraint_name
            WHERE c.table_name = 'ia_ficha_identificacion'
        """;

        return entityManager.createNativeQuery(sql).getResultList();
    }

    // Obtener los campos de tipo fecha de una tabla seleccionada
    public List<String> obtenerCamposFecha(String tabla) {
        String sql = """
            SELECT column_name 
            FROM information_schema.columns 
            WHERE table_name = :tabla 
            AND data_type IN ('date', 'timestamp', 'timestamp without time zone', 'timestamp with time zone')
        """;

        return entityManager.createNativeQuery(sql)
                .setParameter("tabla", tabla)
                .getResultList();
    }
}

