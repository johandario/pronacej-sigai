package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.DetalleFichaAsistenciaPostEgreso;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaAsistenciaPostEgreso;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DetalleFichaAsistenciaPostEgresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.DetalleFichaAsistenciaPostEgresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaAsistenciaPostEgresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class DetalleFichaAsistenciaPostEgresoServiceImpl implements DetalleFichaAsistenciaPostEgresoService {

    private DetalleFichaAsistenciaPostEgresoRepository detalleFichaAsistenciaPostEgresoRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;
    private FichaAsistenciaPostEgresoRepository fichaAsistenciaPostEgresoRepository;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DetalleFichaAsistenciaPostEgresoDTO>> obtenerDetallesPorFichaAsistencia(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<DetalleFichaAsistenciaPostEgresoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
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
            String bodyDesencriptado = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDesencriptado, PaginacionRequest.class);
            String tokenFichaAsistencia = paginacionRequest.getTokenIdentificador();


            FichaAsistenciaPostEgreso ficha = fichaAsistenciaPostEgresoRepository
                    .findByTokenIdentificadorAndRemovido(tokenFichaAsistencia, false);

            if (ficha == null) {
                df.setMensaje("La ficha de asistencia post egreso no fue encontrada o ya fue eliminada.");
                return df;
            }


            Pageable pageable = PageRequest.of(paginacionRequest.getPage(), paginacionRequest.getSize(), Sort.by("fechaDetalle").descending());


            Page<DetalleFichaAsistenciaPostEgreso> detallesPage = detalleFichaAsistenciaPostEgresoRepository
                    .findByFichaAsistenciaPostEgresoTokenIdentificadorAndRemovido(tokenFichaAsistencia, false, pageable);


            List<DetalleFichaAsistenciaPostEgresoDTO> detallesDTO = detallesPage.stream()
                    .map(this::convertirADTO)
                    .toList();


            PaginacionResponse<DetalleFichaAsistenciaPostEgresoDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(detallesDTO);
            paginacionResponse.setTotalItems(detallesPage.getTotalElements());

            df.llenarRespuestaExitosa("Detalles de la ficha asistencia post egreso obtenidos con éxito", paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<DetalleFichaAsistenciaPostEgresoDTO> crearOEditarDetalleFichaAsistencia(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<DetalleFichaAsistenciaPostEgresoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener usuario autenticado desde el JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            UsuarioSistema usuarioSistema = df2.getData().getUsuarioSistema();

            // Desencriptar el body y obtener los datos del DTO
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();
            DetalleFichaAsistenciaPostEgresoDTO detalleDTO = new Gson().fromJson(bodyDesencriptado, DetalleFichaAsistenciaPostEgresoDTO.class);

            DetalleFichaAsistenciaPostEgreso detalle;

            if (detalleDTO.getEsEdicion()) {
                // Buscar el detalle a editar
                detalle = detalleFichaAsistenciaPostEgresoRepository
                        .findByTokenIdentificadorAndRemovido(detalleDTO.getTokenIdentificador(), false);

                if (detalle == null) {
                    df.setMensaje("El detalle de la ficha asistencia no fue encontrado o ya fue eliminado.");
                    return df;
                }
            } else {
                detalle = new DetalleFichaAsistenciaPostEgreso();
            }

            // Asociar la FichaAsistenciaPostEgreso
            if (!ObjectUtils.isEmpty(detalleDTO.getTokenIdentificadorFichaAsistenciaPostEgreso())) {
                FichaAsistenciaPostEgreso fichaAsistencia = fichaAsistenciaPostEgresoRepository
                        .findByTokenIdentificadorAndRemovido(detalleDTO.getTokenIdentificadorFichaAsistenciaPostEgreso(), false);

                if (fichaAsistencia == null) {
                    df.setMensaje("No se encontró la ficha de asistencia post egreso.");
                    return df;
                }
                detalle.setFichaAsistenciaPostEgreso(fichaAsistencia);
            }

            // Setear los valores si no están vacíos
            if (!ObjectUtils.isEmpty(detalleDTO.getFechaDetalle())) {
                detalle.setFechaDetalle(detalleDTO.getFechaDetalle());
            }

            if (!ObjectUtils.isEmpty(detalleDTO.getDescripcionActividad())) {
                detalle.setDescripcionActividad(detalleDTO.getDescripcionActividad());
            }

            if (!ObjectUtils.isEmpty(detalleDTO.getObservaciones())) {
                detalle.setObservaciones(detalleDTO.getObservaciones());
            }

            if (!ObjectUtils.isEmpty(detalleDTO.getModalidadDeEntrevista())) {
                detalle.setModalidadDeEntrevista(dtoToCatalogo(detalleDTO.getModalidadDeEntrevista()));
            }

            if (!ObjectUtils.isEmpty(detalleDTO.getPersonaEntrevistada())) {
                detalle.setPersonaEntrevistada(dtoToCatalogo(detalleDTO.getPersonaEntrevistada()));
            }

            if (!ObjectUtils.isEmpty(detalleDTO.getMotivo())) {
                detalle.setMotivo(dtoToCatalogo(detalleDTO.getMotivo()));
            }

            // Configurar los datos de auditoría
            if (!detalleDTO.getEsEdicion()) {
                detalle.setUsuarioSistemaCrea(usuarioSistema);
                detalle.setFechaCreacion(new Date());
                detalle.setIpCrea(httpServletRequest.getRemoteAddr());
            } else {
                detalle.setUsuarioSistemaEdita(usuarioSistema);
                detalle.setFechaEdicion(new Date());
                detalle.setIpEdita(httpServletRequest.getRemoteAddr());
            }

            // Guardar en base de datos
            detalleFichaAsistenciaPostEgresoRepository.save(detalle);

            // Llenar DTO de respuesta
            detalleDTO.setTokenIdentificador(detalle.getTokenIdentificador());

            df.llenarRespuestaExitosa(
                    detalleDTO.getEsEdicion() ? "Detalle de ficha asistencia actualizado con éxito" : "Detalle de ficha asistencia creado con éxito",
                    detalleDTO
            );

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarDetalleFichaAsistencia(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener usuario autenticado desde el JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            UsuarioSistema usuarioSistema = df2.getData().getUsuarioSistema();

            // Desencriptar el body y obtener el token del detalle a eliminar
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();
            DetalleFichaAsistenciaPostEgresoDTO detalleDTO = new Gson().fromJson(bodyDesencriptado, DetalleFichaAsistenciaPostEgresoDTO.class);

            // Buscar el detalle por su token
            DetalleFichaAsistenciaPostEgreso detalle = detalleFichaAsistenciaPostEgresoRepository
                    .findByTokenIdentificadorAndRemovido(detalleDTO.getTokenIdentificador(), false);

            if (detalle == null) {
                df.setMensaje("El detalle de ficha asistencia no fue encontrado o ya fue eliminado anteriormente.");
                return df;
            }

            // Marcar como eliminado
            detalle.setRemovido(true);
            detalle.setFechaEliminacion(new Date());
            detalle.setUsuarioSistemaElimina(usuarioSistema);
            detalle.setIpElimina(httpServletRequest.getRemoteAddr());

            // Guardar cambios en la base de datos
            detalleFichaAsistenciaPostEgresoRepository.save(detalle);

            df.llenarRespuestaExitosa("Detalle de ficha asistencia eliminado correctamente.", true);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private DetalleFichaAsistenciaPostEgresoDTO convertirADTO(DetalleFichaAsistenciaPostEgreso detalle) {
        DetalleFichaAsistenciaPostEgresoDTO dto = new DetalleFichaAsistenciaPostEgresoDTO();

        if (!ObjectUtils.isEmpty(detalle.getFechaDetalle())) {
            dto.setFechaDetalle(detalle.getFechaDetalle());
        }

        if (!ObjectUtils.isEmpty(detalle.getDescripcionActividad())) {
            dto.setDescripcionActividad(detalle.getDescripcionActividad());
        }

        if (!ObjectUtils.isEmpty(detalle.getObservaciones())) {
            dto.setObservaciones(detalle.getObservaciones());
        }

        if (!ObjectUtils.isEmpty(detalle.getModalidadDeEntrevista())) {
            dto.setModalidadDeEntrevista(catalogoToDTO(detalle.getModalidadDeEntrevista()));
        }

        if (!ObjectUtils.isEmpty(detalle.getPersonaEntrevistada())) {
            dto.setPersonaEntrevistada(catalogoToDTO(detalle.getPersonaEntrevistada()));
        }

        if (!ObjectUtils.isEmpty(detalle.getMotivo())) {
            dto.setMotivo(catalogoToDTO(detalle.getMotivo()));
        }
        dto.setTokenIdentificador(detalle.getTokenIdentificador());
        dto.setTokenIdentificadorFichaAsistenciaPostEgreso(detalle.getFichaAsistenciaPostEgreso().getTokenIdentificador());

        return dto;
    }

    private CatalogoDTO catalogoToDTO(Catalogo catalogo) {
        if (catalogo == null) {
            return null;
        }

        CatalogoDTO catalogoDTO = new CatalogoDTO();
        catalogoDTO.setNombre(catalogo.getNombre());
        catalogoDTO.setNemonico(catalogo.getNemonico());
        catalogoDTO.setDescripcion(catalogo.getDescripcion());
        catalogoDTO.setTokenIdentificador(catalogo.getTokenIdentificador());
        catalogoDTO.setCodigoExterno(catalogo.getCodigoExterno());

        return catalogoDTO;
    }

    private Catalogo dtoToCatalogo(CatalogoDTO catalogoDTO) {
        if (catalogoDTO == null) {
            return null;
        }

        Catalogo catalogo = this.catalogoRepository.findByTokenIdentificadorAndRemovido(catalogoDTO.getTokenIdentificador(), false);

        return catalogo;
    }
}
