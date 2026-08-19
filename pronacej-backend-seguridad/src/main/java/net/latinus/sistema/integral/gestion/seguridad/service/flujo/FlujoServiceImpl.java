package net.latinus.sistema.integral.gestion.seguridad.service.flujo;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.RolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.repository.flujo.*;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.RolRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.UsuarioSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.EmailService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import org.json.JSONObject;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FlujoServiceImpl implements FlujoService {
    private ProcesoRepository procesoRepository;
    private PasoRepository pasoRepository;
    private VariableProcesoRepository variableProcesoRepository;
    private InstanciaProcesoRepository instanciaProcesoRepository;
    private TareaRepository tareaRepository;
    private EmailService emailService;
    private UsuarioSistemaRepository usuarioSistemaRepository;
    private RolRepository rolRepository;
    private PasoUsuarioRepository pasoUsuarioRepository;
    private TareaUsuarioRepository tareaUsuarioRepository;

    private JwtProviderService jwtProviderService;
    private PaginacionService paginacionService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<ProcesoDTO>> obtenerProcesos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<ProcesoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDecifrado, PaginacionRequest.class);

            List<Proceso> procesoPage = this.procesoRepository.obtenerProcesosValido();

            PaginacionResponse<ProcesoDTO> paginacionResponse = new PaginacionResponse<>();
            List<ProcesoDTO> procesoDTOList = new ArrayList<>();

            for (Proceso proceso : procesoPage) {
                ProcesoDTO procesoDTO = this.entidadAProcesoDto(proceso);

                String pattern = "dd-MM-yyyy HH:mm:ss";
                DateFormat fecha = new SimpleDateFormat(pattern);
                procesoDTO.setFecCreacion(fecha.format(proceso.getFechaCreacion()));

                procesoDTOList.add(procesoDTO);
            }

            paginacionResponse = paginacionService.obtenerDatos(procesoDTOList, paginacionRequest);

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "Se han encontrado un total de: " + procesoDTOList.size() + " de: " + procesoPage.size() + " elementos disponibles. Consulta realizada por: " +
                            usuarioSistemaLogin.getUserName() + " con identificación: " + usuarioSistemaLogin.getNumeroDeDocumento()
                            + " (" + usuarioSistemaLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría - formato corregido
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String mensajeAuditoria = "Se han encontrado un total de " + procesoDTOList.size() + " procesos del sistema con " + 
                                    nombreUsuarioResponsable + " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ProcesoDTO> obtenerProcesoPorTokenID(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<ProcesoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

            Proceso procesoEncontrado = this.procesoRepository.obtenerProcesoValidoPorTokenIdentificador(tokenIdentificador);

            if (procesoEncontrado == null) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }

            ProcesoDTO procesoDTO = this.entidadAProcesoDto(procesoEncontrado);

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "Se han encontrado el ID de registro: " + procesoEncontrado.getTokenIdentificador();

            // Mensaje para auditoría - formato corregido
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String mensajeAuditoria = "Se encontró el proceso " + procesoEncontrado.getNombre() + " con " + 
                                    nombreUsuarioResponsable + " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, procesoDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ProcesoDTO> crearProceso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ProcesoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            ProcesoDTO procesoEntranteDTO = new Gson().fromJson(bodyDecifrado, ProcesoDTO.class);

            for (PasoDTO pasoDTO : procesoEntranteDTO.getPasos()) {
                if (pasoDTO.getPasoSalto() != null) {
                    Optional<PasoDTO> pasoEncontrado = procesoEntranteDTO.getPasos().stream()
                            .filter(paso -> paso.getOrden().equals(pasoDTO.getPasoSalto()))
                            .findFirst();
                    if (pasoEncontrado.isEmpty()) {
                        df.setMensaje("El paso " + pasoDTO.getPasoSalto() + " no existe");
                        return df;
                    }
                }
            }

            Proceso procesoEncontrado = this.procesoRepository.findByTokenIdentificadorAndRemovido(procesoEntranteDTO.getTokenIdentificador(), false);

            if (procesoEncontrado == null && procesoEntranteDTO.getEsEdicion()) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }

            boolean esEdicion = procesoEntranteDTO.getEsEdicion();

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            if (!procesoEntranteDTO.getEsEdicion()) {
                Proceso proceso = dtoAEntidadProceso(procesoEntranteDTO);
                proceso.setFechaCreacion(new Date());
                proceso.setIpCrea(httpServletRequest.getRemoteAddr());
                proceso.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                List<Paso> pasosTemp = new ArrayList<>(proceso.getPasos());

                proceso = this.procesoRepository.save(proceso);

                for (Paso paso : pasosTemp) {
                    paso.setProceso(proceso);
                    List<PasoUsuario> pasosUsuarioTemp = new ArrayList<>(paso.getPasoUsuarioList());
                    paso = this.pasoRepository.save(paso);
                    if (paso.getPasoUsuarioList() != null) {
                        for (PasoUsuario pasoUsuario : pasosUsuarioTemp) {
                            pasoUsuario.setPaso(paso);
                            this.pasoUsuarioRepository.save(pasoUsuario);
                        }
                        paso.setPasoUsuarioList(pasosUsuarioTemp);
                    }
                }
                proceso.setPasos(pasosTemp);

                for (PasoDTO pasoDTO : procesoEntranteDTO.getPasos()) {
                    if (pasoDTO.getPasoSalto() != null) {
                        Paso paso1 = this.pasoRepository.findByProcesoIdProcesoAndRemovidoAndOrden(proceso.getIdProceso(), false, pasoDTO.getPasoSalto());
                        Paso paso2 = this.pasoRepository.findByProcesoIdProcesoAndRemovidoAndOrden(proceso.getIdProceso(), false, pasoDTO.getOrden());
                        if (paso1 != null && paso2 != null) {
                            paso2.setPasoSubsanacion(paso1);
                            this.pasoRepository.save(paso2);
                        }
                    }
                }

                // Mensaje original para el usuario (mantener como estaba)
                String mensajeUsuario = "Se ha creado con éxito el registro.";

                // Mensaje para auditoría - formato corregido
                String mensajeAuditoria = "Se creó con éxito el proceso " + proceso.getNombre() + 
                                        " del " + fechaFormateada + " con " + nombreUsuarioResponsable + 
                                        " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

                // Configurar esEdicion para el DTO de respuesta
                ProcesoDTO procesoRespuesta = entidadAProcesoDto(proceso);
                procesoRespuesta.setEsEdicion(false);

                df.llenarRespuestaExitosa(mensajeUsuario, procesoRespuesta, mensajeAuditoria);
            } else {
                Proceso proceso = dtoAEntidadProceso(procesoEntranteDTO);
                proceso.setFechaEdicion(new Date());
                proceso.setIpEdita(httpServletRequest.getRemoteAddr());
                proceso.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());

                List<Paso> pasosTemp = new ArrayList<>(proceso.getPasos());

                proceso = this.procesoRepository.save(proceso);

                for (Paso paso : pasosTemp) {
                    paso.setProceso(proceso);
                    List<PasoUsuario> pasosUsuarioTemp = new ArrayList<>(paso.getPasoUsuarioList());
                    paso = this.pasoRepository.save(paso);
                    if (paso.getPasoUsuarioList() != null) {
                        for (PasoUsuario pasoUsuario : pasosUsuarioTemp) {
                            pasoUsuario.setPaso(paso);
                            this.pasoUsuarioRepository.save(pasoUsuario);
                        }
                        paso.setPasoUsuarioList(pasosUsuarioTemp);
                    }
                }
                proceso.setPasos(pasosTemp);

                for (PasoDTO pasoDTO : procesoEntranteDTO.getPasos()) {
                    if (pasoDTO.getPasoSalto() != null) {
                        Paso paso1 = this.pasoRepository.findByProcesoIdProcesoAndRemovidoAndOrden(proceso.getIdProceso(), false, pasoDTO.getPasoSalto());
                        Paso paso2 = this.pasoRepository.findByProcesoIdProcesoAndRemovidoAndOrden(proceso.getIdProceso(), false, pasoDTO.getOrden());
                        if (paso1 != null && paso2 != null) {
                            paso2.setPasoSubsanacion(paso1);
                            this.pasoRepository.save(paso2);
                        }
                    }
                }

                // Mensaje original para el usuario (mantener como estaba)
                String mensajeUsuario = "Se ha editado con éxito el registro.";

                // Mensaje para auditoría - formato corregido
                String mensajeAuditoria = "Se editó con éxito el proceso " + proceso.getNombre() + 
                                        " del " + fechaFormateada + " con " + nombreUsuarioResponsable + 
                                        " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

                // Configurar esEdicion para el DTO de respuesta
                ProcesoDTO procesoRespuesta = entidadAProcesoDto(proceso);
                procesoRespuesta.setEsEdicion(true);

                df.llenarRespuestaExitosa(mensajeUsuario, procesoRespuesta, mensajeAuditoria);
            }
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<InstanciaProcesoDTO> crearInstanciaProcesoPorProceso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<InstanciaProcesoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            ProcesoDTO procesoEntranteDTO = new Gson().fromJson(bodyDecifrado, ProcesoDTO.class);

            // Buscar el proceso a ser iniciado por medio del nemónico del proceso
            Proceso procesoEncontrado = this.procesoRepository.findByNemonicoAndRemovido(procesoEntranteDTO.getNemonico(), false);

            if (procesoEncontrado == null) {
                df.setMensaje("No existe el proceso solicitado.");
                return df;
            }

            // Verificar si el usuario tiene permiso para iniciar el nuevo flujo
            List<PasoUsuario> pasoUsuariosPrimerPaso = this.pasoUsuarioRepository.findPasoUsuariosConUsuarioSistema(procesoEncontrado.getNemonico(), 1);

            String tokenUsuarioEntrante = df2.getData().getUsuarioSistema().getTokenIdentificador();

            Optional<PasoUsuario> pasoUsuarioEncontrado = pasoUsuariosPrimerPaso.stream()
                    .filter(pu -> pu.getUsuarioSistema() != null &&
                            pu.getUsuarioSistema().getTokenIdentificador().equals(tokenUsuarioEntrante))
                    .findFirst();

            if (pasoUsuarioEncontrado.isEmpty()) {
                df.setMensaje("El usuario actual no se encuentra configurado para iniciar el flujo solicitado.");
                return df;
            }

            // Iniciar el nuevo flujo
            InstanciaProceso instanciaProceso = new InstanciaProceso();
            instanciaProceso.setFechaCreacion(new Date());
            instanciaProceso.setIpCrea(httpServletRequest.getRemoteAddr());
            instanciaProceso.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
            instanciaProceso.setEstado("En curso");
            instanciaProceso.setProceso(procesoEncontrado);
            instanciaProceso.setTareas(new ArrayList<>());

            instanciaProceso = this.instanciaProcesoRepository.save(instanciaProceso);

            List<Paso> pasos = this.pasoRepository.findByProcesoIdProcesoAndRemovido(procesoEncontrado.getIdProceso(), false);
            List<Tarea> tareas = new ArrayList<>();

            // CREAR TAREAS A PARTIR DE PASOS
            for (Paso paso : pasos) {
                Tarea tarea = new Tarea();
                tarea.setFechaCreacion(new Date());
                tarea.setIpCrea(httpServletRequest.getRemoteAddr());
                tarea.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                tarea.setOrden(paso.getOrden());
                if (tarea.getOrden() == 1) {
                    tarea.setEstado("Borrador");
                } else {
                    tarea.setEstado("Pendiente");
                }
                tarea.setPaso(paso);
                tarea.setUrl(paso.getUrl());
                tarea.setInstanciaProceso(instanciaProceso);

                tarea = this.tareaRepository.save(tarea);

                tareas.add(tarea);

                //Crear registro TareaUsuario
                List<PasoUsuario> pasoUsuariosList = this.pasoUsuarioRepository.findByPasoTokenIdentificadorAndRemovido(paso.getTokenIdentificador(), false);

                if (pasoUsuariosList != null) {
                    if (paso.getOrden() == 1) {
                        // Si es el primer paso, crear la tarea sólamente para el usuario que inició el flujo
                        TareaUsuario tareaUsuario = new TareaUsuario();
                        UsuarioSistema usuarioActual = df2.getData().getUsuarioSistema();
                        tareaUsuario.setUsuarioSistema(usuarioActual);
                        tareaUsuario.setTarea(tarea);
                        this.tareaUsuarioRepository.save(tareaUsuario);
                    } else {
                        // El resto de tareas respeta los usuarios configurados
                        for (PasoUsuario pasoUsuario : pasoUsuariosList) {
                            TareaUsuario tareaUsuario = new TareaUsuario();
                            tareaUsuario.setUsuarioSistema(pasoUsuario.getUsuarioSistema());
                            tareaUsuario.setTarea(tarea);
                            this.tareaUsuarioRepository.save(tareaUsuario);
                        }
                    }
                }
            }

            instanciaProceso.setTareas(tareas);

            InstanciaProcesoDTO instanciaProcesoDTO = entidadAInstanciaDto(instanciaProceso);

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "El flujo se ha iniciado con éxito.";

            // Mensaje para auditoría - formato corregido
            String mensajeAuditoria = "Se creó con éxito la instancia del proceso " + procesoEncontrado.getNombre() + 
                                    " del " + fechaFormateada + " con " + nombreUsuarioResponsable + 
                                    " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            // Configurar esEdicion para el DTO de respuesta (siempre false para instancias nuevas)
            instanciaProcesoDTO.setEsEdicion(false);

            df.llenarRespuestaExitosa(mensajeUsuario, instanciaProcesoDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ProcesoDTO> eliminarProceso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ProcesoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            ProcesoDTO procesoEntranteDTO = new Gson().fromJson(bodyDecifrado, ProcesoDTO.class);

            Proceso procesoEncontrado = this.procesoRepository.findByTokenIdentificadorAndRemovido(procesoEntranteDTO.getTokenIdentificador(), false);

            if (procesoEncontrado == null) {
                df.setMensaje("No existe el registro solicitado.");
                return df;
            }

            procesoEncontrado.setRemovido(true);
            procesoEncontrado.setFechaEliminacion(new Date());
            this.procesoRepository.save(procesoEncontrado);

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "Se ha eliminado con éxito el registro.";

            // Mensaje para auditoría - formato corregido
            String mensajeAuditoria = "Se eliminó con éxito el proceso " + procesoEncontrado.getNombre() + 
                                    " del " + fechaFormateada + " con " + nombreUsuarioResponsable + 
                                    " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, procesoEntranteDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    @Transactional
    public InstanciaProceso crearInstancia(String tokenIdentificadorProceso) {

        try {
            Proceso procesoEncontrado = this.procesoRepository.findByTokenIdentificadorAndRemovido(tokenIdentificadorProceso, false);

            if (procesoEncontrado != null) {
                InstanciaProceso instanciaProceso = new InstanciaProceso();
                instanciaProceso.setFechaCreacion(new Date());
                instanciaProceso.setEstado("En curso");
                instanciaProceso.setProceso(procesoEncontrado);
                instanciaProceso.setTareas(new ArrayList<>());

                List<Paso> pasos = this.pasoRepository.findByProcesoIdProcesoAndRemovido(procesoEncontrado.getIdProceso(), false);

                // CREAR TAREAS A PARTIR DE PASOS
                for (Paso paso : pasos) {
                    Tarea tarea = new Tarea();
                    tarea.setFechaCreacion(new Date());
                    tarea.setOrden(paso.getOrden());
                    if (tarea.getOrden() == 1) {
                        tarea.setEstado("En curso");
                    } else {
                        tarea.setEstado("Pendiente");
                    }
                    tarea.setPaso(paso);
                    tarea.setUrl(paso.getUrl());
                    tarea.setComentario(paso.getNombre());
                    tarea.setInstanciaProceso(instanciaProceso);
                    tarea.setNombreProceso(procesoEncontrado.getNombre());
                    if (!paso.getJsonCondicional().isEmpty()) {
                        JSONObject jsonCondicional = new JSONObject(paso.getJsonCondicional());
                        VariableProceso variableProceso = new VariableProceso();
                        variableProceso.setNombre(jsonCondicional.getString("nombre"));
                        variableProceso.setValor(jsonCondicional.getString("valor"));
                        this.variableProcesoRepository.save(variableProceso);
                        tarea.setVariableProceso(variableProceso);
                    }
                    instanciaProceso.getTareas().add(tarea);
                }

                this.instanciaProcesoRepository.save(instanciaProceso);

                return instanciaProceso;
            }
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> obtenerTareas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

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
                    Sort.by("idTarea").descending()
            );

            Page<Tarea> tareaPage = this.tareaRepository.findByRemovidoAndEstado(false, "En curso", pageable);

            PaginacionResponse<TareaDTO> paginacionResponse = new PaginacionResponse<>();
            List<TareaDTO> tareaDTOList = new ArrayList<>();

            for (Tarea tarea : tareaPage.toList()) {
                TareaDTO tareaDTO = this.entidadATareaDto(tarea);
                tareaDTO.setTipo(tarea.getPaso().getProceso().getNombre());
                tareaDTOList.add(tareaDTO);
            }

            paginacionResponse.setData(tareaDTOList);
            paginacionResponse.setTotalItems(tareaPage.getTotalElements());

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "Se han encontrado un total de: " + tareaDTOList.size() + " de: " + tareaPage.getTotalElements() + " elementos disponibles. Consulta realizada por: " +
                            usuarioSistemaLogin.getUserName() + " con identificación: " + usuarioSistemaLogin.getNumeroDeDocumento()
                            + " (" + usuarioSistemaLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría - formato corregido
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String mensajeAuditoria = "Se han encontrado un total de " + tareaPage.getTotalElements() + " tareas en curso del sistema con " + 
                                    nombreUsuarioResponsable + " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> obtenerTareasEnviadas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

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
                    Sort.by("fecha_edicion").descending()
                    //Sort.by("fechaEdicion").descending()
            );

            //Page<Tarea> tareaPage = this.tareaRepository.findByUsuarioSistemaEditaTokenIdentificadorAndEstadoInAndRemovido(df2.getData().getUsuarioSistema().getTokenIdentificador(), Arrays.asList("Completada", "Rechazada"), false, pageable);
            Page<Tarea> tareaPage = this.tareaRepository.obtenerTareasPorTokenUsuarioEditaYEstadosYTokenCentroYRemovido(
                    df2.getData().getUsuarioSistema().getTokenIdentificador(),
                    Arrays.asList("Completada", "Rechazada"),
                    paginacionRequest.getTokenIdentificador(),
                    false,
                    pageable
            );

            PaginacionResponse<TareaDTO> paginacionResponse = new PaginacionResponse<>();
            List<TareaDTO> tareaDTOList = new ArrayList<>();

            for (Tarea tarea : tareaPage.toList()) {
                TareaDTO tareaDTO = this.entidadATareaDto(tarea);
                tareaDTO.setTipo(tarea.getPaso().getProceso().getNombre());
                tareaDTO.setDescripcion(tarea.getPaso().getNombre());
                tareaDTOList.add(tareaDTO);
            }

            paginacionResponse.setData(tareaDTOList);
            paginacionResponse.setTotalItems(tareaPage.getTotalElements());

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "Se han encontrado un total de: " + tareaDTOList.size() + " de: " + tareaPage.getTotalElements() + " elementos disponibles. Consulta realizada por: " +
                            usuarioSistemaLogin.getUserName() + " con identificación: " + usuarioSistemaLogin.getNumeroDeDocumento()
                            + " (" + usuarioSistemaLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría - formato corregido
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String mensajeAuditoria = "Se han encontrado un total de " + tareaPage.getTotalElements() + " tareas enviadas del sistema con " + 
                                    nombreUsuarioResponsable + " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> obtenerTareasEnviadasPorUsuarioRolYTipo(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

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
                    Sort.by("fecha_edicion").descending()
                    //Sort.by("fechaEdicion").descending()
            );

            String tipo = (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isBlank()) ? paginacionRequest.getFilter() : null;

            Page<Tarea> tareaPage = this.tareaRepository.obtenerTareasPorTokenUsuarioEditaYEstadosYTokenCentroYTipoProcesoYRemovido(
                    df2.getData().getUsuarioSistema().getTokenIdentificador(),
                    Arrays.asList("Completada", "Rechazada"),
                    paginacionRequest.getTokenIdentificador(),
                    tipo,
                    false,
                    pageable
            );
            //Page<Tarea> tareaPage = this.tareaRepository.obtenerTareasEnviadasPorTokenUsuario(df2.getData().getUsuarioSistema().getTokenIdentificador(), tipo, pageable);

            PaginacionResponse<TareaDTO> paginacionResponse = new PaginacionResponse<>();
            List<TareaDTO> tareaDTOList = new ArrayList<>();

            for (Tarea tarea : tareaPage.toList()) {
                TareaDTO tareaDTO = this.entidadATareaDto(tarea);
                tareaDTO.setTipo(tarea.getPaso().getProceso().getNombre());
                tareaDTO.setDescripcion(tarea.getPaso().getNombre());
                tareaDTOList.add(tareaDTO);
            }

            paginacionResponse.setData(tareaDTOList);
            paginacionResponse.setTotalItems(tareaPage.getTotalElements());

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "Se han encontrado un total de: " + tareaDTOList.size() + " de: " + tareaPage.getTotalElements() + " elementos disponibles. Consulta realizada por: " +
                            usuarioSistemaLogin.getUserName() + " con identificación: " + usuarioSistemaLogin.getNumeroDeDocumento()
                            + " (" + usuarioSistemaLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría - formato corregido
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String mensajeAuditoria = "Se han encontrado un total de " + tareaPage.getTotalElements() + " tareas enviadas del tipo " + tipo + " del sistema con " + 
                                    nombreUsuarioResponsable + " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<String>> obtenerTipoTareasEnviadasPorUsuarioRol(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<String>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

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
                    Sort.by("fechaCreacion").descending()
            );

            List<String> tipos = this.procesoRepository.obtenerListaDeProcesosHabilitados();

            if (tipos.isEmpty()) {
                tipos = new ArrayList<>();
            }

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "Se han encontrado los siguientes registros. Consulta realizada por: " +
                            usuarioSistemaLogin.getUserName() + " con identificación: " + usuarioSistemaLogin.getNumeroDeDocumento()
                            + " (" + usuarioSistemaLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría - formato corregido
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String mensajeAuditoria = "Se han encontrado un total de " + tipos.size() + " tipos de procesos habilitados del sistema con " + 
                                    nombreUsuarioResponsable + " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, tipos, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> obtenerTareasRecibidas(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

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
                    Sort.by("fecha_creacion").descending()
                    //Sort.by("fechaCreacion").descending()
            );

            //Page<TareaUsuario> tareaUsuarioPage = this.tareaUsuarioRepository.findByUsuarioSistemaTokenIdentificadorAndTareaEstadoInAndTareaRemovidoAndRemovido(df2.getData().getUsuarioSistema().getTokenIdentificador(), Arrays.asList("En curso", "Rechazada"), false, false, pageable);
            Page<TareaUsuario> tareaUsuarioPage = this.tareaUsuarioRepository.obtenerTareasPorEstadosYTokenCentroYTokenUsuarioYRemovido(
                    Arrays.asList("En curso", "Rechazada"),
                    paginacionRequest.getTokenIdentificador(),
                    df2.getData().getUsuarioSistema().getTokenIdentificador(),
                    false,
                    pageable
            );

            Page<Tarea> tareaPage = tareaUsuarioPage.map(TareaUsuario::getTarea);

            PaginacionResponse<TareaDTO> paginacionResponse = new PaginacionResponse<>();
            List<TareaDTO> tareaDTOList = new ArrayList<>();

            for (Tarea tarea : tareaPage.toList()) {
                TareaDTO tareaDTO = this.entidadATareaDto(tarea);
                tareaDTO.setTipo(tarea.getPaso().getProceso().getNombre());
                tareaDTO.setDescripcion(tarea.getPaso().getNombre());
                tareaDTOList.add(tareaDTO);
            }

            tareaDTOList.sort(
                    Comparator.comparing(TareaDTO::getFechaCreacion).reversed()
            );

            paginacionResponse.setData(tareaDTOList);
            paginacionResponse.setTotalItems(tareaPage.getTotalElements());

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "Se han encontrado un total de: " + tareaDTOList.size() + " de: " + tareaPage.getTotalElements() + " elementos disponibles. Consulta realizada por: " +
                            usuarioSistemaLogin.getUserName() + " con identificación: " + usuarioSistemaLogin.getNumeroDeDocumento()
                            + " (" + usuarioSistemaLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría - formato corregido
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String mensajeAuditoria = "Se han encontrado un total de " + tareaPage.getTotalElements() + " tareas recibidas del sistema con " + 
                                    nombreUsuarioResponsable + " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> obtenerTareasBorrador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<TareaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

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
                    Sort.by("fecha_creacion").descending()
                    //Sort.by("fechaCreacion").descending()
            );

            //Page<TareaUsuario> tareaUsuarioPage = this.tareaUsuarioRepository.findByUsuarioSistemaTokenIdentificadorAndTareaEstadoInAndTareaRemovidoAndRemovido(df2.getData().getUsuarioSistema().getTokenIdentificador(), Arrays.asList("Borrador"), false, false, pageable);
            Page<TareaUsuario> tareaUsuarioPage = this.tareaUsuarioRepository.obtenerTareasPorEstadosYTokenCentroYTokenUsuarioYRemovido(
                    List.of("Borrador"),
                    paginacionRequest.getTokenIdentificador(),
                    df2.getData().getUsuarioSistema().getTokenIdentificador(),
                    false,
                    pageable
            );

            Page<Tarea> tareaPage = tareaUsuarioPage.map(TareaUsuario::getTarea);

            PaginacionResponse<TareaDTO> paginacionResponse = new PaginacionResponse<>();
            List<TareaDTO> tareaDTOList = new ArrayList<>();

            for (Tarea tarea : tareaPage.toList()) {
                TareaDTO tareaDTO = this.entidadATareaDto(tarea);
                tareaDTO.setTipo(tarea.getPaso().getProceso().getNombre());
                tareaDTO.setDescripcion(tarea.getPaso().getNombre());
                tareaDTOList.add(tareaDTO);
            }

            tareaDTOList.sort(
                    Comparator.comparing(TareaDTO::getFechaCreacion).reversed()
            );

            paginacionResponse.setData(tareaDTOList);
            paginacionResponse.setTotalItems(tareaPage.getTotalElements());

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "Se han encontrado un total de: " + tareaDTOList.size() + " de: " + tareaPage.getTotalElements() + " elementos disponibles. Consulta realizada por: " +
                            usuarioSistemaLogin.getUserName() + " con identificación: " + usuarioSistemaLogin.getNumeroDeDocumento()
                            + " (" + usuarioSistemaLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría - formato corregido
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String mensajeAuditoria = "Se han encontrado un total de " + tareaPage.getTotalElements() + " tareas borrador del sistema con " + 
                                    nombreUsuarioResponsable + " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<TareaDTO>> obtenerTareasInstanciaProcesoPorTarea(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<TareaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            TareaDTO tareaEntranteDTO = new Gson().fromJson(bodyDecifrado, TareaDTO.class);

            if (tareaEntranteDTO == null) {
                df.setMensaje("Tarea entrante incorrecta");
                return df;
            }

            Tarea tareaEntrante = this.tareaRepository.findByTokenIdentificador(tareaEntranteDTO.getTokenIdentificador());

            if (tareaEntrante == null) {
                df.setMensaje("Tarea incorrecta");
                return df;
            }

            List<Tarea> tareasEncontradas = this.tareaRepository.findByInstanciaProcesoTokenIdentificadorOrderByOrdenAsc(tareaEntrante.getInstanciaProceso().getTokenIdentificador());

            if (tareasEncontradas.isEmpty()) {
                df.setMensaje("No se han encontrado tareas asociadas a la tarea");
                return df;
            }

            List<TareaDTO> listaTareasDTO = new ArrayList<>();

            for (Tarea tarea : tareasEncontradas) {
                TareaDTO dto = entidadATareaDto(tarea);
                dto.setTipo(tarea.getPaso().getProceso().getNombre());
                listaTareasDTO.add(dto);
            }

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "Se han encontrado las siguientes tareas:. Consulta realizada por: " +
                            usuarioSistemaLogin.getUserName() + " con identificación: " + usuarioSistemaLogin.getNumeroDeDocumento()
                            + " (" + usuarioSistemaLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría - formato corregido
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            String mensajeAuditoria = "Se han encontrado un total de " + listaTareasDTO.size() + " tareas asociadas a la instancia de proceso con " + 
                                    nombreUsuarioResponsable + " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, listaTareasDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<InstanciaProcesoDTO> eliminarInstanciaProcesoPorTarea(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<InstanciaProcesoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDecifrado = df22.getData();

            TareaDTO tareaEntranteDTO = new Gson().fromJson(bodyDecifrado, TareaDTO.class);

            if (tareaEntranteDTO == null) {
                df.setMensaje("Tarea entrante incorrecta");
                return df;
            }

            Tarea tareaEntrante = this.tareaRepository.findByTokenIdentificadorAndRemovido(tareaEntranteDTO.getTokenIdentificador(), false);

            if (!tareaEntrante.getEstado().contains("Borrador")) {
                df.setMensaje("El flujo no puede ser eliminado en el estado que se encuentra actualmente");
                return df;
            }

            InstanciaProceso instanciaProceso = this.instanciaProcesoRepository.findByTokenIdentificadorAndRemovido(tareaEntrante.getInstanciaProceso().getTokenIdentificador(), false);

            instanciaProceso.setRemovido(true);
            instanciaProceso.setFechaEliminacion(new Date());
            instanciaProceso.setIpElimina(httpServletRequest.getRemoteAddr());
            instanciaProceso.setUsuarioSistemaElimina(df2.getData().getUsuarioSistema());

            instanciaProceso = this.instanciaProcesoRepository.save(instanciaProceso);

            List<Tarea> tareasRelacionadas = this.tareaRepository.findByInstanciaProcesoTokenIdentificador(instanciaProceso.getTokenIdentificador());

            for (Tarea tareaRelacionada : tareasRelacionadas) {
                tareaRelacionada.setRemovido(true);
                tareaRelacionada.setIpElimina(httpServletRequest.getRemoteAddr());
                tareaRelacionada.setFechaEliminacion(new Date());
                tareaRelacionada.setUsuarioSistemaElimina(df2.getData().getUsuarioSistema());
            }
            this.tareaRepository.saveAll(tareasRelacionadas);

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioSistemaLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje original para el usuario (mantener como estaba)
            String mensajeUsuario = "Se ha eliminado el registro seleccionado." + instanciaProceso.getProceso();

            // Mensaje para auditoría - formato corregido
            String mensajeAuditoria = "Se eliminó con éxito la instancia del proceso " + instanciaProceso.getProceso().getNombre() + 
                                    " del " + fechaFormateada + " con " + nombreUsuarioResponsable + 
                                    " (" + usuarioSistemaLogin.getNumeroDeDocumento() + ")";

            df.llenarRespuestaExitosa(mensajeUsuario, entidadAInstanciaDto(instanciaProceso), mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    @Transactional
    public Tarea completarTareaActualEIniciarSiguiente(BodyJwtValido jwt, InstanciaProceso instancia, String tokenIdDocumento, String html) {
        try {
            Tarea tareaEnCurso = this.tareaRepository.obtenerTareaEnCursoPorIdInstanciaProceso(instancia.getIdInstanciaProceso());

            if (tareaEnCurso == null) {
                // Ya no existen tareas en curso
                return null;
            }

            // Completar tarea actual
            tareaEnCurso.setEstado("Completada");
            tareaEnCurso.setRolUsuarioEnvia(jwt.getUsuarioSistema().getNumeroDeDocumento());
            tareaEnCurso.setUrl(tareaEnCurso.getPaso().getUrl() + "/" + tokenIdDocumento);
            tareaEnCurso.setFechaEdicion(new Date());
            this.tareaRepository.save(tareaEnCurso);

            // Crear proxima tarea y definirla en Curso
            Tarea tareaPendiente = this.tareaRepository.obtenerTareaPendientePorIdInstanciaProceso(instancia.getIdInstanciaProceso());

            if (tareaPendiente == null) {
                return tareaEnCurso;
                //En este caso ya no existen tareas a ser completadas, por lo tanto, completar InstanciaProceso
            }

            tareaPendiente.setEstado("En curso");
            tareaPendiente.setRolUsuarioRecibe(tareaPendiente.getPaso().getRolUsuarioNotificacion());
            tareaPendiente.setUrl(tareaPendiente.getPaso().getUrl() + "/" + tokenIdDocumento);
            tareaPendiente.setFechaEdicion(new Date());
            this.tareaRepository.save(tareaPendiente);

            List<String> correos = new ArrayList<>();
            correos.add(jwt.getUsuarioSistema().getEmail());

            if (tareaEnCurso.getPaso().getRequiereNotificacionCorreo()) {
                UsuarioSistema usuarioEncontrado = this.usuarioSistemaRepository.findByNumeroDeDocumentoAndRemovido(tareaEnCurso.getPaso().getRolUsuarioNotificacion(), false);
                if (usuarioEncontrado != null) {
                    correos = new ArrayList<>();
                    correos.add(usuarioEncontrado.getEmail());
                    this.emailService.enviarCorreo(correos, "RECIBE NOTIFICACION: " + tareaEnCurso.getPaso().getProceso().getNombre() + " - " + tareaPendiente.getPaso().getNombre(), "Tiene un proceso pendiente, revise su bandeja de entrada<br>" + html, jwt.getEmpresa().getTokenIdentificador(), "text/html", null);
                }
            }

            return tareaPendiente;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    @Transactional
    public Tarea rechazarTareaActual(BodyJwtValido jwt, InstanciaProceso instancia, String tokenIdDocumento) {
        try {
            Tarea tareaEnCurso = this.tareaRepository.obtenerTareaEnCursoPorIdInstanciaProceso(instancia.getIdInstanciaProceso());

            if (tareaEnCurso == null || tareaEnCurso.getVariableProceso() == null) {
                // Ya no existen tareas en curso o tarea en curso no tiene condicional
                return null;
            }

            tareaEnCurso.setEstado("Pendiente");
            tareaEnCurso.setFechaEdicion(new Date());
            tareaEnCurso.setRolUsuarioEnvia(jwt.getUsuarioSistema().getNumeroDeDocumento());
            this.tareaRepository.save(tareaEnCurso);

            // Se evalúa el condicional del paso y se retorna a tarea respectiva
            String jsonCondicional = tareaEnCurso.getPaso().getJsonCondicional();

            JSONObject json = new JSONObject(jsonCondicional);
            String idPaso = json.getJSONObject("falso").getString("idPaso");

            Long idPasoIr = Long.parseLong(idPaso);

            Tarea tareaIr = this.tareaRepository.findByPasoIdPasoAndInstanciaProcesoIdInstanciaProcesoAndRemovido(idPasoIr, instancia.getIdInstanciaProceso(), false);
            tareaIr.setEstado("En curso");
            tareaIr.setRolUsuarioRecibe(tareaIr.getPaso().getRolUsuarioNotificacion());
            tareaIr.setRolUsuarioEnvia("");
            this.tareaRepository.save(tareaIr);

            List<String> correos = new ArrayList<>();
            correos.add(jwt.getUsuarioSistema().getEmail());
            this.emailService.enviarCorreo(correos, "ENVIO NOTIFICACION RECHAZO: " + tareaEnCurso.getPaso().getProceso().getNombre() + " - " + tareaIr.getPaso().getNombre(), "Ha realizado el rechazo de un proceso, revise su bandeja de salida.", jwt.getEmpresa().getTokenIdentificador(), "text/html", null);

            if (tareaEnCurso.getPaso().getRequiereNotificacionCorreo()) {
                UsuarioSistema usuarioEncontrado = this.usuarioSistemaRepository.findByNumeroDeDocumentoAndRemovido(tareaEnCurso.getPaso().getRolUsuarioNotificacion(), false);
                if (usuarioEncontrado != null) {
                    correos = new ArrayList<>();
                    correos.add(usuarioEncontrado.getEmail());
                    this.emailService.enviarCorreo(correos, "RECIBE NOTIFICACION RECHAZO: " + tareaEnCurso.getPaso().getProceso().getNombre() + " - " + tareaIr.getPaso().getNombre(), "Tiene un proceso rechazado, revise su bandeja de entrada.", jwt.getEmpresa().getTokenIdentificador(), "text/html", null);
                }
            }

            return tareaIr;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Formatea una fecha al español en el formato: "viernes, 30 de mayo del 2025"
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) {
            return "fecha no disponible";
        }

        try {
            // Configurar el locale para español
            Locale localeEspanol = new Locale("es", "ES");

            // Crear el formato personalizado
            SimpleDateFormat formatoCompleto = new SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy", localeEspanol);

            return formatoCompleto.format(fecha);
        } catch (Exception e) {
            // En caso de error, devolver un formato simple
            SimpleDateFormat formatoSimple = new SimpleDateFormat("dd/MM/yyyy");
            return formatoSimple.format(fecha);
        }
    }

    /**
     * Método auxiliar para obtener nombres completos de un UsuarioSistema
     */
    private String obtenerNombreCompletoUsuarioSistema(UsuarioSistema usuario) {
        if (usuario == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (usuario.getNombres() != null && !usuario.getNombres().trim().isEmpty()) {
            nombreCompleto.append(usuario.getNombres());
        }
        if (usuario.getApellidos() != null && !usuario.getApellidos().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(usuario.getApellidos());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    private Proceso dtoAEntidadProceso(ProcesoDTO dto) {
        if (dto == null) return null;

        Proceso procesoEncontrado = this.procesoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
        Proceso proceso = Objects.requireNonNullElseGet(procesoEncontrado, Proceso::new);

        proceso.setNombre(dto.getNombre());
        proceso.setVersion(dto.getVersion());
        proceso.setNemonico(dto.getNemonico());

        if (dto.getPasos() != null) {
            List<Paso> listaExistente = this.pasoRepository.findByProcesoTokenIdentificadorAndRemovido(proceso.getTokenIdentificador(), false);
            List<Paso> listaEntrante = dto.getPasos().stream()
                    .map(this::dtoAEntidadPaso)
                    .toList();

            // Mapear la lista existente en un Map para búsqueda rápida por UUID
            Map<String, Paso> mapaExistentes = listaExistente.stream()
                    .collect(Collectors.toMap(Paso::getTokenIdentificador, Function.identity()));

            List<Paso> listaFinal = new ArrayList<>();

            for (Paso pasoEntrante : listaEntrante) {
                if (mapaExistentes.containsKey(pasoEntrante.getTokenIdentificador())) {
                    // El paso ya existe, actualizar sus valores
                    Paso pasoExistente = mapaExistentes.get(pasoEntrante.getTokenIdentificador());
                    pasoExistente.setNombre(pasoEntrante.getNombre());
                    pasoExistente.setUrl(pasoEntrante.getUrl());
                    pasoExistente.setPorcentajeAvance(pasoEntrante.getPorcentajeAvance());
                    pasoExistente.setOrden(pasoEntrante.getOrden());
                    pasoExistente.setJsonCondicional(pasoEntrante.getJsonCondicional());
                    pasoExistente.setRolUsuario(pasoEntrante.getRolUsuario());
                    pasoExistente.setRequiereNotificacionCorreo(pasoEntrante.getRequiereNotificacionCorreo());
                    pasoExistente.setRolUsuarioNotificacion(pasoEntrante.getRolUsuarioNotificacion());
                    pasoExistente.setOmitePaso(pasoEntrante.getOmitePaso());
                    pasoExistente.setRemovido(false); // Reactivarlo en caso de que estuviera eliminado
                    listaFinal.add(pasoExistente);
                } else {
                    // Es un paso nuevo, agregarlo
                    pasoEntrante.setProceso(proceso);
                    pasoEntrante.setRemovido(false);
                    listaFinal.add(pasoEntrante);
                }
            }

            // Marcar como removidos los pasos que ya no están en la lista entrante
            for (Paso pasoExistente : listaExistente) {
                if (listaEntrante.stream().noneMatch(p -> p.getTokenIdentificador().equals(pasoExistente.getTokenIdentificador()))) {
                    pasoExistente.setRemovido(true);
                    listaFinal.add(pasoExistente);
                }
            }

            // Asignar la lista final al proceso
            proceso.setPasos(listaFinal);
        }

        return proceso;
    }

    private Paso dtoAEntidadPaso(PasoDTO dto) {
        if (dto == null) return null;

        Paso pasoEncontrado = this.pasoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
        Paso paso = Objects.requireNonNullElseGet(pasoEncontrado, Paso::new);

        paso.setNombre(dto.getNombre());
        paso.setUrl(dto.getUrl());
        paso.setPorcentajeAvance(dto.getPorcentajeAvance());
        paso.setOrden(dto.getOrden());
        paso.setJsonCondicional(dto.getJsonCondicional());
        paso.setRolUsuario(dto.getRolUsuario());
        paso.setRequiereNotificacionCorreo(dto.getRequiereNotificacionCorreo());
        paso.setRolUsuarioNotificacion(dto.getRolUsuarioNotificacion());
        if (dto.getPasoSalto() == null) {
            paso.setPasoSubsanacion(null);
        }
        paso.setOmitePaso(dto.getOmitePaso());

        if (dto.getPasoUsuarioList() != null) {
            List<PasoUsuario> listaExistente = this.pasoUsuarioRepository.findByPasoTokenIdentificadorAndRemovido(paso.getTokenIdentificador(), false);
            List<PasoUsuario> listaEntrante = dto.getPasoUsuarioList().stream()
                    .map(this::dtoAEntidadPasoUsuario)
                    .toList();

            // Mapear la lista existente en un Map para búsqueda rápida por UUID
            Map<String, PasoUsuario> mapaExistentes = listaExistente.stream()
                    .collect(Collectors.toMap(PasoUsuario::getTokenIdentificador, Function.identity()));

            List<PasoUsuario> listaFinal = new ArrayList<>();

            for (PasoUsuario pasoUsuarioEntrante : listaEntrante) {
                if (mapaExistentes.containsKey(pasoUsuarioEntrante.getTokenIdentificador())) {
                    // El paso ya existe, actualizar sus valores
                    PasoUsuario pasoUsuarioExistente = mapaExistentes.get(pasoUsuarioEntrante.getTokenIdentificador());
                    pasoUsuarioExistente.setRemovido(false); // Reactivarlo en caso de que estuviera eliminado
                    listaFinal.add(pasoUsuarioExistente);
                } else {
                    // Es un paso nuevo, agregarlo
                    pasoUsuarioEntrante.setPaso(paso);
                    pasoUsuarioEntrante.setRemovido(false);
                    listaFinal.add(pasoUsuarioEntrante);
                }
            }

            // Marcar como removidos los pasos que ya no están en la lista entrante
            for (PasoUsuario pasoUsuarioExistente : listaExistente) {
                if (listaEntrante.stream().noneMatch(p -> p.getTokenIdentificador().equals(pasoUsuarioExistente.getTokenIdentificador()))) {
                    pasoUsuarioExistente.setRemovido(true);
                    listaFinal.add(pasoUsuarioExistente);
                }
            }

            // Asignar la lista final al proceso
            paso.setPasoUsuarioList(listaFinal);
        }

        return paso;
    }

    private PasoRol dtoAEntidadPasoRol(PasoRolDTO dto) {
        if (dto == null) return null;

        PasoRol entidad = new PasoRol();

        Rol rolEncontrado = this.rolRepository.findByTokenIdentificadorAndRemovido(dto.getRol().getTokenIdentificador(), false);
        entidad.setRol(rolEncontrado);

        return entidad;
    }

    private PasoUsuario dtoAEntidadPasoUsuario(PasoUsuarioDTO dto) {
        if (dto == null) return null;

        PasoUsuario pasoUsuarioEncontrado = this.pasoUsuarioRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
        PasoUsuario entidad = Objects.requireNonNullElseGet(pasoUsuarioEncontrado, PasoUsuario::new);

        UsuarioSistema usuarioSistemaEncontrado = this.usuarioSistemaRepository.findByTokenIdentificador(dto.getUsuarioSistema().getTokenIdentificador());
        entidad.setUsuarioSistema(usuarioSistemaEncontrado);

        return entidad;
    }

    private ProcesoDTO entidadAProcesoDto(Proceso proceso) {
        if (proceso == null) return null;

        ProcesoDTO dto = new ProcesoDTO();
        dto.setIdProceso(proceso.getIdProceso());
        dto.setNombre(proceso.getNombre());
        dto.setVersion(proceso.getVersion());
        dto.setNemonico(proceso.getNemonico());
        dto.setTokenIdentificador(proceso.getTokenIdentificador());
        dto.setFechaCreacion(proceso.getFechaCreacion());

        if (proceso.getPasos() != null) {
            List<PasoDTO> pasos = proceso.getPasos().stream()
                    .map(this::entidadAPasoDto)
                    .toList();
            dto.setPasos(pasos);
        }

        return dto;
    }

    private PasoDTO entidadAPasoDto(Paso paso) {
        if (paso == null) return null;

        PasoDTO dto = new PasoDTO();
        dto.setIdPaso(paso.getIdPaso());
        dto.setNombre(paso.getNombre());
        dto.setUrl(paso.getUrl());
        dto.setPorcentajeAvance(paso.getPorcentajeAvance());
        dto.setOrden(paso.getOrden());
        dto.setJsonCondicional(paso.getJsonCondicional());
        dto.setRolUsuario(paso.getRolUsuario());
        dto.setRolUsuarioNotificacion(paso.getRolUsuarioNotificacion());
        dto.setRequiereNotificacionCorreo(paso.getRequiereNotificacionCorreo());
        dto.setTokenIdentificador(paso.getTokenIdentificador());
        if (paso.getPasoSubsanacion() != null) {
            dto.setPasoSubsanacion(entidadAPasoDto(paso.getPasoSubsanacion()));
            dto.setPasoSalto(dto.getPasoSubsanacion().getOrden());
        }
        dto.setOmitePaso(paso.getOmitePaso());
        dto.setIcono(paso.getIcono());
        if (paso.getRemovido() == null) paso.setRemovido(false);
        dto.setRemovido(paso.getRemovido());

        if (paso.getPasoUsuarioList() != null) {
            List<PasoUsuarioDTO> usuarioListDTO = paso.getPasoUsuarioList().stream()
                    .filter(pasoUsuario -> !pasoUsuario.getRemovido())
                    .map(this::entidadAPasoUsuarioDto)
                    .toList();
            dto.setPasoUsuarioList(usuarioListDTO);
        }

        return dto;
    }

    private PasoUsuarioDTO entidadAPasoUsuarioDto(PasoUsuario entidad) {
        if (entidad == null) return null;

        PasoUsuarioDTO dto = new PasoUsuarioDTO();
        dto.setTokenIdentificador(entidad.getTokenIdentificador());

        UsuarioSistemaDTO usuarioSistemaDTO = new UsuarioSistemaDTO();
        if (entidad.getUsuarioSistema().getNombres() != null)
            usuarioSistemaDTO.setNombres(entidad.getUsuarioSistema().getNombres());
        if (entidad.getUsuarioSistema().getApellidos() != null)
            usuarioSistemaDTO.setApellidos(entidad.getUsuarioSistema().getApellidos());
        usuarioSistemaDTO.setUserName(entidad.getUsuarioSistema().getUserName());
        usuarioSistemaDTO.setEmail(entidad.getUsuarioSistema().getEmail());
        usuarioSistemaDTO.setTokenIdentificador(entidad.getUsuarioSistema().getTokenIdentificador());

        dto.setUsuarioSistema(usuarioSistemaDTO);

        return dto;
    }

    private PasoRolDTO entidadAPasoRolDto(PasoRol entidad) {
        if (entidad == null) return null;

        PasoRolDTO dto = new PasoRolDTO();

        RolDTO rolDTO = new RolDTO();
        rolDTO.setNombre(entidad.getRol().getNombre());
        rolDTO.setCodigo(entidad.getRol().getCodigo());
        rolDTO.setTokenIdentificador(entidad.getRol().getTokenIdentificador());

        return dto;
    }

    private InstanciaProceso dtoAEntidadInstancia(InstanciaProcesoDTO dto) {
        if (dto == null) return null;

        InstanciaProceso entidad = new InstanciaProceso();
        entidad.setIdInstanciaProceso(dto.getIdInstanciaProceso());
        entidad.setEstado(dto.getEstado());
        entidad.setDescripcion(dto.getDescripcion());
        entidad.setTokenIdentificador(dto.getTokenIdentificador());
        entidad.setFechaCreacion(dto.getFechaCreacion());
        entidad.setProceso(this.dtoAEntidadProceso(dto.getProceso()));

        List<Tarea> tareas = dto.getTareas().stream()
                .map(this::dtoAEntidadTarea)
                .toList();
        tareas.forEach(tarea -> tarea.setInstanciaProceso(entidad));
        entidad.setTareas(tareas);

        return entidad;
    }

    private Tarea dtoAEntidadTarea(TareaDTO dto) {
        if (dto == null) return null;

        Tarea entidad = new Tarea();
        entidad.setIdTarea(dto.getIdTarea());
        entidad.setEstado(dto.getEstado());
        entidad.setComentario(dto.getComentario());
        entidad.setComentarioRechazo(dto.getComentarioRechazo());
        entidad.setOrden(dto.getOrden());
        entidad.setUrl(dto.getUrl());
        entidad.setRolUsuarioRecibe(dto.getRolUsuarioRecibe());
        entidad.setRolUsuarioEnvia(dto.getRolUsuarioEnvia());
        entidad.setNombreProceso(dto.getNombreProceso());
        entidad.setPaso(dtoAEntidadPaso(dto.getPaso()));
        entidad.setTokenIdentificador(dto.getTokenIdentificador());
        entidad.setFechaCreacion(dto.getFechaCreacion());
        entidad.setFechaEdicion(dto.getFechaEdicion());
        return entidad;
    }

    private InstanciaProcesoDTO entidadAInstanciaDto(InstanciaProceso entidad) {
        if (entidad == null) return null;

        InstanciaProcesoDTO dto = new InstanciaProcesoDTO();
        dto.setIdInstanciaProceso(entidad.getIdInstanciaProceso());
        dto.setEstado(entidad.getEstado());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setProceso(this.entidadAProcesoDto(entidad.getProceso()));

        if (entidad.getTareas() != null) {
            List<TareaDTO> tareas = entidad.getTareas().stream()
                    .map(this::entidadATareaDto)
                    .toList();
            dto.setTareas(tareas);
        }

        return dto;
    }

    private TareaDTO entidadATareaDto(Tarea entidad) {
        if (entidad == null) return null;

        TareaDTO dto = new TareaDTO();
        dto.setIdTarea(entidad.getIdTarea());
        dto.setEstado(entidad.getEstado());
        dto.setComentario(entidad.getComentario());
        dto.setComentarioRechazo(entidad.getComentarioRechazo());
        dto.setOrden(entidad.getOrden());
        dto.setUrl(entidad.getUrl());
        dto.setRolUsuarioRecibe(entidad.getRolUsuarioRecibe());
        dto.setRolUsuarioEnvia(entidad.getRolUsuarioEnvia());
        dto.setNombreProceso(entidad.getNombreProceso());
        dto.setPaso(entidadAPasoDto(entidad.getPaso()));
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setFechaEdicion(entidad.getFechaEdicion());
        return dto;
    }
}