package net.latinus.sistema.integral.gestion.seguridad.service.param;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.ClasificacionEnfermedad;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ClasificacionEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ClasificacionEnfermedadRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ClasificacionEnfermedadRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Transactional
@RequiredArgsConstructor
public class ClasificacionEnfermedadServiceImpl implements ClasificacionEnfermedadService {

    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final ClasificacionEnfermedadRepository clasificacionEnfermedadRepository;
    private final LogService logService = new LogService(this.getClass());

    @Override
    public RespuestaPorDefectoAuditoria<List<ClasificacionEnfermedadDTO>> obtenerClasificacionEnfermerdades(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<ClasificacionEnfermedadDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            ClasificacionEnfermedadRequest clasificacionEnfermedadRequest = new Gson().fromJson(body, ClasificacionEnfermedadRequest.class);

            List<ClasificacionEnfermedad> clasificacionEnfermedades = this.clasificacionEnfermedadRepository.obtenerClasificacionEnfermedades(clasificacionEnfermedadRequest.valor(), String.valueOf(clasificacionEnfermedadRequest.sexo()));

            List<ClasificacionEnfermedadDTO> listDto = clasificacionEnfermedades.stream().map(ClasificacionEnfermedad::convertirADTO).toList();

            // Mensaje para el usuario
            String mensajeUsuario = "Catálogos obtenidos con éxito.";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de: " + listDto.size() + " catálogos por nemónico padre";

            df.llenarRespuestaExitosa(mensajeUsuario, listDto, mensajeAuditoria);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }



}
