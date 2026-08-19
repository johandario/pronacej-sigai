package net.latinus.sistema.integral.gestion.seguridad.service.institucion;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.AdolescenteDerivadoInst;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.AdolescenteDerivadoInstDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.RegistroInstitucionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.institucion.AdolescenteDerivadoInstRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.institucion.RegistroInstitucionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
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
public class AdolescenteDerivadoInstServiceImpl implements AdolescenteDerivadoInstService{
    private AdolescenteDerivadoInstRepository adolescenteDerivadoInstRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;
    private RegistroInstitucionRepository registroInstitucionRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<AdolescenteDerivadoInstDTO>> obtenerInstituciones(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<AdolescenteDerivadoInstDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> validacionJwt = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!validacionJwt.isExito()) {
                respuesta.setMensaje(validacionJwt.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String bodyDecifrado = df22.getData();
            System.out.println("🔍 JSON recibido: " + bodyDecifrado);
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDecifrado, PaginacionRequest.class);
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idAdolescenteDerivado").descending()
            );
            Page<AdolescenteDerivadoInst> institucionesPage;
            if (paginacionRequest.getTokenIdentificador() != null && !paginacionRequest.getTokenIdentificador().isEmpty()) {
                if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
                    System.out.println("🔍 Filtrando por Token y Filter: " + paginacionRequest.getTokenIdentificador() + " | " + paginacionRequest.getFilter());
                    institucionesPage = adolescenteDerivadoInstRepository.buscarPorTokenYFiltro(
                            paginacionRequest.getTokenIdentificador(),
                            paginacionRequest.getFilter(),
                            pageable
                    );
                } else {
                    institucionesPage = adolescenteDerivadoInstRepository.findAllByFichaIdentificacionTokenIdentificadorAndRemovido(
                            paginacionRequest.getTokenIdentificador(),
                            false,
                            pageable
                    );
                }
            } else if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
                institucionesPage = adolescenteDerivadoInstRepository.buscarPorFiltro(paginacionRequest.getFilter(), pageable);
            } else {
                institucionesPage = adolescenteDerivadoInstRepository.findByRemovido(false, pageable);
            }
            PaginacionResponse<AdolescenteDerivadoInstDTO> paginacionResponse = new PaginacionResponse<>();
            List<AdolescenteDerivadoInstDTO> institucionesDTOList = new ArrayList<>();
            for (AdolescenteDerivadoInst institucion : institucionesPage.toList()) {
                institucionesDTOList.add(entidadADto(institucion));
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            institucionesDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            validacionJwt.getData()
                    );

            paginacionResponse.setData(institucionesDTOList);
            paginacionResponse.setTotalItems(institucionesPage.getTotalElements());
            respuesta.llenarRespuestaExitosa("Se han encontrado " + institucionesDTOList.size() + " registros de " + institucionesPage.getTotalElements(), paginacionResponse);
        } catch (Exception ex) {
            ex.printStackTrace();
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }



    @Override
    public RespuestaPorDefectoAuditoria<AdolescenteDerivadoInstDTO> obtenerRegistroInstitucionPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<AdolescenteDerivadoInstDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            AdolescenteDerivadoInst fuga  = this.adolescenteDerivadoInstRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if(fuga == null ){
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            AdolescenteDerivadoInstDTO fugaDTO = entidadADto(fuga);
            df.llenarRespuestaExitosa("Se ha encontrado el registro: " + fugaDTO.getTokenIdentificador(), fugaDTO);
        }
        catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<AdolescenteDerivadoInstDTO> crearRegistroInstitucion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<AdolescenteDerivadoInstDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            AdolescenteDerivadoInstDTO fugaEntranteDTO = new Gson().fromJson(bodyDecifrado, AdolescenteDerivadoInstDTO.class);
            AdolescenteDerivadoInst fugaEncontrado = this.adolescenteDerivadoInstRepository.findByTokenIdentificadorAndRemovido(fugaEntranteDTO.getTokenIdentificador(), false);
            if(fugaEncontrado== null && fugaEntranteDTO.getEsEdicion()){
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            if (!fugaEntranteDTO.getEsEdicion()) {
                AdolescenteDerivadoInst fuga = dtoAEntidad(fugaEntranteDTO,fichaIdentificacionRepository);
                fuga.setFechaCreacion(new Date());
                fuga.setTokenIdentificador(UUID.randomUUID().toString());
                this.adolescenteDerivadoInstRepository.save(fuga);
                df.llenarRespuestaExitosa("Se ha creado con éxito el registro. " , fugaEntranteDTO);
            } else {
                AdolescenteDerivadoInst fuga = dtoAEntidad(fugaEntranteDTO,fichaIdentificacionRepository);
                fuga.setFechaEdicion(new Date());
                this.adolescenteDerivadoInstRepository.save(fuga);
                df.llenarRespuestaExitosa("Se ha editado con éxito el registro. " , fugaEntranteDTO);
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
            AdolescenteDerivadoInstDTO fugaDTO = new Gson().fromJson(bodyString, AdolescenteDerivadoInstDTO.class);
            AdolescenteDerivadoInst fuga = this.adolescenteDerivadoInstRepository.findByTokenIdentificadorAndRemovido(
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
            this.adolescenteDerivadoInstRepository.save(fuga);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito del sistema"
                    , true);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<AdolescenteDerivadoInstDTO>> obtenerTodasLasInstituciones(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<AdolescenteDerivadoInstDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> validacionJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!validacionJwt.isExito()) {
                respuesta.setMensaje(validacionJwt.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }
            List<AdolescenteDerivadoInst> listaInstituciones = this.adolescenteDerivadoInstRepository.findAllByRemovido(false);
            List<AdolescenteDerivadoInstDTO> listaInstitucionesDTO = new ArrayList<>();
            for (AdolescenteDerivadoInst institucion : listaInstituciones) {
                AdolescenteDerivadoInstDTO dto = entidadADto(institucion); // Método de conversión
                listaInstitucionesDTO.add(dto);
            }

            respuesta.llenarRespuestaExitosa("Se encontraron " + listaInstitucionesDTO.size() + " instituciones.", listaInstitucionesDTO);
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }


    @NotNull
    private static AdolescenteDerivadoInstDTO entidadADto(AdolescenteDerivadoInst fuga) {
        AdolescenteDerivadoInstDTO fugaDTO = new AdolescenteDerivadoInstDTO();
        fugaDTO.setTokenIdentificador(fuga.getTokenIdentificador());
        fugaDTO.setIdAdolescenteDerivado(fuga.getIdAdolescenteDerivado());
        fugaDTO.setFechaDerivacion(fuga.getFechaDerivacion());
        fugaDTO.setFechaRegistro(fuga.getFechaRegistro());
        fugaDTO.setDepartamento(fuga.getDepartamento());
        fugaDTO.setTiempoServicio(fuga.getTiempoServicio());
        fugaDTO.setPersonaResponsable(fuga.getPersonaResponsable());
        fugaDTO.setPersonaResponsable(fuga.getPersonaResponsable());
        fugaDTO.setInstitucion(entidadADtoInstitucion(fuga.getInstitucion()));
        fugaDTO.setEstado(fuga.getEstado());
        fugaDTO.setServicio(fuga.getServicio());
        fugaDTO.setFechaCreacion(fuga.getFechaCreacion());

        if (fuga.getTokenFichaIdentificacion() != null) {
            fugaDTO.setTokenFichaIdentificacion(fuga.getTokenFichaIdentificacion().getTokenIdentificador());
        }

        if(fugaDTO.getInstitucion()!= null){
            fugaDTO.setNombreInstitucion(fuga.getInstitucion().getNombreOrganizacion());
        }
        return fugaDTO;
    }

    private AdolescenteDerivadoInst dtoAEntidad(AdolescenteDerivadoInstDTO dto, FichaIdentificacionRepository fichaIdentificacionRepository) {
        AdolescenteDerivadoInst fuga = new AdolescenteDerivadoInst();
        fuga.setIdAdolescenteDerivado(dto.getIdAdolescenteDerivado());

        if (dto.getTokenFichaIdentificacion() != null) {
            System.out.println(" tokenFichaIdentificacion recibido: " + dto.getTokenFichaIdentificacion());
            FichaIdentificacion ficha = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(dto.getTokenFichaIdentificacion(), false);

            if (ficha == null) {
                throw new IllegalArgumentException(" FichaIdentificacion no encontrada para Token: " + dto.getTokenFichaIdentificacion());
            }
            fuga.setTokenFichaIdentificacion(ficha);
        } else {
            throw new IllegalArgumentException("tokenFichaIdentificacion no puede ser nulo");
        }
        fuga.setServicio(dto.getServicio());
        fuga.setTiempoServicio(dto.getTiempoServicio());
        fuga.setFechaRegistro(dto.getFechaRegistro());
        fuga.setFechaDerivacion(dto.getFechaDerivacion());
        fuga.setDepartamento(dto.getDepartamento());
        fuga.setPersonaResponsable(dto.getPersonaResponsable());
        fuga.setServicio(dto.getServicio());
        fuga.setEstado(dto.getEstado());
        fuga.setInstitucion(dtoAEntidadInstitucion(dto.getInstitucion()));
        return fuga;
    }

    private Catalogo dtoAEntidadCatalogo(CatalogoDTO dto) {
        if (dto == null) return null;
        return catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
    }


    private static CatalogoDTO entidadADtoCatalogo(Catalogo entidad) {
        if (entidad == null) return null;

        CatalogoDTO dto = new CatalogoDTO();
        dto.setIdCatalogo(entidad.getIdCatalogo());
        dto.setNombre(entidad.getNombre());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setNemonico(entidad.getNemonico());
        dto.setCodigoExterno(entidad.getCodigoExterno());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(entidad.getEmpresa().getTokenIdentificador());
        return dto;
    }

    private RegistroInstitucion dtoAEntidadInstitucion(RegistroInstitucionDTO dto) {
        if (dto == null) return null;
        return this.registroInstitucionRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
    }

    private static RegistroInstitucionDTO entidadADtoInstitucion(RegistroInstitucion entidad) {
        if (entidad == null) return null;
        RegistroInstitucionDTO dto = new RegistroInstitucionDTO();
        dto.setIdRegistroInstitucion(entidad.getIdRegistroInstitucion());
        dto.setNombreOrganizacion(entidad.getNombreOrganizacion());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        return dto;
    }
}
