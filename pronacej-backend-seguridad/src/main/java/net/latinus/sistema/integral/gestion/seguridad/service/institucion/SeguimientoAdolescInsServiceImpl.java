package net.latinus.sistema.integral.gestion.seguridad.service.institucion;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.AdolescenteDerivadoInst;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.SeguimientoAdolescInst;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.AdolescenteDerivadoInstDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.RegistroInstitucionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.SeguimientoAdolescInstDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.institucion.SeguimientoAdolescInsRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
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
public class SeguimientoAdolescInsServiceImpl implements SeguimientoAdolescInsService {

    private SeguimientoAdolescInsRepository seguimientoAdolescInsRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;


    private ParametroDelSistemaRepository parametroDelSistemaRepository;


    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoAdolescInstDTO>> obtenerInstituciones(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoAdolescInstDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            // Desencriptar body
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
                    Sort.by("idAdolescenteSeguimiento").descending()
            );

            Page<SeguimientoAdolescInst> seguimientoPage;

            // Filtrado por AdolescenteDerivadoInst si existe el identificador
            if (paginacionRequest.getTokenIdentificador() != null && !paginacionRequest.getTokenIdentificador().isEmpty()) {
                if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
                    seguimientoPage = this.seguimientoAdolescInsRepository.buscarPorAdolescenteYFiltro(
                            paginacionRequest.getTokenIdentificador(),
                            paginacionRequest.getFilter(),
                            pageable
                    );
                } else {
                    seguimientoPage = this.seguimientoAdolescInsRepository.findAllByAdolescenteTokenIdentificador(
                            paginacionRequest.getTokenIdentificador(), pageable
                    );
                }
            } else if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
                seguimientoPage = this.seguimientoAdolescInsRepository.buscarPorFiltro(paginacionRequest.getFilter(), pageable);
            } else {
                seguimientoPage = this.seguimientoAdolescInsRepository.findByRemovido(false, pageable);
            }

            // Mapear a DTO
            PaginacionResponse<SeguimientoAdolescInstDTO> paginacionResponse = new PaginacionResponse<>();
            List<SeguimientoAdolescInstDTO> seguimientoDTOList = new ArrayList<>();
            for (SeguimientoAdolescInst seguimiento : seguimientoPage.toList()) {
                SeguimientoAdolescInstDTO seguimientoDTO = entidadADto(seguimiento);
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
    public RespuestaPorDefectoAuditoria<SeguimientoAdolescInstDTO> obtenerRegistroInstitucionPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<SeguimientoAdolescInstDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            SeguimientoAdolescInst fuga = this.seguimientoAdolescInsRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if (fuga == null) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            SeguimientoAdolescInstDTO fugaDTO = entidadADto(fuga);
            df.llenarRespuestaExitosa("Se ha encontrado el registro: " + fugaDTO.getTokenIdentificador(), fugaDTO);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<SeguimientoAdolescInstDTO> crearRegistroInstitucion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<SeguimientoAdolescInstDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            SeguimientoAdolescInstDTO fugaEntranteDTO = new Gson().fromJson(bodyDecifrado, SeguimientoAdolescInstDTO.class);
            SeguimientoAdolescInst fugaEncontrado = this.seguimientoAdolescInsRepository.findByTokenIdentificadorAndRemovido(fugaEntranteDTO.getTokenIdentificador(), false);
            if (fugaEncontrado == null && fugaEntranteDTO.getEsEdicion()) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            if (!fugaEntranteDTO.getEsEdicion()) {
                SeguimientoAdolescInst fuga = dtoAEntidad(fugaEntranteDTO);
                fuga.setFechaCreacion(new Date());
                fuga.setTokenIdentificador(UUID.randomUUID().toString());
                this.seguimientoAdolescInsRepository.save(fuga);
                df.llenarRespuestaExitosa("Se ha creado con éxito el registro. ", fugaEntranteDTO);
            } else {
                SeguimientoAdolescInst fuga = dtoAEntidad(fugaEntranteDTO);
                fuga.setFechaEdicion(new Date());
                this.seguimientoAdolescInsRepository.save(fuga);
                df.llenarRespuestaExitosa("Se ha editado con éxito el registro. ", fugaEntranteDTO);
            }
        } catch (Exception ex) {
            System.out.println(" ERROR GENERAL: " + ex.getMessage());
            ex.printStackTrace(); // Muestra el error completo en los logs
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
            SeguimientoAdolescInstDTO fugaDTO = new Gson().fromJson(bodyString, SeguimientoAdolescInstDTO.class);
            SeguimientoAdolescInst fuga = this.seguimientoAdolescInsRepository.findByTokenIdentificadorAndRemovido(
                    fugaDTO.getTokenIdentificador(), false
            );
            if (fuga == null) {
                df.setMensaje("La institucion no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }
            Date fecha = new Date();
            fuga.setRemovido(true);
            fuga.setIpElimina(ip);
            fuga.setUsuarioSistemaElimina(usuarioSistemaLogin);
            fuga.setFechaEliminacion(fecha);
            this.seguimientoAdolescInsRepository.save(fuga);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito del sistema"
                    , true);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<SeguimientoAdolescInstDTO>> obtenerTodasLasInstituciones(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<SeguimientoAdolescInstDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> validacionJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!validacionJwt.isExito()) {
                respuesta.setMensaje(validacionJwt.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            // Obtener todas las instituciones no eliminadas
            List<SeguimientoAdolescInst> listaInstituciones = this.seguimientoAdolescInsRepository.findAllByRemovido(false);

            // Convertir las entidades a DTO
            List<SeguimientoAdolescInstDTO> listaInstitucionesDTO = new ArrayList<>();
            for (SeguimientoAdolescInst institucion : listaInstituciones) {
                SeguimientoAdolescInstDTO dto = entidadADto(institucion); // Método de conversión
                listaInstitucionesDTO.add(dto);
            }

            respuesta.llenarRespuestaExitosa("Se encontraron " + listaInstitucionesDTO.size() + " instituciones.", listaInstitucionesDTO);
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }


    @NotNull
    private static SeguimientoAdolescInstDTO entidadADto(SeguimientoAdolescInst fuga) {
        SeguimientoAdolescInstDTO fugaDTO = new SeguimientoAdolescInstDTO();
        fugaDTO.setTokenIdentificador(fuga.getTokenIdentificador());
        fugaDTO.setIdAdolescenteSeguimiento(fuga.getIdAdolescenteSeguimiento());
        fugaDTO.setFechaSeguimiento(fuga.getFechaSeguimiento());
        fugaDTO.setMedioEntrevista(fuga.getMedioEntrevista());
        fugaDTO.setResultadoEntrevista(fuga.getResultadoEntrevista());
        fugaDTO.setObservacion(fuga.getObservacion());
        fugaDTO.setRecomendacion(fuga.getRecomendacion());
        fugaDTO.setRecomendacion(fuga.getRecomendacion());
        fugaDTO.setAdolescenteDerivadoInst(entidadADtoRegistro(fuga.getAdolescenteDerivadoInst()));
        return fugaDTO;
    }

    private SeguimientoAdolescInst dtoAEntidad(SeguimientoAdolescInstDTO dto) {
        SeguimientoAdolescInst fuga = new SeguimientoAdolescInst();
        fuga.setIdAdolescenteSeguimiento(dto.getIdAdolescenteSeguimiento());
        fuga.setFechaSeguimiento(dto.getFechaSeguimiento());
        fuga.setMedioEntrevista(dto.getMedioEntrevista());
        fuga.setObservacion(dto.getObservacion());
        fuga.setResultadoEntrevista(dto.getResultadoEntrevista());
        fuga.setObservacion(dto.getObservacion());
        fuga.setRecomendacion(dto.getRecomendacion());
        fuga.setAdolescenteDerivadoInst(dtoAEntidadRegistro(dto.getAdolescenteDerivadoInst()));
        return fuga;
    }

    public static AdolescenteDerivadoInstDTO entidadADtoRegistro(AdolescenteDerivadoInst registro) {
        if (registro == null) {
            return null;
        }
        AdolescenteDerivadoInstDTO dto = new AdolescenteDerivadoInstDTO();
        dto.setIdAdolescenteDerivado(registro.getIdAdolescenteDerivado());
        dto.setTokenIdentificador(registro.getTokenIdentificador());
        dto.setFechaRegistro(registro.getFechaRegistro());
        dto.setFechaDerivacion(registro.getFechaDerivacion());
        dto.setDepartamento(registro.getDepartamento());
        dto.setTiempoServicio(registro.getTiempoServicio());
        dto.setServicio(registro.getServicio());
        dto.setPersonaResponsable(registro.getPersonaResponsable());
        dto.setEstado(registro.getEstado());
        dto.setInstitucion(entidadADtoInstitucion(registro.getInstitucion()));

        return dto;
    }


    public static AdolescenteDerivadoInst dtoAEntidadRegistro(AdolescenteDerivadoInstDTO dto) {
        if (dto == null) {
            return null;
        }
        AdolescenteDerivadoInst entidad = new AdolescenteDerivadoInst();
        entidad.setIdAdolescenteDerivado(dto.getIdAdolescenteDerivado());
        entidad.setDepartamento(dto.getDepartamento());
        entidad.setFechaRegistro(dto.getFechaRegistro());
        entidad.setFechaDerivacion(dto.getFechaDerivacion());
        entidad.setTiempoServicio(dto.getTiempoServicio());
        entidad.setServicio(dto.getServicio());
        entidad.setPersonaResponsable(dto.getPersonaResponsable());
        entidad.setEstado(dto.getEstado());
        entidad.setInstitucion(dtoAEntidadInstitucion(dto.getInstitucion()));
        return entidad;
    }


    public static RegistroInstitucionDTO entidadADtoInstitucion(RegistroInstitucion registro) {
        if (registro == null) {
            return null;
        }
        RegistroInstitucionDTO dto = new RegistroInstitucionDTO();
        dto.setIdRegistroInstitucion(registro.getIdRegistroInstitucion());
        dto.setTokenIdentificador(registro.getTokenIdentificador());
        dto.setNombreOrganizacion(registro.getNombreOrganizacion());
        dto.setNombreDirector(registro.getNombreDirector());
        dto.setRuc(registro.getRuc());
        dto.setNombContactoOperacional(registro.getNombContactoOperacional());
        dto.setDni(registro.getDni());
        return dto;
    }


    public static RegistroInstitucion dtoAEntidadInstitucion(RegistroInstitucionDTO dto) {
        if (dto == null) {
            return null;
        }
        RegistroInstitucion entidad = new RegistroInstitucion();
        entidad.setIdRegistroInstitucion(dto.getIdRegistroInstitucion());
        entidad.setNombreOrganizacion(dto.getNombreOrganizacion());
        entidad.setNombreDirector(dto.getNombreDirector());
        entidad.setRuc(dto.getRuc());
        entidad.setNombContactoOperacional(dto.getNombContactoOperacional());
        entidad.setDni(dto.getDni());
        return entidad;
    }

}
