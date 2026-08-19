package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanAsistenciaPostEgresoCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanAsistenciaPostEgresoDetalleRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanAsistenciaPostEgresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.DetalleFichaAsistenciaPostEgresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaAsistenciaPostEgresoCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaAsistenciaPostEgresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class FichaAsistenciaPostEgresoServiceImpl implements FichaAsistenciaPostEgresoService{

    private FichaAsistenciaPostEgresoRepository fichaAsistenciaPostEgresoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private CatalogoRepository catalogoRepository;
    private JwtProviderService jwtProviderService;
    private PlanAsistenciaPostEgresoDetalleRepository planAsistenciaPostEgresoDetalleRepository;
    private DetalleFichaAsistenciaPostEgresoRepository detalleFichaAsistenciaPostEgresoRepository;
    private PlanAsistenciaPostEgresoRepository planAsistenciaPostEgresoRepository;
    private PlanAsistenciaPostEgresoCarpetaRepository planAsistenciaPostEgresoCarpetaRepository;
    private FichaAsistenciaPostEgresoCarpetaRepository fichaAsistenciaPostEgresoCarpetaRepository;
    private CarpetaRepository carpetaRepository;

    private CarpetaService carpetaService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<FichaAsistenciaPostEgresoDTO> crearFichaAsistenciaPostEgreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<FichaAsistenciaPostEgresoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener usuario y empresa desde el JWT
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
            String bodyDecifrado = df22.getData();

            FichaAsistenciaPostEgresoDTO fichaDTO = new Gson().fromJson(bodyDecifrado, FichaAsistenciaPostEgresoDTO.class);

            FichaIdentificacion fichaIdentificacion = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(fichaDTO.getTokenIdentificadorFichaIdentificacion(), false);
            if (fichaIdentificacion == null) {
                df.setMensaje("No existe una ficha principal creada");
                return df;
            }

            PlanAsistenciaPostEgreso planAsistenciaPostEgreso = this.planAsistenciaPostEgresoRepository.findByidPlanAsistenciaPostEgresoAndRemovido(fichaDTO.getIdPlanAsistenciaPostEgreso(), false);
            if (planAsistenciaPostEgreso == null) {
                df.setMensaje("No existe un plan de asistencia en el objeto");
                return df;
            }

            PlanAsistenciaPostEgresoDetalle planAsistenciaPostEgresoDetalle = this.planAsistenciaPostEgresoDetalleRepository.findByTokenIdentificadorAndRemovido(fichaDTO.getPlanAsistenciaPostEgresoDetalle().getTokenIdentificador(), false);
            if (planAsistenciaPostEgresoDetalle == null) {
                df.setMensaje("No existe un detalle a relacionarse");
                return df;
            }

            FichaAsistenciaPostEgreso fichaAsistenciaPostEgreso;
            if (!fichaDTO.getEsEdicion()) {
                fichaAsistenciaPostEgreso = dtoAEntidad(fichaDTO);
                fichaAsistenciaPostEgreso.setFechaCreacion(new Date());
                fichaAsistenciaPostEgreso.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                fichaAsistenciaPostEgreso.setIpCrea(httpServletRequest.getRemoteAddr());
                fichaAsistenciaPostEgreso.setFichaIdentificacion(fichaIdentificacion);
                fichaAsistenciaPostEgreso.setPlanAsistenciaPostEgresoDetalle(planAsistenciaPostEgresoDetalle);
                fichaAsistenciaPostEgreso.setPlanAsistenciaPostEgreso(planAsistenciaPostEgreso);
                fichaAsistenciaPostEgreso = this.fichaAsistenciaPostEgresoRepository.save(fichaAsistenciaPostEgreso);

                //CREACIÓN DE CARPETA EN CASO DE QUE NO EXISTA
                PlanAsistenciaPostEgresoCarpeta planAsistenciaPostEgresoCarpeta = this.planAsistenciaPostEgresoCarpetaRepository.findFirstByPlanAsistenciaPostEgresoTokenIdentificadorAndRemovido(fichaAsistenciaPostEgreso.getPlanAsistenciaPostEgreso().getTokenIdentificador(), false);
                Carpeta carpetaPadrePlanAsistencia = planAsistenciaPostEgresoCarpeta.getCarpeta();

                String pattern = "yyyy-MM-dd-HH:mm:ss";
                DateFormat fecha = new SimpleDateFormat(pattern);
                String fechaFormateada = fecha.format(fichaAsistenciaPostEgreso.getFechaCreacion());

                String nombreCarpeta = "seguimiento_" + fechaFormateada;

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpeta);
                carpetaDTO.setDescripcion("Carpeta de seguimiento de plan de asistencia");
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePlanAsistencia.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);
                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                FichaAsistenciaPostEgresoCarpeta carpetaDetalle = new FichaAsistenciaPostEgresoCarpeta();
                carpetaDetalle.setCarpeta(carpetaGuardada);
                carpetaDetalle.setFichaAsistenciaPostEgreso(fichaAsistenciaPostEgreso);
                carpetaDetalle.setFechaCreacion(new Date());
                carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                this.fichaAsistenciaPostEgresoCarpetaRepository.save(carpetaDetalle);

                df.llenarRespuestaExitosa("Se ha creado con éxito el registro: " + fichaAsistenciaPostEgreso.getIdFichaAsistenciaPostEgreso(), entidadADto(fichaAsistenciaPostEgreso));
            } else {
                fichaAsistenciaPostEgreso = this.fichaAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(fichaDTO.getTokenIdentificador(), false);

                if (fichaAsistenciaPostEgreso == null) {
                    df.setMensaje("No existe el registro a editar");
                    return df;
                }

                List<DetalleFichaAsistenciaPostEgreso> guardados = new ArrayList<>();
                for (DetalleFichaAsistenciaPostEgresoDTO detalleDTO : fichaDTO.getDetalleFichaAsistenciaPostEgresos()) {
                    DetalleFichaAsistenciaPostEgreso detalleEncontrado = this.detalleFichaAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(detalleDTO.getTokenIdentificador(), false);
                    DetalleFichaAsistenciaPostEgreso detalleEntrante = this.detalleDtoAEntidad(detalleDTO);
                    detalleEntrante.setFichaAsistenciaPostEgreso(fichaAsistenciaPostEgreso);
                    if (detalleEncontrado != null) {
                        detalleEntrante.setFechaEdicion(new Date());
                        detalleEntrante.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
                        detalleEntrante.setIpEdita(httpServletRequest.getRemoteAddr());
                    } else {
                        detalleEntrante.setFechaCreacion(new Date());
                        detalleEntrante.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                        detalleEntrante.setIpCrea(httpServletRequest.getRemoteAddr());
                    }
                    DetalleFichaAsistenciaPostEgreso guardado = this.detalleFichaAsistenciaPostEgresoRepository.save(detalleEntrante);
                    guardados.add(guardado);
                }

                Set<String> tokensDetalleEntrante = guardados.stream()
                        .map(DetalleFichaAsistenciaPostEgreso::getTokenIdentificador)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());


                for (DetalleFichaAsistenciaPostEgreso detalle : fichaAsistenciaPostEgreso.getDetalleFichaAsistenciaPostEgresos()) {
                    if (!tokensDetalleEntrante.contains(detalle.getTokenIdentificador())) {
                        if (!detalle.getRemovido()) {
                            detalle.setRemovido(true);
                            detalle.setIpElimina(httpServletRequest.getRemoteAddr());
                            detalle.setUsuarioSistemaElimina(df2.getData().getUsuarioSistema());
                            detalle.setFechaEliminacion(new Date());
                            this.detalleFichaAsistenciaPostEgresoRepository.save(detalle);
                        }
                    }
                }

                fichaAsistenciaPostEgreso = this.fichaAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(fichaDTO.getTokenIdentificador(), false);
                fichaAsistenciaPostEgreso.setFechaEdicion(new Date());
                fichaAsistenciaPostEgreso.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
                fichaAsistenciaPostEgreso.setIpEdita(httpServletRequest.getRemoteAddr());
                fichaAsistenciaPostEgreso.setFichaIdentificacion(fichaIdentificacion);
                fichaAsistenciaPostEgreso.setPlanAsistenciaPostEgresoDetalle(planAsistenciaPostEgresoDetalle);
                fichaAsistenciaPostEgreso.setPlanAsistenciaPostEgreso(planAsistenciaPostEgreso);
                fichaAsistenciaPostEgreso = this.fichaAsistenciaPostEgresoRepository.save(fichaAsistenciaPostEgreso);

                //CREACIÓN DE CARPETA EN CASO DE QUE NO EXISTA
                PlanAsistenciaPostEgresoCarpeta planAsistenciaPostEgresoCarpeta = this.planAsistenciaPostEgresoCarpetaRepository.findFirstByPlanAsistenciaPostEgresoTokenIdentificadorAndRemovido(fichaAsistenciaPostEgreso.getPlanAsistenciaPostEgreso().getTokenIdentificador(), false);
                Carpeta carpetaPadrePlanAsistencia = planAsistenciaPostEgresoCarpeta.getCarpeta();

                FichaAsistenciaPostEgresoCarpeta carpetaEncontrada = this.fichaAsistenciaPostEgresoCarpetaRepository.findFirstByFichaAsistenciaPostEgresoTokenIdentificadorAndRemovido(fichaAsistenciaPostEgreso.getTokenIdentificador(), false);

                if (carpetaEncontrada == null) {
                    String pattern = "yyyy-MM-dd-HH:mm:ss";
                    DateFormat fecha = new SimpleDateFormat(pattern);
                    String fechaFormateada = fecha.format(fichaAsistenciaPostEgreso.getFechaCreacion());

                    String nombreCarpeta = "seguimiento_" + fechaFormateada;

                    CarpetaDTO carpetaDTO = new CarpetaDTO();
                    carpetaDTO.setNombreCliente(nombreCarpeta);
                    carpetaDTO.setDescripcion("Carpeta de seguimiento de plan de asistencia");
                    CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                    carpetaPadreDTO.setTokenIdentificador(carpetaPadrePlanAsistencia.getTokenIdentificador());
                    carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);
                    this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                    Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                    FichaAsistenciaPostEgresoCarpeta carpetaDetalle = new FichaAsistenciaPostEgresoCarpeta();
                    carpetaDetalle.setCarpeta(carpetaGuardada);
                    carpetaDetalle.setFichaAsistenciaPostEgreso(fichaAsistenciaPostEgreso);
                    carpetaDetalle.setFechaCreacion(new Date());
                    carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                    carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                    this.fichaAsistenciaPostEgresoCarpetaRepository.save(carpetaDetalle);
                }

                df.llenarRespuestaExitosa("Se ha editado con éxito el registro: " + fichaAsistenciaPostEgreso.getIdFichaAsistenciaPostEgreso(), entidadADto(fichaAsistenciaPostEgreso));
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /*@Override
    public RespuestaPorDefectoAuditoria<FichaAsistenciaPostEgresoDTO> crearFichaAsistenciaPostEgreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<FichaAsistenciaPostEgresoDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener usuario y empresa desde el JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            UsuarioSistema usuarioSistema = df2.getData().getUsuarioSistema();

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();
            FichaAsistenciaPostEgresoDTO fichaDTO = new Gson().fromJson(bodyDesencriptado, FichaAsistenciaPostEgresoDTO.class);


            FichaAsistenciaPostEgreso ficha ;

            if (fichaDTO.getEsEdicion()) {
                // Edición: Buscar el seguimiento por token
                ficha = this.fichaAsistenciaPostEgresoRepository
                        .findByTokenIdentificadorAndRemovido(fichaDTO.getTokenIdentificador(), false);

                if (fichaDTO == null) {
                    df.setMensaje("El seguimiento no fue encontrado o ya fue eliminado.");
                    return df;
                }
            }else{
                ficha = new FichaAsistenciaPostEgreso();
            }


            // Asociar FichaIdentificacion
            if (!ObjectUtils.isEmpty(fichaDTO.getTokenIdentificadorFichaIdentificacion())) {
                FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository
                        .findByTokenIdentificadorAndRemovido(fichaDTO.getTokenIdentificadorFichaIdentificacion(), false);

                if (fichaIdentificacion == null) {
                    df.setMensaje("No se encontró la ficha de identificación.");
                    return df;
                }
                ficha.setFichaIdentificacion(fichaIdentificacion);
            }

            // Asociar plan de asistencia
            if (!ObjectUtils.isEmpty(fichaDTO.getTokenPlanAsistencia())) {
                PlanAsistenciaPostEgreso planAsistenciaPostEgreso = planAsistenciaPostEgresoRepository
                        .findByTokenIdentificadorAndRemovido(fichaDTO.getTokenPlanAsistencia(), false);

                if (planAsistenciaPostEgreso == null) {
                    df.setMensaje("No se encontró el plan de asistencia.");
                    return df;
                }
                ficha.setPlanAsistenciaPostEgreso(planAsistenciaPostEgreso);
            }

            // Asociar Tipo de Formato
            if (!ObjectUtils.isEmpty(fichaDTO.getTipoFormato())) {
                ficha.setTipoFormato(dtoToCatalogo(fichaDTO.getTipoFormato()));
            }

            // Configuración de creación o edición
            if (!fichaDTO.getEsEdicion()) {
                ficha.setUsuarioSistemaCrea(usuarioSistema);
                ficha.setFechaCreacion(new Date());
                ficha.setIpCrea(httpServletRequest.getRemoteAddr());
            } else {
                ficha.setUsuarioSistemaEdita(usuarioSistema);
                ficha.setFechaEdicion(new Date());
                ficha.setIpEdita(httpServletRequest.getRemoteAddr());
            }

            fichaAsistenciaPostEgresoRepository.save(ficha);

            // Llenar DTO de respuesta
            fichaDTO.setTokenIdentificador(ficha.getTokenIdentificador());

            df.llenarRespuestaExitosa(
                    fichaDTO.getEsEdicion() ? "Ficha Asistencia Post Egreso actualizada con éxito" : "Ficha Asistencia Post Egreso creada con éxito",
                    fichaDTO
            );

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
*/

    /*@Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FichaAsistenciaPostEgresoDTO>> obtenerFichasAsistenciaPostEgreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaAsistenciaPostEgresoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener usuario y empresa desde el JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDesencriptado, PaginacionRequest.class);
            String tokenFichaIdentificacion = paginacionRequest.getTokenIdentificador();

            // Configurar la paginación
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize()
            );

            // Buscar fichas de asistencia post egreso por ficha de identificación
            Page<FichaAsistenciaPostEgreso> fichasPage = fichaAsistenciaPostEgresoRepository.findByPlanAsistenciaPostEgresoTokenIdentificadorAndRemovido(tokenFichaIdentificacion, false, pageable);

            // Convertir a DTO
            List<FichaAsistenciaPostEgresoDTO> fichasDTOList = fichasPage.stream().map(ficha -> {
                FichaAsistenciaPostEgresoDTO dto = new FichaAsistenciaPostEgresoDTO();
                dto.setTokenIdentificador(ficha.getTokenIdentificador());
                dto.setFechaCreacion(ficha.getFechaCreacion());
                if (!ObjectUtils.isEmpty(ficha.getFichaIdentificacion())) {
                    dto.setTokenIdentificadorFichaIdentificacion(ficha.getFichaIdentificacion().getTokenIdentificador());
                }

                if (!ObjectUtils.isEmpty(ficha.getTipoFormato())) {
                    dto.setTipoFormato(catalogoToDTO(ficha.getTipoFormato()));
                }

                return dto;
            }).collect(Collectors.toList());

            // Crear respuesta paginada
            PaginacionResponse<FichaAsistenciaPostEgresoDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(fichasDTOList);
            paginacionResponse.setTotalItems(fichasPage.getTotalElements());

            df.llenarRespuestaExitosa("Fichas de Asistencia Post Egreso obtenidas con éxito", paginacionResponse);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
*/

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FichaAsistenciaPostEgresoDTO>> obtenerFichasAsistenciaPostEgreso(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaAsistenciaPostEgresoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener usuario y empresa desde el JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyDesencriptado = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDesencriptado, PaginacionRequest.class);
            String filter = paginacionRequest.getFilter();

            // Si el filtro está vacío o es null, no lo aplicamos
            if (filter != null && filter.trim().isEmpty()) {
                filter = null;
            }

            // Configurar la paginación
            Pageable pageable = PageRequest.of(paginacionRequest.getPage(), paginacionRequest.getSize());

            // Buscar fichas de asistencia post egreso con filtro de tipoFormato
            //Page<FichaAsistenciaPostEgreso> fichasPage = fichaAsistenciaPostEgresoRepository.buscarPorTipoFormato(filter, pageable);
            Page<FichaAsistenciaPostEgreso> fichasPage = this.fichaAsistenciaPostEgresoRepository.findByPlanAsistenciaPostEgresoTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false, pageable);

            // Convertir a DTO
            List<FichaAsistenciaPostEgresoDTO> fichasDTOList = new ArrayList<>(fichasPage.stream().map(this::entidadADto).toList());

            fichasDTOList.sort(
                    Comparator.comparing(FichaAsistenciaPostEgresoDTO::getFechaCreacion).reversed()
            );

            for (FichaAsistenciaPostEgresoDTO fichaDTOTemp : fichasDTOList) {
                FichaAsistenciaPostEgreso fichaTemp = this.fichaAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(fichaDTOTemp.getTokenIdentificador(), false);
                fichaDTOTemp.setIdFichaIdentificacion(fichaTemp.getFichaIdentificacion().getIdFichaIdentificacion());
            }


            // Crear respuesta paginada
            PaginacionResponse<FichaAsistenciaPostEgresoDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(fichasDTOList);
            paginacionResponse.setTotalItems(fichasPage.getTotalElements());

            df.llenarRespuestaExitosa("Fichas de Asistencia Post Egreso obtenidas con éxito", paginacionResponse);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }




    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarFichaAsistenciaPostEgreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Obtener usuario y empresa desde el JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            UsuarioSistema usuarioSistema = df2.getData().getUsuarioSistema();

            // Desencriptar el body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();
            FichaAsistenciaPostEgresoDTO fichaDTO = new Gson().fromJson(bodyDesencriptado, FichaAsistenciaPostEgresoDTO.class);

            // Buscar la ficha por token
            FichaAsistenciaPostEgreso ficha = fichaAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(fichaDTO.getTokenIdentificador(), false);

            if (ficha == null) {
                df.setMensaje("La ficha de asistencia post egreso no fue encontrada o ya fue eliminada.");
                return df;
            }

            // Marcar como eliminada
            ficha.setRemovido(true);
            ficha.setUsuarioSistemaElimina(usuarioSistema);
            ficha.setFechaEliminacion(new Date());
            ficha.setIpElimina(httpServletRequest.getRemoteAddr());

            fichaAsistenciaPostEgresoRepository.save(ficha);

            df.llenarRespuestaExitosa("Ficha de Asistencia Post Egreso eliminada con éxito", true);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private FichaAsistenciaPostEgresoDTO entidadADto(FichaAsistenciaPostEgreso entidad) {
        if (entidad == null) return null;

        FichaAsistenciaPostEgresoDTO dto = new FichaAsistenciaPostEgresoDTO();
        dto.setTipoFormato(this.catalogoToDTO(entidad.getTipoFormato()));
        dto.setFechaCreacion(entidad.getFechaCreacion());
        PlanAsistenciaPostEgresoDetalle planAsistenciaPostEgresoDetalle = this.planAsistenciaPostEgresoDetalleRepository.findByTokenIdentificadorAndRemovido(entidad.getPlanAsistenciaPostEgresoDetalle().getTokenIdentificador(), false);
        dto.setPlanAsistenciaPostEgresoDetalle(detallePlanAsistenciaEntidadADto(planAsistenciaPostEgresoDetalle));
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        if (entidad.getDetalleFichaAsistenciaPostEgresos() != null) {
            List<DetalleFichaAsistenciaPostEgresoDTO> detalleDTO = entidad.getDetalleFichaAsistenciaPostEgresos().stream()
                    .filter(item -> !item.getRemovido())
                    .map(this::detalleADto)
                    .toList();
            dto.setDetalleFichaAsistenciaPostEgresos(detalleDTO);
        }

        return dto;
    }

    private DetalleFichaAsistenciaPostEgresoDTO detalleADto(DetalleFichaAsistenciaPostEgreso entidad) {
        if (entidad == null) return null;

        DetalleFichaAsistenciaPostEgresoDTO dto = new DetalleFichaAsistenciaPostEgresoDTO();
        dto.setIdDetalleFichaAsistenciaPostEgreso(entidad.getIdDetalleFichaAsistenciaPostEgreso());
        dto.setFechaDetalle(entidad.getFechaDetalle());
        dto.setDescripcionActividad(entidad.getDescripcionActividad());
        dto.setObservaciones(entidad.getObservaciones());
        if (entidad.getModalidadDeEntrevista() != null) dto.setModalidadDeEntrevista(catalogoToDTO(entidad.getModalidadDeEntrevista()));
        if (entidad.getPersonaEntrevistada() != null) dto.setPersonaEntrevistada(catalogoToDTO(entidad.getPersonaEntrevistada()));
        if (entidad.getMotivo() != null) dto.setMotivo(catalogoToDTO(entidad.getMotivo()));
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        return dto;
    }

    private FichaAsistenciaPostEgreso dtoAEntidad(FichaAsistenciaPostEgresoDTO dto) {
        if (dto == null) return null;

        FichaAsistenciaPostEgreso fichaAsistenciaPostEgreso = this.fichaAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

        FichaAsistenciaPostEgreso entidad = Objects.requireNonNullElseGet(fichaAsistenciaPostEgreso, FichaAsistenciaPostEgreso::new);

        entidad.setTipoFormato(dtoToCatalogo(dto.getTipoFormato()));

        if (dto.getDetalleFichaAsistenciaPostEgresos() != null) {
            List<DetalleFichaAsistenciaPostEgreso> detalle = dto.getDetalleFichaAsistenciaPostEgresos().stream()
                    .map(this::detalleDtoAEntidad)
                    .toList();
            detalle.forEach(item -> item.setFichaAsistenciaPostEgreso(entidad));
            entidad.setDetalleFichaAsistenciaPostEgresos(detalle);
        }

        return entidad;
    }

    private DetalleFichaAsistenciaPostEgreso detalleDtoAEntidad(DetalleFichaAsistenciaPostEgresoDTO dto) {
        if (dto == null) return null;

        DetalleFichaAsistenciaPostEgreso detallefichaAsistenciaPostEgreso = this.detalleFichaAsistenciaPostEgresoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

        DetalleFichaAsistenciaPostEgreso entidad = Objects.requireNonNullElseGet(detallefichaAsistenciaPostEgreso, DetalleFichaAsistenciaPostEgreso::new);
        //DetalleFichaAsistenciaPostEgreso entidad = new DetalleFichaAsistenciaPostEgreso();
        entidad.setIdDetalleFichaAsistenciaPostEgreso(dto.getIdDetalleFichaAsistenciaPostEgreso());
        entidad.setFechaDetalle(dto.getFechaDetalle());
        entidad.setDescripcionActividad(dto.getDescripcionActividad());
        entidad.setObservaciones(dto.getObservaciones());
        if (dto.getModalidadDeEntrevista() != null) entidad.setModalidadDeEntrevista(dtoToCatalogo(dto.getModalidadDeEntrevista()));
        if (dto.getPersonaEntrevistada() != null) entidad.setPersonaEntrevistada(dtoToCatalogo(dto.getPersonaEntrevistada()));
        if (dto.getMotivo() != null)  entidad.setMotivo(dtoToCatalogo(dto.getMotivo()));

        return entidad;
    }

    private PlanAsistenciaPostEgresoDetalleDTO detallePlanAsistenciaEntidadADto(PlanAsistenciaPostEgresoDetalle entidad) {
        if (entidad == null) return null;

        PlanAsistenciaPostEgresoDetalleDTO dto = new PlanAsistenciaPostEgresoDetalleDTO();
        dto.setIdPlanAsistenciaPostEgresoDetalle(entidad.getIdPlanAsistenciaPostEgresoDetalle());
        dto.setArea(catalogoToDTO(entidad.getArea()));
        dto.setFactores(entidad.getFactores());
        dto.setObjetivoGeneral(entidad.getObjetivoGeneral());
        dto.setObjetivoEspecifico(entidad.getObjetivoEspecifico());
        dto.setActividades(entidad.getActividades());
        dto.setInstitucion(entidad.getInstitucion());
        dto.setFrecuencia(entidad.getFrecuencia());
        dto.setIndicador(entidad.getIndicador());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());


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
