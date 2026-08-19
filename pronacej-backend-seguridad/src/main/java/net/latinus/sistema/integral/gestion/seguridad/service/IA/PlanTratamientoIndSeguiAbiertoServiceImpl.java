package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PlanTratamientoIndSeguiAbiertoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PlanTratamientoIndSeguiDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanTratamientoIndIntervRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanTratamientoIndSeguiAbiertoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class PlanTratamientoIndSeguiAbiertoServiceImpl implements PlanTratamientoIndSeguiAbiertoService {
    private JwtProviderService jwtProviderService;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private PaginacionService paginacionService;

    private PlanTratamientoIndSeguiAbiertoRepository planTratamientoIndSeguiAbiertoRepository;
    private PlanTratamientoIndIntervRepository planTratamientoIndIntervRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndSeguiAbiertoDTO>> obtenerFichasSeguimientoAbierto(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PlanTratamientoIndSeguiAbiertoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDecifrado, PaginacionRequest.class);

            List<PlanTratamientoIndSeguiAbierto> planTratamientoPage = this.planTratamientoIndSeguiAbiertoRepository.findByPlanTratamientoIndIntervTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(),false);

            PaginacionResponse<PlanTratamientoIndSeguiAbiertoDTO> paginacionResponse = new PaginacionResponse<>();
            List<PlanTratamientoIndSeguiAbiertoDTO> planTratamientoIndDTOList = new ArrayList<>();

            for (PlanTratamientoIndSeguiAbierto plan : planTratamientoPage) {
                PlanTratamientoIndSeguiAbiertoDTO planDTO = new PlanTratamientoIndSeguiAbiertoDTO();

                planDTO.setIdPlanTratamientoIndSeguiAbierto(plan.getIdPlanTratamientoIndSeguiAbierto());
                planDTO.setFecha(plan.getFecha());
                planDTO.setHora(plan.getHora());
                planDTO.setDescripcion(plan.getDescripcion());
                planDTO.setTokenIdentificador(plan.getTokenIdentificador());
                /*String pattern = "dd-MM-yyyy";
                DateFormat fecha = new SimpleDateFormat(pattern);
                planDTO.setFecInicio(fecha.format(plan.getFechaInicio()));
                planDTO.setFecFin(fecha.format(plan.getFechaFin()));*/

                planTratamientoIndDTOList.add(planDTO);
            }

            planTratamientoIndDTOList.sort(
                    Comparator.comparing(PlanTratamientoIndSeguiAbiertoDTO::getFecha).reversed()
            );

            paginacionResponse = paginacionService.obtenerDatos(planTratamientoIndDTOList, paginacionRequest);

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + planTratamientoIndDTOList.size() + " de: " + planTratamientoPage.size() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDTO> crearEditarFichaSeguimientoAbierto(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            PlanTratamientoIndSeguiAbiertoDTO planDTO = new Gson().fromJson(bodyDecifrado, PlanTratamientoIndSeguiAbiertoDTO.class);

            PlanTratamientoIndInterv intervencionEncontrada = this.planTratamientoIndIntervRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenPtiInterv(), false);

            if (intervencionEncontrada == null) {
                df.setMensaje("No existe un registro de intervención asociado al registro que quiere crear");
                return df;
            }

            PlanTratamientoIndSeguiAbierto plan = new PlanTratamientoIndSeguiAbierto();
            if (!planDTO.getEsEdicion()) {
                plan.setFecha(planDTO.getFecha());
                plan.setHora(planDTO.getHora());
                plan.setDescripcion(planDTO.getDescripcion());
                plan.setFechaCreacion(new Date());
                plan.setIpCrea(httpServletRequest.getRemoteAddr());
                plan.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                plan.setPlanTratamientoIndInterv(intervencionEncontrada);
                this.planTratamientoIndSeguiAbiertoRepository.save(plan);
                df.llenarRespuestaExitosa("Se ha creado con éxito el registro.", planDTO);
            } else {
                plan = this.planTratamientoIndSeguiAbiertoRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenIdentificador(), false);
                plan.setFecha(planDTO.getFecha());
                plan.setHora(planDTO.getHora());
                plan.setDescripcion(planDTO.getDescripcion());
                plan.setFechaEdicion(new Date());
                plan.setIpEdita(httpServletRequest.getRemoteAddr());
                plan.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
                this.planTratamientoIndSeguiAbiertoRepository.save(plan);
                df.llenarRespuestaExitosa("Se ha editado con éxito el registro.", planDTO);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDTO> eliminarFichaSeguimientoAbierto(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            PlanTratamientoIndSeguiAbiertoDTO planDTO = new Gson().fromJson(bodyDecifrado, PlanTratamientoIndSeguiAbiertoDTO.class);

            PlanTratamientoIndSeguiAbierto seguimientoEncontrado = this.planTratamientoIndSeguiAbiertoRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenIdentificador(), false);

            if (seguimientoEncontrado != null) {
                seguimientoEncontrado.setRemovido(true);
                seguimientoEncontrado.setFechaEliminacion(new Date());
                seguimientoEncontrado.setIpElimina(httpServletRequest.getRemoteAddr());
                seguimientoEncontrado.setUsuarioSistemaElimina(df2.getData().getUsuarioSistema());
                this.planTratamientoIndSeguiAbiertoRepository.save(seguimientoEncontrado);
                df.llenarRespuestaExitosa("Se ha eliminado con éxito el registro.", planDTO);
            } else {
                df.setMensaje("No existe un registro de seguimiento para ser eliminado");
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /*@Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDTO> obtenerFichaSeguimientoAbiertoPorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PlanTratamientoIndSeguiAbiertoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            PlanTratamientoIndSeguiAbiertoDTO planDTO = new Gson().fromJson(bodyDecifrado, PlanTratamientoIndSeguiAbiertoDTO.class);

            PlanTratamientoIndSeguiAbierto seguimientoEncontrado = this.planTratamientoIndSeguiAbiertoRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenIdentificador(), false);

            if (seguimientoEncontrado != null) {

                planDTO.setIdPlanTratamientoIndSeguiAbierto(seguimientoEncontrado.getIdPlanTratamientoIndSeguiAbierto());
                planDTO.setFecha(seguimientoEncontrado.getFecha());
                planDTO.setHora(seguimientoEncontrado.getHora());
                planDTO.setDescripcion(seguimientoEncontrado.getDescripcion());
                df.llenarRespuestaExitosa("Se ha eliminado con éxito el registro.", planDTO);
            } else {
                df.setMensaje("No existe el registro de seguimiento buscado");
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
*/
}
