package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.fuga.EventoFuga;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ActaExternamiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.InformePermisoSalidaAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.RegistroSalida;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.Traslado;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.TrasladoAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.model.both.FichaIdentificacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.fuga.EventoFugaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ActaExternamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.InformePermisoSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.RegistroSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.FichaIdentificacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.ValidarIngresoFichaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.HistoricoEntradaSalidaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.fuga.EventoFugaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.*;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.FichaMedicaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.LocalidadRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.salida.InformePermisoSalidaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.*;
import net.latinus.sistema.integral.gestion.seguridad.repository.tras.TrasladoAdolescenteRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.UtilsService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.DocumentoService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso.HistoricoFichaIdentificacionService;
import net.latinus.sistema.integral.gestion.seguridad.service.tras.TrasladoServiceImpl;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

@Service
@Transactional
@AllArgsConstructor
public class FichaIdentificacionServiceImpl implements FichaIdentificacionService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private JerarquiaRepository jerarquiaRepository;
    private CatalogoRepository catalogoRepository;

    private final LogService logService = new LogService(this.getClass());
    private UtilsService utilsService;
    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private DocumentoService documentoService;
    private FichaDeIdentificacionDocumentoRepository fichaDeIdentificacionDocumentoRepository;
    private DocumentoRepository documentoRepository;
    private LocalidadRepository localidadRepository;
    private FichaIngresoRepository fichaIngresoRepository;
    private ExpedienteMatrizRepository expedienteMatrizRepository;
    private DocumentosFichaIngresoRepository documentosFichaIngresoRepository;
    private PertenenciaRepository pertenenciaRepository;
    private FuncionarioRepository funcionarioRepository;
    private FichaIngresoServiceImpl fichaIngresoServiceImpl;
    private HistoricoEntradaSalidaRepository historicoEntradaSalidaRepository;
    private TrasladoAdolescenteRepository trasladoAdolescenteRepository;
    private InformePermisoSalidaRepository informePermisoSalidaRepository;
    private EventoFugaRepository eventoFugaRepository;
    private InformeFinalAbiertoRepository informeFinalAbiertoRepository;
    private ActaExternamientoRepository actaExternamientoRepository;
    private TrasladoServiceImpl trasladoServiceImpl;
    private FichaMedicaRepository fichaMedicaRepository;

    private HistoricoFichaIdentificacionService historicoFichaIdentificacionService;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIdentificacionDTO>> obtenerFichasIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIdentificacionDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validar el JWT y obtener datos del usuario
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar cuerpo y mapear a request
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            FichaIdentificacionRequest paginacionRequest = new Gson().fromJson(body, FichaIdentificacionRequest.class);

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            // Obtener funcionario y jerarquía
            Funcionario funcionario = funcionarioRepository.findByNumeroDeDocumentoAndRemovidoAndBloqueado(
                    usuarioSistema.getNumeroDeDocumento(), false, false);

            Jerarquia jerarquiaActual = bodyJwtValido.getJerarquia();

            JerarquiaDTO jerarquiaDTO = obtenerJerarquiaDTOFormJerarquia(jerarquiaActual);

            String orderBy = "fechaIngreso";

            if (!ObjectUtils.isEmpty(paginacionRequest.getSort())) {
                orderBy = paginacionRequest.getSort();
                if (orderBy.equals("apellidos")) {
                    orderBy = "apellidoPaterno";
                }

                if (orderBy.equals("nombreTipoDocumento")) {
                    orderBy = "tipoIdentificacion.nombre";
                }

                if (orderBy.equals("numeroDocumento")) {
                    orderBy = "numeroIdentificacion";
                }

                if (orderBy.equals("tipoSexo")) {
                    orderBy = "tipoSexo.nombre";
                }

                if (orderBy.equals("nacionalidad")) {
                    orderBy = "paisNacimiento.nombre";
                }

                if (orderBy.equals("ocupacion")) {
                    orderBy = "tipoOcupacion.nombre";
                }

                if (orderBy.equals("tipoEstadoCivil")) {
                    orderBy = "estadoCivil.nombre";
                }

                if (orderBy.equals("tipoEntrada")) {
                    orderBy = "tipoEntrada.nombre";
                }

            }

            Sort.Direction sortDirection = Sort.Direction.DESC;

            if (!ObjectUtils.isEmpty(paginacionRequest.getDirection())) {
                sortDirection = "desc".equalsIgnoreCase(paginacionRequest.getDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            }


            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by(sortDirection, orderBy)
            );

            // Obtener página de fichas
            Page<FichaIdentificacion> fichaIdentificacionPage = obtenerFichas(empresa, jerarquiaDTO, paginacionRequest, pageable, paginacionRequest.getTodosEstados(),
                    paginacionRequest.getPostEgreso());

            // Mapear resultados a DTOs
            List<FichaIdentificacionDTO> fichaIdentificacionDTOList = fichaIdentificacionPage
                    .map(this::mapearFichaIdentificacion)
                    .toList();

            // Construir respuesta
            PaginacionResponse<FichaIdentificacionDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(fichaIdentificacionDTOList);
            paginacionResponse.setTotalItems(fichaIdentificacionPage.getTotalElements());

            df.llenarRespuestaExitosa(String.format("Se han encontrado %d de %d elementos disponibles",
                    fichaIdentificacionDTOList.size(), fichaIdentificacionPage.getTotalElements()), paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIdentificacionResumenDTO>> obtenerFichasIdentificacionResumido(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaIdentificacionResumenDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validar el JWT y obtener datos del usuario
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar cuerpo y mapear a request
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String body = df22.getData();
            FichaIdentificacionRequest paginacionRequest = new Gson().fromJson(body, FichaIdentificacionRequest.class);

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();

            String valorBusqueda = (paginacionRequest.getFilter() != null) ? paginacionRequest.getFilter().toLowerCase() : "";

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize()
            );

            Page<FichaIdentificacionResumenDTO> fichasPage = this.fichaIdentificacionRepository.obtenerFichasResumenPorTokenEmpresaYRemovido(empresa.getTokenIdentificador(), false, valorBusqueda, pageable);

            // Construir respuesta
            PaginacionResponse<FichaIdentificacionResumenDTO> paginacionResponse = new PaginacionResponse<>();
            paginacionResponse.setData(fichasPage.stream().toList());
            paginacionResponse.setTotalItems(fichasPage.getTotalElements());

            df.llenarRespuestaExitosa(String.format("Se han encontrado %d de %d elementos disponibles",
                    fichasPage.getSize(), fichasPage.getTotalElements()), paginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> crearFichaIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            Empresa empresa = df2.getData().getEmpresa();

            FichaIdentificacionDTO fichaIdentificacionDTO = new Gson().fromJson(bodyString, FichaIdentificacionDTO.class);

            fichaIdentificacionDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            List<FichaIdentificacion> fichasCreadas = new ArrayList<>();

            FichaIdentificacion fichaIdentificacion = null;
            HistoricoEntradaSalida historicoEntradaSalida = null;
            Boolean yaExiste = false;
            Jerarquia jerarquiaAnterior = null;
            if (fichaIdentificacionDTO.getEsEdicion()) {
                fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getTokenIdentificador(), Boolean.FALSE);
                if (fichaIdentificacion == null) {
                    df.setMensaje("La ficha de identificación a editar no existe o ya fue eliminada anteriormente");
                    df.setData(fichaIdentificacionDTO);
                    return df;
                }

                fichasCreadas = this.fichaIdentificacionRepository.
                        findByTokenIdentificadorNotAndRemovidoAndNumeroIdentificacion(fichaIdentificacionDTO.getTokenIdentificador(), false
                                , fichaIdentificacionDTO.getNumeroDocumento());

                if (!fichasCreadas.isEmpty()) {
                    df.setMensaje("Ya existe una ficha identificacion activa con este numero de documento dentro del sistema");
                    df.setData(fichaIdentificacionDTO);
                    return df;
                }

                fichaIdentificacion.setFechaEdicion(new Date());
                fichaIdentificacion.setIpEdita(ip);
                fichaIdentificacion.setUsuarioSistemaEdita(usuarioLogin);
            } else {

                String tokenCentro = fichaIdentificacionDTO.getCentro() != null ?
                        fichaIdentificacionDTO.getCentro().getTokenIdentificador() : null;

                if (tokenCentro == null) {
                    df.setMensaje("El funcionario no tiene un centro asignado.");
                    return df;
                }

                fichasCreadas = this.fichaIdentificacionRepository
                        .findByNumeroIdentificacionNotNullAndRemovidoOrderByIdFichaIdentificacionDesc(
                                fichaIdentificacionDTO.getNumeroDocumento(), false
                        );

                if (!fichasCreadas.isEmpty()) {
                    fichaIdentificacion = fichasCreadas.get(0);
                    yaExiste = true;
                    jerarquiaAnterior = fichaIdentificacion.getCentroIngreso();
                } else {
                    fichaIdentificacion = new FichaIdentificacion();
                }

                if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getTipoEntrada()) && !fichaIdentificacionDTO.getTipoEntrada().getNemonico().equals("ENTRADA_INGRESO_NUEVO")) {
                    List<HistoricoEntradaSalida> registrosAnteriores = historicoEntradaSalidaRepository.findByNumeroIdentificacionAndRegistroActivo
                            (fichaIdentificacionDTO.getNumeroDocumento(), true);

                    if (registrosAnteriores.isEmpty()) {
                        df.setMensaje("No se encontraron registros de salidas activas para el número de identificación ingresado.");
                        df.setData(fichaIdentificacionDTO);
                        return df;
                    }



//                    Optional<HistoricoEntradaSalida> optHistorico = this.historicoEntradaSalidaRepository.findLastByFichaIdentificacionAndRegistroActivo(
//                            fichaIdentificacion.getTokenIdentificador());
//
//                    HistoricoEntradaSalida historico = optHistorico.get();
//                    historico.setCentroIngreso(jerarquiaRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getCentro().getTokenIdentificador(), false));
//                    historico.setRegistroActivo(false);
//                    historico.setFechaEntrada(new Date());
//                    this.historicoEntradaSalidaRepository.save(historico);
//                    historicoEntradaSalida = historico;
                }else if(!ObjectUtils.isEmpty(fichaIdentificacionDTO.getTipoEntrada()) && fichaIdentificacionDTO.getTipoEntrada().getNemonico().equals("ENTRADA_INGRESO_NUEVO")){
                    if(!ObjectUtils.isEmpty(fichaIdentificacion.getEstado())){

                        List<String> estados = List.of(
                                "ESTADO_ADOLESCENTE_LIBRE",
                                "ESTADO_ADOLESCENTE_FUGADO",
                                "ESTADO_ADOLESCENTE_SENTENCIADO_PROCESADO"
                        );

                        if(!estados.contains(fichaIdentificacion.getEstado().getNemonico())){
                            df.setMensaje("El adolescente no se encuentra libre.");
                            df.setData(fichaIdentificacionDTO);
                            return df;
                        }
                    }
                }

                fichaIdentificacion.setFechaCreacion(new Date());
                fichaIdentificacion.setIpCrea(ip);
                fichaIdentificacion.setUsuarioSistemaCrea(usuarioLogin);
                fichaIdentificacion.setEmpresa(empresa);
                fichaIdentificacion.setEstado(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_ADOLESCENTE_INGRESADO", false));
                if (fichaIdentificacionDTO.getCentro() != null) {
                    Jerarquia centro = jerarquiaRepository.findJerarquiaByTokenIdentificador(fichaIdentificacionDTO.getCentro().getTokenIdentificador());
                    fichaIdentificacion.setCentroIngreso(centro);
                }
            }

            if (fichaIdentificacionDTO.getApellidoPaterno() != null) {
                fichaIdentificacion.setApellidoPaterno(fichaIdentificacionDTO.getApellidoPaterno().replace("\u0000", ""));
            }
            if (fichaIdentificacionDTO.getApellidoMaterno() != null) {
                fichaIdentificacion.setApellidoMaterno(fichaIdentificacionDTO.getApellidoMaterno().replace("\u0000", ""));
            }
            if (fichaIdentificacionDTO.getNombres() != null) {
                fichaIdentificacion.setNombres(fichaIdentificacionDTO.getNombres().replace("\u0000", ""));
            }
            if (fichaIdentificacionDTO.getFechaNacimiento() != null) {
                fichaIdentificacion.setFechaNacimiento(fichaIdentificacionDTO.getFechaNacimiento());
            }
            if (fichaIdentificacionDTO.getAlias() != null) {
                fichaIdentificacion.setAlias(fichaIdentificacionDTO.getAlias().replace("\u0000", ""));
            }
            if (fichaIdentificacionDTO.getNumeroHijos() != null) {
                fichaIdentificacion.setNumeroHijos(fichaIdentificacionDTO.getNumeroHijos());
            }
            if (fichaIdentificacionDTO.getImpedimentoDiscapacidad() != null) {
                fichaIdentificacion.setImpedimentoDiscapacidad(fichaIdentificacionDTO.getImpedimentoDiscapacidad());
            }
            if (fichaIdentificacionDTO.getDireccion() != null) {
                fichaIdentificacion.setDireccion(fichaIdentificacionDTO.getDireccion().replace("\u0000", ""));
            }
            if (fichaIdentificacionDTO.getLugarNacimiento() != null) {
                fichaIdentificacion.setLugarNacimiento(fichaIdentificacionDTO.getLugarNacimiento().replace("\u0000", ""));
            }
            if (fichaIdentificacionDTO.getOficioInternamiento() != null) {
                fichaIdentificacion.setOficioInternamiento(fichaIdentificacionDTO.getOficioInternamiento());
            }
            if (fichaIdentificacionDTO.getUbigeoUbicacion() != null) {
                fichaIdentificacion.setCodigoUbigeoDireccion(fichaIdentificacionDTO.getUbigeoUbicacion().replace("\u0000", ""));
            }
//            Catalogo ubigeoDireccion = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getTokenIdentificadorUbigeoDireccion(), Boolean.FALSE);
//            fichaIdentificacion.setUbigeoDireccion(ubigeoDireccion);
            if (fichaIdentificacionDTO.getPaisNacimiento() != null) {
                fichaIdentificacion.setPaisNacimiento(this.localidadRepository.findByNemonicoAndRemovido(fichaIdentificacionDTO.getPaisNacimiento(), false));
                if (fichaIdentificacionDTO.getPaisNacimiento().equals("PAIS-PERU")) {
                    fichaIdentificacion.setCodigoUbigeoNacimiento(fichaIdentificacionDTO.getUbigeoNacimiento().replace("\u0000", ""));

                } else {
                    fichaIdentificacion.setCodigoUbigeoNacimiento(null);
                }
            }

            if (fichaIdentificacionDTO.getTokenIdentificadorEstadoCivil() != null) {
                Catalogo estadoCivil = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getTokenIdentificadorEstadoCivil(), Boolean.FALSE);
                fichaIdentificacion.setEstadoCivil(estadoCivil);
            }
            if (fichaIdentificacionDTO.getTokenIdentificadorOrigenEtnico() != null) {
                Catalogo origenEtnico = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getTokenIdentificadorOrigenEtnico(), Boolean.FALSE);
                fichaIdentificacion.setOrigenEtnico(origenEtnico);
            }

            if (fichaIdentificacionDTO.getTokenIdentificadorGrupoVulnerable() != null) {
                Catalogo grupoVulnerable = catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getTokenIdentificadorGrupoVulnerable(), Boolean.FALSE);
                fichaIdentificacion.setGrupoVulnerable(grupoVulnerable);
            }

            if (fichaIdentificacionDTO.getTipoViveCon() != null) {
                fichaIdentificacion.setViveConParentesco(catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getTipoViveCon(), Boolean.FALSE));

            }

            if (fichaIdentificacionDTO.getTipoGenero() != null) {
                fichaIdentificacion.setGenero(catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getTipoGenero(), Boolean.FALSE));

            }
            if (fichaIdentificacionDTO.getTipoDocumento() != null) {
                fichaIdentificacion.setTipoIdentificacion(catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getTipoDocumento(), Boolean.FALSE));

            }

            if (fichaIdentificacionDTO.getTipoSexo() != null) {
                fichaIdentificacion.setTipoSexo(catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getTipoSexo(), Boolean.FALSE));

            }
            if (fichaIdentificacionDTO.getOcupacion() != null) {
                fichaIdentificacion.setTipoOcupacion(catalogoRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getOcupacion(), Boolean.FALSE));

            }


            if (fichaIdentificacionDTO.getJuez() != null) {
                fichaIdentificacion.setJuez(fichaIdentificacionDTO.getJuez());
            }

            if (fichaIdentificacionDTO.getJuzgado() != null) {
                fichaIdentificacion.setJuzgado(fichaIdentificacionDTO.getJuzgado());
            }

            if (fichaIdentificacionDTO.getIngresahijos() != null) {
                fichaIdentificacion.setIngresoConHijo(fichaIdentificacionDTO.getIngresahijos());
            }

            if (fichaIdentificacionDTO.getOtroOrigenEtnico() != null) {
                fichaIdentificacion.setOtroOrigenEtnico(fichaIdentificacionDTO.getOtroOrigenEtnico());
            }

            if (fichaIdentificacionDTO.getObservacionIngreso() != null) {
                fichaIdentificacion.setObservacionIngreso(fichaIdentificacionDTO.getObservacionIngreso());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getInstancia())) {
                fichaIdentificacion.setInstancia(dtoToCatalogo(fichaIdentificacionDTO.getInstancia()));
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getCorteJusticia())) {
                fichaIdentificacion.setCorteJusticia(dtoToCatalogo(fichaIdentificacionDTO.getCorteJusticia()));
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getEspecialidad())) {
                fichaIdentificacion.setEspecialidad(dtoToCatalogo(fichaIdentificacionDTO.getEspecialidad()));
            }

            if (fichaIdentificacionDTO.getOrganoJurisdiccional() != null) {
                fichaIdentificacion.setOrganoJurisdiccional(fichaIdentificacionDTO.getOrganoJurisdiccional());
            }

            if (fichaIdentificacionDTO.getSecretario() != null) {
                fichaIdentificacion.setSecretario(fichaIdentificacionDTO.getSecretario());
            }

            if (fichaIdentificacionDTO.getEmail() != null) {
                fichaIdentificacion.setEmail(fichaIdentificacionDTO.getEmail());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getNumeroFojas())) {
                fichaIdentificacion.setNumeroFojas(Long.parseLong(fichaIdentificacionDTO.getNumeroFojas()));
            } else {
                fichaIdentificacion.setNumeroFojas(0L);
            }

            if (fichaIdentificacionDTO.getModalidadEstudio() != null) {
                fichaIdentificacion.setModalidadEstudio(fichaIdentificacionDTO.getModalidadEstudio());

                // Manejar los niveles según la modalidad
                switch (fichaIdentificacionDTO.getModalidadEstudio()) {
                    case "MODALIDAD_ESTUDIO_EBR":
                        if (fichaIdentificacionDTO.getNivelEBR() != null) {
                            Catalogo nivelEBR = catalogoRepository.findByTokenIdentificadorAndRemovido(
                                    fichaIdentificacionDTO.getNivelEBR(), Boolean.FALSE);
                            fichaIdentificacion.setNivelEBR(nivelEBR);
                        }
                        break;
                    case "MODALIDAD_ESTUDIO_SUPERIOR":
                        if (fichaIdentificacionDTO.getNivelSuperior() != null) {
                            Catalogo nivelSuperior = catalogoRepository.findByTokenIdentificadorAndRemovido(
                                    fichaIdentificacionDTO.getNivelSuperior(), Boolean.FALSE);
                            fichaIdentificacion.setNivelSuperior(nivelSuperior);
                        }
                        break;
                    case "MODALIDAD_ESTUDIO_EBA":
                        if (fichaIdentificacionDTO.getNivelEBA() != null) {
                            Catalogo nivelEBA = catalogoRepository.findByTokenIdentificadorAndRemovido(
                                    fichaIdentificacionDTO.getNivelEBA(), Boolean.FALSE);
                            fichaIdentificacion.setNivelEBA(nivelEBA);
                        }
                        break;
                }
            }

            fichaIdentificacion = this.fichaIdentificacionRepository.save(fichaIdentificacion);
            if (fichaIdentificacion.getTipoIdentificacion().getNemonico().equals("TIPO_DOCUMENTO_IDENTIFICACION_SIN_DOCUMENTO")
                    && fichasCreadas.isEmpty()) {
                fichaIdentificacion.setNumeroIdentificacion(fichaIdentificacion.getIdFichaIdentificacion() + "");
            } else {
                fichaIdentificacion.setNumeroIdentificacion(fichaIdentificacionDTO.getNumeroDocumento());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getTipoEntrada())) {
                fichaIdentificacion.setTipoEntrada(dtoToCatalogo(fichaIdentificacionDTO.getTipoEntrada()));
            }


            fichaIdentificacion.setFechaIngreso(new Date());

            this.fichaIdentificacionRepository.save(fichaIdentificacion);
            fichaIdentificacionDTO.setTokenIdentificador(fichaIdentificacion.getTokenIdentificador());

            FichaMedica fichaM = this.fichaMedicaRepository.findByFichaIdentificacion_TokenIdentificadorAndRemovido(fichaIdentificacion.getTokenIdentificador(), false);

            if (fichaM == null) {
                FichaMedica fichaMedicaDb = new FichaMedica();
                fichaMedicaDb.setFichaIdentificacion(fichaIdentificacion);
                fichaMedicaDb.setIpCrea(ip);
                fichaMedicaDb.setFechaCreacion(new Date());
                fichaMedicaDb.setUsuarioSistemaCrea(usuarioSistema);
                this.fichaMedicaRepository.save(fichaMedicaDb);
            }


            List<FichaIngreso> fichaIngresoList = this.fichaIngresoRepository.findByFichaIdentificacionTokenIdentificadorAndRemovidoAndActivo(
                    fichaIdentificacion.getTokenIdentificador(),
                    false, true);

//            if (!fichaIdentificacionDTO.getEsEdicion() && !ObjectUtils.isEmpty(fichaIdentificacionDTO.getNumeroDocumento())) {
//
//                List<HistoricoEntradaSalida> registrosAnteriores = historicoEntradaSalidaRepository.findByNumeroIdentificacionAndRegistroActivo
//                        (fichaIdentificacionDTO.getNumeroDocumento(), true);
//                for (HistoricoEntradaSalida registro : registrosAnteriores) {
//                    registro.setRegistroActivo(false);
//                }
//                historicoEntradaSalidaRepository.saveAll(registrosAnteriores);
//
//                Catalogo tipoEntrada = catalogoRepository.findByNemonicoAndRemovido("TIPO_HISTORICO_ENTRADA", false);
//                historicoEntradaSalida = new HistoricoEntradaSalida();
//                historicoEntradaSalida.setNumeroIdentificacion(fichaIdentificacion.getNumeroIdentificacion());
//                historicoEntradaSalida.setTipoDocumentoIdentificacion(fichaIdentificacion.getTipoIdentificacion());
//                historicoEntradaSalida.setFechaEntrada(new Date());
//                historicoEntradaSalida.setRegistroActivo(true); // Se marca como activo
//                historicoEntradaSalida.setTipoRegistro(tipoEntrada);
//                historicoEntradaSalida.setFichaIdentificacion(fichaIdentificacion);
//                historicoEntradaSalida.setCentroIngreso(jerarquiaRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getCentro().getTokenIdentificador(), false));
//                historicoEntradaSalidaRepository.save(historicoEntradaSalida);
//            }

            // Llenar histórico de ficha de identificación

            List<String> tiposEntrada = List.of(
                    "ENTRADA_TRASLADO",
                    "ENTRADA_INGRESO_NUEVO",
                    "ENTRADA_FUGA"
            );

            AuditObject auditObject = AuditObject.builder()
                    .usuarioSistema(df2.getData().getUsuarioSistema())
                    .ip(httpServletRequest.getRemoteAddr())
                    .build();

            if (!fichaIdentificacionDTO.getEsEdicion() || (fichaIdentificacionDTO.getTipoEntrada() != null && tiposEntrada.contains(fichaIdentificacionDTO.getTipoEntrada().getNemonico()))) {
                this.historicoFichaIdentificacionService.crearActualizar(
                        fichaIdentificacion,
                        fichaIdentificacion.getObservacionIngreso(),
                        null,
                        auditObject,
                        false);
            }

            df.llenarRespuestaExitosa(
                    "Se " + (fichaIdentificacionDTO.getEsEdicion() ? "editó" : "creó") +
                            " con éxito la ficha del adolescente" + (fichaIdentificacionDTO.getNumeroDocumento() !=null
                                    ? " con DNI:"+fichaIdentificacionDTO.getNumeroDocumento() : "") + ".",
                    fichaIdentificacionDTO
            );

            //Creacion de las carpetas internas para el manejo de los documentos del adolescente infractor
            if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getTipoEntrada())) {

//
//                Optional<HistoricoEntradaSalida> optHistorico = this.historicoEntradaSalidaRepository.findLastByFichaIdentificacionAndRegistroActivo(
//                        fichaIdentificacion.getTokenIdentificador());

//                List<HistoricoEntradaSalida> historicos = historicoEntradaSalidaRepository
//                        .findByFichaIdentificacionAndRegistroActivo(fichaIdentificacion.getTokenIdentificador());
//
//                HistoricoEntradaSalida ultimoHistorico = historicos.isEmpty() ? null : historicos.get(0);


                if (fichaIdentificacionDTO.getTipoEntrada().getNemonico().equals("ENTRADA_INGRESO_NUEVO")) {
                    if (yaExiste) {

                        if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getInformeFinalAbierto())) {
                            InformeFinalAbierto opt = this.informeFinalAbiertoRepository.findByTokenIdentificadorAndRemovido(
                                    fichaIdentificacionDTO.getInformeFinalAbierto().getTokenIdentificador(), false);
//                            opt.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_INACTIVO",false));
                            this.informeFinalAbiertoRepository.save(opt);

                            Optional<HistoricoEntradaSalida> optH = this.historicoEntradaSalidaRepository.findByInformeFinalTokenIdentificador(
                                    opt.getTokenIdentificador()
                            );
                            if (optH.isPresent()) {
                                HistoricoEntradaSalida historico = optH.get();
                                historico.setCentroIngreso(jerarquiaRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getCentro().getTokenIdentificador(), false));
                                historico.setRegistroActivo(false);
                                historico.setFechaEntrada(new Date());
                                this.historicoEntradaSalidaRepository.save(historico);
                                historicoEntradaSalida = historico;
                            }
                        } else if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getActaExternamiento())) {
                            ActaExternamiento acta = this.actaExternamientoRepository.findByTokenIdentificadorAndRemovido(
                                    fichaIdentificacionDTO.getActaExternamiento().getTokenIdentificador(), false);
                            acta.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_INACTIVO", false));
                            this.actaExternamientoRepository.save(acta);
                            Optional<HistoricoEntradaSalida> optH = this.historicoEntradaSalidaRepository.findByExternamientoTokenIdentificador(
                                    acta.getTokenIdentificador()
                            );
                            if (optH.isPresent()) {
                                HistoricoEntradaSalida historico = optH.get();
                                historico.setCentroIngreso(jerarquiaRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getCentro().getTokenIdentificador(), false));
                                historico.setRegistroActivo(false);
                                historico.setFechaEntrada(new Date());
                                this.historicoEntradaSalidaRepository.save(historico);
                                historicoEntradaSalida = historico;
                            }
                        }

                    } else {
                        this.creacionDeLasCarpetaDeLaFichaDeIdentificacion(httpServletRequest, fichaIdentificacionDTO, empresa, usuarioSistema);
                    }

                } else if (fichaIdentificacionDTO.getTipoEntrada().getNemonico().equals("ENTRADA_TRASLADO")) {
                    Optional<TrasladoAdolescente> optionalTrasladoAdolescente = this.trasladoAdolescenteRepository.findByIdTrasladoAdolescenteAndRemovidoFalse(
                            fichaIdentificacionDTO.getTrasladoAdolescente().getIdTrasladoAdolescente()
                    );
                    if (optionalTrasladoAdolescente.isPresent()) {
                        TrasladoAdolescente trasladoAdolescente = optionalTrasladoAdolescente.get();
//                        trasladoAdolescente.setIsComplete(true);
                        trasladoAdolescente.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_INACTIVO", false));
                        trasladoAdolescente.setIsComplete(true);
                        this.trasladoAdolescenteRepository.save(trasladoAdolescente);
                        Optional<HistoricoEntradaSalida> opt = this.historicoEntradaSalidaRepository.findByTrasladoAdolescenteTokenIdentificador(
                                trasladoAdolescente.getTokenIdentificador()
                        );
                        if (opt.isPresent()) {
                            HistoricoEntradaSalida historico = opt.get();
                            historico.setCentroIngreso(jerarquiaRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getCentro().getTokenIdentificador(), false));
                            historico.setRegistroActivo(false);
                            historico.setFechaEntrada(new Date());
                            this.historicoEntradaSalidaRepository.save(historico);
                            historicoEntradaSalida = historico;
                            fichaIdentificacion.setTieneProceso(false);


                        }
                        fichaIdentificacion.setCentroIngreso(trasladoAdolescente.getTraslado().getCentroDestino());
                        this.trasladoServiceImpl.actualizarTrasladoPorTrasladoAdolescente(trasladoAdolescente);
                    }


                } else if (fichaIdentificacionDTO.getTipoEntrada().getNemonico().equals("ENTRADA_SALIDA_TEMPORAL")) {
                    InformePermisoSalidaAdolescente informe = this.informePermisoSalidaRepository.findByTokenIdentificadorAndRemovido
                            (fichaIdentificacionDTO.getPermisoSalida().getTokenIdentificador(), false);
                    Date ahora = new java.util.Date();

                    Date regreso = informe.getFechaHoraRegreso();
                    if (regreso.before(ahora) || regreso.equals(ahora)) {
                        informe.setIsComplete(true);
                        informe.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_INACTIVO", false));

                        Optional<HistoricoEntradaSalida> opt = this.historicoEntradaSalidaRepository.findByPermisoSalidaTokenIdentificador(
                                informe.getTokenIdentificador()
                        );
                        if (opt.isPresent()) {
                            HistoricoEntradaSalida historico = opt.get();
                            historico.setCentroIngreso(jerarquiaRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getCentro().getTokenIdentificador(), false));
                            historico.setRegistroActivo(false);
                            historico.setFechaEntrada(new Date());
                            this.historicoEntradaSalidaRepository.save(historico);
                            historicoEntradaSalida = historico;
                        }

                        fichaIdentificacion.setPermisoTemporal(false);
                        fichaIdentificacion.setTieneProceso(false);


                    } else {
                        Optional<HistoricoEntradaSalida> opt = this.historicoEntradaSalidaRepository.findByPermisoSalidaTokenIdentificador(
                                informe.getTokenIdentificador()
                        );
                        if (opt.isPresent()) {
                            HistoricoEntradaSalida historico = opt.get();
//                            historico.setCentroIngreso(jerarquiaRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getCentro().getTokenIdentificador(), false));
//                            historico.setRegistroActivo(false);
//                            historico.setFechaEntrada(new Date());
//                            this.historicoEntradaSalidaRepository.save(historico);
                            historicoEntradaSalida = historico;
                            if (historico.getFechaEntrada() != null) {
                                // Ya ingresó, así que debe marcarse como cerrado
                                fichaIdentificacion.setPermisoTemporal(false);
                                fichaIdentificacion.setTieneProceso(false);
                                informe.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_INACTIVO", false));
                            } else {
                                // sigue el permiso activo
                                fichaIdentificacion.setPermisoTemporal(true);
                                fichaIdentificacion.setTieneProceso(true);
                                informe.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_ACTIVO", false));
                            }

                            this.historicoEntradaSalidaRepository.save(historico);
                        }

//                        informe.setIsComplete(true);
                        informe.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_ACTIVO", false));
                        this.informePermisoSalidaRepository.save(informe);
                        fichaIdentificacion.setPermisoTemporal(true);
                    }

                } else if (fichaIdentificacionDTO.getTipoEntrada().getNemonico().equals("ENTRADA_FUGA")) {
                    EventoFuga fuga = this.eventoFugaRepository.findByTokenIdentificadorAndRemovido
                            (fichaIdentificacionDTO.getFuga().getTokenIdentificador(), false);
                    fuga.setEstadoEvento(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_SALIDA_INACTIVO", false));
                    this.eventoFugaRepository.save(fuga);

                    Optional<HistoricoEntradaSalida> opt = this.historicoEntradaSalidaRepository.findByEventoFugaTokenIdentificador(
                            fuga.getTokenIdentificador()
                    );
                    if (opt.isPresent()) {
                        HistoricoEntradaSalida historico = opt.get();
                        historico.setCentroIngreso(jerarquiaRepository.findByTokenIdentificadorAndRemovido(fichaIdentificacionDTO.getCentro().getTokenIdentificador(), false));
                        historico.setRegistroActivo(false);
                        historico.setFechaEntrada(new Date());
                        this.historicoEntradaSalidaRepository.save(historico);
                        historicoEntradaSalida = historico;
                        fichaIdentificacion.setTieneProceso(false);
                    }
                    fichaIdentificacion.setEstado(this.catalogoRepository.findByNemonicoAndRemovido("ESTADO_ADOLESCENTE_SENTENCIADO_PROCESADO", false));
                }
            } else {
                if (!fichaIdentificacionDTO.getEsEdicion() && !yaExiste) {
                    this.creacionDeLasCarpetaDeLaFichaDeIdentificacion(httpServletRequest, fichaIdentificacionDTO, empresa, usuarioSistema);
                }
            }
            this.fichaIdentificacionRepository.save(fichaIdentificacion);

            if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getCrearFichaIngreso()) && fichaIdentificacionDTO.getCrearFichaIngreso()) {

                if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getTipoEntrada())) {
                    if (fichaIdentificacionDTO.getTipoEntrada().getNemonico().equals("ENTRADA_INGRESO_NUEVO")
                            || fichaIdentificacionDTO.getTipoEntrada().getNemonico().equals("ENTRADA_FUGA")) {
                        FichaIngreso fichaIngreso = new FichaIngreso();
                        fichaIngreso.setJuez(fichaIdentificacionDTO.getJuez());
                        Jerarquia centro = jerarquiaRepository.findJerarquiaByTokenIdentificador(fichaIdentificacionDTO.getCentro().getTokenIdentificador());
                        fichaIngreso.setCentro(centro);

                        if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getFechaIngreso())) {
                            fichaIngreso.setFechaIngreso(fichaIdentificacionDTO.getFechaIngreso());
                        } else {
                            fichaIngreso.setFechaIngreso(new Date());
                        }

                        fichaIngreso.setJuzgado(fichaIdentificacionDTO.getJuzgado());
                        fichaIngreso.setActivo(true);
                        if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getNumeroFojas())) {
                            fichaIngreso.setNumeroFojas(Long.parseLong(fichaIdentificacionDTO.getNumeroFojas()));
                        } else {
                            fichaIngreso.setNumeroFojas(0L);
                        }

                        fichaIngreso.setIpCrea(ip);
                        fichaIngreso.setUsuarioSistemaCrea(usuarioLogin);
                        fichaIngreso.setEmpresa(empresa);
                        fichaIngreso.setFichaIdentificacion(fichaIdentificacion);
                        fichaIngreso.setObservaciones(fichaIdentificacionDTO.getObservacionIngreso());
                        fichaIngreso.setIngresaConHijo(fichaIdentificacionDTO.getIngresahijos());
                        this.fichaIngresoRepository.save(fichaIngreso);

                        if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getTokensDocumentosIngreso())) {
                            for (String token : fichaIdentificacionDTO.getTokensDocumentosIngreso()) {
                                DocumentosFichaIngreso dfi = new DocumentosFichaIngreso();
                                dfi.setFichaIngreso(fichaIngreso);
                                dfi.setTipoDocumento(this.catalogoRepository.findByTokenIdentificadorAndRemovido(token, false));
                                dfi.setIpCrea(ip);
                                dfi.setUsuarioSistemaCrea(usuarioLogin);

                                this.documentosFichaIngresoRepository.save(dfi);
                            }
                        }
                        this.fichaIngresoServiceImpl.crearCarpeta(fichaIngreso, fichaIdentificacion.getTokenIdentificador(), httpServletRequest, usuarioSistema);

                    } else if (fichaIdentificacionDTO.getTipoEntrada().getNemonico().equals("ENTRADA_TRASLADO") ||
                            fichaIdentificacionDTO.getTipoEntrada().getNemonico().equals("ENTRADA_SALIDA_TEMPORAL")) {

                        Jerarquia centro = jerarquiaRepository.findJerarquiaByTokenIdentificador(fichaIdentificacionDTO.getCentro().getTokenIdentificador());

                        if (!centro.getTokenIdentificador().equals(historicoEntradaSalida.getCentroSalida().getTokenIdentificador())) {
                            FichaIngreso fichaIngreso = new FichaIngreso();
                            fichaIngreso.setJuez(fichaIdentificacionDTO.getJuez());
                            fichaIngreso.setCentro(historicoEntradaSalida.getCentroSalida());
                            fichaIngreso.setCentro(centro);
                            if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getFechaIngreso())) {
                                fichaIngreso.setFechaIngreso(fichaIdentificacionDTO.getFechaIngreso());
                            } else {
                                fichaIngreso.setFechaIngreso(new Date());
                            }

                            fichaIngreso.setJuzgado(fichaIdentificacionDTO.getJuzgado());
                            fichaIngreso.setActivo(true);
                            if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getNumeroFojas())) {
                                fichaIngreso.setNumeroFojas(Long.parseLong(fichaIdentificacionDTO.getNumeroFojas()));
                            } else {
                                fichaIngreso.setNumeroFojas(0L);
                            }

                            fichaIngreso.setIpCrea(ip);
                            fichaIngreso.setUsuarioSistemaCrea(usuarioLogin);
                            fichaIngreso.setEmpresa(empresa);
                            fichaIngreso.setFichaIdentificacion(fichaIdentificacion);
                            fichaIngreso.setObservaciones(fichaIdentificacionDTO.getObservacionIngreso());
                            fichaIngreso.setIngresaConHijo(fichaIdentificacionDTO.getIngresahijos());
                            this.fichaIngresoRepository.save(fichaIngreso);

                            if (!ObjectUtils.isEmpty(fichaIdentificacionDTO.getTokensDocumentosIngreso())) {
                                for (String token : fichaIdentificacionDTO.getTokensDocumentosIngreso()) {
                                    DocumentosFichaIngreso dfi = new DocumentosFichaIngreso();
                                    dfi.setFichaIngreso(fichaIngreso);
                                    dfi.setTipoDocumento(this.catalogoRepository.findByTokenIdentificadorAndRemovido(token, false));
                                    dfi.setIpCrea(ip);
                                    dfi.setUsuarioSistemaCrea(usuarioLogin);

                                    this.documentosFichaIngresoRepository.save(dfi);
                                }
                            }
                            this.fichaIngresoServiceImpl.crearCarpeta(fichaIngreso, fichaIdentificacion.getTokenIdentificador(), httpServletRequest, usuarioSistema);

                        }
                    }

                } else {
                    if (!fichaIdentificacionDTO.getEsEdicion()) {

                        Jerarquia centro = jerarquiaRepository.findJerarquiaByTokenIdentificador(fichaIdentificacionDTO.getCentro().getTokenIdentificador());
                        String jerarquiaPadreNemonico = centro.getJerarquiaPadre().getNemonico();
                        boolean esCJDR = jerarquiaPadreNemonico.equals("CJDR");
                        boolean esSOA = jerarquiaPadreNemonico.equals("SOA");

                        boolean debeCrearFichaIngreso = (esCJDR && !yaExiste) || (esSOA && (!yaExiste || !centro.getTokenIdentificador().equals(jerarquiaAnterior.getTokenIdentificador())));

                        if (debeCrearFichaIngreso) {
                            FichaIngreso fichaIngreso = new FichaIngreso();
                            fichaIngreso.setCentro(centro);
                            fichaIngreso.setFechaIngreso(ObjectUtils.isEmpty(fichaIdentificacionDTO.getFechaIngreso()) ? new Date() : fichaIdentificacionDTO.getFechaIngreso());
                            fichaIngreso.setActivo(true);
                            fichaIngreso.setNumeroFojas(ObjectUtils.isEmpty(fichaIdentificacionDTO.getNumeroFojas()) ? 0L : Long.parseLong(fichaIdentificacionDTO.getNumeroFojas()));
                            fichaIngreso.setIpCrea(ip);
                            fichaIngreso.setUsuarioSistemaCrea(usuarioLogin);
                            fichaIngreso.setEmpresa(empresa);
                            fichaIngreso.setFichaIdentificacion(fichaIdentificacion);
                            fichaIngreso.setObservaciones(fichaIdentificacionDTO.getObservacionIngreso());
                            fichaIngreso.setIngresaConHijo(fichaIdentificacionDTO.getIngresahijos());

                            this.fichaIngresoRepository.save(fichaIngreso);

                            // Guardar documentos si existen
                            guardarDocumentosFichaIngreso(fichaIngreso, fichaIdentificacionDTO.getTokensDocumentosIngreso(), ip, usuarioLogin);

                            // Crear la carpeta asociada
                            this.fichaIngresoServiceImpl.crearCarpeta(fichaIngreso, fichaIdentificacion.getTokenIdentificador(), httpServletRequest, usuarioSistema);
                        }

                    }
                }

                if (!fichaIngresoList.isEmpty()) {
                    for (FichaIngreso ficha : fichaIngresoList) {
                        ficha.setActivo(false);
                        if (ObjectUtils.isEmpty(ficha.getFechaInactividad())) {
                            ficha.setFechaInactividad(new Date());
                        }
                        this.fichaIngresoRepository.save(ficha);
                    }
                }

            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Creacion de las carpetas internas para el manejo de los documentos del adolescente infractor
     *
     * @param httpServletRequest     request peticion.
     * @param fichaIdentificacionDTO objeto ficha identificacion dto.
     * @param empresa                Empresa
     * @return void
     */
    private void creacionDeLasCarpetaDeLaFichaDeIdentificacion(HttpServletRequest httpServletRequest,
                                                               FichaIdentificacionDTO fichaIdentificacionDTO, Empresa empresa,
                                                               UsuarioSistema usuarioSistema) {

        if (fichaIdentificacionDTO.getEsEdicion() != null && fichaIdentificacionDTO.getEsEdicion()) {
            this.logService.info("No se crean las carpetas de la ficha de identificacion debido a que es una edición");
            return;
        }

        FichaIdentificacion fichaIdentificacion = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                fichaIdentificacionDTO.getTokenIdentificador(), false
        );

        if (fichaIdentificacion == null) {
            this.logService.info("No se crean las carpetas de la ficha de identificacion debido a que no esta creada en la base de datos");
            return;
        }

        String nemonico = EtiquetaNemonico.CARPETA_GESTION_ADOLESCENTE;
        List<Catalogo> catalogoList = this.catalogoRepository.findByCatalogoPadreNemonicoAndEmpresaTokenIdentificadorAndRemovidoOrderByIdCatalogoDesc(
                nemonico,
                empresa.getTokenIdentificador(),
                false
        );

        if (catalogoList == null || catalogoList.isEmpty()) {
            this.logService.warn("Los tipos de catalogos: " + nemonico + " no estan definidos por ende no se puede crear las carpetas de la ficha de identificación");
            return;
        }

        Carpeta carpetaEmpresa = this.carpetaRepository.findByIdentificadorAlfrescoAndRemovido(empresa.getIdCarpetaAlfresco(), false);

        if (carpetaEmpresa == null) {
            this.logService.warn("La carpeta principal de la empresa no existe");
            return;
        }

        String nodoIdGestionDeAdolescente = empresa.getIdCarpetaAlfrescoGestionAdolescente();
        if (nodoIdGestionDeAdolescente == null || nodoIdGestionDeAdolescente.isEmpty()) {

            //Si no existe la carpeta principal de gestion del adolescente se crea
            CarpetaDTO carpetaDTO3 = new CarpetaDTO();
            carpetaDTO3.setNombreCliente("Gestion adolescente");
            carpetaDTO3.setDescripcion("Carpeta asociada a la gestión del adolescente");
            CarpetaDTO carpetaPadreDTO3 = new CarpetaDTO();
            carpetaPadreDTO3.setTokenIdentificador(carpetaEmpresa.getTokenIdentificador());
            carpetaDTO3.setCarpetaDTOPadre(carpetaPadreDTO3);

            RespuestaPorDefectoAuditoria<CarpetaDTO> df4 = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO3);
            if (!df4.isExito()) {
                this.logService.warn("No se pudo crear la carpeta principal gestión del adolescente debido a: "
                        + df4);
                return;
            }

            nodoIdGestionDeAdolescente = this.carpetaRepository.findByTokenIdentificadorAndRemovido(
                    df4.getData().getTokenIdentificador(), false
            ).getIdentificadorAlfresco();
        }

        Carpeta carpetaPadre = this.carpetaRepository.findByIdentificadorAlfrescoAndRemovido(nodoIdGestionDeAdolescente, false);
        if (carpetaPadre == null) {
            this.logService.warn("La carpeta principal de gestion del adolescente no esta registrada en la db");
            return;
        }

        //Creacion de la carpeta padre
        //String dni = fichaIdentificacionDTO.getDni();
        String nombres = fichaIdentificacionDTO.getNombres();
        String apellido1 = fichaIdentificacionDTO.getApellidoPaterno();
        String apellido2 = fichaIdentificacionDTO.getApellidoMaterno();

        String nombreCarpetaPadre = (nombres != null ? nombres.toLowerCase() + "_" : "") +
                (apellido1 != null ? apellido1.toLowerCase() + "_" : "") +
                (apellido2 != null ? apellido2.toLowerCase() : "");

        String nombresCompletos = fichaIdentificacionDTO.getNombres() + " " + fichaIdentificacionDTO.getNombrePadre() + " " +
                fichaIdentificacionDTO.getNombreMadre();
        CarpetaDTO carpetaDTO = new CarpetaDTO();
        carpetaDTO.setNombreCliente(nombreCarpetaPadre);
        carpetaDTO.setDescripcion("Carpeta asociada a la ficha principal de: "
                + nombresCompletos.trim());
        CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
        carpetaPadreDTO.setTokenIdentificador(carpetaPadre.getTokenIdentificador());
        carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

        RespuestaPorDefectoAuditoria<CarpetaDTO> df = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);
        if (!df.isExito()) {
            this.logService.warn("No se pudo crear la carpeta principal de la ficha de identificación debido a: "
                    + df);
            return;
        }

        carpetaDTO = df.getData();
        Carpeta carpetaPrincipal = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

        FichaIdentificacionCarpeta fichaIdentificacionCarpeta = new FichaIdentificacionCarpeta();
        fichaIdentificacionCarpeta.setCarpeta(carpetaPrincipal);
        fichaIdentificacionCarpeta.setFichaIdentificacion(fichaIdentificacion);
        fichaIdentificacionCarpeta.setUsuarioSistemaCrea(usuarioSistema);
        fichaIdentificacionCarpeta.setIpCrea(httpServletRequest.getRemoteAddr());
        this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpeta);

        //Creacion de las carpetas hijos
        for (Catalogo catalogo : catalogoList) {
            String nombreCarpetaHijo = this.utilsService.quitarCaracteresEspeciales(catalogo.getNombre()).replace(" ", "-");
            CarpetaDTO carpetaDTO2 = new CarpetaDTO();
            carpetaDTO2.setNombreCliente(nombreCarpetaHijo);
            carpetaDTO2.setDescripcion(catalogo.getDescripcion());
            CarpetaDTO carpetaPadreDTO2 = new CarpetaDTO();
            carpetaPadreDTO2.setTokenIdentificador(carpetaPrincipal.getTokenIdentificador());
            carpetaDTO2.setCarpetaDTOPadre(carpetaPadreDTO2);

            RespuestaPorDefectoAuditoria<CarpetaDTO> df2 = this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO2);

            if (df2.isExito()) {
                carpetaDTO2 = df2.getData();
                FichaIdentificacionCarpeta fichaIdentificacionCarpetaHijo = new FichaIdentificacionCarpeta();
                fichaIdentificacionCarpetaHijo.setCarpeta(
                        this.carpetaRepository.findByTokenIdentificadorAndRemovido(
                                carpetaDTO2.getTokenIdentificador(), false
                        )
                );
                fichaIdentificacionCarpetaHijo.setFichaIdentificacion(fichaIdentificacion);
                fichaIdentificacionCarpetaHijo.setTipoDeGestionDeAdolescente(catalogo);
                fichaIdentificacionCarpetaHijo.setIpCrea(httpServletRequest.getRemoteAddr());
                fichaIdentificacionCarpetaHijo.setUsuarioSistemaCrea(usuarioSistema);
                this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaHijo);
            }

        }

    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarFichaIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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

            FichaIdentificacionDTO fichaIdentificacionDTO = new Gson().fromJson(bodyString, FichaIdentificacionDTO.class);

            FichaIdentificacion fichaIdentificacion = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                    fichaIdentificacionDTO.getTokenIdentificador(), false
            );

            if (fichaIdentificacion == null) {
                df.setMensaje("La ficha de identificación no fue encontrada o ya fue eliminada anteriormente");
                return df;
            }

            Date fecha = new Date();
            fichaIdentificacion.setRemovido(true);
            fichaIdentificacion.setIpElimina(ip);
            fichaIdentificacion.setUsuarioSistemaElimina(usuarioSistemaLogin);
            fichaIdentificacion.setFechaEliminacion(fecha);

            this.fichaIdentificacionRepository.save(fichaIdentificacion);

            df.llenarRespuestaExitosa("Se ha eliminado con exito la ficha de identificación.", true);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> obtenerFichaIdentificacionPorTokenIdentificador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> df = new RespuestaPorDefectoAuditoria<>();

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

//            FichaIdentificacionDTO fichaIdentificacionDTO = new Gson().fromJson(bodyString, FichaIdentificacionDTO.class);
            String tokenIdentificador = new Gson().fromJson(bodyString, String.class);

            System.out.println("bodyString: " + bodyString);
            System.out.println("tokenIdentificador: " + tokenIdentificador);

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, Boolean.FALSE);
            if (fichaIdentificacion == null) {
                df.setMensaje("La ficha de identificación no existe o fue eliminada anteriormente");
                return df;
            }

            // SETEAR EL DTO QUE SE VA A DAR COMO RESPUESTA            
            FichaIdentificacionDTO fichaIdentificacionDTO = new FichaIdentificacionDTO();
            fichaIdentificacionDTO.setIdFichaIdentificacion(fichaIdentificacion.getIdFichaIdentificacion());
            fichaIdentificacionDTO.setTokenIdentificador(fichaIdentificacion.getTokenIdentificador());
            fichaIdentificacionDTO.setTokenIdentificadorEmpresa(fichaIdentificacion.getEmpresa().getTokenIdentificador());
            if (!ObjectUtils.isEmpty(fichaIdentificacion.getApellidoPaterno())) {
                fichaIdentificacionDTO.setApellidoPaterno(fichaIdentificacion.getApellidoPaterno());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getApellidoMaterno())) {
                fichaIdentificacionDTO.setApellidoMaterno(fichaIdentificacion.getApellidoMaterno());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getNombres())) {
                fichaIdentificacionDTO.setNombres(fichaIdentificacion.getNombres());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getDni())) {
                fichaIdentificacionDTO.setDni(fichaIdentificacion.getDni());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getFechaNacimiento())) {
                fichaIdentificacionDTO.setFechaNacimiento(fichaIdentificacion.getFechaNacimiento());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getEdad())) {
                fichaIdentificacionDTO.setEdad(fichaIdentificacion.getEdad());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getFechaIngreso())) {
                fichaIdentificacionDTO.setFechaIngreso(fichaIdentificacion.getFechaIngreso());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getAlias())) {
                fichaIdentificacionDTO.setAlias(fichaIdentificacion.getAlias());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getNacionalidad())) {
                fichaIdentificacionDTO.setNacionalidad(fichaIdentificacion.getNacionalidad());
            }
//            fichaIdentificacionDTO.setSinDni(
//                    fichaIdentificacion.getSinDni() != null ? fichaIdentificacion.getSinDni() : null
//            );
//            fichaIdentificacionDTO.setDni(
//                    fichaIdentificacion.getDni() != null ? fichaIdentificacion.getDni() : null
//            );
            if (!ObjectUtils.isEmpty(fichaIdentificacion.getEstadoCivil())) {
                fichaIdentificacionDTO.setTokenIdentificadorEstadoCivil(fichaIdentificacion.getEstadoCivil().getTokenIdentificador());
            }
            fichaIdentificacionDTO.setNumeroHijos(fichaIdentificacion.getNumeroHijos());
            if (fichaIdentificacion.getOrigenEtnico() != null) {
                fichaIdentificacionDTO.setTokenIdentificadorOrigenEtnico(fichaIdentificacion.getOrigenEtnico().getTokenIdentificador());
            }
            if (!ObjectUtils.isEmpty(fichaIdentificacion.getImpedimentoDiscapacidad())) {
                fichaIdentificacionDTO.setImpedimentoDiscapacidad(fichaIdentificacion.getImpedimentoDiscapacidad());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getNombrePadre())) {
                fichaIdentificacionDTO.setNombrePadre(fichaIdentificacion.getNombrePadre());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getNombreMadre())) {
                fichaIdentificacionDTO.setNombreMadre(fichaIdentificacion.getNombreMadre());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getCentroIngreso())) {
                fichaIdentificacionDTO.setCentroIngreso(fichaIdentificacion.getCentroIngreso().getNombre());
            }
//            fichaIdentificacionDTO.setDomicilioActual(fichaIdentificacion.getDomicilioActual());

            /*
            if(fichaIdentificacion.getDepartamento()!=null) {
                fichaIdentificacionDTO.setTokenIdentificadorDepartamento(fichaIdentificacion.getDepartamento().getTokenIdentificador());
            }
            if(fichaIdentificacion.getProvincia()!=null) {
                fichaIdentificacionDTO.setTokenIdentificadorProvincia(fichaIdentificacion.getProvincia().getTokenIdentificador());
            }
            if(fichaIdentificacion.getDistrito()!=null) {
                fichaIdentificacionDTO.setTokenIdentificadorDistrito(fichaIdentificacion.getDistrito().getTokenIdentificador());
            }
            */

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getLugarNacimiento())) {
                fichaIdentificacionDTO.setLugarNacimiento(fichaIdentificacion.getLugarNacimiento());
            }

            if (fichaIdentificacion.getPaisNacimiento() != null) {
                fichaIdentificacionDTO.setPaisNacimiento(fichaIdentificacion.getPaisNacimiento().getNemonico());
            }
            if (fichaIdentificacion.getGrupoVulnerable() != null) {
                fichaIdentificacionDTO.setTokenIdentificadorGrupoVulnerable(fichaIdentificacion.getGrupoVulnerable().getTokenIdentificador());
            }
            if (fichaIdentificacion.getCodigoUbigeoNacimiento() != null) {
                fichaIdentificacionDTO.setUbigeoNacimiento(fichaIdentificacion.getCodigoUbigeoNacimiento());
            }
            if (fichaIdentificacion.getCodigoUbigeoDireccion() != null) {
                fichaIdentificacionDTO.setUbigeoUbicacion(fichaIdentificacion.getCodigoUbigeoDireccion());
            }
            if (fichaIdentificacion.getNumeroIdentificacion() != null) {
                fichaIdentificacionDTO.setNumeroDocumento(fichaIdentificacion.getNumeroIdentificacion());
            }
            if (fichaIdentificacion.getDireccion() != null) {
                fichaIdentificacionDTO.setDireccion(fichaIdentificacion.getDireccion());
            }

            if (fichaIdentificacion.getTipoSexo() != null && fichaIdentificacion.getTipoSexo().getTokenIdentificador() != null) {
                fichaIdentificacionDTO.setTipoSexo(fichaIdentificacion.getTipoSexo().getTokenIdentificador());
                fichaIdentificacionDTO.setNombreSexo(fichaIdentificacion.getTipoSexo().getNombre());
            }
            if (fichaIdentificacion.getGenero() != null && fichaIdentificacion.getGenero().getTokenIdentificador() != null) {
                fichaIdentificacionDTO.setTipoGenero(fichaIdentificacion.getGenero().getTokenIdentificador());
            }
            if (fichaIdentificacion.getTipoIdentificacion() != null && fichaIdentificacion.getTipoIdentificacion().getTokenIdentificador() != null) {
                fichaIdentificacionDTO.setTipoDocumento(fichaIdentificacion.getTipoIdentificacion().getTokenIdentificador());
                fichaIdentificacionDTO.setNombreTipoDocumento(fichaIdentificacion.getTipoIdentificacion().getNombre());
            }
            if (fichaIdentificacion.getViveConParentesco() != null && fichaIdentificacion.getViveConParentesco().getTokenIdentificador() != null) {
                fichaIdentificacionDTO.setTipoViveCon(fichaIdentificacion.getViveConParentesco().getTokenIdentificador());
            }
            if (fichaIdentificacion.getTipoOcupacion() != null && fichaIdentificacion.getTipoOcupacion().getTokenIdentificador() != null) {
                fichaIdentificacionDTO.setOcupacion(fichaIdentificacion.getTipoOcupacion().getTokenIdentificador());
            }


            if (fichaIdentificacion.getUbigeoDireccion() != null) {
                fichaIdentificacionDTO.setUbigeoUbicacion(fichaIdentificacion.getCodigoUbigeoDireccion());
            }

            fichaIdentificacionDTO.setCantIngresos(this.fichaIngresoRepository.
                    countByFichaIdentificacionTokenIdentificadorAndRemovido(fichaIdentificacion.getTokenIdentificador(), false).intValue());

            fichaIdentificacionDTO.setCantExpedientes(this.expedienteMatrizRepository.
                    countByFichaIdentificacionTokenIdentificadorAndRemovido(fichaIdentificacion.getTokenIdentificador(), false).intValue());

            fichaIdentificacionDTO.setCantPertenencias(this.pertenenciaRepository.
                    countByFichaIdentificacionTokenIdentificadorAndRemovido(fichaIdentificacion.getTokenIdentificador(), false).intValue());

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getIngresoConHijo())) {
                fichaIdentificacionDTO.setIngresahijos(fichaIdentificacion.getIngresoConHijo());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getOtroOrigenEtnico())) {
                fichaIdentificacionDTO.setOtroOrigenEtnico(fichaIdentificacion.getOtroOrigenEtnico());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getInstancia())) {
                fichaIdentificacionDTO.setInstancia(catalogoToDTO(fichaIdentificacion.getInstancia()));
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getCorteJusticia())) {
                fichaIdentificacionDTO.setCorteJusticia(catalogoToDTO(fichaIdentificacion.getCorteJusticia()));
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getEspecialidad())) {
                fichaIdentificacionDTO.setEspecialidad(catalogoToDTO(fichaIdentificacion.getEspecialidad()));
            }

            if (fichaIdentificacion.getOrganoJurisdiccional() != null) {
                fichaIdentificacionDTO.setOrganoJurisdiccional(fichaIdentificacion.getOrganoJurisdiccional());
            }

            if (fichaIdentificacion.getEmail() != null) {
                fichaIdentificacionDTO.setEmail(fichaIdentificacion.getEmail());
            }

            if (fichaIdentificacion.getSecretario() != null) {
                fichaIdentificacionDTO.setSecretario(fichaIdentificacion.getSecretario());
            }

            if (fichaIdentificacion.getJuez() != null) {
                fichaIdentificacionDTO.setJuez(fichaIdentificacion.getJuez());
            }

            if (fichaIdentificacion.getObservacionIngreso() != null) {
                fichaIdentificacionDTO.setObservacionIngreso(fichaIdentificacion.getObservacionIngreso());
            }

            if (fichaIdentificacion.getEstado() != null) {
                fichaIdentificacionDTO.setEstadoAdolescente(this.catalogoToDTO(fichaIdentificacion.getEstado()));
            }

            if (fichaIdentificacion.getTipoEntrada() != null) {
                fichaIdentificacionDTO.setTipoEntrada(this.catalogoToDTO(fichaIdentificacion.getTipoEntrada()));
            }

            if (fichaIdentificacion.getNumeroFojas() != null) {
                fichaIdentificacionDTO.setNumeroFojas(fichaIdentificacion.getNumeroFojas().toString());
            }

            if (fichaIdentificacion.getCentroIngreso() != null) {
                JerarquiaDTO jerarquiaDTO = new JerarquiaDTO();
                jerarquiaDTO.setTokenIdentificador(fichaIdentificacion.getCentroIngreso().getTokenIdentificador());
                jerarquiaDTO.setNombre(fichaIdentificacion.getCentroIngreso().getNombre());
                fichaIdentificacionDTO.setCentro(jerarquiaDTO);
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getModalidadEstudio())) {
                fichaIdentificacionDTO.setModalidadEstudio(fichaIdentificacion.getModalidadEstudio());

                // Mapear el nivel correspondiente según la modalidad
                switch (fichaIdentificacion.getModalidadEstudio()) {
                    case "MODALIDAD_ESTUDIO_EBR":
                        if (fichaIdentificacion.getNivelEBR() != null) {
                            fichaIdentificacionDTO.setNivelEBR(fichaIdentificacion.getNivelEBR().getTokenIdentificador());
                        }
                        break;
                    case "MODALIDAD_ESTUDIO_SUPERIOR":
                        if (fichaIdentificacion.getNivelSuperior() != null) {
                            fichaIdentificacionDTO.setNivelSuperior(fichaIdentificacion.getNivelSuperior().getTokenIdentificador());
                        }
                        break;
                    case "MODALIDAD_ESTUDIO_EBA":
                        if (fichaIdentificacion.getNivelEBA() != null) {
                            fichaIdentificacionDTO.setNivelEBA(fichaIdentificacion.getNivelEBA().getTokenIdentificador());
                        }
                        break;
                }
            }

            df.llenarRespuestaExitosa("Se obtuvo con éxito la ficha de identificación con DNI:-" +
                    fichaIdentificacionDTO.getNumeroDocumento() + " Nombres:" +
                    fichaIdentificacionDTO.getApellidoPaterno() + " " +
                    fichaIdentificacionDTO.getApellidoMaterno() + " " +
                    fichaIdentificacionDTO.getNombres(), fichaIdentificacionDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> obtenerFichaIdentificacionPorId(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> df = new RespuestaPorDefectoAuditoria<>();

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

//            FichaIdentificacionDTO fichaIdentificacionDTO = new Gson().fromJson(bodyString, FichaIdentificacionDTO.class);
            Long idFicha = new Gson().fromJson(bodyString, Long.class);

            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            FichaIdentificacion fichaIdentificacion = fichaIdentificacionRepository.findByIdFichaIdentificacion(idFicha);
            if (fichaIdentificacion == null) {
                df.setMensaje("La ficha de identificación no existe o fue eliminada anteriormente");
                return df;
            }

            // SETEAR EL DTO QUE SE VA A DAR COMO RESPUESTA
            FichaIdentificacionDTO fichaIdentificacionDTO = new FichaIdentificacionDTO();
            fichaIdentificacionDTO.setTokenIdentificador(fichaIdentificacion.getTokenIdentificador());
            fichaIdentificacionDTO.setTokenIdentificadorEmpresa(fichaIdentificacion.getEmpresa().getTokenIdentificador());

            safeAssign(fichaIdentificacionDTO::setApellidoPaterno, fichaIdentificacion.getApellidoPaterno());
            safeAssign(fichaIdentificacionDTO::setApellidoMaterno, fichaIdentificacion.getApellidoMaterno());
            safeAssign(fichaIdentificacionDTO::setNombres, fichaIdentificacion.getNombres());
            safeAssign(fichaIdentificacionDTO::setFechaNacimiento, fichaIdentificacion.getFechaNacimiento());
            safeAssign(fichaIdentificacionDTO::setDni, fichaIdentificacion.getDni());
            safeAssign(fichaIdentificacionDTO::setEdad, fichaIdentificacion.getEdad());
            safeAssign(fichaIdentificacionDTO::setAlias, fichaIdentificacion.getAlias());
            safeAssign(fichaIdentificacionDTO::setNacionalidad, fichaIdentificacion.getNacionalidad());
            safeAssign(fichaIdentificacionDTO::setTokenIdentificadorEstadoCivil, fichaIdentificacion.getEstadoCivil() != null ? fichaIdentificacion.getEstadoCivil().getTokenIdentificador() : null);
            safeAssign(fichaIdentificacionDTO::setImpedimentoDiscapacidad, fichaIdentificacion.getImpedimentoDiscapacidad());
            safeAssign(fichaIdentificacionDTO::setNombrePadre, fichaIdentificacion.getNombrePadre());
            safeAssign(fichaIdentificacionDTO::setNombreMadre, fichaIdentificacion.getNombreMadre());
            safeAssign(fichaIdentificacionDTO::setCentroIngreso, fichaIdentificacion.getCentroIngreso() != null ? fichaIdentificacion.getCentroIngreso().getNombre() : null);
            safeAssign(fichaIdentificacionDTO::setPaisNacimiento, fichaIdentificacion.getPaisNacimiento() != null ? fichaIdentificacion.getPaisNacimiento().getNemonico() : null);
            safeAssign(fichaIdentificacionDTO::setTokenIdentificadorGrupoVulnerable, fichaIdentificacion.getGrupoVulnerable() != null ? fichaIdentificacion.getGrupoVulnerable().getTokenIdentificador() : null);
            safeAssign(fichaIdentificacionDTO::setUbigeoNacimiento, fichaIdentificacion.getCodigoUbigeoNacimiento());
            safeAssign(fichaIdentificacionDTO::setUbigeoUbicacion, fichaIdentificacion.getCodigoUbigeoDireccion());
            safeAssign(fichaIdentificacionDTO::setNumeroDocumento, fichaIdentificacion.getNumeroIdentificacion());
            safeAssign(fichaIdentificacionDTO::setDireccion, fichaIdentificacion.getDireccion());
            safeAssign(fichaIdentificacionDTO::setTipoSexo, fichaIdentificacion.getTipoSexo() != null ? fichaIdentificacion.getTipoSexo().getTokenIdentificador() : null);
            safeAssign(fichaIdentificacionDTO::setTipoGenero, fichaIdentificacion.getGenero() != null ? fichaIdentificacion.getGenero().getTokenIdentificador() : null);
            safeAssign(fichaIdentificacionDTO::setTipoDocumento, fichaIdentificacion.getTipoIdentificacion() != null ? fichaIdentificacion.getTipoIdentificacion().getTokenIdentificador() : null);
            safeAssign(fichaIdentificacionDTO::setTipoViveCon, fichaIdentificacion.getViveConParentesco() != null ? fichaIdentificacion.getViveConParentesco().getTokenIdentificador() : null);
            safeAssign(fichaIdentificacionDTO::setOcupacion, fichaIdentificacion.getTipoOcupacion() != null ? fichaIdentificacion.getTipoOcupacion().getTokenIdentificador() : null);
            safeAssign(fichaIdentificacionDTO::setNivelEBA, fichaIdentificacion.getNivelEBA() != null ? fichaIdentificacion.getNivelEBA().getNombre() : null);
            safeAssign(fichaIdentificacionDTO::setNivelEBR, fichaIdentificacion.getNivelEBR() != null ? fichaIdentificacion.getNivelEBR().getNombre() : null);
            safeAssign(fichaIdentificacionDTO::setNivelSuperior, fichaIdentificacion.getNivelSuperior() != null ? fichaIdentificacion.getNivelSuperior().getNombre() : null);

            fichaIdentificacionDTO.setCantIngresos(this.fichaIngresoRepository.
                    countByFichaIdentificacionTokenIdentificadorAndRemovido(fichaIdentificacion.getTokenIdentificador(), false).intValue());

            fichaIdentificacionDTO.setCantExpedientes(this.expedienteMatrizRepository.
                    countByFichaIdentificacionTokenIdentificadorAndRemovido(fichaIdentificacion.getTokenIdentificador(), false).intValue());

            if (Objects.equals(fichaIdentificacionDTO.getPaisNacimiento(), EtiquetaNemonico.PAIS_PERU)) {
                Localidad localidad = this.localidadRepository.findByCodigoUbigeoAndRemovido(fichaIdentificacion.getCodigoUbigeoNacimiento(), false);
                if (localidad != null) fichaIdentificacionDTO.setLugarNacimiento(localidad.getNombre());
            } else {
                if (fichaIdentificacion.getPaisNacimiento() != null)
                    fichaIdentificacionDTO.setLugarNacimiento(fichaIdentificacion.getPaisNacimiento().getNombre());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getModalidadEstudio())) {
                fichaIdentificacionDTO.setModalidadEstudio(fichaIdentificacion.getModalidadEstudio());

                // Mapear el nivel correspondiente según la modalidad
                switch (fichaIdentificacion.getModalidadEstudio()) {
                    case "MODALIDAD_ESTUDIO_EBR":
                        if (fichaIdentificacion.getNivelEBR() != null) {
                            fichaIdentificacionDTO.setNivelEBR(fichaIdentificacion.getNivelEBR().getTokenIdentificador());
                        }
                        break;
                    case "MODALIDAD_ESTUDIO_SUPERIOR":
                        if (fichaIdentificacion.getNivelSuperior() != null) {
                            fichaIdentificacionDTO.setNivelSuperior(fichaIdentificacion.getNivelSuperior().getTokenIdentificador());
                        }
                        break;
                    case "MODALIDAD_ESTUDIO_EBA":
                        if (fichaIdentificacion.getNivelEBA() != null) {
                            fichaIdentificacionDTO.setNivelEBA(fichaIdentificacion.getNivelEBA().getTokenIdentificador());
                        }
                        break;
                }
            }

            df.llenarRespuestaExitosa("Se obtuvo con éxito la ficha de identificación: IDENTIFICACIÓN-" +
                    fichaIdentificacionDTO.getTokenIdentificador() + "-" +
                    fichaIdentificacionDTO.getApellidoPaterno() + " " +
                    fichaIdentificacionDTO.getApellidoMaterno() + " " +
                    fichaIdentificacionDTO.getNombres(), fichaIdentificacionDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<FichaIdentificacionDTO>> obtenerNombresFichas(HttpServletRequest httpServletRequest, String tokenCentro) {

        RespuestaPorDefectoAuditoria<List<FichaIdentificacionDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            Empresa empresa = df2.getData().getEmpresa();

            List<FichaIdentificacion> listaFichas;

            if (tokenCentro != null)
                listaFichas = fichaIdentificacionRepository.findByEmpresaIdEmpresaAndRemovidoAndCentroIngreso_TokenIdentificador(empresa.getIdEmpresa(), false, tokenCentro);
            else
                listaFichas = fichaIdentificacionRepository.findByEmpresaIdEmpresaAndRemovido(empresa.getIdEmpresa(), false);

            List<FichaIdentificacionDTO> fichaDTOList = new ArrayList<>();
            for (FichaIdentificacion ficha : listaFichas) {
                FichaIdentificacionDTO fichaDTO = new FichaIdentificacionDTO();
                fichaDTO.setIdFichaIdentificacion(ficha.getIdFichaIdentificacion());
                fichaDTO.setNombres(ficha.getNombres());
                fichaDTO.setApellidoPaterno(ficha.getApellidoPaterno());
                fichaDTO.setApellidoMaterno(ficha.getApellidoMaterno());
                fichaDTO.setTokenIdentificador(ficha.getTokenIdentificador());
                fichaDTO.setDni(ficha.getDni());
                fichaDTO.setNumeroIdentificacion(ficha.getNumeroIdentificacion());
                fichaDTO.setPermisoTemporal(ficha.getPermisoTemporal());
                fichaDTO.setTieneProceso(ficha.getTieneProceso());
                fichaDTO.setEmail(ficha.getEmail());
                if (!ObjectUtils.isEmpty(ficha.getCentroIngreso())) {
                    fichaDTO.setCentroIngreso(ficha.getCentroIngreso().getNombre());
                }

                fichaDTOList.add(fichaDTO);
            }

            fichaDTOList.sort(
                    Comparator.comparing(f -> ((FichaIdentificacionDTO) f).getApellidoPaterno().toLowerCase())
                            .thenComparing(f -> ((FichaIdentificacionDTO) f).getApellidoMaterno().toLowerCase())
                            .thenComparing(f -> ((FichaIdentificacionDTO) f).getNombres().toLowerCase())
            );


            respuesta.llenarRespuestaExitosa("Fichas", fichaDTOList);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> obtenerFichaIdentificacionPorNumeroDocumento(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<FichaIdentificacionDTO> df = new RespuestaPorDefectoAuditoria<>();

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

//            FichaIdentificacionDTO fichaIdentificacionDTO = new Gson().fromJson(bodyString, FichaIdentificacionDTO.class);
            String numeroIndentificacion = new Gson().fromJson(bodyString, String.class);


            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            List<FichaIdentificacion> fichasCreadas = this.fichaIdentificacionRepository.
                    findByNumeroIdentificacionAndRemovidoOrderByIdFichaIdentificacionDesc(numeroIndentificacion, false
                    );

            if (fichasCreadas.isEmpty()) {
                df.setMensaje("No existe ninguna ficha de identificación con ese número de documento");
                return df;
            }

            FichaIdentificacion fichaIdentificacion = fichasCreadas.get(fichasCreadas.size() - 1);
            if (fichaIdentificacion == null) {
                df.setMensaje("La ficha de identificación no existe o fue eliminada anteriormente");
                return df;
            }

            // SETEAR EL DTO QUE SE VA A DAR COMO RESPUESTA
            FichaIdentificacionDTO fichaIdentificacionDTO = new FichaIdentificacionDTO();
            fichaIdentificacionDTO.setTokenIdentificador(fichaIdentificacion.getTokenIdentificador());
            fichaIdentificacionDTO.setTokenIdentificadorEmpresa(fichaIdentificacion.getEmpresa().getTokenIdentificador());
            if (!ObjectUtils.isEmpty(fichaIdentificacion.getApellidoPaterno())) {
                fichaIdentificacionDTO.setApellidoPaterno(fichaIdentificacion.getApellidoPaterno());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getApellidoMaterno())) {
                fichaIdentificacionDTO.setApellidoMaterno(fichaIdentificacion.getApellidoMaterno());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getNombres())) {
                fichaIdentificacionDTO.setNombres(fichaIdentificacion.getNombres());
            }
            if (!ObjectUtils.isEmpty(fichaIdentificacion.getFechaNacimiento())) {
                fichaIdentificacionDTO.setFechaNacimiento(fichaIdentificacion.getFechaNacimiento());
            }

//            fichaIdentificacionDTO.setSinDni(
//                    fichaIdentificacion.getSinDni() != null ? fichaIdentificacion.getSinDni() : null
//            );
//            fichaIdentificacionDTO.setDni(
//                    fichaIdentificacion.getDni() != null ? fichaIdentificacion.getDni() : null
//            );

//            fichaIdentificacionDTO.setDomicilioActual(fichaIdentificacion.getDomicilioActual());

            /*
            if(fichaIdentificacion.getDepartamento()!=null) {
                fichaIdentificacionDTO.setTokenIdentificadorDepartamento(fichaIdentificacion.getDepartamento().getTokenIdentificador());
            }
            if(fichaIdentificacion.getProvincia()!=null) {
                fichaIdentificacionDTO.setTokenIdentificadorProvincia(fichaIdentificacion.getProvincia().getTokenIdentificador());
            }
            if(fichaIdentificacion.getDistrito()!=null) {
                fichaIdentificacionDTO.setTokenIdentificadorDistrito(fichaIdentificacion.getDistrito().getTokenIdentificador());
            }
            */


            if (fichaIdentificacion.getNumeroIdentificacion() != null) {
                fichaIdentificacionDTO.setNumeroDocumento(fichaIdentificacion.getNumeroIdentificacion());
            }
            if (fichaIdentificacion.getTipoSexo() != null && fichaIdentificacion.getTipoSexo().getTokenIdentificador() != null) {
                fichaIdentificacionDTO.setTipoSexo(fichaIdentificacion.getTipoSexo().getTokenIdentificador());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getIngresoConHijo())) {
                fichaIdentificacionDTO.setIngresahijos(fichaIdentificacion.getIngresoConHijo());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getAlias())) {
                fichaIdentificacionDTO.setAlias(fichaIdentificacion.getAlias());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getNacionalidad())) {
                fichaIdentificacionDTO.setNacionalidad(fichaIdentificacion.getNacionalidad());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getEstadoCivil())) {
                fichaIdentificacionDTO.setTokenIdentificadorEstadoCivil(fichaIdentificacion.getEstadoCivil().getTokenIdentificador());
            }

            if (fichaIdentificacion.getEstado() != null) {
                fichaIdentificacionDTO.setEstadoAdolescente(this.catalogoToDTO(fichaIdentificacion.getEstado()));
            }

            if (fichaIdentificacion.getCentroIngreso() != null) {
                fichaIdentificacionDTO.setCentroIngreso(fichaIdentificacion.getCentroIngreso().getNombre());
            }

            fichaIdentificacionDTO.setNumeroHijos(fichaIdentificacion.getNumeroHijos());
            if (fichaIdentificacion.getOrigenEtnico() != null) {
                fichaIdentificacionDTO.setTokenIdentificadorOrigenEtnico(fichaIdentificacion.getOrigenEtnico().getTokenIdentificador());
            }
            if (!ObjectUtils.isEmpty(fichaIdentificacion.getImpedimentoDiscapacidad())) {
                fichaIdentificacionDTO.setImpedimentoDiscapacidad(fichaIdentificacion.getImpedimentoDiscapacidad());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getNombrePadre())) {
                fichaIdentificacionDTO.setNombrePadre(fichaIdentificacion.getNombrePadre());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getNombreMadre())) {
                fichaIdentificacionDTO.setNombreMadre(fichaIdentificacion.getNombreMadre());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getCentroIngreso())) {
                fichaIdentificacionDTO.setCentroIngreso(fichaIdentificacion.getCentroIngreso().getNombre());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getLugarNacimiento())) {
                fichaIdentificacionDTO.setLugarNacimiento(fichaIdentificacion.getLugarNacimiento());
            }

            if (fichaIdentificacion.getPaisNacimiento() != null) {
                fichaIdentificacionDTO.setPaisNacimiento(fichaIdentificacion.getPaisNacimiento().getNemonico());
            }
            if (fichaIdentificacion.getGrupoVulnerable() != null) {
                fichaIdentificacionDTO.setTokenIdentificadorGrupoVulnerable(fichaIdentificacion.getGrupoVulnerable().getTokenIdentificador());
            }
            if (fichaIdentificacion.getCodigoUbigeoNacimiento() != null) {
                fichaIdentificacionDTO.setUbigeoNacimiento(fichaIdentificacion.getCodigoUbigeoNacimiento());
            }
            if (fichaIdentificacion.getCodigoUbigeoDireccion() != null) {
                fichaIdentificacionDTO.setUbigeoUbicacion(fichaIdentificacion.getCodigoUbigeoDireccion());
            }
            if (fichaIdentificacion.getNumeroIdentificacion() != null) {
                fichaIdentificacionDTO.setNumeroDocumento(fichaIdentificacion.getNumeroIdentificacion());
            }
            if (fichaIdentificacion.getDireccion() != null) {
                fichaIdentificacionDTO.setDireccion(fichaIdentificacion.getDireccion());
            }

            if (fichaIdentificacion.getTipoSexo() != null && fichaIdentificacion.getTipoSexo().getTokenIdentificador() != null) {
                fichaIdentificacionDTO.setTipoSexo(fichaIdentificacion.getTipoSexo().getTokenIdentificador());
            }
            if (fichaIdentificacion.getGenero() != null && fichaIdentificacion.getGenero().getTokenIdentificador() != null) {
                fichaIdentificacionDTO.setTipoGenero(fichaIdentificacion.getGenero().getTokenIdentificador());
            }
            if (fichaIdentificacion.getTipoIdentificacion() != null && fichaIdentificacion.getTipoIdentificacion().getTokenIdentificador() != null) {
                fichaIdentificacionDTO.setTipoDocumento(fichaIdentificacion.getTipoIdentificacion().getTokenIdentificador());
            }
            if (fichaIdentificacion.getViveConParentesco() != null && fichaIdentificacion.getViveConParentesco().getTokenIdentificador() != null) {
                fichaIdentificacionDTO.setTipoViveCon(fichaIdentificacion.getViveConParentesco().getTokenIdentificador());
            }
            if (fichaIdentificacion.getTipoOcupacion() != null && fichaIdentificacion.getTipoOcupacion().getTokenIdentificador() != null) {
                fichaIdentificacionDTO.setOcupacion(fichaIdentificacion.getTipoOcupacion().getTokenIdentificador());
            }


            if (fichaIdentificacion.getUbigeoDireccion() != null) {
                fichaIdentificacionDTO.setUbigeoUbicacion(fichaIdentificacion.getCodigoUbigeoDireccion());
            }

            fichaIdentificacionDTO.setCantIngresos(this.fichaIngresoRepository.
                    countByFichaIdentificacionTokenIdentificadorAndRemovido(fichaIdentificacion.getTokenIdentificador(), false).intValue());

            fichaIdentificacionDTO.setCantExpedientes(this.expedienteMatrizRepository.
                    countByFichaIdentificacionTokenIdentificadorAndRemovido(fichaIdentificacion.getTokenIdentificador(), false).intValue());

            fichaIdentificacionDTO.setCantPertenencias(this.pertenenciaRepository.
                    countByFichaIdentificacionTokenIdentificadorAndRemovido(fichaIdentificacion.getTokenIdentificador(), false).intValue());

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getIngresoConHijo())) {
                fichaIdentificacionDTO.setIngresahijos(fichaIdentificacion.getIngresoConHijo());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getOtroOrigenEtnico())) {
                fichaIdentificacionDTO.setOtroOrigenEtnico(fichaIdentificacion.getOtroOrigenEtnico());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getInstancia())) {
                fichaIdentificacionDTO.setInstancia(catalogoToDTO(fichaIdentificacion.getInstancia()));
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getCorteJusticia())) {
                fichaIdentificacionDTO.setCorteJusticia(catalogoToDTO(fichaIdentificacion.getCorteJusticia()));
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getEspecialidad())) {
                fichaIdentificacionDTO.setEspecialidad(catalogoToDTO(fichaIdentificacion.getEspecialidad()));
            }

            if (fichaIdentificacion.getOrganoJurisdiccional() != null) {
                fichaIdentificacionDTO.setOrganoJurisdiccional(fichaIdentificacion.getOrganoJurisdiccional());
            }

            if (fichaIdentificacion.getEmail() != null) {
                fichaIdentificacionDTO.setEmail(fichaIdentificacion.getEmail());
            }

            if (fichaIdentificacion.getSecretario() != null) {
                fichaIdentificacionDTO.setSecretario(fichaIdentificacion.getSecretario());
            }

            if (fichaIdentificacion.getJuez() != null) {
                fichaIdentificacionDTO.setJuez(fichaIdentificacion.getJuez());
            }

            if (fichaIdentificacion.getObservacionIngreso() != null) {
                fichaIdentificacionDTO.setObservacionIngreso(fichaIdentificacion.getObservacionIngreso());
            }

            if (!ObjectUtils.isEmpty(fichaIdentificacion.getModalidadEstudio())) {
                fichaIdentificacionDTO.setModalidadEstudio(fichaIdentificacion.getModalidadEstudio());

                // Mapear el nivel correspondiente según la modalidad
                switch (fichaIdentificacion.getModalidadEstudio()) {
                    case "MODALIDAD_ESTUDIO_EBR":
                        if (fichaIdentificacion.getNivelEBR() != null) {
                            fichaIdentificacionDTO.setNivelEBR(fichaIdentificacion.getNivelEBR().getTokenIdentificador());
                        }
                        break;
                    case "MODALIDAD_ESTUDIO_SUPERIOR":
                        if (fichaIdentificacion.getNivelSuperior() != null) {
                            fichaIdentificacionDTO.setNivelSuperior(fichaIdentificacion.getNivelSuperior().getTokenIdentificador());
                        }
                        break;
                    case "MODALIDAD_ESTUDIO_EBA":
                        if (fichaIdentificacion.getNivelEBA() != null) {
                            fichaIdentificacionDTO.setNivelEBA(fichaIdentificacion.getNivelEBA().getTokenIdentificador());
                        }
                        break;
                }
            }

            List<HistoricoEntradaSalida> historico = this.historicoEntradaSalidaRepository.findHistoricoSalidasByNumeroIdentificacion(numeroIndentificacion);
            HistoricoEntradaSalida historicoEntradaSalida = null;
            if (!historico.isEmpty()) {
                historicoEntradaSalida = historico.get(historico.size() - 1);
                fichaIdentificacionDTO.setRegistroSalidaDTO(entidadADto(historicoEntradaSalida.getRegistroSalida()));
            }

            df.llenarRespuestaExitosa("Se obtuvo con éxito la ficha de identificación: NOMBRE-" +
                    fichaIdentificacionDTO.getApellidoPaterno() + " " +
                    fichaIdentificacionDTO.getApellidoMaterno() + " " +
                    fichaIdentificacionDTO.getNombres(), fichaIdentificacionDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<EdadEstadisticaDTO>> obtenerEstadisticasEdades(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<EdadEstadisticaDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {

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

            List<Object[]> resultados = fichaIdentificacionRepository.obtenerEstadisticasPorEdad(reportesDTO.getNemonicoTipoSexo(),
                    reportesDTO.getTokenIdentificadorCentro(), reportesDTO.getNemonicoCentro());

            List<EdadEstadisticaDTO> estadisticas = resultados.stream()
                    .map(obj -> new EdadEstadisticaDTO(
                            ((Number) obj[0]).intValue(),  // Edad calculada
                            ((Number) obj[1]).intValue()   // Cantidad de personas con esa edad
                    ))
                    .collect(Collectors.toList());

            respuesta.llenarRespuestaExitosa("Estadísticas de edades obtenidas con éxito", estadisticas);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<EstadoAdolescenteEstadisticoDTO>> obtenerEstadisticasEstados(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<EstadoAdolescenteEstadisticoDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

//            Funcionario funcionario = this.funcionarioRepository.findByNumeroDeDocumentoAndRemovidoAndBloqueado(
//                    usuarioSistema.getNumeroDeDocumento(),
//                    false,
//                    false
//            );

            Jerarquia jerarquiaActual = bodyJwtValido.getJerarquia();

            List<Object[]> resultados = fichaIdentificacionRepository.countFichaIdentificacionEstados(jerarquiaActual.getTokenIdentificador());

            List<EstadoAdolescenteEstadisticoDTO> estadisticas = resultados.stream()
                    .map(obj -> new EstadoAdolescenteEstadisticoDTO(
                            (String) obj[0],  // Edad calculada
                            ((Number) obj[1]).intValue()   // Cantidad de personas con esa edad
                    ))
                    .collect(Collectors.toList());

            respuesta.llenarRespuestaExitosa("Estadísticas de estados obtenidas con éxito", estadisticas);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<EstadoAdolescenteEstadisticoDTO>> obtenerEstadisticasSexo(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<EstadoAdolescenteEstadisticoDTO>> respuesta = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                respuesta.setMensaje(df2.getMensaje());
                respuesta.setLogOut(true);
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

//            Funcionario funcionario = this.funcionarioRepository.findByNumeroDeDocumentoAndRemovidoAndBloqueado(
//                    usuarioSistema.getNumeroDeDocumento(),
//                    false,
//                    false
//            );

            Jerarquia jerarquiaActual = bodyJwtValido.getJerarquia();

            List<Object[]> resultados = fichaIdentificacionRepository.countFichaIdentificacionSexo(jerarquiaActual.getTokenIdentificador());

            List<EstadoAdolescenteEstadisticoDTO> estadisticas = resultados.stream()
                    .map(obj -> new EstadoAdolescenteEstadisticoDTO(
                            (String) obj[0],  // Edad calculada
                            ((Number) obj[1]).intValue()   // Cantidad de personas con esa edad
                    ))
                    .collect(Collectors.toList());

            respuesta.llenarRespuestaExitosa("Estadísticas de estados obtenidas con éxito", estadisticas);

        } catch (Exception ex) {
            respuesta.llenarConDatosDeException(ex);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> validarIngresoNuevo(
            HttpServletRequest httpServletRequest,
            BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> jwtResponse =
                    this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!jwtResponse.isExito()) {
                df.setMensaje(jwtResponse.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> bodyResponse =
                    bodyEncriptado.desencriptarPorEmpresa(
                            this.parametroDelSistemaRepository,
                            null);

            if (!bodyResponse.isExito()) {
                df.setMensaje(bodyResponse.getMensaje());
                return df;
            }

            ValidarIngresoFichaRequest request =
                    new Gson().fromJson(
                            bodyResponse.getData(),
                            ValidarIngresoFichaRequest.class);

            if (request.getNemonicoTipoIngreso() == null) {
                df.llenarConDatosDeException(
                        new Exception("El tipo de ingreso no puede ser nulo"));
                return df;
            }

            FichaIdentificacion adolescente =
                    this.fichaIdentificacionRepository
                            .findByTokenIdentificadorAndRemovido(
                                    request.getTokenIdentificadorFicha(),
                                    false);

            if (adolescente == null) {
                df.llenarConDatosDeException(
                        new Exception("No se encontró la ficha de identificación"));
                return df;
            }

            String tipoIngreso = request.getNemonicoTipoIngreso();

            switch (tipoIngreso) {

                case EtiquetaNemonico.TIPO_ENTRADA_INGRESO_NUEVO -> {

                    // Debe estar libre
                    if (!EtiquetaNemonico.NEMONICO_ESTADO_ADOLESCENTE_LIBRE
                            .equalsIgnoreCase(adolescente.getEstado().getNemonico())) {

                        String mensaje = "No es posible realizar el registro del adolescente debido a que el CJDR/SOA: " + adolescente.getCentroIngreso().getNombre() + " no ha generado su registro de salida.";
                        df.llenarRespuestaExitosa(
                                mensaje,
                                false);
                        return df;
                    }

                    // No debe estar en proceso
                    if (Boolean.TRUE.equals(adolescente.getTieneProceso())) {
                        df.llenarRespuestaExitosa(
                                "El adolescente posee un proceso activo y no puede ser registrado como nuevo ingreso.",
                                false);
                        return df;
                    }

                    // No debe tener traslado activo
                    boolean tieneTrasladoActivo =
                            this.trasladoAdolescenteRepository
                                    .obtenerTrasladoActivo(adolescente.getTokenIdentificador())
                                    .isPresent();

                    if (tieneTrasladoActivo) {
                        df.llenarRespuestaExitosa(
                                "El adolescente tiene un traslado pendiente y no puede registrarse como nuevo ingreso.",
                                false);
                        return df;
                    }

                    df.llenarRespuestaExitosa(
                            "La ficha de identificación es válida.",
                            true);
                }

                case EtiquetaNemonico.TIPO_ENTRADA_TRASLADO -> {

                    Optional<TrasladoAdolescente> trasladoOpt =
                            this.trasladoAdolescenteRepository
                                    .obtenerTrasladoActivo(adolescente.getTokenIdentificador());

                    if (trasladoOpt.isEmpty()) {
                        df.llenarRespuestaExitosa(
                                "El adolescente no posee un traslado activo.",
                                false);
                        return df;
                    }

                    Jerarquia centroActual = jwtResponse.getData().getJerarquia();

                    Traslado traslado = trasladoOpt.get().getTraslado();

                    if (!traslado.getCentroDestino()
                            .getTokenIdentificador()
                            .equals(centroActual.getTokenIdentificador())) {

                        df.llenarRespuestaExitosa(
                                "El traslado está dirigido al centro "
                                        + traslado.getCentroDestino().getNombre()
                                        + " y no al centro actual.",
                                false);
                        return df;
                    }

                    df.llenarRespuestaExitosa(
                            "La ficha de identificación es válida.",
                            true);
                }

                case EtiquetaNemonico.TIPO_ENTRADA_FUGA -> {

                    if (!EtiquetaNemonico.NEMONICO_ESTADO_ADOLESCENTE_FUGADO
                            .equalsIgnoreCase(adolescente.getEstado().getNemonico())) {

                        df.llenarRespuestaExitosa(
                                "El adolescente no se encuentra en estado FUGADO.",
                                false);
                        return df;
                    }

                    df.llenarRespuestaExitosa(
                            "La ficha de identificación es válida.",
                            true);
                }

                default -> {
                    df.llenarRespuestaExitosa(
                            "Tipo de ingreso no reconocido.",
                            false);
                }
            }

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
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

    private FichaIdentificacionDTO mapearFichaIdentificacion(FichaIdentificacion fichaIdentificacion) {
        FichaIdentificacionDTO dto = new FichaIdentificacionDTO();
        dto.setTokenIdentificador(fichaIdentificacion.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(fichaIdentificacion.getEmpresa() != null
                ? fichaIdentificacion.getEmpresa().getTokenIdentificador() : null);
        dto.setApellidoPaterno(fichaIdentificacion.getApellidoPaterno());
        dto.setApellidoMaterno(fichaIdentificacion.getApellidoMaterno());
        dto.setNombres(fichaIdentificacion.getNombres());
        dto.setFechaNacimiento(fichaIdentificacion.getFechaNacimiento());
        dto.setEdad(fichaIdentificacion.getEdad());
//        dto.setTipoSexo(fichaIdentificacion.getTipoSexo() != null
//                ? fichaIdentificacion.getTipoSexo().getNombre() : null);

        if (fichaIdentificacion.getTipoSexo() != null) {
            dto.setNombreSexo(fichaIdentificacion.getTipoSexo().getNombre());
            dto.setTipoSexo(fichaIdentificacion.getTipoSexo().getTokenIdentificador());
        }
        dto.setAlias(fichaIdentificacion.getAlias());
        dto.setNacionalidad(fichaIdentificacion.getPaisNacimiento() != null
                ? fichaIdentificacion.getPaisNacimiento().getGentilicio() : null);
        dto.setPaisNacimiento(fichaIdentificacion.getPaisNacimiento() != null
                ? fichaIdentificacion.getPaisNacimiento().getNemonico() : null);
        dto.setTokenIdentificadorEstadoCivil(fichaIdentificacion.getEstadoCivil() != null
                ? fichaIdentificacion.getEstadoCivil().getTokenIdentificador() : null);
        dto.setNumeroHijos(fichaIdentificacion.getNumeroHijos());
        dto.setTokenIdentificadorOrigenEtnico(fichaIdentificacion.getOrigenEtnico() != null
                ? fichaIdentificacion.getOrigenEtnico().getTokenIdentificador() : null);
        dto.setSinDni(fichaIdentificacion.getSinDni());
        dto.setDni(fichaIdentificacion.getDni());
        dto.setImpedimentoDiscapacidad(fichaIdentificacion.getImpedimentoDiscapacidad());
        dto.setNombrePadre(fichaIdentificacion.getNombrePadre());
        dto.setNombreMadre(fichaIdentificacion.getNombreMadre());
        dto.setDomicilioActual(fichaIdentificacion.getDomicilioActual());
        dto.setDireccion(fichaIdentificacion.getDireccion());
        dto.setUbigeoUbicacion(fichaIdentificacion.getUbigeoDireccion() != null
                ? fichaIdentificacion.getCodigoUbigeoDireccion() : null);
        dto.setOcupacion(fichaIdentificacion.getTipoOcupacion() != null
                ? fichaIdentificacion.getTipoOcupacion().getNombre() : null);
        dto.setViveCon(fichaIdentificacion.getViveCon());
        dto.setOficioInternamiento(fichaIdentificacion.getOficioInternamiento());
        dto.setSentenciaResolucion(fichaIdentificacion.getSentenciaResolucion());
        dto.setLugarNacimiento(fichaIdentificacion.getLugarNacimiento());
        dto.setOtrosEspecificar(fichaIdentificacion.getOtrosEspecificar());
        dto.setDniFisico(fichaIdentificacion.getDniFisico());
        dto.setTokenIdentificadorGrupoVulnerable(fichaIdentificacion.getGrupoVulnerable() != null
                ? fichaIdentificacion.getGrupoVulnerable().getTokenIdentificador() : null);
        dto.setIngresahijos(fichaIdentificacion.getIngresoConHijo());
        dto.setOtroOrigenEtnico(fichaIdentificacion.getOtroOrigenEtnico());
        dto.setInstancia(catalogoToDTO(fichaIdentificacion.getInstancia()));
        dto.setCorteJusticia(catalogoToDTO(fichaIdentificacion.getCorteJusticia()));
        dto.setEspecialidad(catalogoToDTO(fichaIdentificacion.getEspecialidad()));
        dto.setOrganoJurisdiccional(fichaIdentificacion.getOrganoJurisdiccional());
        dto.setSecretario(fichaIdentificacion.getSecretario());
        dto.setJuez(fichaIdentificacion.getJuez());
        dto.setModalidadEstudio(fichaIdentificacion.getModalidadEstudio());
        if (fichaIdentificacion.getNivelEBR() != null) {
            dto.setNivelEBR(fichaIdentificacion.getNivelEBR().getTokenIdentificador());
        }
        if (fichaIdentificacion.getNivelSuperior() != null) {
            dto.setNivelSuperior(fichaIdentificacion.getNivelSuperior().getTokenIdentificador());
        }
        if (fichaIdentificacion.getNivelEBA() != null) {
            dto.setNivelEBA(fichaIdentificacion.getNivelEBA().getTokenIdentificador());
        }
        if (fichaIdentificacion.getEstadoCivil() != null) {
            dto.setTipoEstadoCivil(fichaIdentificacion.getEstadoCivil().getNombre());
        }
        if (fichaIdentificacion.getJuzgado() != null) {
            dto.setJuzgado(fichaIdentificacion.getJuzgado());
        }
        if (fichaIdentificacion.getObservacionIngreso() != null) {
            dto.setObservacionIngreso(fichaIdentificacion.getObservacionIngreso());
        }
        dto.setNombreTipoDocumento(fichaIdentificacion.getTipoIdentificacion().getNombre());
        dto.setNumeroDocumento(fichaIdentificacion.getNumeroIdentificacion());
        if (fichaIdentificacion.getFechaIngreso() != null) {
            dto.setFechaIngreso(fichaIdentificacion.getFechaIngreso());
        }
        if (fichaIdentificacion.getTipoEntrada() != null) {
            dto.setTipoEntrada(catalogoToDTO(fichaIdentificacion.getTipoEntrada()));
        }
        if (fichaIdentificacion.getEmail() != null) {
            dto.setEmail(fichaIdentificacion.getEmail());
        }

        return dto;
    }

    private JerarquiaDTO obtenerJerarquiaDTO(Funcionario funcionario) {
        Jerarquia jerarquia = funcionario != null ? funcionario.getDepartamento() : null;
        if (jerarquia == null) {
            return null;
        }
        JerarquiaDTO jerarquiaDTO = new JerarquiaDTO();
        jerarquiaDTO.setNombre(jerarquia.getNombre());
        jerarquiaDTO.setTokenIdentificador(jerarquia.getTokenIdentificador());
        jerarquiaDTO.setNemonico(jerarquia.getNemonico());
        jerarquiaDTO.setUbigeo(jerarquia.getUbigeo());
        jerarquiaDTO.setEsOficinaCentral(jerarquia.getEsOficinaCentral());
        Jerarquia jerarquiaPadre = jerarquia.getJerarquiaPadre();
        JerarquiaDTO jerarquiaPadreDTO = new JerarquiaDTO();
        if (jerarquiaPadre != null) {
            jerarquiaPadreDTO.setNemonico(jerarquiaPadre.getNemonico());
            jerarquiaDTO.setNemonicoPadre(jerarquiaPadre.getNemonico());
        }
        jerarquiaDTO.setJerarquiaPadre(jerarquiaPadreDTO);
        return jerarquiaDTO;
    }

    private JerarquiaDTO obtenerJerarquiaDTOFormJerarquia(Jerarquia jerarquia) {
        if (jerarquia == null) {
            return null;
        }
        JerarquiaDTO jerarquiaDTO = new JerarquiaDTO();
        jerarquiaDTO.setNombre(jerarquia.getNombre());
        jerarquiaDTO.setTokenIdentificador(jerarquia.getTokenIdentificador());
        jerarquiaDTO.setNemonico(jerarquia.getNemonico());
        jerarquiaDTO.setUbigeo(jerarquia.getUbigeo());
        jerarquiaDTO.setEsOficinaCentral(jerarquia.getEsOficinaCentral());
        Jerarquia jerarquiaPadre = jerarquia.getJerarquiaPadre();
        JerarquiaDTO jerarquiaPadreDTO = new JerarquiaDTO();
        if (jerarquiaPadre != null) {
            jerarquiaPadreDTO.setNemonico(jerarquiaPadre.getNemonico());
            jerarquiaDTO.setNemonicoPadre(jerarquiaPadre.getNemonico());
        }
        jerarquiaDTO.setJerarquiaPadre(jerarquiaPadreDTO);
        return jerarquiaDTO;
    }

    private Page<FichaIdentificacion> obtenerFichasPorCentro(Empresa empresa, String tokenCentro,
                                                             String filter, Pageable pageable, JerarquiaDTO jerarquiaDTO) {
        if (ObjectUtils.isEmpty(tokenCentro)) {
            if (jerarquiaDTO != null && ("SOA".equals(jerarquiaDTO.getNemonico()) || "CJDR".equals(jerarquiaDTO.getNemonico()))) {
                // Obtener nemonicoPadre de la jerarquía asociada al centroIngreso
                return fichaIdentificacionRepository.findByEmpresaIdEmpresaAndRemovidoAndCentroIngreso_JerarquiaPadre_Nemonico(
                        empresa.getIdEmpresa(),
                        false,
                        jerarquiaDTO.getNemonico(),
                        pageable
                );
            }
        } else {
            if (!ObjectUtils.isEmpty(filter)) {
                return fichaIdentificacionRepository.findByEmpresaIdEmpresaAndRemovidoAndCentroIngreso_TokenIdentificadorAndNombresContainingIgnoreCase(
                        empresa.getIdEmpresa(), false, tokenCentro, filter, pageable
                );
            }
            return fichaIdentificacionRepository.findByEmpresaIdEmpresaAndRemovidoAndCentroIngreso_TokenIdentificador(
                    empresa.getIdEmpresa(), false, tokenCentro, pageable
            );
        }

        if (!ObjectUtils.isEmpty(filter)) {
            return fichaIdentificacionRepository.findByEmpresaIdEmpresaAndRemovidoAndNombresContainingIgnoreCase(
                    empresa.getIdEmpresa(), false, filter, pageable
            );
        }
        return fichaIdentificacionRepository.findByEmpresaIdEmpresaAndRemovido(empresa.getIdEmpresa(), false, pageable);
    }

    private Page<FichaIdentificacion> obtenerFichasPorEmpresa(Empresa empresa, String filter, Pageable pageable) {
        return !ObjectUtils.isEmpty(filter)
                ? fichaIdentificacionRepository.findByEmpresaIdEmpresaAndRemovidoAndNombresContainingIgnoreCase(
                empresa.getIdEmpresa(), false, filter, pageable)
                : fichaIdentificacionRepository.findByEmpresaIdEmpresaAndRemovido(
                empresa.getIdEmpresa(), false, pageable);
    }

    private Page<FichaIdentificacion> obtenerFichasPorJerarquia(Empresa empresa, JerarquiaDTO jerarquiaDTO, String filter, Pageable pageable) {
        return !ObjectUtils.isEmpty(filter)
                ? fichaIdentificacionRepository.findByEmpresaIdEmpresaAndRemovidoAndCentroIngreso_TokenIdentificadorAndNombresContainingIgnoreCase(
                empresa.getIdEmpresa(), false, jerarquiaDTO.getTokenIdentificador(), filter, pageable)
                : fichaIdentificacionRepository.findByEmpresaIdEmpresaAndRemovidoAndCentroIngreso_TokenIdentificador(
                empresa.getIdEmpresa(), false, jerarquiaDTO.getTokenIdentificador(), pageable);
    }

    private Page<FichaIdentificacion> obtenerFichas(Empresa empresa, JerarquiaDTO jerarquiaDTO, FichaIdentificacionRequest paginacionRequest,
                                                    Pageable pageable, Boolean todosEstados, Boolean postEgreso) {
        String tokenCentro = paginacionRequest.getTokenCentro();
        String filter = paginacionRequest.getFilter();

        if (tokenCentro != null && !tokenCentro.isEmpty()) {
            return this.fichaIdentificacionRepository.buscarPorFiltroTokenCentro(empresa.getIdEmpresa(), tokenCentro, filter, todosEstados, postEgreso, pageable);
        } else {
            if (("SOA".equals(jerarquiaDTO.getNemonicoPadre()) || "CJDR".equals(jerarquiaDTO.getNemonicoPadre())) && (jerarquiaDTO.getEsOficinaCentral())) {
                return this.fichaIdentificacionRepository.buscarPorFiltroCentroPadre(empresa.getIdEmpresa(), jerarquiaDTO.getNemonicoPadre(), filter, todosEstados, postEgreso, pageable);
            } else {
                return this.fichaIdentificacionRepository.buscarPorFiltroTokenCentro(empresa.getIdEmpresa(), jerarquiaDTO.getTokenIdentificador(), filter, todosEstados
                        , postEgreso, pageable);
            }
        }

    }

    private <T> void safeAssign(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    @NotNull
    private static RegistroSalidaDTO entidadADto(RegistroSalida fuga) {
        RegistroSalidaDTO fugaDTO = new RegistroSalidaDTO();
        fugaDTO.setIdRegistroSalida(fuga.getIdRegistroSalida());
        fugaDTO.setFechaHoraSalida(fuga.getFechaHoraSalida());
        fugaDTO.setUsuarioSalida(fuga.getUsuarioSalida());
        fugaDTO.setMotivoSalida(entidadADtoCatalogo(fuga.getMotivoSalida()));
        fugaDTO.setNroDocumento(fuga.getNroDocumento());
        fugaDTO.setFechaHoraRegreso(fuga.getFechaHoraRegreso());
        fugaDTO.setObservaciones(fuga.getObservaciones());
        fugaDTO.setTipoSalida(entidadADtoCatalogo(fuga.getTipoSalida()));
        fugaDTO.setTipoSalidaLugar(fuga.getTipoSalidaLugar());
        fugaDTO.setCentroSalida(entidadADtoJerarquia(fuga.getCentroSalida()));
        fugaDTO.setTokenIdentificador(fuga.getTokenIdentificador());
        fugaDTO.setMotivoSalida(entidadADtoCatalogo(fuga.getMotivoSalida()));
        fugaDTO.setEventoFuga(entidadADtoFuga(fuga.getEventoFuga()));
        fugaDTO.setTraslado(entidadADtoTraslado(fuga.getTraslado()));
        fugaDTO.setPermisoSalida(entidadADtoPermiso(fuga.getPermisoSalida()));
        fugaDTO.setExternamiento(entidadADtoermisoExternamiento(fuga.getExternamiento()));
        if (fuga.getMotivoSalida() != null) {
            fugaDTO.setNombreMotivoSalida(fuga.getMotivoSalida().getNombre());
        }
        fugaDTO.setTipoSalida(entidadADtoCatalogo(fuga.getTipoSalida()));
        if (fuga.getTipoSalida() != null) {
            fugaDTO.setNombreTipoSalida(fuga.getTipoSalida().getNombre());
        }
        if (fuga.getTokenFichaIdentificacion() != null) {
            FichaIdentificacion ficha = fuga.getTokenFichaIdentificacion();
            fugaDTO.setTokenFichaIdentificacion(ficha.getIdFichaIdentificacion());
            fugaDTO.setTokenIdentificadorAdolescente(ficha.getTokenIdentificador());
            fugaDTO.setDniAdolescente(ficha.getDni());
            fugaDTO.setNombreAdolescente(
                    (ficha.getNombres() != null ? ficha.getNombres() : "") + " " +
                            (ficha.getApellidoPaterno() != null ? ficha.getApellidoPaterno() : "") + " " +
                            (ficha.getApellidoMaterno() != null ? ficha.getApellidoMaterno() : "")
            );
        }
        return fugaDTO;
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

    private static JerarquiaDTO entidadADtoJerarquia(Jerarquia entidad) {
        if (entidad == null) return null;

        JerarquiaDTO dto = new JerarquiaDTO();
        dto.setId(entidad.getIdJerarquia());
        dto.setNombre(entidad.getNombre());
        dto.setNemonico(entidad.getNemonico());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(entidad.getEmpresa().getTokenIdentificador());
        return dto;
    }

    private static EventoFugaDTO entidadADtoFuga(EventoFuga entidad) {
        if (entidad == null) return null;
        EventoFugaDTO dto = new EventoFugaDTO();
        dto.setIdFuga(entidad.getIdFuga());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        return dto;
    }

    private static TrasladoDTO entidadADtoTraslado(Traslado entidad) {
        if (entidad == null) return null;
        TrasladoDTO dto = new TrasladoDTO();
        dto.setIdTraslado(entidad.getIdTraslado());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        return dto;
    }

    private static InformePermisoSalidaDTO entidadADtoPermiso(InformePermisoSalidaAdolescente entidad) {
        if (entidad == null) return null;
        InformePermisoSalidaDTO dto = new InformePermisoSalidaDTO();
        dto.setIdPermisoSalida(entidad.getIdPermisoSalida());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        return dto;
    }

    private static ActaExternamientoDTO entidadADtoermisoExternamiento(ActaExternamiento entidad) {
        if (entidad == null) return null;
        ActaExternamientoDTO dto = new ActaExternamientoDTO();
        dto.setIdActaExternamiento(entidad.getIdActaExternamiento());
        dto.setTokenIdentificador(entidad.getTokenIdentificador());
        return dto;
    }

    private void guardarDocumentosFichaIngreso(FichaIngreso fichaIngreso, List<String> tokensDocumentosIngreso, String ip, UsuarioSistema usuarioLogin) {
        if (!ObjectUtils.isEmpty(tokensDocumentosIngreso)) {
            List<DocumentosFichaIngreso> documentos = tokensDocumentosIngreso.stream().map(token -> {
                DocumentosFichaIngreso dfi = new DocumentosFichaIngreso();
                dfi.setFichaIngreso(fichaIngreso);
                dfi.setTipoDocumento(this.catalogoRepository.findByTokenIdentificadorAndRemovido(token, false));
                dfi.setIpCrea(ip);
                dfi.setUsuarioSistemaCrea(usuarioLogin);
                return dfi;
            }).collect(Collectors.toList());

            this.documentosFichaIngresoRepository.saveAll(documentos);
        }
    }
}
