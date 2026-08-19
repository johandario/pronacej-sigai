package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.*;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIngresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ExpedienteMatrizServiceImpl implements ExpedienteMatrizService {
    private PaginacionService paginacionService;
    private CatalogoRepository catalogoRepository;
    private ExpedienteMatrizRepository expedienteMatrizRepository;
    private ExpedienteMatrizDetalleRepository expedienteMatrizDetalleRepository;
    private ExpedienteMatrizDetalleCarpetaRepository expedienteMatrizDetalleCarpetaRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private FichaIngresoRepository fichaIngresoRepository;
    private JwtProviderService jwtProviderService;
    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;
    private ExpedienteMatrizDelitoRepository expedienteMatrizDelitoRepository;
    private ExpedienteMatrizMedidaRepository expedienteMatrizMedidaRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<ExpedienteMatrizDTO>> obtenerExpedientes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<ExpedienteMatrizDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            /*Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idExpediente").descending()
            );*/

            /*FichaIngreso ingresoEncontrado = this.fichaIngresoRepository.obtenerUltimaFichaIngresoValidaPorTokenFichaIdentificacion(paginacionRequest.getTokenIdentificador());

            if (ingresoEncontrado == null) {
                df.setMensaje("No existen fichas de ingreso, primero cree una.");
                return df;
            }*/


            List<ExpedienteMatriz> expedienteMatrizPage = this.expedienteMatrizRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(), false);


            PaginacionResponse<ExpedienteMatrizDTO> paginacionResponse = new PaginacionResponse<>();
            List<ExpedienteMatrizDTO> expedienteMatrizDTOList = new ArrayList<>();

            for (ExpedienteMatriz expedienteMatriz : expedienteMatrizPage) {

                ExpedienteMatrizDTO dto = entidadADto(expedienteMatriz);
                dto.setFechaCreacion(expedienteMatriz.getFechaCreacion());
                dto.setTokenIdentificador(expedienteMatriz.getTokenIdentificador());

                if (dto.getFechaOficio() != null) {
                    String pattern = "dd-MM-yyyy";
                    DateFormat fecha = new SimpleDateFormat(pattern);
                    dto.setFecOficioTexto(fecha.format(expedienteMatriz.getFechaOficio()));
                }

                expedienteMatrizDTOList.add(dto);
            }

            /*paginacionResponse.setData(expedienteMatrizDTOList);
            paginacionResponse.setTotalItems(expedienteMatrizPage.getTotalElements());*/

            expedienteMatrizDTOList.sort(
                    Comparator.comparing(ExpedienteMatrizDTO::getFechaCreacion).reversed()
            );

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            expedienteMatrizDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

           /* expedienteMatrizDTOList.forEach(expedienteMatrizDTO ->
                            this.historicoFichaIdentificacionService.procesarObjeto(expedienteMatrizDTO, df2.getData())
            );*/

            paginacionResponse = paginacionService.obtenerDatos(expedienteMatrizDTOList, paginacionRequest);

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + expedienteMatrizDTOList.size() + " de: " + expedienteMatrizPage.size() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> crearExpediente(HttpServletRequest httpServletRequest, ExpedienteMatrizDTO expedienteMatrizDTO) {
        RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> df =
                new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            ExpedienteMatriz expedienteMatrizTemp = this.expedienteMatrizRepository.findByNumExpediente(expedienteMatrizDTO.getNumExpediente());

            if (expedienteMatrizTemp != null && !expedienteMatrizDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un expediente con el número de documento");
                return df;
            }

            ExpedienteMatriz expedienteMatriz = dtoAEntidad(expedienteMatrizDTO);
            if (expedienteMatrizTemp == null && !expedienteMatrizDTO.getEsEdicion()) {
                expedienteMatriz.setNumExpediente(generarCodigoIncremental());
                expedienteMatriz.setFechaCreacion(new Date());
                expedienteMatriz.setIpCrea(httpServletRequest.getRemoteAddr());
                expedienteMatriz.setEmpresa(df2.getData().getEmpresa());
                expedienteMatriz.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());

                FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(expedienteMatrizDTO.getTokenFichaIdentificacion(), false);
                expedienteMatriz.setFichaIdentificacion(ficha);

                ficha.setEstado(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_ADOLESCENTE_SENTENCIADO_PROCESADO", false));
                if (!ObjectUtils.isEmpty(ficha.getCentroIngreso())) {
                    if (!ObjectUtils.isEmpty(ficha.getCentroIngreso().getJerarquiaPadre())) {
                        if (ficha.getCentroIngreso().getJerarquiaPadre().getNemonico().equals("SOA")) {
                            ficha.setEstado(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_ADOLESCENTE_MEDIDA_SOCIOEDUCATIVA", false));
                        }
                    }
                }

                this.fichaIdentificacionRepository.save(ficha);

                FichaIngreso fichaIngreso = this.fichaIngresoRepository.findByTokenIdentificadorAndRemovido(expedienteMatrizDTO.getTokenFichaIngreso(), false);
                expedienteMatriz.setFichaIngreso(fichaIngreso);

                expedienteMatriz = this.expedienteMatrizRepository.save(expedienteMatriz);
                this.actualizarExpedienteDesdeDto(expedienteMatrizDTO, expedienteMatriz);

                List<ExpedienteMatrizDetalle> listaGuardada = this.expedienteMatrizDetalleRepository
                        .findByExpedienteMatrizTokenIdentificadorAndRemovido(expedienteMatriz.getTokenIdentificador(), false);

                String numExpediente = expedienteMatriz.getNumExpediente();

                // CREACIÓN DE CARPETA

                String nemonico = EtiquetaNemonico.CARPETA_GESTION_ADOLES_LEGAL;

                FichaIdentificacionCarpeta carpetaFicha = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(expedienteMatriz.getFichaIdentificacion().getTokenIdentificador(), nemonico, false);

                if (carpetaFicha != null) {
                    Carpeta carpetaPadre = carpetaFicha.getCarpeta();

                    listaGuardada.forEach(detalle -> {
                        String pattern = "yyyy-MM-dd";
                        DateFormat fecha = new SimpleDateFormat(pattern);
                        String fechaFormateada = fecha.format(detalle.getFechaResolucion());

                        String nombreCarpeta = numExpediente + "_disposicion_resolucion-" + detalle.getNumResolucion() + "_" + fechaFormateada;


                        CarpetaDTO carpetaDTO = new CarpetaDTO();
                        carpetaDTO.setNombreCliente(nombreCarpeta);
                        carpetaDTO.setDescripcion("Carpeta de mandato legal relacionado al detalle: " + detalle.getTokenIdentificador());
                        CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                        carpetaPadreDTO.setTokenIdentificador(carpetaPadre.getTokenIdentificador());
                        carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                        this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                        Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                        ExpedienteMatrizDetalleCarpeta carpetaDetalle = new ExpedienteMatrizDetalleCarpeta();
                        carpetaDetalle.setCarpeta(carpetaGuardada);
                        carpetaDetalle.setExpedienteMatrizDetalle(detalle);
                        carpetaDetalle.setFechaCreacion(new Date());
                        carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                        carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                        this.expedienteMatrizDetalleCarpetaRepository.save(carpetaDetalle);
                    });
                }


                df.llenarRespuestaExitosa("Se ha creado con éxito el expediente: " + expedienteMatriz.getNumExpediente(), entidadADto(expedienteMatriz));
            } else {

                /*boolean permitido = this.permisoRolUsuarioService.validarPermisoObjetoYAccion(
                        expedienteMatrizDTO,
                        expedienteMatrizDTO.getTokenFichaIdentificacion(),
                        df2.getData(),
                        EtiquetaNemonico.ACCIONES_MENU_PERMISO_EDITAR
                );

                if (!permitido) {
                    df.setMensaje("No tiene permiso para editar el expediente");
                    return df;
                }*/

                expedienteMatriz.setFechaEdicion(new Date());
                expedienteMatriz.setIpEdita(httpServletRequest.getRemoteAddr());
                expedienteMatriz.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
                expedienteMatriz = this.expedienteMatrizRepository.save(expedienteMatriz);
                this.actualizarExpedienteDesdeDto(expedienteMatrizDTO, expedienteMatriz);

                String numExpediente = expedienteMatriz.getNumExpediente();

                // CREACIÓN DE CARPETA
                String nemonico = EtiquetaNemonico.CARPETA_GESTION_ADOLES_LEGAL;

                FichaIdentificacionCarpeta carpetaFicha = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(expedienteMatriz.getFichaIdentificacion().getTokenIdentificador(), nemonico, false);

                Carpeta carpetaPadre = carpetaFicha.getCarpeta();

                expedienteMatriz.getExpedienteDetalle().forEach(detalle -> {

                    ExpedienteMatrizDetalleCarpeta carpetaEncontrada = this.expedienteMatrizDetalleCarpetaRepository.findFirstByExpedienteMatrizDetalleTokenIdentificadorAndRemovido(detalle.getTokenIdentificador(), false);

                    if (carpetaEncontrada == null) {
                        String pattern = "yyyy-MM-dd";
                        DateFormat fecha = new SimpleDateFormat(pattern);
                        String fechaFormateada = fecha.format(detalle.getFechaResolucion());

                        String nombreCarpeta = numExpediente + "_disposicion_resolucion-" + detalle.getNumResolucion() + "_" + fechaFormateada;

                        CarpetaDTO carpetaDTO = new CarpetaDTO();
                        carpetaDTO.setNombreCliente(nombreCarpeta);
                        carpetaDTO.setDescripcion("Carpeta de mandato legal relacionado al detalle: " + detalle.getTokenIdentificador());
                        CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                        carpetaPadreDTO.setTokenIdentificador(carpetaPadre.getTokenIdentificador());
                        carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                        this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                        Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                        ExpedienteMatrizDetalleCarpeta carpetaDetalle = new ExpedienteMatrizDetalleCarpeta();
                        carpetaDetalle.setCarpeta(carpetaGuardada);
                        carpetaDetalle.setExpedienteMatrizDetalle(detalle);
                        carpetaDetalle.setFechaCreacion(new Date());
                        carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                        carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                        this.expedienteMatrizDetalleCarpetaRepository.save(carpetaDetalle);
                    }
                });


                df.llenarRespuestaExitosa("Se ha editado con éxito el expediente: " + expedienteMatriz.getNumExpediente(), entidadADto(expedienteMatriz));
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> eliminarExpediente(HttpServletRequest httpServletRequest, ExpedienteMatrizDTO expedienteMatrizDTO) {
        RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> df =
                new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            ExpedienteMatriz expedienteMatrizTemp = this.expedienteMatrizRepository.findByNumExpediente(expedienteMatrizDTO.getNumExpediente());

            if (expedienteMatrizTemp == null) {
                df.setMensaje("No existen expedientes con ese número");
                return df;
            }

            boolean permitido = this.permisoRolUsuarioService.validarPermisoObjetoYAccion(
                    entidadADto(expedienteMatrizTemp),
                    entidadADto(expedienteMatrizTemp).getTokenFichaIdentificacion(),
                    df2.getData(),
                    EtiquetaNemonico.ACCIONES_MENU_PERMISO_ELIMINAR
            );

            if (!permitido) {
                df.setMensaje("No tiene permiso para eliminar el expediente");
                return df;
            }

            ExpedienteMatriz expedienteMatriz = dtoAEntidad(expedienteMatrizDTO);
            expedienteMatriz.setRemovido(true);
            expedienteMatriz.setIpElimina(httpServletRequest.getRemoteAddr());
            expedienteMatriz.setFechaEliminacion(new Date());
            expedienteMatriz.setUsuarioSistemaElimina(df2.getData().getUsuarioSistema());
            expedienteMatriz = this.expedienteMatrizRepository.save(expedienteMatriz);

            df.llenarRespuestaExitosa("Se ha eliminado con éxito el expediente: " + expedienteMatriz.getNumExpediente(), expedienteMatrizDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> obtenerExpedientePorNum(HttpServletRequest httpServletRequest, String numExpediente) {
        RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> df =
                new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            ExpedienteMatriz expedienteMatrizTemp = this.expedienteMatrizRepository.findByNumExpediente(numExpediente);

            if (expedienteMatrizTemp == null) {
                df.setMensaje("No existen expedientes con ese número");
                return df;
            }
            ExpedienteMatrizDTO expedienteMatrizDTO = entidadADto(expedienteMatrizTemp);
            expedienteMatrizDTO.setTokenIdentificador(expedienteMatrizTemp.getTokenIdentificador());
            expedienteMatrizDTO.setTokenFichaIngreso(expedienteMatrizTemp.getFichaIngreso().getTokenIdentificador());
            expedienteMatrizDTO.setFechaCreacion(expedienteMatrizTemp.getFechaCreacion());
            expedienteMatrizDTO.setTokenIdentificadorEmpresa(expedienteMatrizTemp.getEmpresa().getTokenIdentificador());

            df.llenarRespuestaExitosa("Se ha encontrado el expediente: " + expedienteMatrizTemp.getNumExpediente(), expedienteMatrizDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> obtenerExpedientePorToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
   /*     RespuestaPorDefectoAuditoria<ExpedienteMatrizDTO> df =
                new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            ExpedienteMatriz expedienteMatrizTemp = this.expedienteMatrizRepository.findByNumExpediente(numExpediente);

            if (expedienteMatrizTemp == null) {
                df.setMensaje("No existen expedientes con ese número");
                return df;
            }
            ExpedienteMatrizDTO expedienteMatrizDTO = entidadADto(expedienteMatrizTemp);
            expedienteMatrizDTO.setTokenIdentificador(expedienteMatrizTemp.getTokenIdentificador());
            expedienteMatrizDTO.setFechaCreacion(expedienteMatrizTemp.getFechaCreacion());
            expedienteMatrizDTO.setTokenIdentificadorEmpresa(expedienteMatrizTemp.getEmpresa().getTokenIdentificador());

            df.llenarRespuestaExitosa("Se ha encontrado el expediente: " + expedienteMatrizTemp.getNumExpediente(), expedienteMatrizDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;*/
        return null;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<ExpedienteMatrizDTO>> obtenerExpedientePorTokenFicha(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<ExpedienteMatrizDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idExpediente").descending()
            );

            Page<ExpedienteMatriz> expedienteMatrizPage = this.expedienteMatrizRepository.findExpedientesByTokenFicha(tokenIdentificador, pageable);

            PaginacionResponse<ExpedienteMatrizDTO> paginacionResponse = new PaginacionResponse<>();
            List<ExpedienteMatrizDTO> expedienteMatrizDTOList = new ArrayList<>();

            ExpedienteMatrizDTO expedienteMatrizDTO;
            for (ExpedienteMatriz expedienteMatriz : expedienteMatrizPage.toList()) {
                expedienteMatrizDTO = entidadADto(expedienteMatriz);

                expedienteMatrizDTOList.add(expedienteMatrizDTO);
            }

            paginacionResponse.setData(expedienteMatrizDTOList);
            paginacionResponse.setTotalItems(expedienteMatrizPage.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + expedienteMatrizDTOList.size() + " de: " + expedienteMatrizPage.getTotalElements() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<DelitoEstadisticaDTO>> obtenerEstadisticasDelitos(HttpServletRequest httpServletRequest
            , BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<DelitoEstadisticaDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validación de usuario y empresa desde el JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            Empresa empresa = df2.getData().getEmpresa();
            respuesta.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            ReportesDTO reportesDTO = new Gson().fromJson(body, ReportesDTO.class);

            List<Object[]> resultados = null;

            // Obtener estadísticas desde el repositorio

            resultados = expedienteMatrizDelitoRepository.countDelitosGenericos(reportesDTO.getNemonicoTipoSexo(),
                    reportesDTO.getTokenIdentificadorCentro(), reportesDTO.getNemonicoCentro());

            List<DelitoEstadisticaDTO> estadisticas = resultados.stream()
                    .map(obj -> new DelitoEstadisticaDTO(
                            (String) obj[0],
                            ((Long) obj[1]).intValue()
                    ))
                    .collect(Collectors.toList());

            respuesta.llenarRespuestaExitosa("Estadísticas de delitos obtenidas correctamente.", estadisticas);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ExpedienteMatrizDetalleDTO> obtenerExpedienteDetallePorFicha(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ExpedienteMatrizDetalleDTO> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            String tokenIdentificacion = new Gson().fromJson(body, String.class);

            List<ExpedienteMatrizDetalle> expedienteMatrizDetalles = this.expedienteMatrizDetalleRepository.findExpedienteMatrizDetalleByFichaIdentificacionAndExpedienteRemovido(
                    tokenIdentificacion,
                    false
            );

            ExpedienteMatrizDetalleDTO expedienteMatrizDetalleDTO = new ExpedienteMatrizDetalleDTO();

            if (!expedienteMatrizDetalles.isEmpty()) {
                ExpedienteMatrizDetalle expedienteMatrizDetalles1 = expedienteMatrizDetalles.get(0);
                expedienteMatrizDetalleDTO = detalleEntidadADto(expedienteMatrizDetalles1);
                expedienteMatrizDetalleDTO.setNumExpediente(expedienteMatrizDetalles1.getExpedienteMatriz().getNumExpediente());
            } else {
                expedienteMatrizDetalleDTO.setRemovido(true);
            }

            respuesta.llenarRespuestaExitosa("Ultimo detalle expediente encontrado.", expedienteMatrizDetalleDTO);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ExpedienteMatrizDetalleDTO> obtenerExpedienteCabeceraYDetalleActualPorFicha(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ExpedienteMatrizDetalleDTO> respuesta = new RespuestaPorDefectoAuditoria<>();
        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String body = df22.getData();
            String tokenIdentificacion = new Gson().fromJson(body, String.class);

            ExpedienteMatriz ultimoExpedienteMatriz = this.expedienteMatrizRepository
                    .findFirstByFichaIdentificacionTokenIdentificadorAndRemovidoOrderByFechaCreacionDesc(
                            tokenIdentificacion,
                            false
                    );

            if (ultimoExpedienteMatriz != null) {
                ExpedienteMatrizDetalleDTO expedienteMatrizDetalleDTO = new ExpedienteMatrizDetalleDTO();

                ExpedienteMatrizDetalle ultimoDetallePorExpediente = this.expedienteMatrizDetalleRepository
                        .findFirstByExpedienteMatrizTokenIdentificadorAndRemovidoOrderByFechaCreacionDesc(
                                ultimoExpedienteMatriz.getTokenIdentificador(),
                                false
                        );

                if (ultimoDetallePorExpediente != null) {
                    expedienteMatrizDetalleDTO = detalleEntidadADto(ultimoDetallePorExpediente);

                }

                expedienteMatrizDetalleDTO.setNumExpediente(ultimoExpedienteMatriz.getNumExpediente());
                expedienteMatrizDetalleDTO.setNumExpedienteJudicial(ultimoExpedienteMatriz.getNumExpedienteJudicial());

                respuesta.llenarRespuestaExitosa("Ultimo detalle expediente encontrado.", expedienteMatrizDetalleDTO);

            } else {
                respuesta.setMensajeErrorReal("No se ha encontrado un expediente legal");
                return respuesta;
            }

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }
        return respuesta;
    }

    private String generarCodigoIncremental() {
        int anioActual = Year.now().getValue();
        String anioPrefix = anioActual + "-";

        // Buscar el último documento creado en el año actual
        ExpedienteMatriz ultimoDocumento = expedienteMatrizRepository.findTopByAnio(anioPrefix);

        int nuevoIncremento = 1; // Comienza desde 1 si no existe ningún documento

        if (ultimoDocumento != null) {
            // Extraer el número incremental del último código
            String ultimoCodigo = ultimoDocumento.getNumExpediente();
            int ultimoIncremento = Integer.parseInt(ultimoCodigo.substring(5));
            nuevoIncremento = ultimoIncremento + 1;
        }

        // Formatear el código con el año y el nuevo incremento en formato de seis dígitos
        return String.format("%d-%06d", anioActual, nuevoIncremento);
    }

    private void actualizarExpedienteDesdeDto(ExpedienteMatrizDTO dto, ExpedienteMatriz entidad) throws Exception {
        List<ExpedienteMatrizDetalle> detalle = this.sincronizarDetalle(entidad, dto);
        this.expedienteMatrizDetalleRepository.saveAll(detalle);

        // Sincronizar sub-listas de cada detalle
        for (int i = 0; i < detalle.size(); i++) {
            ExpedienteMatrizDetalle detalleEntidad = detalle.get(i);
            ExpedienteMatrizDetalleDTO detalleDto = dto.getExpedienteDetalle().get(i);

            List<ExpedienteMatrizDelito> delitos = this.sincronizarDelitos(detalleEntidad, detalleDto);
            this.expedienteMatrizDelitoRepository.saveAll(delitos);

            List<ExpedienteMatrizMedida> medidasSocio = this.sincronizarMedidasSocioEducativas(
                    detalleEntidad, detalleDto);
            List<ExpedienteMatrizMedida> medidasAcc = this.sincronizarMedidasAccesorias(
                    detalleEntidad, detalleDto);

            this.expedienteMatrizMedidaRepository.saveAll(medidasSocio);
            this.expedienteMatrizMedidaRepository.saveAll(medidasAcc);
        }
    }

    private List<ExpedienteMatrizDetalle> sincronizarDetalle(ExpedienteMatriz entidad, ExpedienteMatrizDTO dto) throws Exception {
        List<ExpedienteMatrizDetalle> listaOriginal = this.expedienteMatrizDetalleRepository
                .findByExpedienteMatrizTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

        return this.sincronizarLista(
                listaOriginal,
                dto.getExpedienteDetalle(),
                ExpedienteMatrizDetalle::getTokenIdentificador,   // token de la entidad
                ExpedienteMatrizDetalleDTO::getTokenIdentificador, // token del DTO
                (detalleDto, existente) -> {                       // actualizar
                    this.actualizarDetalleDesdeDto(detalleDto, existente);
                    existente.setExpedienteMatriz(entidad);
                },
                detalleDto -> {                                    // crear nuevo
                    ExpedienteMatrizDetalle nuevo = new ExpedienteMatrizDetalle();
                    this.actualizarDetalleDesdeDto(detalleDto, nuevo);
                    nuevo.setExpedienteMatriz(entidad);
                    nuevo.setRemovido(false);
                    return nuevo;
                },
                restante -> restante.setRemovido(true)             // eliminación lógica
        );
    }

    private List<ExpedienteMatrizDelito> sincronizarDelitos(
            ExpedienteMatrizDetalle detalleEntidad,
            ExpedienteMatrizDetalleDTO detalleDto
    ) {
        List<ExpedienteMatrizDelito> listaOriginal = this.expedienteMatrizDelitoRepository
                .findByExpedienteMatrizDetalleTokenIdentificadorAndRemovido(
                        detalleEntidad.getTokenIdentificador(), false);

        return this.sincronizarLista(
                listaOriginal,
                detalleDto.getExpedienteDelitos(),
                ExpedienteMatrizDelito::getTokenIdentificador,
                ExpedienteMatrizDelitoDTO::getTokenIdentificador,
                (delitoDto, existente) -> {
                    this.actualizarDelitoDesdeDto(delitoDto, existente);
                    existente.setExpedienteMatrizDetalle(detalleEntidad);
                },
                delitoDto -> {
                    ExpedienteMatrizDelito nuevo = new ExpedienteMatrizDelito();
                    this.actualizarDelitoDesdeDto(delitoDto, nuevo);
                    nuevo.setExpedienteMatrizDetalle(detalleEntidad);
                    nuevo.setRemovido(false);
                    return nuevo;
                },
                restante -> restante.setRemovido(true)
        );
    }

    private List<ExpedienteMatrizMedida> sincronizarMedidasSocioEducativas(
            ExpedienteMatrizDetalle detalleEntidad,
            ExpedienteMatrizDetalleDTO detalleDto
    ) {
        List<ExpedienteMatrizMedida> listaOriginal = this.expedienteMatrizMedidaRepository
                .findByExpedienteDetalleMedidaSocioeducativaTokenIdentificadorAndRemovido(
                        detalleEntidad.getTokenIdentificador(), false);

        return this.sincronizarLista(
                listaOriginal,
                detalleDto.getMedidasSocioeducativas(),
                ExpedienteMatrizMedida::getTokenIdentificador,
                ExpedienteMatrizMedidaDTO::getTokenIdentificador,
                (medidaDTO, existente) -> {
                    this.actualizarMedidaDesdeDto(medidaDTO, existente);
                    existente.setExpedienteDetalleMedidaSocioeducativa(detalleEntidad);
                },
                medidaDto -> {
                    ExpedienteMatrizMedida nuevo = new ExpedienteMatrizMedida();
                    this.actualizarMedidaDesdeDto(medidaDto, nuevo);
                    nuevo.setExpedienteDetalleMedidaSocioeducativa(detalleEntidad);
                    nuevo.setRemovido(false);
                    return nuevo;
                },
                restante -> restante.setRemovido(true)
        );
    }

    private List<ExpedienteMatrizMedida> sincronizarMedidasAccesorias(
            ExpedienteMatrizDetalle detalleEntidad,
            ExpedienteMatrizDetalleDTO detalleDto
    ) {
        List<ExpedienteMatrizMedida> listaOriginal = this.expedienteMatrizMedidaRepository
                .findByExpedienteDetalleMedidaAccesoriaTokenIdentificadorAndRemovido(
                        detalleEntidad.getTokenIdentificador(), false);

        return this.sincronizarLista(
                listaOriginal,
                detalleDto.getMedidasAccesorias(),
                ExpedienteMatrizMedida::getTokenIdentificador,
                ExpedienteMatrizMedidaDTO::getTokenIdentificador,
                (medidaDTO, existente) -> {
                    this.actualizarMedidaDesdeDto(medidaDTO, existente);
                    existente.setExpedienteDetalleMedidaAccesoria(detalleEntidad);
                },
                medidaDto -> {
                    ExpedienteMatrizMedida nuevo = new ExpedienteMatrizMedida();
                    this.actualizarMedidaDesdeDto(medidaDto, nuevo);
                    nuevo.setExpedienteDetalleMedidaAccesoria(detalleEntidad);
                    nuevo.setRemovido(false);
                    return nuevo;
                },
                restante -> restante.setRemovido(true)
        );
    }

    private ExpedienteMatriz dtoAEntidad(ExpedienteMatrizDTO dto) {
        if (dto == null) return null;

        ExpedienteMatriz expediente = this.expedienteMatrizRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

        ExpedienteMatriz entidad = Objects.requireNonNullElseGet(expediente, ExpedienteMatriz::new);

        entidad.setNumExpediente(dto.getNumExpediente());
        entidad.setEstado(this.dtoAEntidadCatalogo(dto.getEstado()));
        entidad.setNumOficio(dto.getNumOficio());
        entidad.setFechaOficio(dto.getFechaOficio());
        entidad.setNumExpedienteJudicial(dto.getNumExpedienteJudicial());
        entidad.setObservacion(dto.getObservacion());
        entidad.setTipoCentro(dto.getTipoCentro());
        entidad.setMotivoIngreso(dto.getMotivoIngreso());
        entidad.setNumExpedienteJudicial(dto.getNumExpedienteJudicial());

        return entidad;
    }

    private ExpedienteMatrizDetalle detalleDtoAEntidad(ExpedienteMatrizDetalleDTO dto) {
        if (dto == null) return null;

        ExpedienteMatrizDetalle detalle = new ExpedienteMatrizDetalle();
        detalle.setTipoRegistro(this.dtoAEntidadCatalogo(dto.getTipoRegistro()));
        detalle.setEstado(this.dtoAEntidadCatalogo(dto.getEstado()));
        detalle.setSituacionJuridica(this.dtoAEntidadCatalogo(dto.getSituacionJuridica()));
        detalle.setVariacionMedida(this.dtoAEntidadCatalogo(dto.getVariacionMedida()));
        detalle.setTipoVariacion(this.dtoAEntidadCatalogo(dto.getTipoVariacion()));
        detalle.setMotivoVariacion(this.dtoAEntidadCatalogo(dto.getMotivoVariacion()));
        detalle.setNumResolucion(dto.getNumResolucion());
        detalle.setFechaResolucion(dto.getFechaResolucion());
        detalle.setDecision(dto.getDecision());
        detalle.setTiempoMedSocEduAnios(dto.getTiempoMedSocEduAnios());
        detalle.setTiempoMedSocEduMeses(dto.getTiempoMedSocEduMeses());
        detalle.setTiempoMedSocEduDias(dto.getTiempoMedSocEduDias());
        detalle.setFechaInicioMedida(dto.getFechaInicioMedida());
        detalle.setFechaFinMedida(dto.getFechaFinMedida());
        detalle.setCorteJusticia(this.dtoAEntidadCatalogo(dto.getCorteJusticia()));
        detalle.setInstancia(this.dtoAEntidadCatalogo(dto.getInstancia()));
        detalle.setEspecialidad(this.dtoAEntidadCatalogo(dto.getEspecialidad()));
        detalle.setOrganoJurisdiccional(dto.getOrganoJurisdiccional());
        detalle.setJuez(dto.getJuez());
        detalle.setSecretario(dto.getSecretario());
        detalle.setSancionImpuesta(this.dtoAEntidadCatalogo(dto.getSancionImpuesta()));
        detalle.setNumJornadas(dto.getNumJornadas());
        detalle.setMontoReparacion(dto.getMontoReparacion());
        detalle.setTipoMedSocEduImp(this.dtoAEntidadCatalogo(dto.getTipoMedSocEduImp()));
        detalle.setLugarInfraccion(dto.getLugarInfraccion());
        detalle.setNumJornadas(dto.getNumJornadas());
        detalle.setFrecuenciaIngreso(this.dtoAEntidadCatalogo(dto.getFrecuenciaIngreso()));
        detalle.setRemovido(dto.getRemovido());

        if (dto.getExpedienteDelitos() != null) {
            List<ExpedienteMatrizDelito> detalles = dto.getExpedienteDelitos().stream()
                    .map(this::delitoDtoAEntidad)
                    .collect(Collectors.toList());
            detalles.forEach(delito -> delito.setExpedienteMatrizDetalle(detalle));
            detalle.setExpedienteDelitos(detalles);
        }

        if (dto.getMedidasSocioeducativas() != null) {
            List<ExpedienteMatrizMedida> medidas = dto.getMedidasSocioeducativas().stream()
                    .map(this::medidaDtoAEntidad)
                    .collect(Collectors.toList());
            medidas.forEach(delito -> delito.setExpedienteDetalleMedidaSocioeducativa(detalle));
            detalle.setMedidasSocioeducativas(medidas);
        }

        if (dto.getMedidasAccesorias() != null) {
            List<ExpedienteMatrizMedida> medidas = dto.getMedidasAccesorias().stream()
                    .map(this::medidaDtoAEntidad)
                    .collect(Collectors.toList());
            medidas.forEach(delito -> delito.setExpedienteDetalleMedidaAccesoria(detalle));
            detalle.setMedidasAccesorias(medidas);
        }

        return detalle;
    }

    private void actualizarDetalleDesdeDto(ExpedienteMatrizDetalleDTO dto, ExpedienteMatrizDetalle detalle) {
        detalle.setTipoRegistro(this.dtoAEntidadCatalogo(dto.getTipoRegistro()));
        detalle.setEstado(this.dtoAEntidadCatalogo(dto.getEstado()));
        detalle.setSituacionJuridica(this.dtoAEntidadCatalogo(dto.getSituacionJuridica()));
        detalle.setVariacionMedida(this.dtoAEntidadCatalogo(dto.getVariacionMedida()));
        detalle.setTipoVariacion(this.dtoAEntidadCatalogo(dto.getTipoVariacion()));
        detalle.setMotivoVariacion(this.dtoAEntidadCatalogo(dto.getMotivoVariacion()));
        detalle.setNumResolucion(dto.getNumResolucion());
        detalle.setFechaResolucion(dto.getFechaResolucion());
        detalle.setDecision(dto.getDecision());
        detalle.setTiempoMedSocEduAnios(dto.getTiempoMedSocEduAnios());
        detalle.setTiempoMedSocEduMeses(dto.getTiempoMedSocEduMeses());
        detalle.setTiempoMedSocEduDias(dto.getTiempoMedSocEduDias());
        detalle.setFechaInicioMedida(dto.getFechaInicioMedida());
        detalle.setFechaFinMedida(dto.getFechaFinMedida());
        detalle.setCorteJusticia(this.dtoAEntidadCatalogo(dto.getCorteJusticia()));
        detalle.setInstancia(this.dtoAEntidadCatalogo(dto.getInstancia()));
        detalle.setEspecialidad(this.dtoAEntidadCatalogo(dto.getEspecialidad()));
        detalle.setOrganoJurisdiccional(dto.getOrganoJurisdiccional());
        detalle.setJuez(dto.getJuez());
        detalle.setSecretario(dto.getSecretario());
        detalle.setSancionImpuesta(this.dtoAEntidadCatalogo(dto.getSancionImpuesta()));
        detalle.setNumJornadas(dto.getNumJornadas());
        detalle.setMontoReparacion(dto.getMontoReparacion());
        detalle.setTipoMedSocEduImp(this.dtoAEntidadCatalogo(dto.getTipoMedSocEduImp()));
        detalle.setLugarInfraccion(dto.getLugarInfraccion());
        detalle.setNumJornadas(dto.getNumJornadas());
        detalle.setFrecuenciaIngreso(this.dtoAEntidadCatalogo(dto.getFrecuenciaIngreso()));
    }

    private ExpedienteMatrizDelito delitoDtoAEntidad(ExpedienteMatrizDelitoDTO dto) {
        if (dto == null) return null;

        ExpedienteMatrizDelito entidad = new ExpedienteMatrizDelito();
        entidad.setIdExpedienteDelito(dto.getIdExpedienteDelito());
        entidad.setDelitoGenerico(dtoAEntidadCatalogo(dto.getDelitoGenerico()));
        entidad.setDelitoEspecifico(dtoAEntidadCatalogo(dto.getDelitoEspecifico()));
        entidad.setRemovido(dto.getRemovido());
        return entidad;
    }

    private void actualizarDelitoDesdeDto(ExpedienteMatrizDelitoDTO dto, ExpedienteMatrizDelito entidad) {
        entidad.setDelitoGenerico(dtoAEntidadCatalogo(dto.getDelitoGenerico()));
        entidad.setDelitoEspecifico(dtoAEntidadCatalogo(dto.getDelitoEspecifico()));
        entidad.setRemovido(dto.getRemovido());
    }

    private ExpedienteMatrizMedida medidaDtoAEntidad(ExpedienteMatrizMedidaDTO dto) {
        if (dto == null) return null;

        ExpedienteMatrizMedida entidad = new ExpedienteMatrizMedida();
        entidad.setIdExpedienteMedida(dto.getIdExpedienteMedida());
        entidad.setMedida(dtoAEntidadCatalogo(dto.getMedida()));
        entidad.setRemovido(dto.getRemovido());
        return entidad;
    }

    private void actualizarMedidaDesdeDto(ExpedienteMatrizMedidaDTO dto, ExpedienteMatrizMedida entidad) {
        entidad.setMedida(dtoAEntidadCatalogo(dto.getMedida()));
        entidad.setRemovido(dto.getRemovido());
    }

    private Catalogo dtoAEntidadCatalogo(CatalogoDTO dto) {
        if (dto == null) return null;
        return this.catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
    }

    private ExpedienteMatrizDTO entidadADto(ExpedienteMatriz entidad) {
        if (entidad == null) return null;

        ExpedienteMatrizDTO dto = new ExpedienteMatrizDTO();

        dto.setIdExpediente(entidad.getIdExpediente());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setNumExpediente(entidad.getNumExpediente());
        dto.setEstado(this.entidadADtoCatalogo(entidad.getEstado()));
        dto.setNumOficio(entidad.getNumOficio());
        dto.setFechaOficio(entidad.getFechaOficio());
        dto.setObservacion(entidad.getObservacion());
        dto.setTipoCentro(entidad.getTipoCentro());
        dto.setMotivoIngreso(entidad.getMotivoIngreso());
        dto.setNumExpedienteJudicial(entidad.getNumExpedienteJudicial());
        if (entidad.getFichaIdentificacion() != null) dto.setTokenFichaIdentificacion(entidad.getFichaIdentificacion().getTokenIdentificador());

        if (entidad.getExpedienteDetalle() != null) {
            List<ExpedienteMatrizDetalleDTO> detallesDto = entidad.getExpedienteDetalle().stream()
                    .filter(detalle -> !detalle.getRemovido())
                    .map(this::detalleEntidadADto)
                    .toList();
            dto.setExpedienteDetalle(detallesDto);
        }

        return dto;
    }

    private ExpedienteMatrizDetalleDTO detalleEntidadADto(ExpedienteMatrizDetalle entidad) {
        if (entidad == null) return null;

        ExpedienteMatrizDetalleDTO dto = new ExpedienteMatrizDetalleDTO();

        dto.setIdExpedienteDetalle(entidad.getIdExpedienteDetalle());
        dto.setTipoRegistro(this.entidadADtoCatalogo(entidad.getTipoRegistro()));
        dto.setEstado(this.entidadADtoCatalogo(entidad.getEstado()));
        dto.setSituacionJuridica(this.entidadADtoCatalogo(entidad.getSituacionJuridica()));
        dto.setVariacionMedida(this.entidadADtoCatalogo(entidad.getVariacionMedida()));
        dto.setTipoVariacion(this.entidadADtoCatalogo(entidad.getTipoVariacion()));
        dto.setMotivoVariacion(this.entidadADtoCatalogo(entidad.getMotivoVariacion()));
        dto.setNumResolucion(entidad.getNumResolucion());
        dto.setFechaResolucion(entidad.getFechaResolucion());
        dto.setDecision(entidad.getDecision());
        dto.setTiempoMedSocEduAnios(entidad.getTiempoMedSocEduAnios());
        dto.setTiempoMedSocEduMeses(entidad.getTiempoMedSocEduMeses());
        dto.setTiempoMedSocEduDias(entidad.getTiempoMedSocEduDias());
        dto.setFechaInicioMedida(entidad.getFechaInicioMedida());
        dto.setFechaFinMedida(entidad.getFechaFinMedida());
        dto.setCorteJusticia(this.entidadADtoCatalogo(entidad.getCorteJusticia()));
        dto.setInstancia(this.entidadADtoCatalogo(entidad.getInstancia()));
        dto.setEspecialidad(this.entidadADtoCatalogo(entidad.getEspecialidad()));
        dto.setOrganoJurisdiccional(entidad.getOrganoJurisdiccional());
        dto.setJuez(entidad.getJuez());
        dto.setJuez(entidad.getJuez());
        dto.setSecretario(entidad.getSecretario());
        dto.setSancionImpuesta(this.entidadADtoCatalogo(entidad.getSancionImpuesta()));
        dto.setMontoReparacion(entidad.getMontoReparacion());
        dto.setTipoMedSocEduImp(this.entidadADtoCatalogo(entidad.getTipoMedSocEduImp()));
        dto.setLugarInfraccion(entidad.getLugarInfraccion());
        dto.setNumJornadas(entidad.getNumJornadas());
        dto.setFrecuenciaIngreso(this.entidadADtoCatalogo(entidad.getFrecuenciaIngreso()));
        dto.setRemovido(entidad.getRemovido());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setNumExpedienteJudicial(entidad.getExpedienteMatriz().getNumExpedienteJudicial());


        if (entidad.getExpedienteDelitos() != null) {
            List<ExpedienteMatrizDelitoDTO> delitosDto = entidad.getExpedienteDelitos().stream()
                    .filter(delito -> !delito.getRemovido())
                    .map(this::delitoEntidadADto)
                    .toList();
            dto.setExpedienteDelitos(delitosDto);
        }

        if (entidad.getMedidasSocioeducativas() != null) {
            List<ExpedienteMatrizMedidaDTO> medidasDto = entidad.getMedidasSocioeducativas().stream()
                    .filter(delito -> !delito.getRemovido())
                    .map(this::medidaEntidadADto)
                    .toList();
            dto.setMedidasSocioeducativas(medidasDto);
        }

        if (entidad.getMedidasAccesorias() != null) {
            List<ExpedienteMatrizMedidaDTO> medidasDto = entidad.getMedidasAccesorias().stream()
                    .filter(delito -> !delito.getRemovido())
                    .map(this::medidaEntidadADto)
                    .toList();
            dto.setMedidasAccesorias(medidasDto);
        }

        return dto;
    }

    private ExpedienteMatrizDelitoDTO delitoEntidadADto(ExpedienteMatrizDelito entidad) {
        if (entidad == null) return null;

        ExpedienteMatrizDelitoDTO dto = new ExpedienteMatrizDelitoDTO();
        dto.setIdExpedienteDelito(entidad.getIdExpedienteDelito());
        dto.setDelitoGenerico(entidadADtoCatalogo(entidad.getDelitoGenerico()));
        dto.setDelitoEspecifico(entidadADtoCatalogo(entidad.getDelitoEspecifico()));
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setRemovido(entidad.getRemovido());
        return dto;
    }

    private ExpedienteMatrizMedidaDTO medidaEntidadADto(ExpedienteMatrizMedida entidad) {
        if (entidad == null) return null;

        ExpedienteMatrizMedidaDTO dto = new ExpedienteMatrizMedidaDTO();
        dto.setIdExpedienteMedida(entidad.getIdExpedienteMedida());
        dto.setMedida(entidadADtoCatalogo(entidad.getMedida()));
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setRemovido(entidad.getRemovido());
        return dto;
    }

    private CatalogoDTO entidadADtoCatalogo(Catalogo entidad) {
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

    /**
     * Sincroniza una lista de entidades hija contra los DTOs recibidos.
     * Actualiza existentes, inserta nuevos y marca como removidos los que no vienen en el DTO.
     *
     * @param listaOriginal       Lista actual desde la DB
     * @param listaDtos           Lista de DTOs recibidos (puede ser null)
     * @param getTokenEntidad     Extrae el token de la entidad
     * @param getTokenDto         Extrae el token del DTO
     * @param actualizarEntidad   Aplica los cambios del DTO sobre la entidad
     * @param crearEntidad        Crea una nueva entidad a partir del DTO
     * @param marcarRemovido      Marca la entidad como eliminada lógicamente
     * @param <E>                 Tipo de la entidad
     * @param <D>                 Tipo del DTO
     * @return Lista final lista para guardar
     */
    private <E, D> List<E> sincronizarLista(
            List<E> listaOriginal,
            List<D> listaDtos,
            Function<E, String> getTokenEntidad,
            Function<D, String> getTokenDto,
            BiConsumer<D, E> actualizarEntidad,
            Function<D, E> crearEntidad,
            Consumer<E> marcarRemovido
    ) {
        // 1. Indexar entidades originales en un mapa editable para acceso O(1)
        Map<String, E> mapaOriginal = listaOriginal.stream()
                .collect(Collectors.toMap(getTokenEntidad, u -> u));

        List<E> listaFinalParaGuardar = new ArrayList<>();

        // 2. Procesar los DTOs recibidos
        if (listaDtos != null) {
            for (D dto : listaDtos) {
                String token = getTokenDto.apply(dto);

                if (mapaOriginal.containsKey(token)) {
                    // CASO: ACTUALIZAR
                    E existente = mapaOriginal.remove(token); // remove + get en un solo paso
                    actualizarEntidad.accept(dto, existente);
                    listaFinalParaGuardar.add(existente);
                } else {
                    // CASO: INSERTAR
                    E nuevo = crearEntidad.apply(dto);
                    listaFinalParaGuardar.add(nuevo);
                }
            }
        }

        // 3. ELIMINACIÓN LÓGICA: Los sobrantes no vinieron en el DTO
        mapaOriginal.values().forEach(restante -> {
            marcarRemovido.accept(restante);
            listaFinalParaGuardar.add(restante);
        });

        return listaFinalParaGuardar;
    }
}
