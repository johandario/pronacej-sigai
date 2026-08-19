package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.encuesta.Encabezado;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.SeguimientoConductual;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.SeguimientoPsicologico;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.SeguimientoConductualDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.SeguimientoPsicologicoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.encuesta.EncabezadoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.SeguimientoConductualRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.SeguimientoPsicologicoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class SeguimientoServiceImpl implements SeguimientoService {

    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private CatalogoRepository catalogoRepository;
    private EncabezadoRepository encabezadoRepository;
    private SeguimientoPsicologicoRepository psicologicoRepository;
    private SeguimientoConductualRepository conductualRepository;
    private PaginacionService paginacionService;
    private JerarquiaRepository jerarquiaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoPsicologicoDTO>> obtenerSeguimientosPsicologicos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoPsicologicoDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            var listaPsicologicos = psicologicoRepository.findByEvaluacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false);

            List<SeguimientoPsicologicoDTO> psicologicoDTOList = new ArrayList<>();
            for (SeguimientoPsicologico psicologico : listaPsicologicos) {

                SeguimientoPsicologicoDTO psicologicoDTO = new SeguimientoPsicologicoDTO();
                psicologicoDTO.setIdSeguimientoPsicologico(psicologico.getIdSeguimientoPsicologico());
                psicologicoDTO.setTokenEvaluacion(psicologico.getEvaluacion().getTokenIdentificador());
                psicologicoDTO.setIntervencionConcejeria(psicologico.getIntervencionConcejeria());
                psicologicoDTO.setAccionesRealizar(psicologico.getAccionesRealizar());
                psicologicoDTO.setComentariosObservaciones(psicologico.getComentariosObservaciones());

                psicologicoDTO.setPrograma(entidadADtoJerarquia(psicologico.getPrograma()));
                psicologicoDTO.setAmbiente(entidadADtoJerarquia(psicologico.getAmbiente()));

                psicologicoDTO.setFechaCreacion(psicologico.getFechaCreacion());
                psicologicoDTO.setNombreUsuarioCrea(psicologico.getUsuarioSistemaCrea().getNombres() + " " + psicologico.getUsuarioSistemaCrea().getApellidos());

                psicologicoDTOList.add(psicologicoDTO);
            }

            psicologicoDTOList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            PaginacionResponse<SeguimientoPsicologicoDTO> paginacionResponse = paginacionService.obtenerDatos(psicologicoDTOList, paginacionRequest);

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + listaPsicologicos.size() + " seguimientos psicológicos";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + listaPsicologicos.size() + " seguimientos psicológicos registrados";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> crearSeguimientoPsicologico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

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
            SeguimientoPsicologicoDTO psicologicoDTO = new Gson().fromJson(body, SeguimientoPsicologicoDTO.class);

            SeguimientoPsicologico seguimientoPsicologico = new SeguimientoPsicologico();

            Encabezado encabezado = encabezadoRepository.findByTokenIdentificadorAndRemovido(psicologicoDTO.getTokenEvaluacion(), false);
            seguimientoPsicologico.setEvaluacion(encabezado);

            seguimientoPsicologico.setIntervencionConcejeria(psicologicoDTO.getIntervencionConcejeria());
            seguimientoPsicologico.setAccionesRealizar(psicologicoDTO.getAccionesRealizar());
            seguimientoPsicologico.setComentariosObservaciones(psicologicoDTO.getComentariosObservaciones());

            seguimientoPsicologico.setPrograma(dtoAEntidadJerarquia(psicologicoDTO.getPrograma()));
            seguimientoPsicologico.setAmbiente(dtoAEntidadJerarquia(psicologicoDTO.getAmbiente()));

            seguimientoPsicologico.setIpCrea(httpServletRequest.getRemoteAddr());
            seguimientoPsicologico.setUsuarioSistemaCrea(usuarioSistema);

            psicologicoRepository.save(seguimientoPsicologico);

            // Obtener nombres completos y DNI para los mensajes
            String nombresCompletos = obtenerNombresCompletos(encabezado);
            String dniPersona = obtenerDniPersona(encabezado);

            // Mensaje para el usuario
            String mensajeUsuario = "Se creó con éxito el seguimiento psicológico de " + nombresCompletos;

            // Mensaje para auditoría con información adicional
            String mensajeAuditoria = "Se creó con éxito el seguimiento psicológico de " + usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos();
            if (dniPersona != null && !dniPersona.isEmpty() && !"N/A".equals(dniPersona)) {
                mensajeAuditoria += " para la persona con DNI: " + dniPersona;
            }

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> actualizarSeguimientoPsicologico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

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
            SeguimientoPsicologicoDTO psicologicoDTO = new Gson().fromJson(body, SeguimientoPsicologicoDTO.class);

            SeguimientoPsicologico seguimientoPsicologico = psicologicoRepository.findByIdSeguimientoPsicologicoAndRemovido(psicologicoDTO.getIdSeguimientoPsicologico(), false);

            if (seguimientoPsicologico == null) {
                respuesta.setMensaje("No se encontró el seguimiento psicológico.");
                return respuesta;
            }

            seguimientoPsicologico.setIntervencionConcejeria(psicologicoDTO.getIntervencionConcejeria());
            seguimientoPsicologico.setAccionesRealizar(psicologicoDTO.getAccionesRealizar());
            seguimientoPsicologico.setComentariosObservaciones(psicologicoDTO.getComentariosObservaciones());

            seguimientoPsicologico.setPrograma(dtoAEntidadJerarquia(psicologicoDTO.getPrograma()));
            seguimientoPsicologico.setAmbiente(dtoAEntidadJerarquia(psicologicoDTO.getAmbiente()));

            seguimientoPsicologico.setIpEdita(httpServletRequest.getRemoteAddr());
            seguimientoPsicologico.setUsuarioSistemaEdita(usuarioSistema);

            psicologicoRepository.save(seguimientoPsicologico);

            // Obtener nombres completos y DNI para los mensajes
            String nombresCompletos = obtenerNombresCompletos(seguimientoPsicologico.getEvaluacion());
            String dniPersona = obtenerDniPersona(seguimientoPsicologico.getEvaluacion());

            // Mensaje para el usuario
            String mensajeUsuario = "Se actualizó con éxito el seguimiento psicológico de " + nombresCompletos;

            // Mensaje para auditoría con información adicional
            String mensajeAuditoria = "Se actualizó con éxito el seguimiento psicológico de " + usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos();
            if (dniPersona != null && !dniPersona.isEmpty() && !"N/A".equals(dniPersona)) {
                mensajeAuditoria += " para la persona con DNI: " + dniPersona;
            }

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarSeguimientoPsicologico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

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
            SeguimientoPsicologicoDTO psicologicoDTO = new Gson().fromJson(body, SeguimientoPsicologicoDTO.class);

            SeguimientoPsicologico seguimientoPsicologico = psicologicoRepository.findByIdSeguimientoPsicologicoAndRemovido(psicologicoDTO.getIdSeguimientoPsicologico(), false);

            if (seguimientoPsicologico == null) {
                respuesta.setMensaje("No se encontró el seguimiento psicológico.");
                return respuesta;
            }

            // Obtener nombres completos y DNI antes de eliminar
            String nombresCompletos = obtenerNombresCompletos(seguimientoPsicologico.getEvaluacion());
            String dniPersona = obtenerDniPersona(seguimientoPsicologico.getEvaluacion());

            seguimientoPsicologico.setRemovido(true);
            seguimientoPsicologico.setIpElimina(httpServletRequest.getRemoteAddr());
            seguimientoPsicologico.setUsuarioSistemaElimina(usuarioSistema);

            psicologicoRepository.save(seguimientoPsicologico);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito el seguimiento psicológico de " + nombresCompletos;

            // Mensaje para auditoría con información adicional
            String mensajeAuditoria = "Se eliminó con éxito el seguimiento psicológico de " + usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos();
            if (dniPersona != null && !dniPersona.isEmpty() && !"N/A".equals(dniPersona)) {
                mensajeAuditoria += " para la persona con DNI: " + dniPersona;
            }

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoConductualDTO>> obtenerSeguimientosConductuales(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoConductualDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            var listaConductuales = conductualRepository.findByEvaluacionFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false);

            List<SeguimientoConductualDTO> conductualDTOList = new ArrayList<>();
            for (SeguimientoConductual conductual : listaConductuales) {

                SeguimientoConductualDTO conductualDTO = new SeguimientoConductualDTO();
                conductualDTO.setIdSeguimientoConductual(conductual.getIdSeguimientoConductual());
                conductualDTO.setTokenEvaluacion(conductual.getEvaluacion().getTokenIdentificador());
                conductualDTO.setEstable(conductual.getEstable());
                conductualDTO.setPeriodoDesde(conductual.getPeriodoDesde());
                conductualDTO.setPeriodoHasta(conductual.getPeriodoHasta());

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

                // Formatear las fechas
                String desdeFormatted = dateFormat.format(conductual.getPeriodoDesde());
                String hastaFormatted = dateFormat.format(conductual.getPeriodoHasta());

                conductualDTO.setPeriodo("Del " + desdeFormatted + " hasta el " + hastaFormatted);
                conductualDTO.setNemonicoTipoConducta(conductual.getTipoConducta().getNemonico());
                conductualDTO.setTipoConducta(conductual.getTipoConducta().getNombre());
                conductualDTO.setDescripcionConducta(conductual.getDescripcionConducta());
                conductualDTO.setAccionesAdoptadas(conductual.getAccionesAdoptadas());
                conductualDTO.setFechaCreacion(conductual.getFechaCreacion());
                conductualDTO.setPrograma(entidadADtoJerarquia(conductual.getPrograma()));
                conductualDTO.setAmbiente(entidadADtoJerarquia(conductual.getAmbiente()));

                conductualDTO.setFechaCreacion(conductual.getFechaCreacion());
                conductualDTO.setNombreUsuarioCrea(conductual.getUsuarioSistemaCrea().getNombres() + " " + conductual.getUsuarioSistemaCrea().getApellidos());

                conductualDTOList.add(conductualDTO);
            }

            conductualDTOList.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));

            PaginacionResponse<SeguimientoConductualDTO> paginacionResponse = paginacionService.obtenerDatos(conductualDTOList, paginacionRequest);

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + listaConductuales.size() + " seguimientos conductuales";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + listaConductuales.size() + " seguimientos conductuales registrados";

            respuesta.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> crearSeguimientoConductual(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

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
            SeguimientoConductualDTO conductualDTO = new Gson().fromJson(body, SeguimientoConductualDTO.class);

            SeguimientoConductual seguimientoConductual = new SeguimientoConductual();

            Encabezado encabezado = encabezadoRepository.findByTokenIdentificadorAndRemovido(conductualDTO.getTokenEvaluacion(), false);
            seguimientoConductual.setEvaluacion(encabezado);

            seguimientoConductual.setEstable(conductualDTO.getEstable());
            seguimientoConductual.setPeriodoDesde(conductualDTO.getPeriodoDesde());
            seguimientoConductual.setPeriodoHasta(conductualDTO.getPeriodoHasta());

            Catalogo tipoConducta = catalogoRepository.findByNemonicoAndRemovido(conductualDTO.getNemonicoTipoConducta(), false);
            seguimientoConductual.setTipoConducta(tipoConducta);

            seguimientoConductual.setDescripcionConducta(conductualDTO.getDescripcionConducta());
            seguimientoConductual.setAccionesAdoptadas(conductualDTO.getAccionesAdoptadas());
            seguimientoConductual.setPrograma(dtoAEntidadJerarquia(conductualDTO.getPrograma()));
            seguimientoConductual.setAmbiente(dtoAEntidadJerarquia(conductualDTO.getAmbiente()));

            // CORRECCIÓN: Usar campos de CREACIÓN, no de edición
            seguimientoConductual.setIpCrea(httpServletRequest.getRemoteAddr());
            seguimientoConductual.setUsuarioSistemaCrea(usuarioSistema);

            conductualRepository.save(seguimientoConductual);

            // Obtener nombres completos y DNI para los mensajes
            String nombresCompletos = obtenerNombresCompletos(encabezado);
            String dniPersona = obtenerDniPersona(encabezado);

            // Construir información de fechas para auditoría
            String infoFechas = construirInfoFechas(conductualDTO.getPeriodoDesde(), conductualDTO.getPeriodoHasta());

            // CORRECCIÓN: Mensaje correcto para CREACIÓN
            String mensajeUsuario = "Se creó con éxito el seguimiento conductual de " + nombresCompletos;

            // CORRECCIÓN: Mensaje de auditoría correcto para CREACIÓN
            String mensajeAuditoria = "Se creó con éxito el seguimiento conductual";
            if (!infoFechas.isEmpty()) {
                mensajeAuditoria += " " + infoFechas;
            }
            mensajeAuditoria += " de " + usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos();
            if (dniPersona != null && !dniPersona.isEmpty() && !"N/A".equals(dniPersona)) {
                mensajeAuditoria += " para la persona con DNI: " + dniPersona;
            }

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<Boolean> actualizarSeguimientoConductual(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

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
            SeguimientoConductualDTO conductualDTO = new Gson().fromJson(body, SeguimientoConductualDTO.class);

            SeguimientoConductual seguimientoConductual = conductualRepository.findByIdSeguimientoConductualAndRemovido(conductualDTO.getIdSeguimientoConductual(), false);

            if (seguimientoConductual == null) {
                respuesta.setMensaje("No se encontró el seguimiento conductual.");
                return respuesta;
            }

            seguimientoConductual.setEstable(conductualDTO.getEstable());
            seguimientoConductual.setPeriodoDesde(conductualDTO.getPeriodoDesde());
            seguimientoConductual.setPeriodoHasta(conductualDTO.getPeriodoHasta());

            Catalogo tipoConducta = catalogoRepository.findByNemonicoAndRemovido(conductualDTO.getNemonicoTipoConducta(), false);
            seguimientoConductual.setTipoConducta(tipoConducta);

            seguimientoConductual.setDescripcionConducta(conductualDTO.getDescripcionConducta());
            seguimientoConductual.setAccionesAdoptadas(conductualDTO.getAccionesAdoptadas());
            seguimientoConductual.setPrograma(dtoAEntidadJerarquia(conductualDTO.getPrograma()));
            seguimientoConductual.setAmbiente(dtoAEntidadJerarquia(conductualDTO.getAmbiente()));

            seguimientoConductual.setIpEdita(httpServletRequest.getRemoteAddr());
            seguimientoConductual.setUsuarioSistemaEdita(usuarioSistema);

            conductualRepository.save(seguimientoConductual);

            // Obtener nombres completos y DNI para los mensajes
            String nombresCompletos = obtenerNombresCompletos(seguimientoConductual.getEvaluacion());
            String dniPersona = obtenerDniPersona(seguimientoConductual.getEvaluacion());

            // Construir información de fechas para auditoría
            String infoFechas = construirInfoFechas(conductualDTO.getPeriodoDesde(), conductualDTO.getPeriodoHasta());

            // Mensaje para el usuario
            String mensajeUsuario = "Se actualizó con éxito el seguimiento conductual de " + nombresCompletos;

            // Mensaje para auditoría con información adicional
            String mensajeAuditoria = "Se actualizó con éxito el seguimiento conductual";
            if (!infoFechas.isEmpty()) {
                mensajeAuditoria += " " + infoFechas;
            }
            mensajeAuditoria += " de " + usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos();
            if (dniPersona != null && !dniPersona.isEmpty() && !"N/A".equals(dniPersona)) {
                mensajeAuditoria += " para la persona con DNI: " + dniPersona;
            }

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarSeguimientoConductual(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

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
            SeguimientoConductualDTO conductualDTO = new Gson().fromJson(body, SeguimientoConductualDTO.class);

            SeguimientoConductual seguimientoConductual = conductualRepository.findByIdSeguimientoConductualAndRemovido(conductualDTO.getIdSeguimientoConductual(), false);

            if (seguimientoConductual == null) {
                respuesta.setMensaje("No se encontró el seguimiento conductual.");
                return respuesta;
            }

            // Obtener nombres completos y DNI antes de eliminar
            String nombresCompletos = obtenerNombresCompletos(seguimientoConductual.getEvaluacion());
            String dniPersona = obtenerDniPersona(seguimientoConductual.getEvaluacion());

            // Construir información de fechas para auditoría
            String infoFechas = construirInfoFechas(seguimientoConductual.getPeriodoDesde(), seguimientoConductual.getPeriodoHasta());

            seguimientoConductual.setRemovido(true);
            seguimientoConductual.setIpElimina(httpServletRequest.getRemoteAddr());
            seguimientoConductual.setUsuarioSistemaElimina(usuarioSistema);

            conductualRepository.save(seguimientoConductual);

            // Mensaje para el usuario
            String mensajeUsuario = "Se eliminó con éxito el seguimiento conductual de " + nombresCompletos;

            // Mensaje para auditoría con información adicional
            String mensajeAuditoria = "Se eliminó con éxito el seguimiento conductual";
            if (!infoFechas.isEmpty()) {
                mensajeAuditoria += " " + infoFechas;
            }
            mensajeAuditoria += " de " + usuarioSistema.getNombres() + " " + usuarioSistema.getApellidos();
            if (dniPersona != null && !dniPersona.isEmpty() && !"N/A".equals(dniPersona)) {
                mensajeAuditoria += " para la persona con DNI: " + dniPersona;
            }

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    private Jerarquia dtoAEntidadJerarquia(JerarquiaDTO dto) {
        if (dto == null) return null;
        return this.jerarquiaRepository.findJerarquiaByTokenIdentificador(dto.getTokenIdentificador());
    }

    private static JerarquiaDTO entidadADtoJerarquia(Jerarquia entidad) {
        if (entidad == null) return null;

        JerarquiaDTO dto = new JerarquiaDTO();
        dto.setId(entidad.getIdJerarquia());
        dto.setNombre(entidad.getNombre());
        dto.setNemonico(entidad.getNemonico());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(entidad.getEmpresa().getTokenIdentificador());
        return dto;
    }

    /**
     * Método auxiliar para obtener nombres completos de una evaluación
     */
    private String obtenerNombresCompletos(Encabezado encabezado) {
        if (encabezado == null || encabezado.getFichaIdentificacion() == null) {
            return "N/A";
        }

        var fichaIdentificacion = encabezado.getFichaIdentificacion();
        StringBuilder nombreCompleto = new StringBuilder();
        
        if (fichaIdentificacion.getNombres() != null && !fichaIdentificacion.getNombres().trim().isEmpty()) {
            nombreCompleto.append(fichaIdentificacion.getNombres());
        }
        if (fichaIdentificacion.getApellidoPaterno() != null && !fichaIdentificacion.getApellidoPaterno().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(fichaIdentificacion.getApellidoPaterno());
        }
        if (fichaIdentificacion.getApellidoMaterno() != null && !fichaIdentificacion.getApellidoMaterno().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(fichaIdentificacion.getApellidoMaterno());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener el DNI de una persona desde la evaluación
     */
    private String obtenerDniPersona(Encabezado encabezado) {
        if (encabezado == null || encabezado.getFichaIdentificacion() == null) {
            return "N/A";
        }

        var fichaIdentificacion = encabezado.getFichaIdentificacion();
        String dni = "N/A";
        
        if (fichaIdentificacion.getDni() != null && !fichaIdentificacion.getDni().trim().isEmpty()) {
            dni = fichaIdentificacion.getDni();
        }
        else if (fichaIdentificacion.getNumeroIdentificacion() != null && !fichaIdentificacion.getNumeroIdentificacion().trim().isEmpty()) {
            dni = fichaIdentificacion.getNumeroIdentificacion();
        }

        return dni;
    }

    /**
     * Método auxiliar para construir información de fechas para auditoría
     */
    private String construirInfoFechas(java.util.Date fechaDesde, java.util.Date fechaHasta) {
        if (fechaDesde == null && fechaHasta == null) {
            return "";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder infoFechas = new StringBuilder();

        if (fechaDesde != null && fechaHasta != null) {
            infoFechas.append("del ").append(dateFormat.format(fechaDesde))
                     .append(" al ").append(dateFormat.format(fechaHasta));
        } else if (fechaDesde != null) {
            infoFechas.append("desde el ").append(dateFormat.format(fechaDesde));
        } else if (fechaHasta != null) {
            infoFechas.append("hasta el ").append(dateFormat.format(fechaHasta));
        }

        return infoFechas.toString();
    }
}

