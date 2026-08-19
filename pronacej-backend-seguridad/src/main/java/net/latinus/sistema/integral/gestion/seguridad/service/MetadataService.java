package net.latinus.sistema.integral.gestion.seguridad.service;

import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.MetadataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MetadataService {

    private MetadataRepository metadataRepository;

    public RespuestaPorDefectoAuditoria<List<String>> obtenerTablasQueUsanFicha() {

        RespuestaPorDefectoAuditoria<List<String>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            List<String> tablas = metadataRepository.obtenerTablasQueUsanFicha();

            respuesta.llenarRespuestaExitosa("Nombres de Tablas", tablas);

        }catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    public RespuestaPorDefectoAuditoria<List<String>> obtenerCamposFecha(String tabla) {

        RespuestaPorDefectoAuditoria<List<String>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            List<String> campos = metadataRepository.obtenerCamposFecha(tabla);

            respuesta.llenarRespuestaExitosa("Nombres de campos", campos);

        }catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }
}
