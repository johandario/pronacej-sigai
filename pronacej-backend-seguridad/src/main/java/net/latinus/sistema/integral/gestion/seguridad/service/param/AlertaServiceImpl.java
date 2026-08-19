package net.latinus.sistema.integral.gestion.seguridad.service.param;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Alerta;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.AlertaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.AlertaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ProcedureRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
@AllArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private PaginacionService paginacionService;
    private AlertaRepository alertaRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private JerarquiaRepository jerarquiaRepository;
    private ProcedureRepository procedureRepository;


    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<AlertaDTO>> obtenerListaAlertas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<AlertaDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setMensajeErrorReal(df2.getMensajeErrorReal());
                respuesta.setLogOut(df2.getLogOut());
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }

            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            var listaAlertas = alertaRepository.findByCentroTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false);

            List<AlertaDTO> alertaDTOList = new ArrayList<>();
            for (Alerta alerta : listaAlertas) {
                AlertaDTO alertaDTO = new AlertaDTO();
                alertaDTO.setIdAlerta(alerta.getIdAlerta());
                alertaDTO.setDescripcion(alerta.getDescripcion());
                alertaDTO.setMensaje(alerta.getMensaje());
                alertaDTO.setRuta(alerta.getRuta());
                alertaDTO.setTabla(alerta.getTabla());
                alertaDTO.setCampo(alerta.getCampo());
                alertaDTO.setPrioridad(alerta.getPrioridad());
                alertaDTO.setUnidadTiempo(alerta.getUnidadTiempo());
                alertaDTO.setTiempo(alerta.getTiempo());
                alertaDTO.setActivo(alerta.getActivo());
                alertaDTO.setTokenCentro(alerta.getCentro().getTokenIdentificador());
                alertaDTO.setNombreCentro(alerta.getCentro().getNombre());
                alertaDTO.setFechaCreacion(alerta.getFechaCreacion());
                alertaDTO.setTokenIdentificador(alerta.getTokenIdentificador());

                alertaDTOList.add(alertaDTO);
            }

            alertaDTOList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            PaginacionResponse<AlertaDTO> paginacionResponse = paginacionService.obtenerDatos(alertaDTOList, paginacionRequest);

            // Obtener datos para los mensajes
            String nombreUsuarioCompleto = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
            String identificacionUsuario = obtenerIdentificacionUsuarioSistema(usuarioSistema);

            // Mensaje para el usuario - mantener formato original pero agregar nombre y DNI
            String mensajeUsuario = "Alertas. Consulta realizada por: " + nombreUsuarioCompleto + " (" + identificacionUsuario + ")";

            // Mensaje para auditoría - mismo formato que Auth
            String mensajeAuditoria = "Se han encontrado un total de " + listaAlertas.size() + " alertas del sistema";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<AlertaDTO>> obtenerAlertas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<List<AlertaDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setMensajeErrorReal(df2.getMensajeErrorReal());
                respuesta.setLogOut(df2.getLogOut());
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }

            String body = df22.getData();
            AlertaDTO alertaDTO = new Gson().fromJson(body, AlertaDTO.class);

            Jerarquia centro = jerarquiaRepository.findByTokenIdentificadorAndRemovido(alertaDTO.getTokenCentro(), false);

            var listaAlertas = procedureRepository.obtenerAlertas(centro.getIdJerarquia(), empresa.getIdEmpresa());

            // Obtener datos para los mensajes
            String nombreUsuarioCompleto = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
            String identificacionUsuario = obtenerIdentificacionUsuarioSistema(usuarioSistema);

            // Mensaje para el usuario - mantener formato original pero agregar nombre y DNI
            String mensajeUsuario = "Alertas del día. Consulta realizada por: " + nombreUsuarioCompleto + " (" + identificacionUsuario + ")";

            // Mensaje para auditoría - mismo formato que Auth
            String mensajeAuditoria = "Se han encontrado un total de " + listaAlertas.size() + " alertas del día";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, listaAlertas, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> crearAlerta(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setMensajeErrorReal(df2.getMensajeErrorReal());
                respuesta.setLogOut(df2.getLogOut());
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }

            String body = df22.getData();
            AlertaDTO alertaDTO = new Gson().fromJson(body, AlertaDTO.class);

            Alerta alerta = new Alerta();
            alerta.setDescripcion(alertaDTO.getDescripcion());
            alerta.setMensaje(alertaDTO.getMensaje());
            alerta.setRuta(alertaDTO.getRuta());
            alerta.setTabla(alertaDTO.getTabla());
            alerta.setCampo(alertaDTO.getCampo());
            alerta.setPrioridad(alertaDTO.getPrioridad());
            alerta.setUnidadTiempo(alertaDTO.getUnidadTiempo());
            alerta.setTiempo(alertaDTO.getTiempo());
            alerta.setActivo(alertaDTO.getActivo());

            FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(alertaDTO.getTokenFichaIdentificacion(), false);

            Jerarquia centro = jerarquiaRepository.findByTokenIdentificadorAndRemovido(alertaDTO.getTokenCentro(), false);
            alerta.setCentro(centro);

            alerta.setEmpresa(empresa);
            alerta.setIpCrea(httpServletRequest.getRemoteAddr());
            alerta.setUsuarioSistemaCrea(usuarioSistema);

            alertaRepository.save(alerta);

            // Obtener datos para los mensajes
            String nombreUsuarioCompleto = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
            String identificacionUsuario = obtenerIdentificacionUsuarioSistema(usuarioSistema);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje para el usuario - mantener formato original pero agregar nombre y DNI
            String mensajeUsuario = "Alerta creada (" + nombreUsuarioCompleto + " - " + identificacionUsuario + ")";

            // Mensaje para auditoría - formato como Auth
            String mensajeAuditoria = "Se creó con éxito la alerta " + alerta.getDescripcion() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioCompleto;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> actualizarAlerta(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setMensajeErrorReal(df2.getMensajeErrorReal());
                respuesta.setLogOut(df2.getLogOut());
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }

            String body = df22.getData();
            AlertaDTO alertaDTO = new Gson().fromJson(body, AlertaDTO.class);

            Alerta alerta = alertaRepository.findByTokenIdentificadorAndRemovido(alertaDTO.getTokenIdentificador(), false);

            alerta.setDescripcion(alertaDTO.getDescripcion());
            alerta.setMensaje(alertaDTO.getMensaje());
            alerta.setRuta(alertaDTO.getRuta());
            alerta.setTabla(alertaDTO.getTabla());
            alerta.setCampo(alertaDTO.getCampo());
            alerta.setPrioridad(alertaDTO.getPrioridad());
            alerta.setUnidadTiempo(alertaDTO.getUnidadTiempo());
            alerta.setTiempo(alertaDTO.getTiempo());
            alerta.setActivo(alertaDTO.getActivo());

            alerta.setIpEdita(httpServletRequest.getRemoteAddr());
            alerta.setUsuarioSistemaEdita(usuarioSistema);

            alertaRepository.save(alerta);

            // Obtener datos para los mensajes
            String nombreUsuarioCompleto = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
            String identificacionUsuario = obtenerIdentificacionUsuarioSistema(usuarioSistema);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje para el usuario - mantener formato original pero agregar nombre y DNI
            String mensajeUsuario = "Alerta actualizada (" + nombreUsuarioCompleto + " - " + identificacionUsuario + ")";

            // Mensaje para auditoría - formato como Auth
            String mensajeAuditoria = "Se editó con éxito la alerta " + alerta.getDescripcion() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioCompleto;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> removerAlerta(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setMensajeErrorReal(df2.getMensajeErrorReal());
                respuesta.setLogOut(df2.getLogOut());
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }

            String body = df22.getData();
            AlertaDTO alertaDTO = new Gson().fromJson(body, AlertaDTO.class);

            Alerta alerta = alertaRepository.findByTokenIdentificadorAndRemovido(alertaDTO.getTokenIdentificador(), false);

            alerta.setRemovido(true);
            alerta.setIpElimina(httpServletRequest.getRemoteAddr());
            alerta.setUsuarioSistemaElimina(usuarioSistema);

            alertaRepository.save(alerta);

            // Obtener datos para los mensajes
            String nombreUsuarioCompleto = obtenerNombreCompletoUsuarioSistema(usuarioSistema);
            String identificacionUsuario = obtenerIdentificacionUsuarioSistema(usuarioSistema);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje para el usuario - mantener formato original pero agregar nombre y DNI
            String mensajeUsuario = "Alerta removida (" + nombreUsuarioCompleto + " - " + identificacionUsuario + ")";

            // Mensaje para auditoría - formato como Auth
            String mensajeAuditoria = "Se eliminó con éxito la alerta " + alerta.getDescripcion() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioCompleto;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    /**
     * Formatea una fecha al español en el formato: "viernes, 30 de mayo del 2025"
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) {
            return "fecha no disponible";
        }

        try {
            // Configurar el locale para español
            Locale localeEspanol = new Locale("es", "ES");

            // Crear el formato personalizado
            SimpleDateFormat formatoCompleto = new SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy", localeEspanol);

            return formatoCompleto.format(fecha);
        } catch (Exception e) {
            // En caso de error, devolver un formato simple
            SimpleDateFormat formatoSimple = new SimpleDateFormat("dd/MM/yyyy");
            return formatoSimple.format(fecha);
        }
    }

    /**
     * Método auxiliar para obtener nombres completos de un UsuarioSistema
     */
    private String obtenerNombreCompletoUsuarioSistema(UsuarioSistema usuario) {
        if (usuario == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (usuario.getNombres() != null && !usuario.getNombres().trim().isEmpty()) {
            nombreCompleto.append(usuario.getNombres());
        }
        if (usuario.getApellidos() != null && !usuario.getApellidos().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(usuario.getApellidos());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener la identificación de un UsuarioSistema
     */
    private String obtenerIdentificacionUsuarioSistema(UsuarioSistema usuario) {
        if (usuario == null) {
            return "N/A";
        }

        String identificacion = "N/A";

        if (usuario.getNumeroDeDocumento() != null && !usuario.getNumeroDeDocumento().trim().isEmpty()) {
            identificacion = usuario.getNumeroDeDocumento();
        }
        else if (usuario.getUserName() != null && !usuario.getUserName().trim().isEmpty()) {
            identificacion = usuario.getUserName();
        }
        else {
            String nombresCompletos = obtenerNombreCompletoUsuarioSistema(usuario);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = nombresCompletos;
            }
        }

        return identificacion;
    }
}