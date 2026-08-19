package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.ContactoAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ContactoAdolescenteDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.ContactoAdolescenteRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
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
public class ContactoAdolescenteServiceImpl implements ContactoAdolescenteService {
    private ContactoAdolescenteRepository contactoAdolescenteRepository;
    private JwtProviderService jwtProviderService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private ParametroDelSistemaRepository parametroDelSistemaRepository;


    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<ContactoAdolescenteDTO>> obtenerContactos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<ContactoAdolescenteDTO>> df = new RespuestaPorDefectoAuditoria<>();
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
                    Sort.by("idContactoAdolescente").descending()
            );
            Page<ContactoAdolescente> fugaPage;
            if (paginacionRequest.getTokenIdentificador() != null && !paginacionRequest.getTokenIdentificador().isEmpty()) {
                if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
                    fugaPage = this.contactoAdolescenteRepository.buscarPorTokenYFiltro(
                            paginacionRequest.getTokenIdentificador(),
                            paginacionRequest.getFilter(),
                            pageable
                    );
                } else {
                    fugaPage = this.contactoAdolescenteRepository.findAllByFichaIdentificacionTokenIdentificadorAndRemovido(
                            paginacionRequest.getTokenIdentificador(), false, pageable
                    );
                }
            } else if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
                fugaPage = this.contactoAdolescenteRepository.buscarPorFiltro(paginacionRequest.getFilter(), pageable);
            } else {
                fugaPage = this.contactoAdolescenteRepository.findByRemovido(false, pageable);
            }
            PaginacionResponse<ContactoAdolescenteDTO> paginacionResponse = new PaginacionResponse<>();
            List<ContactoAdolescenteDTO> fugaDTOList = new ArrayList<>();
            for (ContactoAdolescente fuga : fugaPage.toList()) {
                ContactoAdolescenteDTO fugaDTO = entidadADto(fuga);
                fugaDTOList.add(fugaDTO);
            }
            paginacionResponse.setData(fugaDTOList);
            paginacionResponse.setTotalItems(fugaPage.getTotalElements());
            df.llenarRespuestaExitosa("Se han encontrado un total de: " + fugaDTOList.size() + " de: " + fugaPage.getTotalElements() + " elementos disponibles", paginacionResponse);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }




    @Override
    public RespuestaPorDefectoAuditoria<ContactoAdolescenteDTO> obtenerContactosPorToken(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<ContactoAdolescenteDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            ContactoAdolescente fuga  = this.contactoAdolescenteRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
            if(fuga == null ){
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            ContactoAdolescenteDTO fugaDTO = entidadADto(fuga);
            df.llenarRespuestaExitosa("Se ha encontrado el registro: " + fugaDTO.getTokenIdentificador(), fugaDTO);
        }
        catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<ContactoAdolescenteDTO> crearContacto(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ContactoAdolescenteDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            ContactoAdolescenteDTO fugaEntranteDTO = new Gson().fromJson(bodyDecifrado, ContactoAdolescenteDTO.class);
            ContactoAdolescente fugaEncontrado = this.contactoAdolescenteRepository.findByTokenIdentificadorAndRemovido(fugaEntranteDTO.getTokenIdentificador(), false);
            if(fugaEncontrado== null && fugaEntranteDTO.getEsEdicion()){
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }
            if (!fugaEntranteDTO.getEsEdicion()) {
                ContactoAdolescente fuga = dtoAEntidad(fugaEntranteDTO, fichaIdentificacionRepository);
                fuga.setFechaCreacion(new Date());
                fuga.setTokenIdentificador(UUID.randomUUID().toString());
                this.contactoAdolescenteRepository.save(fuga);
                df.llenarRespuestaExitosa("Se ha creado con éxito el registro de contacto. " , fugaEntranteDTO);
            } else {
                ContactoAdolescente fuga = dtoAEntidad(fugaEntranteDTO, fichaIdentificacionRepository);
                fuga.setFechaEdicion(new Date());
                this.contactoAdolescenteRepository.save(fuga);
                df.llenarRespuestaExitosa("Se ha editado con éxito el registro de contacto. " , fugaEntranteDTO);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex.getCause() != null) {
                ex.getCause().printStackTrace();
            }
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarContactos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            ContactoAdolescenteDTO fugaDTO = new Gson().fromJson(bodyString, ContactoAdolescenteDTO.class);
            ContactoAdolescente fuga = this.contactoAdolescenteRepository.findByTokenIdentificadorAndRemovido(
                    fugaDTO.getTokenIdentificador(), false
            );
            if (fuga == null) {
                df.setMensaje("El contacto del adolescente no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }
            Date fecha = new Date();
            fuga.setRemovido(true);
            fuga.setIpElimina(ip);
            fuga.setUsuarioSistemaElimina(usuarioSistemaLogin);
            fuga.setFechaEliminacion(fecha);
            this.contactoAdolescenteRepository.save(fuga);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito del sistema"
                    , true);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<ContactoAdolescenteDTO>> obtenerTodasLasContactos(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<ContactoAdolescenteDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> validacionJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!validacionJwt.isExito()) {
                respuesta.setMensaje(validacionJwt.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }
            List<ContactoAdolescente> listaInstituciones = this.contactoAdolescenteRepository.findAllByRemovido(false);
            List<ContactoAdolescenteDTO> listaInstitucionesDTO = new ArrayList<>();
            for (ContactoAdolescente institucion : listaInstituciones) {
                ContactoAdolescenteDTO dto = entidadADto(institucion);
                listaInstitucionesDTO.add(dto);
            }
            respuesta.llenarRespuestaExitosa("Se encontraron " + listaInstitucionesDTO.size() + " instituciones.", listaInstitucionesDTO);
        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }



    private static ContactoAdolescenteDTO entidadADto(ContactoAdolescente fuga) {
        ContactoAdolescenteDTO fugaDTO = new ContactoAdolescenteDTO();
        fugaDTO.setTokenIdentificador(fuga.getTokenIdentificador());
        fugaDTO.setIdContactoAdolescente(fuga.getIdContactoAdolescente());
        fugaDTO.setActividades(fuga.getActividades());
        fugaDTO.setObservaciones(fuga.getObservaciones());
        fugaDTO.setFechaRegistro(fuga.getFechaRegistro());
        fugaDTO.setModalidadEntrevista(fuga.getModalidadEntrevista());
        fugaDTO.setUsuarioResponsable(fuga.getUsuarioResponsable());
        return fugaDTO;
    }

    private ContactoAdolescente dtoAEntidad(ContactoAdolescenteDTO dto, FichaIdentificacionRepository fichaIdentificacionRepository) {
        ContactoAdolescente fuga = new ContactoAdolescente();
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
        fuga.setIdContactoAdolescente(dto.getIdContactoAdolescente());
        fuga.setUsuarioResponsable(dto.getUsuarioResponsable());
        fuga.setFechaRegistro(dto.getFechaRegistro());
        fuga.setModalidadEntrevista(dto.getModalidadEntrevista());
        fuga.setActividades(dto.getActividades());
        fuga.setObservaciones(dto.getObservaciones());
        return fuga;
    }


}
