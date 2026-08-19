package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.Medicamento;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.MedicamentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.MedicamentoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico.MedicamentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class MedicamentoServiceImpl implements MedicamentoService {

    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final LogService logService = new LogService(this.getClass());

    @Override
    public RespuestaPorDefectoAuditoria<List<MedicamentoDTO>> obtenerMedicamentos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<MedicamentoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            MedicamentoRequest medicamentoRequest = new Gson().fromJson(body, MedicamentoRequest.class);

            List<Medicamento> medicamentos = this.medicamentoRepository.obtenerMedicamentosBusqueda(medicamentoRequest.valor());

            List<MedicamentoDTO> listDto = medicamentos.stream().map(Medicamento::convertirADTO).toList();

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
