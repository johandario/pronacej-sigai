package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.CondHistViol;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionConductual;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.SituPersCaraPers;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CondHistViolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionConductualDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.SituPersCaraPersDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.CondHistViolRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EvaluacionConductualRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.SituPersCaraPersRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class EvaluacionConductualServiceImpl implements EvaluacionConductualService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;
    private EvaluacionConductualRepository evaluacionConductualRepository;
    private SituPersCaraPersRepository situPersCaraPersRepository;
    private CondHistViolRepository condHistViolRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionConductualDTO>> obtenerEvaluacionesConductuales(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionConductualDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener JWT y validar
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            // Convertir el body a PaginacionRequest
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            Empresa empresa = df2.getData().getEmpresa();

            // Configurar paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idEvaluacionConductual").descending()
            );

            // Consultar base de datos
            Page<EvaluacionConductual> evaluacionesPage = this.evaluacionConductualRepository.findByFichaIdentificacionTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
                    paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false, pageable);

            // Crear respuesta paginada
            PaginacionResponse<EvaluacionConductualDTO> paginacionResponse = new PaginacionResponse<>();
            List<EvaluacionConductualDTO> evaluacionDTOList = new ArrayList<>();

            for (EvaluacionConductual evaluacion : evaluacionesPage.toList()) {
                EvaluacionConductualDTO evaluacionDTO = new EvaluacionConductualDTO();
                evaluacionDTO.setTokenIdentificador(evaluacion.getTokenIdentificador());
                evaluacionDTO.setTokenIdentificadorFichaIdentificacion(evaluacion.getFichaIdentificacion().getTokenIdentificador());
                evaluacionDTO.setTokenIdentificadorEmpresa(evaluacion.getEmpresa().getTokenIdentificador());
                evaluacionDTO.setFechaCreacion(evaluacion.getFechaCreacion());
                evaluacionDTO.setNombreCompletoUsuarioCreacion(evaluacion.getUsuarioSistemaCrea().getNombres() + " " + evaluacion.getUsuarioSistemaCrea().getApellidos());


                evaluacionDTOList.add(evaluacionDTO);
            }

            paginacionResponse.setData(evaluacionDTOList);
            paginacionResponse.setTotalItems(evaluacionesPage.getTotalElements());

            df.llenarRespuestaExitosa(
                    "Se han encontrado un total de: " + evaluacionDTOList.size() + 
                    " de: " + evaluacionesPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SituPersCaraPersDTO>> obtenerSituPersCaraPers(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<SituPersCaraPersDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validar JWT y obtener información del usuario/empresa
            RespuestaPorDefectoAuditoria<BodyJwtValido> jwtResponse = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!jwtResponse.isExito()) {
                respuesta.setMensaje(jwtResponse.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
                }

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            Empresa empresa = jwtResponse.getData().getEmpresa();

            // Configuración de paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idSituPersCaraPers").descending()
            );

            // Consultar la base de datos
            Page<SituPersCaraPers> situPersCaraPersPage = this.situPersCaraPersRepository.findByEvaluacionConductualTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
                    paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false, pageable
            );

            // Construir respuesta paginada
            PaginacionResponse<SituPersCaraPersDTO> paginacionResponse = new PaginacionResponse<>();
            List<SituPersCaraPersDTO> situPersCaraPersDTOList = new ArrayList<>();
        
            for (SituPersCaraPers situPersCaraPers : situPersCaraPersPage.toList()) {
                SituPersCaraPersDTO situPersCaraPersDTO = new SituPersCaraPersDTO();
                situPersCaraPersDTO.setCriterio(situPersCaraPers.getCriterio());
                situPersCaraPersDTO.setComentario(situPersCaraPers.getComentario());
                situPersCaraPersDTO.setTokenIdentificador(situPersCaraPers.getTokenIdentificador());
                situPersCaraPersDTO.setTokenIdentificadorEvaluacionConductual(situPersCaraPers.getEvaluacionConductual().getTokenIdentificador());
                situPersCaraPersDTO.setTokenIdentificadorEmpresa(situPersCaraPers.getEmpresa().getTokenIdentificador());

                situPersCaraPersDTOList.add(situPersCaraPersDTO);
            }

            paginacionResponse.setData(situPersCaraPersDTOList);
            paginacionResponse.setTotalItems(situPersCaraPersPage.getTotalElements());

            // Respuesta exitosa
            respuesta.llenarRespuestaExitosa(
                    "Se han encontrado un total de: " + situPersCaraPersDTOList.size() + 
                    " de: " + situPersCaraPersPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse
            );
        } catch (Exception ex) {
            // Manejo de excepciones
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<CondHistViolDTO>> obtenerCondHistViol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<CondHistViolDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validar JWT y obtener información del usuario/empresa
            RespuestaPorDefectoAuditoria<BodyJwtValido> jwtResponse = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!jwtResponse.isExito()) {
                respuesta.setMensaje(jwtResponse.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
                }

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            Empresa empresa = jwtResponse.getData().getEmpresa();

            // Configuración de paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idCondHistViol").descending()
            );

            // Consultar la base de datos
            Page<CondHistViol> CondHistViolPage = this.condHistViolRepository.findByEvaluacionConductualTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
                    paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false, pageable
            );

            // Construir respuesta paginada
            PaginacionResponse<CondHistViolDTO> paginacionResponse = new PaginacionResponse<>();
            List<CondHistViolDTO> condHistViolDTOList = new ArrayList<>();

            for (CondHistViol condHistViol : CondHistViolPage.toList()) {
                CondHistViolDTO condHistViolDTO = new CondHistViolDTO();
                condHistViolDTO.setCriterio(condHistViol.getCriterio());
                condHistViolDTO.setComentario(condHistViol.getComentario());
                condHistViolDTO.setTokenIdentificador(condHistViol.getTokenIdentificador());
                condHistViolDTO.setTokenIdentificadorEvaluacionConductual(condHistViol.getEvaluacionConductual().getTokenIdentificador());
                condHistViolDTO.setTokenIdentificadorEmpresa(condHistViol.getEmpresa().getTokenIdentificador());

                condHistViolDTOList.add(condHistViolDTO);
            }

            paginacionResponse.setData(condHistViolDTOList);
            paginacionResponse.setTotalItems(CondHistViolPage.getTotalElements());

            // Respuesta exitosa
            respuesta.llenarRespuestaExitosa(
                    "Se han encontrado un total de: " + condHistViolDTOList.size() + 
                    " de: " + CondHistViolPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse
            );
        } catch (Exception ex) {
            // Manejo de excepciones
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EvaluacionConductualDTO> crearEvaluacionConductual(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EvaluacionConductualDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validar y obtener datos del JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar el body recibido
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            // Datos comunes
            Empresa empresa = df2.getData().getEmpresa();
            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            // Convertir el body en el DTO
            EvaluacionConductualDTO evaluacionConductualDTO = new Gson().fromJson(bodyString, EvaluacionConductualDTO.class);
            
            EvaluacionConductual evaluacionConductual;
            
            if(evaluacionConductualDTO.getEsEdicion()){
                evaluacionConductual = evaluacionConductualRepository.findByTokenIdentificadorAndRemovido(evaluacionConductualDTO.getTokenIdentificador(), Boolean.FALSE);
                if (evaluacionConductual == null) {
                    df.setMensaje("La evaluación conductual a editar no existe o ya fue eliminada anteriormente");
                    return df;
                }
                evaluacionConductual.setFechaEdicion(new Date());
                evaluacionConductual.setIpEdita(ip);
                evaluacionConductual.setUsuarioSistemaEdita(usuarioLogin);
            }else{                
                evaluacionConductual = new EvaluacionConductual();
                evaluacionConductual.setFechaCreacion(new Date());
                evaluacionConductual.setIpCrea(ip);
                evaluacionConductual.setUsuarioSistemaCrea(usuarioLogin);
                evaluacionConductual.setEmpresa(empresa);

                FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(evaluacionConductualDTO.getTokenIdentificadorFichaIdentificacion(), Boolean.FALSE);
                evaluacionConductual.setFichaIdentificacion(fichaIdentificacion);
            }

            // Guardar la entidad en la base de datos
            evaluacionConductual = this.evaluacionConductualRepository.save(evaluacionConductual);
            evaluacionConductualDTO.setTokenIdentificador(evaluacionConductual.getTokenIdentificador());
            evaluacionConductualDTO.setFechaCreacion(evaluacionConductual.getFechaCreacion());
            
            for (SituPersCaraPersDTO situPersCaraPersDTO : evaluacionConductualDTO.getListaSituPersCaraPers()) {
                if (situPersCaraPersDTO.getTokenIdentificador().equals("0")) {
                    SituPersCaraPers situPersCaraPers = new SituPersCaraPers();
                    situPersCaraPers.setFechaCreacion(new Date());
                    situPersCaraPers.setIpCrea(ip);
                    situPersCaraPers.setUsuarioSistemaCrea(usuarioLogin);
                    situPersCaraPers.setEmpresa(empresa);
                    
                    situPersCaraPers.setCriterio(situPersCaraPersDTO.getCriterio());
                    situPersCaraPers.setComentario(situPersCaraPersDTO.getComentario());
                    
                    situPersCaraPers.setEvaluacionConductual(evaluacionConductual);
                    
                    situPersCaraPers = this.situPersCaraPersRepository.save(situPersCaraPers);
                    situPersCaraPersDTO.setTokenIdentificador(situPersCaraPers.getTokenIdentificador());
                }
            }
            
            for (CondHistViolDTO condHistViolDTO : evaluacionConductualDTO.getListaCondHistViolDTO()) {
                if (condHistViolDTO.getTokenIdentificador().equals("0")) {
                    CondHistViol condHistViol = new CondHistViol();
                    condHistViol.setFechaCreacion(new Date());
                    condHistViol.setIpCrea(ip);
                    condHistViol.setUsuarioSistemaCrea(usuarioLogin);
                    condHistViol.setEmpresa(empresa);
                    
                    condHistViol.setCriterio(condHistViolDTO.getCriterio());
                    condHistViol.setComentario(condHistViolDTO.getComentario());
                    
                    condHistViol.setEvaluacionConductual(evaluacionConductual);
                    
                    condHistViol = this.condHistViolRepository.save(condHistViol);
                    condHistViolDTO.setTokenIdentificador(condHistViol.getTokenIdentificador());
                }
            }

            // Llenar respuesta exitosa
            df.llenarRespuestaExitosa(
                "Se " + (evaluacionConductualDTO.getEsEdicion() ? "editó" : "creó") + " con éxito la evaluación conductual: " + evaluacionConductualDTO.getTokenIdentificador(), 
                evaluacionConductualDTO
            );

        } catch (Exception ex) {
            // Manejo de excepciones
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarEvaluacionConductual(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            EvaluacionConductualDTO evaluacionConductualDTO = new Gson().fromJson(bodyString, EvaluacionConductualDTO.class);

            EvaluacionConductual evaluacionConductual = this.evaluacionConductualRepository.findByTokenIdentificadorAndRemovido(evaluacionConductualDTO.getTokenIdentificador(), false);

            if (evaluacionConductual == null) {
                df.setMensaje("La evaluación conductual no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            Date fecha = new Date();
            evaluacionConductual.setRemovido(true);
            evaluacionConductual.setIpElimina(ip);
            evaluacionConductual.setUsuarioSistemaElimina(usuarioSistemaLogin);
            evaluacionConductual.setFechaEliminacion(fecha);

            this.evaluacionConductualRepository.save(evaluacionConductual);

            df.llenarRespuestaExitosa("Se ha eliminado con éxito del sistema a la evaluación conductual: "
                    + evaluacionConductual.getTokenIdentificador(), true);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarSituPersCaraPers(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            SituPersCaraPersDTO situPersCaraPersDTO = new Gson().fromJson(bodyString, SituPersCaraPersDTO.class);

            SituPersCaraPers situPersCaraPers = this.situPersCaraPersRepository.findByTokenIdentificadorAndRemovido(situPersCaraPersDTO.getTokenIdentificador(), false);

            if (situPersCaraPers == null) {
                df.setMensaje("La situación personal/característica personal no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            Date fecha = new Date();
            situPersCaraPers.setRemovido(true);
            situPersCaraPers.setIpElimina(ip);
            situPersCaraPers.setUsuarioSistemaElimina(usuarioSistemaLogin);
            situPersCaraPers.setFechaEliminacion(fecha);

            this.situPersCaraPersRepository.save(situPersCaraPers);

            df.llenarRespuestaExitosa("Se ha eliminado con éxito del sistema la situación personal/característica personal: "
                    + situPersCaraPers.getTokenIdentificador(), true);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarCondHistViol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validar el JWT y obtener información del usuario
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            // Desencriptar el cuerpo de la solicitud
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            // Parsear los datos del cuerpo a DTO
            CondHistViolDTO condHistViolDTO = new Gson().fromJson(bodyString, CondHistViolDTO.class);

            // Buscar el registro en la base de datos
            CondHistViol condHistViol = this.condHistViolRepository.findByTokenIdentificadorAndRemovido(
                condHistViolDTO.getTokenIdentificador(), 
                false
            );

            if (condHistViol == null) {
                df.setMensaje("La condición histórica de violencia no fue encontrada o ya fue eliminada anteriormente.");
                return df;
            }

            // Actualizar los campos de eliminación
            Date fecha = new Date();
            condHistViol.setRemovido(true);
            condHistViol.setIpElimina(ip);
            condHistViol.setUsuarioSistemaElimina(usuarioSistemaLogin);
            condHistViol.setFechaEliminacion(fecha);

            // Guardar los cambios en la base de datos
            this.condHistViolRepository.save(condHistViol);

            // Respuesta exitosa
            df.llenarRespuestaExitosa("Se ha eliminado con éxito del sistema la condición histórica de violencia: "
                    + condHistViol.getTokenIdentificador(), true);

        } catch (Exception ex) {
            // Manejar excepciones
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    
}
