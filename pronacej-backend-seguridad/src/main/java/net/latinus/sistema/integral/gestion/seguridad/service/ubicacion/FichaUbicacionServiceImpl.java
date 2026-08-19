package net.latinus.sistema.integral.gestion.seguridad.service.ubicacion;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.ubicacion.FichaUbicacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.ubicacion.UbicacionJerarquia;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ubicacion.FichaUbicacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ubicacion.UbicacionJerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ubicacion.FichaUbicacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ubicacion.UbicacionJerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FichaUbicacionServiceImpl implements FichaUbicacionService {

    private final JwtProviderService jwtProviderService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final FichaUbicacionRepository fichaUbicacionRepository;
    private final FichaIdentificacionRepository fichaIdentificacionRepository;
    private final UbicacionJerarquiaRepository ubicacionJerarquiaRepository;
    private final JerarquiaRepository jerarquiaRepository;
    private final PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FichaUbicacionDTO>> obtenerListaPaginada(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    ) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaUbicacionDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> dfDesencriptado = bodyEncriptado.desencriptarPorEmpresa(
                    this.parametroDelSistemaRepository,
                    null
            );
            if (!dfDesencriptado.isExito()) {
                df.setMensaje(dfDesencriptado.getMensaje());
                return df;
            }

            PaginacionRequest paginacionRequest = new Gson().fromJson(dfDesencriptado.getData(), PaginacionRequest.class);
            if (!StringUtils.hasText(paginacionRequest.getTokenIdentificador())) {
                df.setMensaje("Debe enviar tokenIdentificador de la ficha de identificación");
                return df;
            }

            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = dfJwt.getData().getEmpresa();
            FichaIdentificacion fichaIdentificacion = this.obtenerFichaValida(
                    paginacionRequest.getTokenIdentificador(),
                    empresa,
                    df
            );
            if (fichaIdentificacion == null) {
                return df;
            }

            int pagina = paginacionRequest.getPage() != null ? paginacionRequest.getPage() : 0;
            int tamanio = paginacionRequest.getSize() != null ? paginacionRequest.getSize() : 20;
            Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("fechaIngreso").descending());

            Page<FichaUbicacion> page = this.fichaUbicacionRepository
                    .findByFichaIdentificacionTokenIdentificadorAndRemovido(
                            fichaIdentificacion.getTokenIdentificador(),
                            false,
                            pageable
                    );

            List<FichaUbicacionDTO> data = page.getContent().stream()
                    .map(this::fichaUbicacionToDTO)
                    .map(dto -> {
                        if (dto.getCentro() != null) {
                            dto.setCentroActualTexto(dto.getCentro().getNombre());
                        }

                        if (dto.getUbicacionJerarquia() != null) {
                            dto.setCeldaActualTexto(dto.getUbicacionJerarquia().getNombre());
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            data,
                            paginacionRequest.getTokenIdentificador(),
                            dfJwt.getData()
                    );

            PaginacionResponse<FichaUbicacionDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(data);
            paginacionResponse.setTotalItems(page.getTotalElements());

            df.llenarRespuestaExitosa(
                    "Se han obtenido " + data.size() + " ubicaciones de la ficha",
                    paginacionResponse
            );

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaUbicacionDTO> obtenerPorTokenIdentificador(
            HttpServletRequest httpServletRequest,
            String tokenIdentificador
    ) {
        RespuestaPorDefectoAuditoria<FichaUbicacionDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            FichaUbicacion fichaUbicacion = this.fichaUbicacionRepository.findByTokenIdentificadorAndRemovido(
                    tokenIdentificador,
                    false
            );

            if (fichaUbicacion == null || !this.esFichaDeEmpresa(fichaUbicacion.getFichaIdentificacion(), dfJwt.getData().getEmpresa())) {
                df.setMensaje("No se encontró la ficha ubicación con el token especificado");
                return df;
            }

            df.llenarRespuestaExitosa(
                    "Se obtuvo con éxito la ficha ubicación",
                    this.fichaUbicacionToDTO(fichaUbicacion)
            );

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaUbicacionDTO> crearEditar(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    ) {
        RespuestaPorDefectoAuditoria<FichaUbicacionDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> dfDesencriptado = bodyEncriptado.desencriptarPorEmpresa(
                    this.parametroDelSistemaRepository,
                    null
            );
            if (!dfDesencriptado.isExito()) {
                df.setMensaje(dfDesencriptado.getMensaje());
                return df;
            }

            FichaUbicacionDTO fichaUbicacionDTO = new Gson().fromJson(dfDesencriptado.getData(), FichaUbicacionDTO.class);
            if (!StringUtils.hasText(fichaUbicacionDTO.getTokenIdentificadorFichaIdentificacion())) {
                df.setMensaje("Debe enviar tokenIdentificadorFichaIdentificacion");
                return df;
            }

            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = dfJwt.getData().getEmpresa();
            FichaIdentificacion fichaIdentificacion = this.obtenerFichaValida(
                    fichaUbicacionDTO.getTokenIdentificadorFichaIdentificacion(),
                    empresa,
                    df
            );
            if (fichaIdentificacion == null) {
                return df;
            }

            FichaUbicacion fichaUbicacion;
            boolean esEdicion = StringUtils.hasText(fichaUbicacionDTO.getTokenIdentificador());
            String ip = httpServletRequest.getRemoteAddr();

            if (esEdicion) {
                fichaUbicacion = this.fichaUbicacionRepository.findByTokenIdentificadorAndRemovido(
                        fichaUbicacionDTO.getTokenIdentificador(),
                        false
                );
                if (fichaUbicacion == null || !this.esFichaDeEmpresa(fichaUbicacion.getFichaIdentificacion(), empresa)) {
                    df.setMensaje("No se encontró la ficha ubicación a editar");
                    return df;
                }
                fichaUbicacion.setIpEdita(ip);
                fichaUbicacion.setUsuarioSistemaEdita(dfJwt.getData().getUsuarioSistema());
                fichaUbicacion.setFechaEdicion(new Date());
            } else {
                fichaUbicacion = new FichaUbicacion();
                fichaUbicacion.setIpCrea(ip);
                fichaUbicacion.setUsuarioSistemaCrea(dfJwt.getData().getUsuarioSistema());
                fichaUbicacion.setFechaCreacion(new Date());
            }

            UbicacionJerarquia ubicacionJerarquia = this.obtenerUbicacionJerarquiaValida(fichaUbicacionDTO, df);
            if (ubicacionJerarquia == null) {
                return df;
            }

            Jerarquia centro = this.obtenerCentroValido(fichaUbicacionDTO, df);
            if (centro == null) {
                return df;
            }

            fichaUbicacion.setFichaIdentificacion(fichaIdentificacion);
            fichaUbicacion.setUbicacionJerarquia(ubicacionJerarquia);
            fichaUbicacion.setCentro(centro);
            fichaUbicacion.setFechaIngreso(fichaUbicacionDTO.getFechaIngreso());
            fichaUbicacion.setUbicacionActual(Boolean.TRUE.equals(fichaUbicacionDTO.getUbicacionActual()));
            fichaUbicacion.setAtencionPrioritaria(Boolean.TRUE.equals(fichaUbicacionDTO.getAtencionPrioritaria()));
            fichaUbicacion.setIngresoExpediente(Boolean.TRUE.equals(fichaUbicacionDTO.getIngresoExpediente()));
            fichaUbicacion.setNumeroCama(fichaUbicacionDTO.getNumeroCama());
            fichaUbicacion.setObservaciones(fichaUbicacionDTO.getObservaciones());

            if (Boolean.TRUE.equals(fichaUbicacion.getUbicacionActual())) {
                this.desactivarUbicacionesActualesDeLaFicha(
                        fichaIdentificacion.getTokenIdentificador(),
                        fichaUbicacion.getTokenIdentificador(),
                        ip
                );
            }

            fichaUbicacion = this.fichaUbicacionRepository.save(fichaUbicacion);

            String mensaje = esEdicion
                    ? "Se ha editado con éxito la ficha ubicación"
                    : "Se ha creado con éxito la ficha ubicación";

            df.llenarRespuestaExitosa(mensaje, this.fichaUbicacionToDTO(fichaUbicacion));

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaUbicacionDTO> eliminar(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado
    ) {
        RespuestaPorDefectoAuditoria<FichaUbicacionDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<String> dfDesencriptado = bodyEncriptado.desencriptarPorEmpresa(
                    this.parametroDelSistemaRepository,
                    null
            );
            if (!dfDesencriptado.isExito()) {
                df.setMensaje(dfDesencriptado.getMensaje());
                return df;
            }

            FichaUbicacionDTO fichaUbicacionDTO = new Gson().fromJson(dfDesencriptado.getData(), FichaUbicacionDTO.class);

            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setLogOut(true);
                return df;
            }

            FichaUbicacion fichaUbicacion = this.fichaUbicacionRepository.findByTokenIdentificadorAndRemovido(
                    fichaUbicacionDTO.getTokenIdentificador(),
                    false
            );

            if (fichaUbicacion == null || !this.esFichaDeEmpresa(fichaUbicacion.getFichaIdentificacion(), dfJwt.getData().getEmpresa())) {
                df.setMensaje("No se encontró la ficha ubicación a eliminar");
                return df;
            }

            fichaUbicacion.setRemovido(true);
            fichaUbicacion.setUbicacionActual(false);
            fichaUbicacion.setFechaEliminacion(new Date());
            fichaUbicacion.setUsuarioSistemaElimina(dfJwt.getData().getUsuarioSistema());
            fichaUbicacion.setIpEdita(httpServletRequest.getRemoteAddr());

            fichaUbicacion = this.fichaUbicacionRepository.save(fichaUbicacion);

            df.llenarRespuestaExitosa(
                    "Se ha eliminado con éxito la ficha ubicación",
                    this.fichaUbicacionToDTO(fichaUbicacion)
            );

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private void desactivarUbicacionesActualesDeLaFicha(
            String tokenFichaIdentificacion,
            String tokenFichaUbicacionActual,
            String ip
    ) {
        List<FichaUbicacion> ubicacionesActuales = this.fichaUbicacionRepository
                .findByFichaIdentificacionTokenIdentificadorAndRemovidoAndUbicacionActual(
                        tokenFichaIdentificacion,
                        false,
                        true
                );

        for (FichaUbicacion item : ubicacionesActuales) {
            if (!item.getTokenIdentificador().equals(tokenFichaUbicacionActual)) {
                item.setUbicacionActual(false);
                item.setIpEdita(ip);
                this.fichaUbicacionRepository.save(item);
            }
        }
    }

    private FichaIdentificacion obtenerFichaValida(
            String tokenFichaIdentificacion,
            Empresa empresa,
            RespuestaPorDefectoAuditoria<?> df
    ) {
        FichaIdentificacion fichaIdentificacion = this.fichaIdentificacionRepository
                .findByTokenIdentificadorAndRemovido(tokenFichaIdentificacion, false);

        if (fichaIdentificacion == null || !this.esFichaDeEmpresa(fichaIdentificacion, empresa)) {
            df.setMensaje("No se encontró la ficha de identificación especificada");
            return null;
        }

        return fichaIdentificacion;
    }

    private Jerarquia obtenerCentroValido(
            FichaUbicacionDTO fichaUbicacionDTO,
            RespuestaPorDefectoAuditoria<?> df
    ) {
        if (fichaUbicacionDTO.getCentro() == null
                || !StringUtils.hasText(fichaUbicacionDTO.getCentro().getTokenIdentificador())) {
            df.setMensaje("Debe enviar el centro");
            return null;
        }

        Jerarquia centro = this.jerarquiaRepository
                .findByTokenIdentificadorAndRemovido(
                        fichaUbicacionDTO.getCentro().getTokenIdentificador(),
                        false
                );

        if (centro == null) {
            df.setMensaje("No se encontró la ubicación jerárquica especificada");
            return null;
        }

        return centro;
    }

    private UbicacionJerarquia obtenerUbicacionJerarquiaValida(
            FichaUbicacionDTO fichaUbicacionDTO,
            RespuestaPorDefectoAuditoria<?> df
    ) {
        if (fichaUbicacionDTO.getUbicacionJerarquia() == null
                || !StringUtils.hasText(fichaUbicacionDTO.getUbicacionJerarquia().getTokenIdentificador())) {
            df.setMensaje("Debe enviar la ubicación jerárquica");
            return null;
        }

        UbicacionJerarquia ubicacionJerarquia = this.ubicacionJerarquiaRepository
                .findByTokenIdentificadorAndRemovido(
                        fichaUbicacionDTO.getUbicacionJerarquia().getTokenIdentificador(),
                        false
                );

        if (ubicacionJerarquia == null) {
            df.setMensaje("No se encontró la ubicación jerárquica especificada");
            return null;
        }

        return ubicacionJerarquia;
    }

    private boolean esFichaDeEmpresa(FichaIdentificacion fichaIdentificacion, Empresa empresa) {
        if (fichaIdentificacion == null || fichaIdentificacion.getEmpresa() == null || empresa == null) {
            return false;
        }

        return fichaIdentificacion.getEmpresa().getIdEmpresa().equals(empresa.getIdEmpresa());
    }

    private FichaUbicacionDTO fichaUbicacionToDTO(FichaUbicacion fichaUbicacion) {
        FichaUbicacionDTO dto = new FichaUbicacionDTO();
        dto.setTokenIdentificador(fichaUbicacion.getTokenIdentificador());
        dto.setFechaIngreso(fichaUbicacion.getFechaIngreso());
        dto.setUbicacionActual(fichaUbicacion.getUbicacionActual());
        dto.setAtencionPrioritaria(fichaUbicacion.getAtencionPrioritaria());
        dto.setIngresoExpediente(fichaUbicacion.getIngresoExpediente());
        dto.setNumeroCama(fichaUbicacion.getNumeroCama());
        dto.setObservaciones(fichaUbicacion.getObservaciones());

        if (fichaUbicacion.getFichaIdentificacion() != null) {
            dto.setTokenIdentificadorFichaIdentificacion(fichaUbicacion.getFichaIdentificacion().getTokenIdentificador());
            if (fichaUbicacion.getFichaIdentificacion().getEmpresa() != null) {
                dto.setTokenIdentificadorEmpresa(fichaUbicacion.getFichaIdentificacion().getEmpresa().getTokenIdentificador());
            }
        }

        if (fichaUbicacion.getUbicacionJerarquia() != null) {
            UbicacionJerarquiaDTO ubicacionJerarquiaDTO = new UbicacionJerarquiaDTO();
            ubicacionJerarquiaDTO.setTokenIdentificador(fichaUbicacion.getUbicacionJerarquia().getTokenIdentificador());
            ubicacionJerarquiaDTO.setNombre(fichaUbicacion.getUbicacionJerarquia().getNombre());
            dto.setUbicacionJerarquia(ubicacionJerarquiaDTO);
        }

        if (fichaUbicacion.getCentro() != null) {
            JerarquiaDTO centroDTO = new JerarquiaDTO();
            centroDTO.setTokenIdentificador(fichaUbicacion.getCentro().getTokenIdentificador());
            centroDTO.setNombre(fichaUbicacion.getCentro().getNombre());
            dto.setCentro(centroDTO);
        }

        dto.setFechaCreacion(fichaUbicacion.getFechaCreacion());
        return dto;
    }
}

