package net.latinus.sistema.integral.gestion.seguridad.service.institucion;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.institucion.RegistroInstitucionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.institucion.RegistroInstitucionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RegistroInstitucionServiceImpl implements RegistroInstitucionService{

    private RegistroInstitucionRepository registroSalidaRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;
    private JerarquiaRepository jerarquiaRepository;
   
    private ParametroDelSistemaRepository parametroDelSistemaRepository;


    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroInstitucionDTO>> obtenerInstituciones(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<RegistroInstitucionDTO>> df = new RespuestaPorDefectoAuditoria<>();
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
            // Desencriptar body
            String bodyDecifrado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDecifrado, PaginacionRequest.class);
            // Configuración de paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idRegistroInstitucion").descending()
            );
            Page<RegistroInstitucion> fugaPage;
//            if (paginacionRequest.getTokenIdentificador() != null && !paginacionRequest.getTokenIdentificador().isEmpty()) {
//                if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
//                    fugaPage = this.registroSalidaRepository.buscarPorFiltroYCentro(
//                            paginacionRequest.getTokenIdentificador(),
//                            paginacionRequest.getFilter(),
//                            pageable
//                    );
//                }
//                else {
//                    fugaPage = this.registroSalidaRepository.findByCentroTokenIdentificador(
//                            paginacionRequest.getTokenIdentificador(), pageable
//                    );
//                }
//            }
//            else if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
//                fugaPage = this.registroSalidaRepository.buscarPorFiltro(paginacionRequest.getFilter(), pageable);
//            }
//            else {
//                fugaPage = this.registroSalidaRepository.findByRemovido(false, pageable);
//            }

            if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
                if (paginacionRequest.getTokenIdentificador() != null && !paginacionRequest.getTokenIdentificador().isEmpty()) {
                    fugaPage = this.registroSalidaRepository.buscarPorFiltroYCentro(
                            paginacionRequest.getTokenIdentificador(),
                            paginacionRequest.getFilter(),
                            pageable
                    );
                } else {
                    // solo filtro
                    fugaPage = this.registroSalidaRepository.buscarPorFiltro(
                            paginacionRequest.getFilter(),
                            pageable
                    );
                }
            } else {
                if (paginacionRequest.getTokenIdentificador() != null && !paginacionRequest.getTokenIdentificador().isEmpty()) {
                    // solo tokenIdentificador

                    fugaPage = this.registroSalidaRepository.findByCentroTokenIdentificador(
                            paginacionRequest.getTokenIdentificador(),
                            pageable
                    );
                    fugaPage.getContent().forEach(f -> System.out.println("REGISTRO: " + f.getIdRegistroInstitucion() + " - Centro: " + (f.getCentro() != null ? f.getCentro().getNombre() : "NULL")));
                } else {

                    fugaPage = this.registroSalidaRepository.findByRemovido(false, pageable);
                }
            }
            PaginacionResponse<RegistroInstitucionDTO> paginacionResponse = new PaginacionResponse<>();
            List<RegistroInstitucionDTO> fugaDTOList = new ArrayList<>();
            for (RegistroInstitucion fuga : fugaPage.toList()) {
                try {
                    RegistroInstitucionDTO fugaDTO = entidadADto(fuga);
                    fugaDTOList.add(fugaDTO);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            paginacionResponse.setData(fugaDTOList);
            paginacionResponse.setTotalItems(fugaPage.getTotalElements());
            df.llenarRespuestaExitosa("Se han encontrado un total de: " + fugaDTOList.size() + " de: " + fugaPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }


    @Override
    public RespuestaPorDefectoAuditoria<RegistroInstitucionDTO> obtenerRegistroInstitucionPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<RegistroInstitucionDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            RegistroInstitucion fuga  = this.registroSalidaRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if(fuga == null ){
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            RegistroInstitucionDTO fugaDTO = entidadADto(fuga);
            df.llenarRespuestaExitosa("Se ha encontrado el registro: " + fugaDTO.getTokenIdentificador(), fugaDTO);
        }
        catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<RegistroInstitucionDTO> crearRegistroInstitucion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<RegistroInstitucionDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            String bodyDecifrado = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
            RegistroInstitucionDTO fugaEntranteDTO = new Gson().fromJson(bodyDecifrado, RegistroInstitucionDTO.class);
            RegistroInstitucion fugaEncontrado = this.registroSalidaRepository.findByTokenIdentificadorAndRemovido(fugaEntranteDTO.getTokenIdentificador(), false);
            if(fugaEncontrado== null && fugaEntranteDTO.getEsEdicion()){
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            if (!fugaEntranteDTO.getEsEdicion()) {
                RegistroInstitucion fuga = dtoAEntidad(fugaEntranteDTO);
                fuga.setFechaCreacion(new Date());
                fuga.setTokenIdentificador(UUID.randomUUID().toString());
                this.registroSalidaRepository.save(fuga);
                String mensajeUsuario = "Se creó con éxito la institución " + fugaEntranteDTO.getNombreOrganizacion();
                String mensajeAuditoria = "Se creó con éxito la institución con RUC: " + fugaEntranteDTO.getRuc();
                df.llenarRespuestaExitosa(mensajeUsuario, fugaEntranteDTO, mensajeAuditoria);
            } else {
                RegistroInstitucion fuga = dtoAEntidad(fugaEntranteDTO);
                fuga.setFechaEdicion(new Date());
                this.registroSalidaRepository.save(fuga);
                String mensajeUsuario = "Se editado con éxito la institución " + fugaEntranteDTO.getNombreOrganizacion();
                String mensajeAuditoria = "Se editó con éxito la institución con RUC: " + fugaEntranteDTO.getRuc();
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
            String bodyString = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null).getData();
            RegistroInstitucionDTO fugaDTO = new Gson().fromJson(bodyString, RegistroInstitucionDTO.class);
            RegistroInstitucion fuga = this.registroSalidaRepository.findByTokenIdentificadorAndRemovido(
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
            this.registroSalidaRepository.save(fuga);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito del sistema"
                    , true);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<RegistroInstitucionDTO>> obtenerTodasLasInstituciones(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<RegistroInstitucionDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> validacionJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!validacionJwt.isExito()) {
                respuesta.setMensaje(validacionJwt.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            // Obtener todas las instituciones no eliminadas
            List<RegistroInstitucion> listaInstituciones = this.registroSalidaRepository.findAllByRemovido(false);

            // Convertir las entidades a DTO
            List<RegistroInstitucionDTO> listaInstitucionesDTO = new ArrayList<>();
            for (RegistroInstitucion institucion : listaInstituciones) {
                RegistroInstitucionDTO dto = entidadADto(institucion); // Método de conversión
                listaInstitucionesDTO.add(dto);
            }

            respuesta.llenarRespuestaExitosa("Se encontraron " + listaInstitucionesDTO.size() + " instituciones.", listaInstitucionesDTO);
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }


    @NotNull
    private static RegistroInstitucionDTO entidadADto(RegistroInstitucion fuga) {
        RegistroInstitucionDTO fugaDTO = new RegistroInstitucionDTO();
        fugaDTO.setTokenIdentificador(fuga.getTokenIdentificador());
        fugaDTO.setIdRegistroInstitucion(fuga.getIdRegistroInstitucion());
        fugaDTO.setNombreOrganizacion(fuga.getNombreOrganizacion());
        fugaDTO.setNombreDirector(fuga.getNombreDirector());
        fugaDTO.setRuc(fuga.getRuc());
        fugaDTO.setNombContactoOperacional(fuga.getNombContactoOperacional());
        fugaDTO.setDireccion(fuga.getDireccion());
        fugaDTO.setTelefono(fuga.getTelefono());
        fugaDTO.setFax(fuga.getFax());
        fugaDTO.setEmail(fuga.getEmail());
        fugaDTO.setSitioWeb(fuga.getSitioWeb());
        fugaDTO.setDni(fuga.getDni());
        fugaDTO.setMisionInstitucional(fuga.getMisionInstitucional());
        fugaDTO.setObjetivoInstitucional(fuga.getObjetivoInstitucional());
        fugaDTO.setDepartamento(fuga.getDepartamento());
        fugaDTO.setServicios(fuga.getServicios());
        fugaDTO.setBeneficios(fuga.getBeneficios());
        fugaDTO.setHorariosServicios(fuga.getHorariosServicios());
        fugaDTO.setServiciosArticulados(fuga.getServiciosArticulados());
        fugaDTO.setAreaGeografica(fuga.getAreaGeografica());
        fugaDTO.setParticipacionEspaciosLocales(fuga.getParticipacionEspaciosLocales());
        fugaDTO.setOtroSitioWeb(fuga.getOtroSitioWeb());
        fugaDTO.setTipoOrganizacion(entidadADtoCatalogo(fuga.getTipoOrganizacion()));
        fugaDTO.setTieneConvenio(fuga.getTieneConvenio());
        fugaDTO.setCodigoUbigeoUbicacion(fuga.getCodigoUbigeoUbicacion());
        fugaDTO.setFinalidadInstitucion(fuga.getFinalidadInstitucion());
        fugaDTO.setTipoInstitucion(fuga.getTipoInstitucion());
        fugaDTO.setEstado(fuga.getEstado());
//        fugaDTO.setCentro(entidadADtoJerarquia(fuga.getCentro()));
        if (fuga.getCentro() == null) {
            fugaDTO.setCentro(null); // ya lo hace, pero es explícito
        }
        return fugaDTO;
    }

    @Override
    public RespuestaPorDefectoAuditoria<RegistroInstitucionDTO> obtenerRegistroInstitucionPorRuc(
            HttpServletRequest httpServletRequest,
            String ruc
    ) {
        RespuestaPorDefectoAuditoria<RegistroInstitucionDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 =
                    this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            if (ruc == null || ruc.trim().isEmpty()) {
                df.setMensaje("Debe ingresar el RUC de la institución.");
                return df;
            }
            RegistroInstitucion institucion =
                    this.registroSalidaRepository.findByRucAndRemovido(ruc.trim(), false);
            if (institucion == null) {
                df.setMensaje("No se encontró una institución registrada con el RUC ingresado.");
                return df;
            }
            RegistroInstitucionDTO dto = entidadADto(institucion);
            df.llenarRespuestaExitosa(
                    "Se encontró la institución.",
                    dto
            );
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    private RegistroInstitucion dtoAEntidad(RegistroInstitucionDTO dto) {
        RegistroInstitucion fuga = new RegistroInstitucion();
        fuga.setIdRegistroInstitucion(dto.getIdRegistroInstitucion());
        fuga.setNombreOrganizacion(dto.getNombreOrganizacion());
        fuga.setNombreDirector(dto.getNombreDirector());
        fuga.setRuc(dto.getRuc());
        fuga.setNombContactoOperacional(dto.getNombContactoOperacional());
        fuga.setDireccion(dto.getDireccion());
        fuga.setTelefono(dto.getTelefono());
        fuga.setFax(dto.getFax());
        fuga.setEmail(dto.getEmail());
        fuga.setSitioWeb(dto.getSitioWeb());
        fuga.setDni(dto.getDni());
        fuga.setMisionInstitucional(dto.getMisionInstitucional());
        fuga.setObjetivoInstitucional(dto.getObjetivoInstitucional());
        fuga.setDepartamento(dto.getDepartamento());
        fuga.setServicios(dto.getServicios());
        fuga.setBeneficios(dto.getBeneficios());
        fuga.setHorariosServicios(dto.getHorariosServicios());
        fuga.setServiciosArticulados(dto.getServiciosArticulados());
        fuga.setAreaGeografica(dto.getAreaGeografica());
        fuga.setParticipacionEspaciosLocales(dto.getParticipacionEspaciosLocales());
        fuga.setOtroSitioWeb(dto.getOtroSitioWeb());
        fuga.setTipoOrganizacion(dtoAEntidadCatalogo(dto.getTipoOrganizacion()));
        fuga.setTieneConvenio(dto.getTieneConvenio());
        fuga.setCodigoUbigeoUbicacion(dto.getCodigoUbigeoUbicacion());
        fuga.setFinalidadInstitucion(dto.getFinalidadInstitucion());
        fuga.setTipoInstitucion(dto.getTipoInstitucion());
        fuga.setEstado(dto.getEstado());
        fuga.setCentro(dtoAEntidadJerarquia(dto.getCentro()));
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

    private Jerarquia dtoAEntidadJerarquia(JerarquiaDTO dto) {
        if (dto == null) return null;
        return this.jerarquiaRepository.findJerarquiaByTokenIdentificador(dto.getTokenIdentificador());
    }

    private static  JerarquiaDTO entidadADtoJerarquia(Jerarquia entidad) {
        if (entidad == null) return null;
        JerarquiaDTO dto = new JerarquiaDTO();
        dto.setId(entidad.getIdJerarquia());
        dto.setNombre(entidad.getNombre());
        dto.setNemonico(entidad.getNemonico());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(entidad.getEmpresa().getTokenIdentificador());
        return dto;
    }
}
