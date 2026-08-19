package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PertenenciaCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PertenenciaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIngresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.PermisoRolUsuarioService;
import net.latinus.sistema.integral.gestion.seguridad.service.util.PaginacionService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PertenenciaServiceImpl implements PertenenciaService {
    private CatalogoRepository catalogoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private PertenenciaRepository pertenenciaRepository;
    private FichaIngresoRepository fichaIngresoRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;
    private PertenenciaCarpetaRepository pertenenciaCarpetaRepository;
    private PaginacionService paginacionService;

    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private PermisoRolUsuarioService permisoRolUsuarioService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PertenenciaDTO>> obtenerPertenenciasEncrypt(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PertenenciaDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
                    Sort.by("idPertenencia").descending()
            );*/

            /*FichaIngreso ingresoEncontrado = this.fichaIngresoRepository.obtenerUltimaFichaIngresoValidaPorTokenFichaIdentificacion(paginacionRequest.getTokenIdentificador());

            if (ingresoEncontrado == null) {
                df.setMensaje("No existen fichas de ingreso, primero cree una.");
                return df;
            }*/

            List<Pertenencia> pertenenciaPage = this.pertenenciaRepository.findByFichaIdentificacionTokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(),false);

            PaginacionResponse<PertenenciaDTO> paginacionResponse = new PaginacionResponse<>();
            List<PertenenciaDTO> pertenenciaDTOList = new ArrayList<>();

            for (Pertenencia pertenencia : pertenenciaPage) {
                PertenenciaDTO pertenenciaDTO = entidadADto(pertenencia);
                pertenenciaDTO.setIdFichaIdentificacion(pertenencia.getFichaIdentificacion().getIdFichaIdentificacion());
                pertenenciaDTO.setNumArticulosRetirados(String.valueOf(pertenencia.getDetalleIngresos().size()));
                pertenenciaDTO.setNumArticulosEntregados(String.valueOf(pertenencia.getDetalleEgresos().size()));
                pertenenciaDTO.setNumArticulosRetiradosSalida(String.valueOf(pertenencia.getDetalleSalidaIngresos().size()));

                StringBuilder sb = new StringBuilder();
                for (PertenenciaDetalleDTO item : pertenenciaDTO.getDetalleIngresos()) {
                    sb.append("(").append(item.getCantidad()).append(") ").append(item.getTipo().getNombre()).append(" - ").append(item.getNombre()).append(" - ").append(item.getEstado().getNombre()).append(", ");
                }
                pertenenciaDTO.setArticulosRetirados(sb.toString());

                sb = new StringBuilder();
                for (PertenenciaDetalleDTO item : pertenenciaDTO.getDetalleEgresos()) {
                    sb.append("(").append(item.getCantidad()).append(") ").append(item.getTipo().getNombre()).append(" - ").append(item.getNombre()).append(" - ").append(item.getEstado().getNombre()).append(", ");
                }
                pertenenciaDTO.setArticulosEntregados(sb.toString());

                sb = new StringBuilder();
                for (PertenenciaDetalleDTO item : pertenenciaDTO.getDetalleSalidaIngresos()) {
                    sb.append("(").append(item.getCantidad()).append(") ").append(item.getTipo().getNombre()).append(" - ").append(item.getNombre()).append(" - ").append(item.getEstado().getNombre()).append(", ");
                }
                pertenenciaDTO.setArticulosRetiradosSalida(sb.toString());

                String pattern = "dd-MM-yyyy HH:mm:ss";
                DateFormat fecha = new SimpleDateFormat(pattern);
                pertenenciaDTO.setFecCreacionTexto(fecha.format(pertenencia.getFechaCreacion()));
                pertenenciaDTOList.add(pertenenciaDTO);
            }

            pertenenciaDTOList.sort(
                    Comparator.comparing(PertenenciaDTO::getFechaCreacion).reversed()
            );

            this.permisoRolUsuarioService
                    .validarPermisoLista(
                            pertenenciaDTOList,
                            paginacionRequest.getTokenIdentificador(),
                            df2.getData()
                    );

            /*paginacionResponse.setData(pertenenciaDTOList);
            paginacionResponse.setTotalItems(pertenenciaPage.getTotalElements());*/
            paginacionResponse = paginacionService.obtenerDatos(pertenenciaDTOList, paginacionRequest);

            df.llenarRespuestaExitosa("Se han encontrado un total de: " + pertenenciaDTOList.size() + " de: " + pertenenciaPage.size() + " elementos disponibles",
                    paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PertenenciaDTO> obtenerPertenenciasPorId(HttpServletRequest httpServletRequest, Long id) {
        RespuestaPorDefectoAuditoria<PertenenciaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            List<Pertenencia> pertenencia = this.pertenenciaRepository.findByIdPertenenciaAndRemovido(id, false);

            if (pertenencia.isEmpty()) {
                df.setMensaje("No existe el documento solicitado.");
                return df;
            }

            PertenenciaDTO pertenenciaDTO = entidadADto(pertenencia.get(0));
            pertenenciaDTO.setTokenFichaIngreso(pertenencia.get(0).getFichaIngreso().getTokenIdentificador());
            pertenenciaDTO.setTokenIdentificador(pertenencia.get(0).getTokenIdentificador());
            pertenenciaDTO.setIdFichaIdentificacion(pertenencia.get(0).getFichaIdentificacion().getIdFichaIdentificacion());

            df.llenarRespuestaExitosa("Se ha encontrado el documento: " + pertenenciaDTO.getIdPertenencia(), pertenenciaDTO);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PertenenciaDTO> crearPertenencia(HttpServletRequest httpServletRequest, PertenenciaDTO pertenenciaDTO) {
        RespuestaPorDefectoAuditoria<PertenenciaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            /*FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(pertenenciaDTO.getTokenPadre(), false);
            if (ficha == null) {
                df.setMensaje("No existe una ficha principal creada");
                return df;
            }*/

            List<Pertenencia> pertenenciaTemp = this.pertenenciaRepository.findByIdPertenenciaAndRemovido(pertenenciaDTO.getIdPertenencia(), false);

            if (!pertenenciaTemp.isEmpty() && !pertenenciaDTO.getEsEdicion()) {
                df.setMensaje("Ya existe un registro con el mismo identificador");
                return df;
            }

            FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(pertenenciaDTO.getTokenFichaIdentificacion(), false);
            FichaIngreso fichaIngreso = this.fichaIngresoRepository.findByTokenIdentificadorAndRemovido(pertenenciaDTO.getTokenFichaIngreso(), false);

            // CREACIÓN DE CARPETA PERTENENCIAS SI NO EXISTE
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(pertenenciaDTO.getTokenFichaIdentificacion(), null, false);
            Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();

            String nemonicoPertenencia = EtiquetaNemonico.CARPETA_GESTION_ADOLES_PERTENENCIAS;
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaPertenencia = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(pertenenciaDTO.getTokenFichaIdentificacion(), nemonicoPertenencia, false);

            if (fichaIdentificacionCarpetaPertenencia == null) {
                String nombreCarpetaPrincipal = "Pertenencias";

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
                carpetaDTO.setDescripcion("Carpeta de entrega/retiro pertenencias");
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardadaRecientemente = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                fichaIdentificacionCarpetaPertenencia = new FichaIdentificacionCarpeta();
                fichaIdentificacionCarpetaPertenencia.setCarpeta(carpetaGuardadaRecientemente);
                fichaIdentificacionCarpetaPertenencia.setFichaIdentificacion(ficha);
                Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoPertenencia, false);
                fichaIdentificacionCarpetaPertenencia.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
                fichaIdentificacionCarpetaPertenencia.setFechaCreacion(new Date());
                fichaIdentificacionCarpetaPertenencia.setIpCrea(httpServletRequest.getRemoteAddr());
                fichaIdentificacionCarpetaPertenencia.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaPertenencia);
            }

            Pertenencia pertenencia;
            if (pertenenciaTemp.isEmpty() && !pertenenciaDTO.getEsEdicion()) {
                pertenencia = dtoAEntidad(pertenenciaDTO, null);
                pertenencia.setFichaIdentificacion(ficha);
                pertenencia.setFichaIngreso(fichaIngreso);
                pertenencia.setFechaCreacion(new Date());
                pertenencia.setIpCrea(httpServletRequest.getRemoteAddr());
                pertenencia.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                pertenencia.setFichaIdentificacion(ficha);
                this.pertenenciaRepository.save(pertenencia);

                // CREACIÓN DE CARPETA

                String nemonico = EtiquetaNemonico.CARPETA_GESTION_ADOLES_PERTENENCIAS;
                FichaIdentificacionCarpeta fichaIdentificacionCarpeta = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(pertenencia.getFichaIdentificacion().getTokenIdentificador(), nemonico, false);
                Carpeta carpetaPadrePertenencias = fichaIdentificacionCarpeta.getCarpeta();

                String pattern = "yyyy-MM-dd-HH:mm:ss";
                DateFormat fecha = new SimpleDateFormat(pattern);
                String fechaFormateada = fecha.format(pertenencia.getFechaCreacion());

                String nombreCarpeta = "pertenencias_" + fechaFormateada;

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpeta);
                carpetaDTO.setDescripcion("Carpeta de entrega y retiro de pertenencia relacionado a: " + pertenencia.getTokenIdentificador());
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePertenencias.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                PertenenciaCarpeta carpetaDetalle = new PertenenciaCarpeta();
                carpetaDetalle.setCarpeta(carpetaGuardada);
                carpetaDetalle.setPertenencia(pertenencia);
                carpetaDetalle.setFechaCreacion(new Date());
                carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                this.pertenenciaCarpetaRepository.save(carpetaDetalle);

                df.llenarRespuestaExitosa("Se ha creado con éxito el registro: " + pertenencia.getIdPertenencia(), pertenenciaDTO);
            } else {
                pertenencia = pertenenciaTemp.get(0);
                pertenencia = dtoAEntidad(pertenenciaDTO, pertenencia);
                pertenencia.setFichaIdentificacion(ficha);
                pertenencia.setFichaIngreso(pertenenciaTemp.get(0).getFichaIngreso());
                pertenencia.setFechaEdicion(new Date());
                pertenencia.setIpEdita(httpServletRequest.getRemoteAddr());
                pertenencia.setUsuarioSistemaEdita(df2.getData().getUsuarioSistema());
                this.pertenenciaRepository.save(pertenencia);

                // CREACIÓN DE CARPETA

                String nemonico = EtiquetaNemonico.CARPETA_GESTION_ADOLES_PERTENENCIAS;
                FichaIdentificacionCarpeta fichaIdentificacionCarpeta = this.fichaIdentificacionCarpetaRepository.findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(pertenencia.getFichaIdentificacion().getTokenIdentificador(), nemonico, false);
                Carpeta carpetaPadrePertenencias = fichaIdentificacionCarpeta.getCarpeta();

                PertenenciaCarpeta carpetaEncontrada = this.pertenenciaCarpetaRepository.findFirstByPertenenciaTokenIdentificadorAndRemovido(pertenencia.getTokenIdentificador(), false);

                if (carpetaEncontrada == null) {
                    String nombreCarpeta = "per_reg_" + pertenencia.getTokenIdentificador();

                    CarpetaDTO carpetaDTO = new CarpetaDTO();
                    carpetaDTO.setNombreCliente(nombreCarpeta);
                    carpetaDTO.setDescripcion("Carpeta de entrega y retiro de pertenencia relacionado a: " + pertenencia.getTokenIdentificador());
                    CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                    carpetaPadreDTO.setTokenIdentificador(carpetaPadrePertenencias.getTokenIdentificador());
                    carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                    this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                    Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                    PertenenciaCarpeta carpetaDetalle = new PertenenciaCarpeta();
                    carpetaDetalle.setCarpeta(carpetaGuardada);
                    carpetaDetalle.setPertenencia(pertenencia);
                    carpetaDetalle.setFechaCreacion(new Date());
                    carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                    carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                    this.pertenenciaCarpetaRepository.save(carpetaDetalle);
                }

                df.llenarRespuestaExitosa("Se ha editado con éxito el registro: " + pertenencia.getIdPertenencia(), pertenenciaDTO);
            }

        }  catch (Exception ex) {
        df.llenarConDatosDeException(ex);
    }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PertenenciaDTO> eliminarPertenencia(HttpServletRequest httpServletRequest, PertenenciaDTO pertenenciaDTO) {
        RespuestaPorDefectoAuditoria<PertenenciaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            List<Pertenencia> pertenenciaTemp = this.pertenenciaRepository.findByIdPertenenciaAndRemovido(pertenenciaDTO.getIdPertenencia(), false);

            if (pertenenciaTemp.isEmpty()) {
                df.setMensaje("No existen registros que coincidan");
                return df;
            }

            Pertenencia pertenencia = pertenenciaTemp.get(0);
            pertenencia.setRemovido(true);
            pertenencia.setFechaEliminacion(new Date());
            pertenencia.setIpElimina(httpServletRequest.getRemoteAddr());
            pertenencia.setUsuarioSistemaElimina(df2.getData().getUsuarioSistema());
            this.pertenenciaRepository.save(pertenencia);
            df.llenarRespuestaExitosa("Se ha eliminado con éxito el registro: " + pertenencia.getIdPertenencia(), pertenenciaDTO);


        }  catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    public Pertenencia dtoAEntidad(PertenenciaDTO dto, Pertenencia pertenenciaIn) {
        if (dto == null) return null;

        Pertenencia pertenencia = new Pertenencia();

        pertenencia.setIdPertenencia(dto.getIdPertenencia());
        pertenencia.setEstado(dtoAEntidadCatalogo(dto.getEstado()));
        pertenencia.setComentarioEgresos(dto.getComentarioEgresos());
        pertenencia.setComentarioIngresos(dto.getComentarioIngresos());
        pertenencia.setComentarioSalidaEgresos(dto.getComentarioSalidaEgresos());
        pertenencia.setComentarioSalidaIngresos(dto.getComentarioSalidaIngresos());
        pertenencia.setFechaCreacion(dto.getFechaCreacion());

        if (dto.getDetalleEgresos() != null) {
            List<PertenenciaDetalle> detalles = dto.getDetalleEgresos().stream()
                    .map(this::detalleDtoAEntidad)
                    .collect(Collectors.toList());
            detalles.forEach(detalle -> detalle.setPertenenciaEgreso(pertenencia));
            pertenencia.setDetalleEgresos(detalles);
        }

        if (dto.getDetalleIngresos() != null) {
            List<PertenenciaDetalle> detalles = dto.getDetalleIngresos().stream()
                    .map(this::detalleDtoAEntidad)
                    .collect(Collectors.toList());
            detalles.forEach(detalle -> detalle.setPertenenciaIngreso(pertenencia));
            pertenencia.setDetalleIngresos(detalles);
        }

        if (dto.getDetalleSalidaIngresos() != null) {
            List<PertenenciaDetalle> detalles = dto.getDetalleSalidaIngresos().stream()
                    .map(this::detalleDtoAEntidad)
                    .collect(Collectors.toList());
            detalles.forEach(detalle -> detalle.setPertenenciaSalidaIngreso(pertenencia));
            pertenencia.setDetalleSalidaIngresos(detalles);
        }

        return pertenencia;
    }

    private PertenenciaDetalle detalleDtoAEntidad(PertenenciaDetalleDTO dto) {
        if (dto == null) return null;

        PertenenciaDetalle detalle = new PertenenciaDetalle();
        detalle.setIdPertenenciaDetalle(dto.getIdPertenenciaDetalle());
        detalle.setNombre(dto.getNombre());
        detalle.setTipo(dtoAEntidadCatalogo(dto.getTipo()));
        detalle.setEstado(dtoAEntidadCatalogo(dto.getEstado()));
        detalle.setCantidad(dto.getCantidad());
        detalle.setObservacion(dto.getObservacion());
        return detalle;
    }

    public PertenenciaDTO entidadADto(Pertenencia entidad) {
        if (entidad == null) return null;

        PertenenciaDTO dto = new PertenenciaDTO();
        dto.setIdPertenencia(entidad.getIdPertenencia());
        dto.setEstado(entidadADtoCatalogo(entidad.getEstado()));
        dto.setComentarioEgresos(entidad.getComentarioEgresos());
        dto.setComentarioIngresos(entidad.getComentarioIngresos());
        dto.setComentarioSalidaEgresos(entidad.getComentarioSalidaEgresos());
        dto.setComentarioSalidaIngresos(entidad.getComentarioSalidaIngresos());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        if (entidad.getFichaIdentificacion() != null) dto.setTokenFichaIdentificacion(entidad.getFichaIdentificacion().getTokenIdentificador());

        if (entidad.getDetalleEgresos() != null) {
            List<PertenenciaDetalleDTO> detallesDto = entidad.getDetalleEgresos().stream()
                    .map(this::detalleEntidadADto)
                    .collect(Collectors.toList());
            dto.setDetalleEgresos(detallesDto);
        }

        if (entidad.getDetalleIngresos() != null) {
            List<PertenenciaDetalleDTO> detallesDto = entidad.getDetalleIngresos().stream()
                    .map(this::detalleEntidadADto)
                    .collect(Collectors.toList());
            dto.setDetalleIngresos(detallesDto);
        }

        if (entidad.getDetalleSalidaIngresos() != null) {
            List<PertenenciaDetalleDTO> detallesDto = entidad.getDetalleSalidaIngresos().stream()
                    .map(this::detalleEntidadADto)
                    .collect(Collectors.toList());
            dto.setDetalleSalidaIngresos(detallesDto);
        }

        return dto;
    }

    private PertenenciaDetalleDTO detalleEntidadADto(PertenenciaDetalle entidad) {
        if (entidad == null) return null;

        PertenenciaDetalleDTO dto = new PertenenciaDetalleDTO();
        dto.setIdPertenenciaDetalle(entidad.getIdPertenenciaDetalle());
        dto.setNombre(entidad.getNombre());
        dto.setTipo(entidadADtoCatalogo(entidad.getTipo()));
        dto.setEstado(entidadADtoCatalogo(entidad.getEstado()));
        dto.setCantidad(entidad.getCantidad());
        dto.setObservacion(entidad.getObservacion());
        return dto;
    }

    private Catalogo dtoAEntidadCatalogo(CatalogoDTO dto) {
        if (dto == null) return null;
        return this.catalogoRepository.findByTokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);
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
}
