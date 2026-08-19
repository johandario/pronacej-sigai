package net.latinus.sistema.integral.gestion.seguridad.repository.param;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import net.latinus.sistema.integral.gestion.seguridad.model.both.AlertaDTO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProcedureRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<AlertaDTO> obtenerAlertas(Long centroId, Long empresaId) {

        // Usar SELECT para llamar a la función
        String queryStr = "SELECT * FROM SP_VERIFICAR_ALERTAS(:centroId, :empresaId)";

        // Ejecutar la consulta
        List<Object[]> resultados = entityManager.createNativeQuery(queryStr)
                .setParameter("centroId", centroId)
                .setParameter("empresaId", empresaId)
                .getResultList();

        // Obtener el resultado
        List<AlertaDTO> alertas = new ArrayList<>();

        for (Object[] obj : resultados) {
            alertas.add(new AlertaDTO(
                    ((Number) obj[0]).longValue(),  // id_alerta
                    (String) obj[1],               // descripcion
                    (String) obj[2],               // mensaje
                    (String) obj[3],               // ruta
                    (String) obj[4],               // prioridad
                    (String) obj[5],               // tokenFicha
                    (String) obj[6],               // nombresAdolescente
                    (String) obj[7],               // apellidoPaternoAdolescente
                    (String) obj[8]               // apellidoMaternoAdolescente
            ));
        }

        return alertas;
    }
}
