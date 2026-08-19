package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@AllArgsConstructor
public class SeguimientoSocialServiceImpl implements SeguimientoSocialService {
   
   private ParametroDelSistemaRepository parametroDelSistemaRepository;
   private JwtProviderService jwtProviderService;
   private FichaIdentificacionRepository fichaIdentificacionRepository;
   private CatalogoRepository catalogoRepository;
   private SeguimientoSocialRepository seguimientoSocialRepository;
   private JerarquiaRepository jerarquiaRepository;
    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;
    private DocumentoService documentoService;
    private DocumentoRepository documentoRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private SeguimientoSocialCarpetaRepository seguimientoCarpetaRepository;
    private SeguimientoSocialDocumentoRepository seguimientoDocumentoRepository;
    // Variable para protección contra duplicados
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();

    private PermisoRolUsuarioService permisoRolUsuarioService;

    /**
     * Método auxiliar para obtener nombres completos desde FichaIdentificacion
     */
    private String obtenerNombresCompletos(FichaIdentificacion fichaIdentificacion) {
        if (fichaIdentificacion == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (fichaIdentificacion.getNombres() != null && !fichaIdentificacion.getNombres().trim().isEmpty()) {
            nombreCompleto.append(fichaIdentificacion.getNombres());
        }
        if (fichaIdentificacion.getApellidoPaterno() != null && !fichaIdentificacion.getApellidoPaterno().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(fichaIdentificacion.getApellidoPaterno());
        }
        if (fichaIdentificacion.getApellidoMaterno() != null && !fichaIdentificacion.getApellidoMaterno().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(fichaIdentificacion.getApellidoMaterno());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener la identificación de una persona desde su ficha
     */
    private String obtenerIdentificacionPersona(FichaIdentificacion fichaIdentificacion) {
        if (fichaIdentificacion == null) {
            return "N/A";
        }

        String identificacion = "N/A";
        
        if (fichaIdentificacion.getDni() != null && !fichaIdentificacion.getDni().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getDni();
        }
        else if (fichaIdentificacion.getNumeroIdentificacion() != null && !fichaIdentificacion.getNumeroIdentificacion().trim().isEmpty()) {
            identificacion = fichaIdentificacion.getNumeroIdentificacion();
        }
        else {
            String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = nombresCompletos;
            }
        }

        return identificacion;
    }

    /**
     * Método auxiliar para formatear fechas
     */
    private String formatearFecha(Date fecha) {
        if (fecha == null) {
            return "fecha no especificada";
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(fecha);
    }

    /**
     * Formatea una fecha para mostrarla en español en los mensajes de auditoría
     * @param fecha La fecha a formatear
     * @return String con formato "martes, 2 de enero del 2025"
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) return "";
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy", new Locale("es", "ES"));
        return formatter.format(fecha);
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoSocialDTO>> obtenerSeguimientosSociales(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<SeguimientoSocialDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            Empresa empresa = df2.getData().getEmpresa();

            // Usar el filtro si existe
            String filtro = paginacionRequest.getFilter() != null ? paginacionRequest.getFilter() : "";

            // Variable para almacenar el resultado
            Page<SeguimientoSocial> seguimientoSocialPage;

            // Manejar ordenamiento especial para campos calculados
            if (paginacionRequest.getSort() != null && 
                (esOrdenamientoEspecial(paginacionRequest.getSort()))) {

                String direccion = paginacionRequest.getDirection() != null ? 
                    paginacionRequest.getDirection().toUpperCase() : "ASC";

                // Crear pageable para paginación (sin ordenamiento ya que se maneja en la consulta)
                Pageable pageable = PageRequest.of(paginacionRequest.getPage(), paginacionRequest.getSize());

                seguimientoSocialPage = obtenerConOrdenamientoEspecial(
                    paginacionRequest.getTokenIdentificador(),
                    empresa.getIdEmpresa(),
                    filtro,
                    paginacionRequest.getSort(),
                    direccion,
                    pageable
                );

            } else {
                // Ordenamiento normal para otros campos
                Pageable pageable;

                if (paginacionRequest.getSort() != null && !paginacionRequest.getSort().isEmpty() 
                        && paginacionRequest.getDirection() != null && !paginacionRequest.getDirection().isEmpty()) {

                    // Mapear los campos del frontend a los campos reales de la entidad
                    String campoOrdenamiento = mapearCampoOrdenamiento(paginacionRequest.getSort());

                    Sort.Direction direction = paginacionRequest.getDirection().equalsIgnoreCase("asc") 
                            ? Sort.Direction.ASC : Sort.Direction.DESC;

                    pageable = PageRequest.of(
                            paginacionRequest.getPage(),
                            paginacionRequest.getSize(),
                            Sort.by(direction, campoOrdenamiento)
                    );
                } else {
                    // Ordenamiento por defecto
                    pageable = PageRequest.of(
                            paginacionRequest.getPage(),
                            paginacionRequest.getSize(),
                            Sort.by("idSeguimientoSocial").descending()
                    );
                }

                // Usar el método de búsqueda normal con filtro
                seguimientoSocialPage = this.seguimientoSocialRepository.buscarPorFiltro(
                        paginacionRequest.getTokenIdentificador(), 
                        empresa.getIdEmpresa(), 
                        filtro, 
                        pageable);
            }

            // Convertir entidades a DTOs
            PaginacionResponse<SeguimientoSocialDTO> paginacionResponse = new PaginacionResponse<>();
            List<SeguimientoSocialDTO> seguimientoSocialDTOList = new ArrayList<>();

            // Obtener la ficha de identificación para el mensaje
            FichaIdentificacion fichaIdentificacion = null;
            if (!seguimientoSocialPage.isEmpty()) {
                fichaIdentificacion = seguimientoSocialPage.getContent().get(0).getFichaIdentificacion();
            } else {
                // Si no hay seguimientos, buscar la ficha directamente
                fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                        paginacionRequest.getTokenIdentificador(), Boolean.FALSE);
            }

            for (SeguimientoSocial seguimientoSocial : seguimientoSocialPage.getContent()) {
                SeguimientoSocialDTO seguimientoSocialDTO = new SeguimientoSocialDTO();
                seguimientoSocialDTO.setTokenIdentificador(seguimientoSocial.getTokenIdentificador());
                seguimientoSocialDTO.setTokenIdentificadorEmpresa(seguimientoSocial.getEmpresa().getTokenIdentificador());
                seguimientoSocialDTO.setFechaCreacion(seguimientoSocial.getFechaCreacion());
                seguimientoSocialDTO.setFecha(seguimientoSocial.getFecha());

                if(seguimientoSocial.getTipoActividadSocial() != null) {
                    seguimientoSocialDTO.setNemonicoTipoActividadSocial(
                        seguimientoSocial.getTipoActividadSocial().getNemonico());
                }

                seguimientoSocialDTO.setDescripcionSocial(seguimientoSocial.getDescripcionSocial());
                seguimientoSocialDTO.setAccionesAdoptadas(seguimientoSocial.getAccionesAdoptadas());
                seguimientoSocialDTO.setComentarios(seguimientoSocial.getComentarios());

                // Mapeo de centro
                if (seguimientoSocial.getPrograma() != null && 
                     seguimientoSocial.getPrograma().getJerarquiaPadre() != null) {
                     JerarquiaDTO centroDTO = new JerarquiaDTO();
                     Jerarquia centro = seguimientoSocial.getPrograma().getJerarquiaPadre();
                     centroDTO.setTokenIdentificador(centro.getTokenIdentificador());
                     centroDTO.setNombre(centro.getNombre());
                     seguimientoSocialDTO.setCentro(centroDTO);
                 }

                // Mapeo de programa
                if (seguimientoSocial.getPrograma() != null) {
                    JerarquiaDTO programaDTO = new JerarquiaDTO();
                    programaDTO.setTokenIdentificador(seguimientoSocial.getPrograma().getTokenIdentificador());
                    programaDTO.setNombre(seguimientoSocial.getPrograma().getNombre());
                    seguimientoSocialDTO.setPrograma(programaDTO);
                }

                // Mapeo de ambiente
                if (seguimientoSocial.getAmbiente() != null) {
                    JerarquiaDTO ambienteDTO = new JerarquiaDTO();
                    ambienteDTO.setTokenIdentificador(seguimientoSocial.getAmbiente().getTokenIdentificador());
                    ambienteDTO.setNombre(seguimientoSocial.getAmbiente().getNombre());
                    seguimientoSocialDTO.setAmbiente(ambienteDTO);
                }

                if (seguimientoSocial.getFichaIdentificacion() != null) {
                    seguimientoSocialDTO.setTokenFichaIdentificacion(seguimientoSocial.getFichaIdentificacion().getTokenIdentificador());
                }

                seguimientoSocialDTO.setNombreCompletoUsuarioCreacion(
                    seguimientoSocial.getUsuarioSistemaCrea().getNombres() + " " + 
                    seguimientoSocial.getUsuarioSistemaCrea().getApellidos());

                seguimientoSocialDTOList.add(seguimientoSocialDTO);
            }

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            seguimientoSocialDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            paginacionResponse.setData(seguimientoSocialDTOList);
            paginacionResponse.setTotalItems(seguimientoSocialPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + seguimientoSocialPage.getTotalElements() + " seguimientos sociales";

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
            String mensajeAuditoria = "Se han encontrado un total de " + seguimientoSocialPage.getTotalElements() + " seguimientos sociales registrados de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            ex.printStackTrace(); // Para debug
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Determina si el campo requiere ordenamiento especial
     * @param campo Campo a verificar
     * @return true si requiere ordenamiento especial
     */
    private boolean esOrdenamientoEspecial(String campo) {
        return "nombreCompletoUsuarioCreacion".equals(campo) ||
               "programa.nombre".equals(campo) ||
               "ambiente.nombre".equals(campo);
    }

    /**
     * Obtiene los datos con ordenamiento especial según el campo
     * @param tokenIdentificador Token de la ficha
     * @param idEmpresa ID de la empresa
     * @param filtro Filtro de búsqueda
     * @param campo Campo por el que ordenar
     * @param direccion Dirección del ordenamiento (ASC/DESC)
     * @param pageable Configuración de paginación
     * @return Page con los resultados ordenados
     */
    private Page<SeguimientoSocial> obtenerConOrdenamientoEspecial(
            String tokenIdentificador, 
            Long idEmpresa, 
            String filtro, 
            String campo, 
            String direccion, 
            Pageable pageable) {

        boolean esAscendente = "ASC".equals(direccion);
        boolean tieneFiltro = !filtro.isEmpty();

        switch (campo) {
            case "nombreCompletoUsuarioCreacion":
                if (tieneFiltro) {
                    return esAscendente ? 
                        seguimientoSocialRepository.buscarConFiltroOrdenadoPorUsuarioCreacionAsc(tokenIdentificador, idEmpresa, filtro, pageable) :
                        seguimientoSocialRepository.buscarConFiltroOrdenadoPorUsuarioCreacionDesc(tokenIdentificador, idEmpresa, filtro, pageable);
                } else {
                    return esAscendente ?
                        seguimientoSocialRepository.buscarOrdenadoPorUsuarioCreacionAsc(tokenIdentificador, idEmpresa, pageable) :
                        seguimientoSocialRepository.buscarOrdenadoPorUsuarioCreacionDesc(tokenIdentificador, idEmpresa, pageable);
                }

            case "programa.nombre":
                // Para programa, usar el filtro normal ya que las consultas especiales no tienen filtro implementado
                return esAscendente ?
                    seguimientoSocialRepository.buscarOrdenadoPorProgramaAsc(tokenIdentificador, idEmpresa, pageable) :
                    seguimientoSocialRepository.buscarOrdenadoPorProgramaDesc(tokenIdentificador, idEmpresa, pageable);

            case "ambiente.nombre":
                // Para ambiente, usar el filtro normal ya que las consultas especiales no tienen filtro implementado
                return esAscendente ?
                    seguimientoSocialRepository.buscarOrdenadoPorAmbienteAsc(tokenIdentificador, idEmpresa, pageable) :
                    seguimientoSocialRepository.buscarOrdenadoPorAmbienteDesc(tokenIdentificador, idEmpresa, pageable);

            default:
                // Fallback a búsqueda normal
                return seguimientoSocialRepository.buscarPorFiltro(tokenIdentificador, idEmpresa, filtro, pageable);
        }
    }

    /**
     * Mapea los campos del frontend a los campos reales de la entidad JPA
     * @param campoFrontend Campo solicitado desde el frontend
     * @return Campo real de la entidad que se puede usar para ordenamiento
     */
    private String mapearCampoOrdenamiento(String campoFrontend) {
        switch (campoFrontend) {
            case "fecha":
                return "fecha";
            case "fechaCreacion":
                return "fechaCreacion";
            case "numero":
                // Para ordenamiento por número, usar el ID (orden inverso)
                return "idSeguimientoSocial";
            default:
                // Si no se encuentra mapeo, usar fecha como default
                return "fecha";
        }
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarSeguimientoSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            SeguimientoSocialDTO seguimientoSocialDTO = new Gson()
                .fromJson(bodyString, SeguimientoSocialDTO.class);

            SeguimientoSocial seguimientoSocial = this.seguimientoSocialRepository
                .findByTokenIdentificadorAndRemovido(seguimientoSocialDTO.getTokenIdentificador(), false);

            if (seguimientoSocial == null) {
                df.setMensaje("El seguimiento social no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            String nombresCompletos = obtenerNombresCompletos(seguimientoSocial.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(seguimientoSocial.getFichaIdentificacion());
            String fechaFormateada = formatearFecha(seguimientoSocial.getFecha());
            String fechaFormateadaEspanol = formatearFechaEspanol(seguimientoSocial.getFecha());

            Date fecha = new Date();
            seguimientoSocial.setRemovido(true);
            seguimientoSocial.setIpElimina(ip);
            seguimientoSocial.setUsuarioSistemaElimina(usuarioSistemaLogin);
            seguimientoSocial.setFechaEliminacion(fecha);

            this.seguimientoSocialRepository.save(seguimientoSocial);

            // Mensaje para el usuario
            String mensajeUsuario;
            if (!"N/A".equals(nombresCompletos)) {
                mensajeUsuario = "Se eliminó con éxito el seguimiento social de " + nombresCompletos;
            } else {
                mensajeUsuario = "Se eliminó con éxito el seguimiento social";
            }

            // Mensaje para auditoría
            String mensajeAuditoria = "Se eliminó con éxito el seguimiento social del " + fechaFormateadaEspanol + " de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<SeguimientoSocialDTO> crearSeguimientoSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<SeguimientoSocialDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            String bodyString = df22.getData();

            Empresa empresa = df2.getData().getEmpresa();

            SeguimientoSocialDTO seguimientoSocialDTO = new Gson()
                .fromJson(bodyString, SeguimientoSocialDTO.class);

            seguimientoSocialDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            // Generar clave única para esta solicitud
            String requestKey = seguimientoSocialDTO.getTokenEvaluacion() + "_" + 
                                usuarioLogin.getIdUsuarioSistema() + "_" + 
                                (seguimientoSocialDTO.getEsEdicion() ? "edit" : "create");

            // Verificar si ya hay una solicitud en proceso con esta clave
            Long tiempoProcesamiento = solicitudesEnProcesamiento.get(requestKey);
            if (tiempoProcesamiento != null) {
                if (System.currentTimeMillis() - tiempoProcesamiento < 5000) {
                    df.setExito(false);
                    df.setMensaje("Una solicitud similar ya está siendo procesada. Por favor, espere unos segundos antes de intentar nuevamente.");
                    return df;
                }
            }

            // Registrar esta solicitud como en procesamiento
            solicitudesEnProcesamiento.put(requestKey, System.currentTimeMillis());

            try {
                SeguimientoSocial seguimientoSocial;
                FichaIdentificacion fichaIdentificacion = null;

                if(seguimientoSocialDTO.getEsEdicion()){
                    seguimientoSocial = seguimientoSocialRepository
                        .findByTokenIdentificadorAndRemovido(seguimientoSocialDTO.getTokenIdentificador(), Boolean.FALSE);
                    if (seguimientoSocial == null) {
                        df.setMensaje("El seguimiento social a editar no existe o ya fue eliminado anteriormente");
                        return df;
                    }
                    seguimientoSocial.setFechaEdicion(new Date());
                    seguimientoSocial.setIpEdita(ip);
                    seguimientoSocial.setUsuarioSistemaEdita(usuarioLogin);
                    fichaIdentificacion = seguimientoSocial.getFichaIdentificacion();
                } else {                
                    seguimientoSocial = new SeguimientoSocial();
                    seguimientoSocial.setFechaCreacion(new Date());
                    seguimientoSocial.setIpCrea(ip);
                    seguimientoSocial.setUsuarioSistemaCrea(usuarioLogin);
                    seguimientoSocial.setEmpresa(empresa);
                    fichaIdentificacion = fichaIdentificacionRepository
                        .findByTokenIdentificadorAndRemovido(seguimientoSocialDTO.getTokenEvaluacion(), Boolean.FALSE);
                    seguimientoSocial.setFichaIdentificacion(fichaIdentificacion);
                }

                seguimientoSocial.setFecha(seguimientoSocialDTO.getFecha());

                if (seguimientoSocialDTO.getNemonicoTipoActividadSocial() != null) {
                    Catalogo tipoActividadSocial = catalogoRepository
                        .findByNemonicoAndRemovido(seguimientoSocialDTO.getNemonicoTipoActividadSocial(), Boolean.FALSE);
                    seguimientoSocial.setTipoActividadSocial(tipoActividadSocial);
                }

                // Setear programa y ambiente
                if (seguimientoSocialDTO.getPrograma() != null) {
                    Jerarquia programa = jerarquiaRepository
                        .findByTokenIdentificadorAndRemovido(seguimientoSocialDTO.getPrograma().getTokenIdentificador(), Boolean.FALSE);
                    seguimientoSocial.setPrograma(programa);
                }

                if (seguimientoSocialDTO.getAmbiente() != null) {
                    Jerarquia ambiente = jerarquiaRepository
                        .findByTokenIdentificadorAndRemovido(seguimientoSocialDTO.getAmbiente().getTokenIdentificador(), Boolean.FALSE);
                    seguimientoSocial.setAmbiente(ambiente);
                }

                seguimientoSocial.setDescripcionSocial(seguimientoSocialDTO.getDescripcionSocial());
                seguimientoSocial.setAccionesAdoptadas(seguimientoSocialDTO.getAccionesAdoptadas());
                seguimientoSocial.setComentarios(seguimientoSocialDTO.getComentarios());

                seguimientoSocial = this.seguimientoSocialRepository.save(seguimientoSocial);
                seguimientoSocialDTO.setTokenIdentificador(seguimientoSocial.getTokenIdentificador());

                String nombresCompletos = obtenerNombresCompletos(fichaIdentificacion);
                String identificacionPersona = obtenerIdentificacionPersona(fichaIdentificacion);
                String fechaFormateada = formatearFecha(seguimientoSocial.getFecha());
                String fechaFormateadaEspanol = formatearFechaEspanol(seguimientoSocial.getFecha());
                String accion = seguimientoSocialDTO.getEsEdicion() ? "editó" : "creó";

                // Mensaje para el usuario
                String mensajeUsuario;
                if (!"N/A".equals(nombresCompletos)) {
                    mensajeUsuario = "Se " + accion + " con éxito el seguimiento social de " + nombresCompletos;
                } else {
                    mensajeUsuario = "Se " + accion + " con éxito el seguimiento social";
                }

                // Mensaje para auditoría
                String mensajeAuditoria = "Se " + accion + " con éxito el seguimiento social del " + fechaFormateadaEspanol + " de la persona con identificación: " + identificacionPersona;

                df.llenarRespuestaExitosa(mensajeUsuario, seguimientoSocialDTO, mensajeAuditoria);

            } finally {
                // Eliminar la entrada del mapa una vez procesada (ya sea con éxito o error)
                solicitudesEnProcesamiento.remove(requestKey);
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> subirDocumentos(HttpServletRequest httpServletRequest,
                                                                 BodyEncriptado bodyEncriptado,
                                                                 MultipartFile[] multipartFiles) {

        RespuestaPorDefectoAuditoria<Boolean> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setMensajeErrorReal(df2.getMensajeErrorReal());
                respuesta.setLogOut(true);
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String bodyDesencriptado = df22.getData();
            SeguimientoEducativoDTO seguimientoDTO = new Gson().fromJson(bodyDesencriptado, SeguimientoEducativoDTO.class);

            SeguimientoSocial seguimiento = this.seguimientoSocialRepository.findByTokenIdentificadorAndRemovido(
                    seguimientoDTO.getTokenIdentificadorSeguimiento(), false
            );

            if (seguimiento == null) {
                respuesta.setMensaje("No existe el registro solicitado");
                return respuesta;
            }

            SeguimientoSocialCarpeta registroCarpeta = this.seguimientoCarpetaRepository.findFirstBySeguimientoSocialTokenIdentificadorAndRemovido(seguimiento.getTokenIdentificador(), false);

            List<DocumentoDTO> documentoDTOList = seguimientoDTO.getDocumentoDTOList();

            String nombresCompletos = obtenerNombresCompletos(seguimiento.getFichaIdentificacion());
            String identificacionPersona = obtenerIdentificacionPersona(seguimiento.getFichaIdentificacion());
            
            String falloUsuario = !"N/A".equals(nombresCompletos) ? 
                "No se pudieron subir los documentos al seguimiento social de " + nombresCompletos :
                "No se pudieron subir los documentos al seguimiento social";
            
            String falloAuditoria = "No se pudieron subir los documentos al seguimiento social de la persona con identificación: " + identificacionPersona;
            
            if (registroCarpeta == null) {
                // Crear carpeta si no existe
                FichaIdentificacion fichaIdentificacion = seguimiento.getFichaIdentificacion();

                // Buscar carpeta principal de fichaIdentificación
                FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                        fichaIdentificacion.getTokenIdentificador(), null, false);

                if (fichaIdentificacionCarpetaPrincipal == null) {
                    respuesta.setMensaje(falloUsuario + ", debido a que no existe la carpeta principal.");
                    return respuesta;
                }

                Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();

                // Crear o buscar carpeta para seguimientos sociales
                String nemonicoSeguimientoSocial = EtiquetaNemonico.CARPETA_GESTION_ADOLES_SEGUIMIENTO_SOCIAL;
                FichaIdentificacionCarpeta fichaIdentificacionCarpetaSeguimiento = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                        fichaIdentificacion.getTokenIdentificador(), nemonicoSeguimientoSocial, false);

                Carpeta carpetaPadreSeguimientos;

                if (fichaIdentificacionCarpetaSeguimiento == null) {
                    // Crear carpeta para seguimientos sociales
                    String nombreCarpetaPrincipal = "Seguimiento social";

                    CarpetaDTO carpetaDTO = new CarpetaDTO();
                    carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
                    carpetaDTO.setDescripcion("Carpeta de seguimientos sociales");
                    CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                    carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
                    carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                    RespuestaPorDefectoAuditoria<CarpetaDTO> respuestaCarpeta = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                    if (!respuestaCarpeta.isExito()) {
                        respuesta.setMensaje(falloUsuario + ", debido a que no se pudo crear la carpeta principal para seguimientos sociales.");
                        return respuesta;
                    }

                    Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(respuestaCarpeta.getData().getTokenIdentificador(), false);

                    // Crear relación entre la carpeta y la ficha de identificación
                    fichaIdentificacionCarpetaSeguimiento = new FichaIdentificacionCarpeta();
                    fichaIdentificacionCarpetaSeguimiento.setCarpeta(carpetaGuardada);
                    fichaIdentificacionCarpetaSeguimiento.setFichaIdentificacion(fichaIdentificacion);
                    Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoSeguimientoSocial, false);
                    fichaIdentificacionCarpetaSeguimiento.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
                    fichaIdentificacionCarpetaSeguimiento.setFechaCreacion(new Date());
                    fichaIdentificacionCarpetaSeguimiento.setIpCrea(httpServletRequest.getRemoteAddr());
                    fichaIdentificacionCarpetaSeguimiento.setUsuarioSistemaCrea(usuarioSistema);
                    this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaSeguimiento);

                    carpetaPadreSeguimientos = carpetaGuardada;
                } else {
                    carpetaPadreSeguimientos = fichaIdentificacionCarpetaSeguimiento.getCarpeta();
                }

                // Crear carpeta específica para este seguimiento social
                String nombreCarpeta = "segui_soc_" + seguimiento.getTokenIdentificador();

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpeta);
                carpetaDTO.setDescripcion("Carpeta de seguimiento relacionada a: " + seguimiento.getTokenIdentificador());
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadreSeguimientos.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                RespuestaPorDefectoAuditoria<CarpetaDTO> respuestaCarpeta = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                if (!respuestaCarpeta.isExito()) {
                    respuesta.setMensaje(falloUsuario + ", debido a que no se pudo crear la carpeta específica para el seguimiento.");
                    return respuesta;
                }

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(respuestaCarpeta.getData().getTokenIdentificador(), false);

                // Crear relación entre la carpeta y el seguimiento social
                registroCarpeta = new SeguimientoSocialCarpeta();
                registroCarpeta.setCarpeta(carpetaGuardada);
                registroCarpeta.setSeguimientoSocial(seguimiento);
                registroCarpeta.setFechaCreacion(new Date());
                registroCarpeta.setIpCrea(httpServletRequest.getRemoteAddr());
                registroCarpeta.setUsuarioSistemaCrea(usuarioSistema);
                this.seguimientoCarpetaRepository.save(registroCarpeta);
            }

            Carpeta carpeta = registroCarpeta.getCarpeta();

            String idNodo = carpeta.getIdentificadorAlfresco();

            if (documentoDTOList != null && !documentoDTOList.isEmpty()) {
                for (int i = 0; multipartFiles.length > i; i++) {

                    MultipartFile multipartFile = multipartFiles[i];
                    DocumentoDTO documentoDTO = documentoDTOList.get(i);

                    RespuestaPorDefectoAuditoria<DocumentoDTO> respuestaDocumento = this.documentoService.subirDocumentoAlfresco(httpServletRequest,
                            idNodo, multipartFile, documentoDTO);

                    if (!respuestaDocumento.isExito()) {
                        respuesta.setMensaje(respuestaDocumento.getMensaje());
                        respuesta.setMensajeErrorReal(respuestaDocumento.getMensajeErrorReal());
                        return respuesta;
                    }

                    documentoDTO = respuestaDocumento.getData();
                    Documento documento = this.documentoRepository.findByTokenIdentificadorAndRemovido(
                            documentoDTO.getTokenIdentificador(), false
                    );

                    SeguimientoSocialDocumento seguimientoDocumento = new SeguimientoSocialDocumento();
                    seguimientoDocumento.setDocumento(documento);
                    seguimientoDocumento.setSeguimientoSocial(seguimiento);
                    seguimientoDocumento.setCarpeta(carpeta);
                    seguimientoDocumento.setUsuarioSistemaCrea(usuarioSistema);
                    seguimientoDocumento.setIpCrea(httpServletRequest.getRemoteAddr());
                    this.seguimientoDocumentoRepository.save(seguimientoDocumento);
                }
            }

            // Mensaje para el usuario
            String mensajeUsuario = !"N/A".equals(nombresCompletos) ? 
                "Se subieron con éxito los documentos al seguimiento social de " + nombresCompletos :
                "Se subieron con éxito los documentos al seguimiento social";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se subieron con éxito los documentos al seguimiento social de la persona con identificación: " + identificacionPersona;

            respuesta.llenarRespuestaExitosa(mensajeUsuario, true, mensajeAuditoria);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> obtenerDocumentos(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<DocumentoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            Pageable pageable = PageRequest.of(paginacionRequest.getPage(), paginacionRequest.getSize());

            Page<SeguimientoSocialDocumento> documentosPage;

            documentosPage = this.seguimientoDocumentoRepository.findBySeguimientoSocialTokenIdentificadorAndRemovido(
                    paginacionRequest.getTokenIdentificador(),
                    false,
                    pageable);

            List<DocumentoDTO> documentoList = new ArrayList<>();
            for (SeguimientoSocialDocumento segDoc : documentosPage.toList()) {
                Documento documento = segDoc.getDocumento();
                DocumentoDTO documentoDTO = new DocumentoDTO();
                // Asigna campos al DTO según sea necesario
                Catalogo tipoDeDocumentoSistema = documento.getTipoDeDocumentoSistema();
                CatalogoDTO tipoDeDocumentoSistemaDTO = tipoDeDocumentoSistema.convertirADTO();

                documentoDTO.setTipoDocumentoSistema(tipoDeDocumentoSistemaDTO);
                documentoDTO.setTokenIdentificador(documento.getTokenIdentificador());
                documentoDTO.setNombre(documento.getNombreReal());
                documentoDTO.setDescripcion(documento.getDescripcion());
                documentoDTO.setFechaCreacion(documento.getFechaCreacion());
                documentoDTO.setMimeType(documento.getMimeType());
                documentoDTO.setTamanioBytes(documento.getTamanioByteDocumento());
                documentoDTO.setTipoDeDocumentoSistemaOtro(documento.getTipoDeDocumentoSistemaOtro());
                documentoList.add(documentoDTO);
            }

            PaginacionResponse<DocumentoDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(documentoList);
            paginacionResponse.setTotalItems(documentosPage.getTotalElements());

            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo " + documentosPage.getTotalElements() + " documentos del seguimiento social";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + documentosPage.getTotalElements() + " documentos asociados al seguimiento social";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
