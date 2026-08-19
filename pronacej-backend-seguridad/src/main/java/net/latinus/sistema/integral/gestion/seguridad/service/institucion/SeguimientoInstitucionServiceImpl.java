package net.latinus.sistema.integral.gestion.seguridad.service.institucion;


import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.SeguimientoInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.RegistroInstitucionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.SeguimientoInstitucionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.institucion.SeguimientoInstitucionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class SeguimientoInstitucionServiceImpl implements SeguimientoInstitucionService {
    private SeguimientoInstitucionRepository seguimientoInstitucionRepository;
    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoInstitucionDTO>> obtenerInstituciones(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoInstitucionDTO>> df = new RespuestaPorDefectoAuditoria<>();

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

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idSeguimientoInstitucion").descending()
            );

            Page<SeguimientoInstitucion> seguimientoPage;

            // Filtrado por institución si existe el identificador
            if (paginacionRequest.getTokenIdentificador() != null && !paginacionRequest.getTokenIdentificador().isEmpty()) {
                if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
                    seguimientoPage = this.seguimientoInstitucionRepository.buscarPorInstitucionYFiltro(
                            paginacionRequest.getTokenIdentificador(),
                            paginacionRequest.getFilter(),
                            pageable
                    );
                }
                else {
                    seguimientoPage = this.seguimientoInstitucionRepository.findAllByInstitucionTokenIdentificador(
                            paginacionRequest.getTokenIdentificador(), pageable
                    );
                }
            }
            else if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
                seguimientoPage = this.seguimientoInstitucionRepository.buscarPorFiltro(paginacionRequest.getFilter(), pageable);
            }
            else {
                seguimientoPage = this.seguimientoInstitucionRepository.findByRemovido(false, pageable);
            }


            PaginacionResponse<SeguimientoInstitucionDTO> paginacionResponse = new PaginacionResponse<>();
            List<SeguimientoInstitucionDTO> seguimientoDTOList = new ArrayList<>();
            for (SeguimientoInstitucion seguimiento : seguimientoPage.toList()) {
                SeguimientoInstitucionDTO seguimientoDTO = entidadADto(seguimiento);
                seguimientoDTOList.add(seguimientoDTO);
            }

            paginacionResponse.setData(seguimientoDTOList);
            paginacionResponse.setTotalItems(seguimientoPage.getTotalElements());

            df.llenarRespuestaExitosa(
                    "Se han encontrado un total de: " + seguimientoDTOList.size() + " de: " +
                            seguimientoPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse
            );

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


    @Override
    public RespuestaPorDefectoAuditoria<SeguimientoInstitucionDTO> obtenerRegistroInstitucionPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<SeguimientoInstitucionDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            SeguimientoInstitucion fuga = this.seguimientoInstitucionRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if (fuga == null) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            SeguimientoInstitucionDTO fugaDTO = entidadADto(fuga);
            df.llenarRespuestaExitosa("Se ha encontrado el registro: " + fugaDTO.getTokenIdentificador(), fugaDTO);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<SeguimientoInstitucionDTO> crearRegistroInstitucion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<SeguimientoInstitucionDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            SeguimientoInstitucionDTO fugaEntranteDTO = new Gson().fromJson(bodyDecifrado, SeguimientoInstitucionDTO.class);
            SeguimientoInstitucion fugaEncontrado = this.seguimientoInstitucionRepository.findByTokenIdentificadorAndRemovido(fugaEntranteDTO.getTokenIdentificador(), false);
            if (fugaEncontrado == null && fugaEntranteDTO.getEsEdicion()) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            if (!fugaEntranteDTO.getEsEdicion()) {
                SeguimientoInstitucion fuga = dtoAEntidad(fugaEntranteDTO);
                fuga.setFechaCreacion(new Date());
                fuga.setTokenIdentificador(UUID.randomUUID().toString());
                this.seguimientoInstitucionRepository.save(fuga);
                String mensajeUsuario = "Se creó con éxito el seguimiento de la institución: " + fugaEntranteDTO.getRegistroInstitucion().getNombreOrganizacion();
                String mensajeAuditoria = "Se creó con éxito el seguimiento de la institución: " + fugaEntranteDTO.getRegistroInstitucion().getNombreOrganizacion();
                df.llenarRespuestaExitosa(mensajeUsuario, fugaEntranteDTO, mensajeAuditoria);
            } else {
                SeguimientoInstitucion fuga = dtoAEntidad(fugaEntranteDTO);
                fuga.setFechaEdicion(new Date());
                this.seguimientoInstitucionRepository.save(fuga);
                String mensajeUsuario = "Se editó con éxito el seguimiento de la institución: " + fugaEntranteDTO.getRegistroInstitucion().getNombreOrganizacion();
                String mensajeAuditoria = "Se editó con éxito el seguimiento de la institución: " + fugaEntranteDTO.getRegistroInstitucion().getNombreOrganizacion();
                df.llenarRespuestaExitosa(mensajeUsuario, fugaEntranteDTO, mensajeAuditoria);
            }
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarRegistroInstitucion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            Empresa empresa = df2.getData().getEmpresa();
            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            SeguimientoInstitucionDTO fugaDTO = new Gson().fromJson(bodyString, SeguimientoInstitucionDTO.class);
            SeguimientoInstitucion fuga = this.seguimientoInstitucionRepository.findByTokenIdentificadorAndRemovido(
                    fugaDTO.getTokenIdentificador(), false
            );
            if (fuga == null) {
                df.setMensaje("La gestion de seguimiento no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }
            Date fecha = new Date();
            fuga.setRemovido(true);
            fuga.setIpElimina(ip);
            fuga.setUsuarioSistemaElimina(usuarioSistemaLogin);
            fuga.setFechaEliminacion(fecha);
            this.seguimientoInstitucionRepository.save(fuga);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito el seguimiento"
                    , true);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    @NotNull
    private static SeguimientoInstitucionDTO entidadADto(SeguimientoInstitucion fuga) {
        SeguimientoInstitucionDTO fugaDTO = new SeguimientoInstitucionDTO();
        fugaDTO.setTokenIdentificador(fuga.getTokenIdentificador());
        fugaDTO.setFechaRegistro(fuga.getFechaRegistro());
        fugaDTO.setNumeroDoc(fuga.getNumeroDoc());
        fugaDTO.setEstado(fuga.getEstado());
        fugaDTO.setFecha(fuga.getFecha());
        fugaDTO.setPersonaEntrevistada(fuga.getPersonaEntrevistada());
        fugaDTO.setFortalezas(fuga.getFortalezas());
        fugaDTO.setDebilidades(fuga.getDebilidades());
        fugaDTO.setCumpleObjetivo(fuga.getCumpleObjetivo());
        fugaDTO.setPersonaResponsable(fuga.getPersonaResponsable());
        fugaDTO.setIdSeguimientoInstitucion(fuga.getIdSeguimientoInstitucion());
        if (fuga.getRegistroInstitucion() != null) {
            fugaDTO.setRegistroInstitucion(
                    RegistroInstitucionDTO.entidadADtoRegistro(fuga.getRegistroInstitucion())
            );
        }
        return fugaDTO;
    }

    private static SeguimientoInstitucion dtoAEntidad(SeguimientoInstitucionDTO dto) {
        SeguimientoInstitucion fuga = new SeguimientoInstitucion();
        fuga.setIdSeguimientoInstitucion(dto.getIdSeguimientoInstitucion());
        fuga.setFechaRegistro(dto.getFechaRegistro());
        fuga.setNumeroDoc(dto.getNumeroDoc());
        fuga.setEstado(dto.getEstado());
        fuga.setFecha(dto.getFecha());
        fuga.setPersonaEntrevistada(dto.getPersonaEntrevistada());
        fuga.setFortalezas(dto.getFortalezas());
        fuga.setDebilidades(dto.getDebilidades());
        fuga.setCumpleObjetivo(dto.getCumpleObjetivo());
        fuga.setPersonaResponsable(dto.getPersonaResponsable());
        if (dto.getRegistroInstitucion() != null) {
            fuga.setRegistroInstitucion(
                    RegistroInstitucionDTO.dtoAEntidadRegistro(dto.getRegistroInstitucion())
            );
        }

        return fuga;
    }

    public static RegistroInstitucionDTO entidadADtoRegistro(RegistroInstitucion registro) {
        if (registro == null) {
            return null;
        }
        RegistroInstitucionDTO dto = new RegistroInstitucionDTO();
        dto.setTokenIdentificador(registro.getTokenIdentificador());
        dto.setIdRegistroInstitucion(registro.getIdRegistroInstitucion());
        dto.setNombreOrganizacion(registro.getNombreOrganizacion());
        dto.setNombreDirector(registro.getNombreDirector());
        dto.setRuc(registro.getRuc());
        dto.setNombContactoOperacional(registro.getNombContactoOperacional());
        dto.setDireccion(registro.getDireccion());
        dto.setTelefono(registro.getTelefono());
        dto.setFax(registro.getFax());
        dto.setEmail(registro.getEmail());
        dto.setSitioWeb(registro.getSitioWeb());
        dto.setDni(registro.getDni());
        dto.setMisionInstitucional(registro.getMisionInstitucional());
        dto.setObjetivoInstitucional(registro.getObjetivoInstitucional());
        dto.setDepartamento(registro.getDepartamento());
        dto.setServicios(registro.getServicios());
        dto.setBeneficios(registro.getBeneficios());
        dto.setHorariosServicios(registro.getHorariosServicios());
        dto.setServiciosArticulados(registro.getServiciosArticulados());
        dto.setAreaGeografica(registro.getAreaGeografica());
        dto.setParticipacionEspaciosLocales(registro.getParticipacionEspaciosLocales());
        dto.setOtroSitioWeb(registro.getOtroSitioWeb());
        dto.setTipoInstitucion(registro.getTipoInstitucion());
        dto.setFinalidadInstitucion(registro.getFinalidadInstitucion());
        dto.setCodigoUbigeoUbicacion(registro.getCodigoUbigeoUbicacion());
        return dto;
    }


}
