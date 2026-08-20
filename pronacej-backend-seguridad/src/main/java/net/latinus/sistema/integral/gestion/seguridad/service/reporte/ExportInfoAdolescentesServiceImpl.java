package net.latinus.sistema.integral.gestion.seguridad.service.reporte;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionEstadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionJobIniciadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ExportacionEstadoRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ExportacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.reporte.ExportInfoAdolescentesService;
import net.latinus.sistema.integral.gestion.seguridad.service.reporte.ExportacionAdolescentesJobService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.stereotype.Service;

@Service
public class ExportInfoAdolescentesServiceImpl
implements ExportInfoAdolescentesService {
    private final ExportacionAdolescentesJobService exportacionAdolescentesJobService;
    private final JwtProviderService jwtProviderService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<ExportacionJobIniciadoDTO> iniciarExportacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria df = new RespuestaPorDefectoAuditoria();
        try {
            RespuestaPorDefectoAuditoria df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(Boolean.valueOf(true));
                return df;
            }
            RespuestaPorDefectoAuditoria df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            ExportacionRequest exportacionRequest = (ExportacionRequest)new Gson().fromJson((String)df22.getData(), ExportacionRequest.class);
            List<String> numerosIdentificacion = this.normalizarLista(exportacionRequest != null ? exportacionRequest.getNumerosIdentificacion() : null);
            if (numerosIdentificacion.isEmpty()) {
                df.setMensaje("Debe enviar al menos un numero de identificacion para exportar.");
                return df;
            }
            List<String> nemonicosSecciones = this.normalizarLista(exportacionRequest != null ? exportacionRequest.getNemonicosSecciones() : null);
            Long idUsuarioSistema = ((BodyJwtValido)df2.getData()).getUsuarioSistema().getIdUsuarioSistema();
            String jobId = this.exportacionAdolescentesJobService.iniciarJob(numerosIdentificacion, nemonicosSecciones, idUsuarioSistema);
            String mensajeUsuario = "Se inici\u00f3 la exportaci\u00f3n de " + numerosIdentificacion.size() + " adolescente(s).";
            String mensajeAuditoria = "Inicio de exportaci\u00f3n de " + numerosIdentificacion.size() + " adolescente(s) con " + nemonicosSecciones.size() + " secci\u00f3n(es).";
            df.llenarRespuestaExitosa(mensajeUsuario, (Object)new ExportacionJobIniciadoDTO(jobId), mensajeAuditoria);
        }
        catch (IllegalStateException e) {
            df.setMensaje(e.getMessage());
        }
        catch (Exception e) {
            df.llenarConDatosDeException(e);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ExportacionEstadoDTO> consultarEstadoExportacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria df = new RespuestaPorDefectoAuditoria();
        try {
            String jobId;
            RespuestaPorDefectoAuditoria df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(Boolean.valueOf(true));
                return df;
            }
            RespuestaPorDefectoAuditoria df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            ExportacionEstadoRequest estadoRequest = (ExportacionEstadoRequest)new Gson().fromJson((String)df22.getData(), ExportacionEstadoRequest.class);
            String string = jobId = estadoRequest != null ? estadoRequest.getJobId() : null;
            if (jobId == null || jobId.isBlank()) {
                df.setMensaje("Debe enviar el identificador del proceso de exportaci\u00f3n.");
                return df;
            }
            Long idUsuarioSistema = ((BodyJwtValido)df2.getData()).getUsuarioSistema().getIdUsuarioSistema();
            ExportacionEstadoDTO estado = this.exportacionAdolescentesJobService.consultarEstado(jobId, idUsuarioSistema);
            df.llenarRespuestaExitosa("Estado de exportaci\u00f3n obtenido correctamente.", (Object)estado);
        }
        catch (NoSuchElementException e) {
            df.setMensaje(e.getMessage());
        }
        catch (Exception e) {
            df.llenarConDatosDeException(e);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<ExportacionEstadoDTO>> listarExportaciones(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria df = new RespuestaPorDefectoAuditoria();
        try {
            RespuestaPorDefectoAuditoria df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(Boolean.valueOf(true));
                return df;
            }
            Long idUsuarioSistema = ((BodyJwtValido)df2.getData()).getUsuarioSistema().getIdUsuarioSistema();
            List<ExportacionEstadoDTO> jobs = this.exportacionAdolescentesJobService.listarJobs(idUsuarioSistema);
            df.llenarRespuestaExitosa("Exportaciones obtenidas correctamente.", jobs);
        }
        catch (Exception e) {
            df.llenarConDatosDeException(e);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Void> cancelarExportacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria df = new RespuestaPorDefectoAuditoria();
        try {
            String jobId;
            RespuestaPorDefectoAuditoria df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(Boolean.valueOf(true));
                return df;
            }
            RespuestaPorDefectoAuditoria df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            ExportacionEstadoRequest estadoRequest = (ExportacionEstadoRequest)new Gson().fromJson((String)df22.getData(), ExportacionEstadoRequest.class);
            String string = jobId = estadoRequest != null ? estadoRequest.getJobId() : null;
            if (jobId == null || jobId.isBlank()) {
                df.setMensaje("Debe enviar el identificador del proceso de exportaci\u00f3n.");
                return df;
            }
            Long idUsuarioSistema = ((BodyJwtValido)df2.getData()).getUsuarioSistema().getIdUsuarioSistema();
            this.exportacionAdolescentesJobService.cancelarJob(jobId, idUsuarioSistema);
            df.llenarRespuestaExitosa("Se solicit\u00f3 la cancelaci\u00f3n de la exportaci\u00f3n.", null);
        }
        catch (IllegalStateException | NoSuchElementException e) {
            df.setMensaje(e.getMessage());
        }
        catch (Exception e) {
            df.llenarConDatosDeException(e);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Void> descartarExportacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria df = new RespuestaPorDefectoAuditoria();
        try {
            String jobId;
            RespuestaPorDefectoAuditoria df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(Boolean.valueOf(true));
                return df;
            }
            RespuestaPorDefectoAuditoria df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            ExportacionEstadoRequest estadoRequest = (ExportacionEstadoRequest)new Gson().fromJson((String)df22.getData(), ExportacionEstadoRequest.class);
            String string = jobId = estadoRequest != null ? estadoRequest.getJobId() : null;
            if (jobId == null || jobId.isBlank()) {
                df.setMensaje("Debe enviar el identificador del proceso de exportaci\u00f3n.");
                return df;
            }
            Long idUsuarioSistema = ((BodyJwtValido)df2.getData()).getUsuarioSistema().getIdUsuarioSistema();
            this.exportacionAdolescentesJobService.descartarJob(jobId, idUsuarioSistema);
            df.llenarRespuestaExitosa("Se descart\u00f3 la exportaci\u00f3n.", null);
        }
        catch (IllegalStateException | NoSuchElementException e) {
            df.setMensaje(e.getMessage());
        }
        catch (Exception e) {
            df.llenarConDatosDeException(e);
        }
        return df;
    }

    private List<String> normalizarLista(List<String> listaEntrada) {
        ArrayList<String> salida = new ArrayList<String>();
        if (listaEntrada == null) {
            return salida;
        }
        for (String valor : listaEntrada) {
            String limpio;
            if (valor == null || (limpio = valor.trim()).isEmpty()) continue;
            salida.add(limpio);
        }
        return salida;
    }

    public ExportInfoAdolescentesServiceImpl(ExportacionAdolescentesJobService exportacionAdolescentesJobService, JwtProviderService jwtProviderService, ParametroDelSistemaRepository parametroDelSistemaRepository) {
        this.exportacionAdolescentesJobService = exportacionAdolescentesJobService;
        this.jwtProviderService = jwtProviderService;
        this.parametroDelSistemaRepository = parametroDelSistemaRepository;
    }
}
