package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.*;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico.*;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.FichaMedicaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class ConsultaAtencionIntegralServiceImpl implements ConsultaAtencionIntegralService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private CatalogoRepository catalogoRepository;
    private FichaMedicaRepository fichaMedicaRepository;
    private JwtProviderService jwtProviderService;
    private ConsultaAtencionIntegralRepository consultaAtencionIntegralRepository;
    private RecetaRepository recetaRepository;
    private DetalleRecetaRepository detalleRecetaRepository;
    private OrdenMedicaRepository ordenMedicaRepository;
    private OrdenMedicaDetalleRepository ordenMedicaDetalleRepository;
    private MedicamentoRepository medicamentoRepository;
    private EspecialidadProductoRepository especialidadProductoRepository;

    @Override
    public RespuestaPorDefectoAuditoria<ConsultaAtencionIntegralDTO> crearConsulta(HttpServletRequest request, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ConsultaAtencionIntegralDTO> respuesta = new RespuestaPorDefectoAuditoria<>();

        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> jwtResponse = jwtProviderService.obtenerBodyJwtApp(request);
            if (!jwtResponse.isExito()) {
                respuesta.setMensaje(jwtResponse.getMensaje());
                respuesta.setMensajeErrorReal(jwtResponse.getMensajeErrorReal());
                respuesta.setLogOut(true);
                return respuesta;
            }

            BodyJwtValido bodyJwtValido = jwtResponse.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            String ip = request.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                respuesta.setMensaje(df22.getMensaje());
                return respuesta;
            }
            String bodyDesencriptado = df22.getData();
            ConsultaAtencionIntegralDTO consultaDTO = new Gson().fromJson(bodyDesencriptado, ConsultaAtencionIntegralDTO.class);

            FichaMedica evaluacionMedica = fichaMedicaRepository.findByTokenIdentificadorAndRemovido(
                    consultaDTO.getTokenIdFichaMedica(), false);

            if (evaluacionMedica == null) {
                respuesta.setMensaje("La evaluación médica asociada no fue encontrada o ya fue eliminada.");
                return respuesta;
            }

            ConsultaAtencionIntegral consulta;

            if (consultaDTO.getEsEdicion()) {
                consulta = consultaAtencionIntegralRepository.findByTokenIdentificadorAndRemovido(consultaDTO.getTokenIdentificador(), false);
                if (consulta == null) {
                    respuesta.setMensaje("No se encontró la consulta de atención integral para editar o ya fue eliminada.");
                    return respuesta;
                }


                consulta.setUsuarioSistemaEdita(usuarioSistema);
                consulta.setIpEdita(ip);
                consulta.setFechaEdicion(new Date());
            } else {

                consulta = new ConsultaAtencionIntegral();

                // Datos de auditoría para creación
                consulta.setUsuarioSistemaCrea(usuarioSistema);
                consulta.setIpCrea(ip);
                consulta.setFechaCreacion(new Date());
            }
            consulta.setFichaMedica(evaluacionMedica);

            if (!ObjectUtils.isEmpty(consultaDTO.getFechaInicio())) {
                consulta.setFechaInicio(consultaDTO.getFechaInicio());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getObservaciones())) {
                consulta.setObservaciones(consultaDTO.getObservaciones());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getMotivoConsulta())) {
                consulta.setMotivoConsulta(consultaDTO.getMotivoConsulta());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getEdad())) {
                consulta.setEdad(consultaDTO.getEdad());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getTipoEnfermedad())) {
                consulta.setTipoEnfermedad(consultaDTO.getTipoEnfermedad());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getFormaDeInicio())) {
                consulta.setFormaDeInicio(consultaDTO.getFormaDeInicio());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getEstadoDeAnimo())) {
                consulta.setEstadoDeAnimo(consultaDTO.getEstadoDeAnimo());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getOrina())) {
                consulta.setOrina(consultaDTO.getOrina());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getDeposiciones())) {
                consulta.setDeposiciones(consultaDTO.getDeposiciones());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getFiebre15dias())) {
                consulta.setFiebre15dias(consultaDTO.getFiebre15dias());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getTos15dias())) {
                consulta.setTos15dias(consultaDTO.getTos15dias());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getSecrecionGenitales())) {
                consulta.setSecrecionGenitales(consultaDTO.getSecrecionGenitales());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getPerdidaPeso())) {
                consulta.setPerdidaPeso(consultaDTO.getPerdidaPeso());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getPeso())) {
                consulta.setPeso(consultaDTO.getPeso());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getTalla())) {
                consulta.setTalla(consultaDTO.getTalla());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getPresion())) {
                consulta.setPresion(consultaDTO.getPresion());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getImc())) {
                consulta.setIMC(consultaDTO.getImc());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getTemperatura())) {
                consulta.setTemperatura(consultaDTO.getTemperatura());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getDiagnostico())) {
                consulta.setDiagnostico(consultaDTO.getDiagnostico());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getTratamiento())) {
                consulta.setTratamiento(consultaDTO.getTratamiento());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getExamenesAuxiliares())) {
                consulta.setExamenesAuxiliares(consultaDTO.getExamenesAuxiliares());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getFechaProximaCita())) {
                consulta.setFechaProximaCita(consultaDTO.getFechaProximaCita());
            }
            if (!ObjectUtils.isEmpty(consultaDTO.getTiempoEnfermedad())) {
                consulta.setTiempoEnfermedad(consultaDTO.getTiempoEnfermedad());
            }
//            if (!ObjectUtils.isEmpty(consultaDTO.getLugarAtencion())) {
//                consulta.setLugarAtencion(consultaDTO.getLugarAtencion());
//            }
//
//            if (!ObjectUtils.isEmpty(consultaDTO.getDoctorAtencion())) {
//                consulta.setDoctorAtencion(consultaDTO.getDoctorAtencion());
//            }

            consulta.setSed(consultaDTO.getSed() != null && consultaDTO.getSed());
            consulta.setSueno(consultaDTO.getSueno() != null && consultaDTO.getSueno());
            consulta.setApetito(consultaDTO.getApetito() != null && consultaDTO.getApetito());

            // Guardar la consulta en la base de datos
            consultaAtencionIntegralRepository.save(consulta);

            if(!ObjectUtils.isEmpty(consultaDTO.getReceta())){
                RecetaDTO recetaDTO = consultaDTO.getReceta();
                Receta receta = null;
                receta = this.recetaRepository.findByConsultaAtencionIntegral_TokenIdentificadorAndRemovido(consulta.getTokenIdentificador(),false);
                if(receta==null){
                    receta = new Receta();
                    receta.setConsultaAtencionIntegral(consulta);
                    receta.setIpCrea(ip);
                    receta.setFechaCreacion(new Date());
                    receta.setUsuarioSistemaCrea(usuarioSistema);
                } else {
                    receta.setIpEdita(ip);
                    receta.setFechaEdicion(new Date());
                    receta.setUsuarioSistemaEdita(usuarioSistema);
                }

                receta.setNumeroReceta(recetaDTO.getNumeroReceta());
                receta.setFechaEmision(recetaDTO.getFechaEmision());
                receta.setObservaciones(recetaDTO.getObservaciones());
                if(recetaDTO.getEspecialidad() != null){
                    receta.setEspecialidad(dtoToCatalogo(recetaDTO.getEspecialidad()));
                } else {
                    receta.setEspecialidad(null);
                }

                receta = this.recetaRepository.save(receta);
                receta.setTokenIdentificador(receta.getTokenIdentificador());

                List<DetalleReceta> detallesActuales = this.detalleRecetaRepository.findAllByReceta_TokenIdentificadorAndRemovido(receta.getTokenIdentificador(), false);

                if(!detallesActuales.isEmpty()){
                    detallesActuales.forEach(det -> det.setRemovido(true));
                }

                if(recetaDTO.getDetalles() != null){
                    for(DetalleRecetaDTO detalleDTO : recetaDTO.getDetalles()){
                        DetalleReceta detalleDb = null;
                        // Si el detalle no tiene token, es uno nuevo
                        // Si tiene token, intentamos encontrarlo entre los detalles actuales
                        if(!ObjectUtils.isEmpty(detalleDTO.getTokenIdentificador())){
                            detalleDb = detallesActuales.stream()
                                    .filter(d -> d.getTokenIdentificador().equals(detalleDTO.getTokenIdentificador()))
                                    .findFirst()
                                    .orElse(null);
                        }

                        if(detalleDb == null){
                            // Crear nuevo detalle
                            detalleDb = new DetalleReceta();
                            detalleDb.setReceta(receta);
                            detalleDb.setIpCrea(ip);
                            detalleDb.setFechaCreacion(new Date());
                            detalleDb.setUsuarioSistemaCrea(usuarioSistema);
                        } else {
                            // Actualizar detalle existente
                            detalleDb.setIpEdita(ip);
                            detalleDb.setFechaEdicion(new Date());
                            detalleDb.setUsuarioSistemaEdita(usuarioSistema);
                        }

                        detalleDb.setMedicamento(detalleDTO.getMedicamento());
                        if (detalleDTO.getMedicamentoCompleto() != null) {
                            detalleDb.setMedicamentoCompleto(this.medicamentoRepository.findByTokenIdentificadorAndRemovido(detalleDTO.getMedicamentoCompleto().getTokenIdentificador(), false));
                        }
                        detalleDb.setDosis(detalleDTO.getDosis());
                        detalleDb.setFrecuencia(detalleDTO.getFrecuencia());
                        detalleDb.setIndicaciones(detalleDTO.getIndicaciones());
                        detalleDb.setConcentracion(detalleDTO.getConcentracion());
                        if(detalleDTO.getFormaFarmaceutica() != null){
                            detalleDb.setFormaFarmaceutica(dtoToCatalogo(detalleDTO.getFormaFarmaceutica()));
                        } else {
                            detalleDb.setFormaFarmaceutica(null);
                        }
                        detalleDb.setRemovido(false);

                        detalleDb = this.detalleRecetaRepository.save(detalleDb);
                        detalleDTO.setTokenIdentificador(detalleDb.getTokenIdentificador());
                    }
                }

                // Guardar los que quedaron marcados como removidos (los que no se actualizaron)
                for(DetalleReceta detActual : detallesActuales){
                    if(detActual.getRemovido()){
                        detActual.setUsuarioSistemaElimina(usuarioSistema);
                        detActual.setFechaEliminacion(new Date());
                        detActual.setIpElimina(ip);
                        this.detalleRecetaRepository.save(detActual);
                    }
                }

            }

            if(!ObjectUtils.isEmpty(consultaDTO.getOrden())){
                OrdenMedicaDTO ordenDTO = consultaDTO.getOrden();
                OrdenMedica ordenMedica = null;
                ordenMedica = this.ordenMedicaRepository.findByConsultaAtencionIntegral_TokenIdentificadorAndRemovido(consulta.getTokenIdentificador(),false);
                if(ordenMedica==null){
                    ordenMedica = new OrdenMedica();
                    ordenMedica.setConsultaAtencionIntegral(consulta);
                    ordenMedica.setIpCrea(ip);
                    ordenMedica.setFechaCreacion(new Date());
                    ordenMedica.setUsuarioSistemaCrea(usuarioSistema);
                } else {
                    ordenMedica.setIpEdita(ip);
                    ordenMedica.setFechaEdicion(new Date());
                    ordenMedica.setUsuarioSistemaEdita(usuarioSistema);
                }

                ordenMedica.setNumeroOrden(ordenDTO.getNumeroOrden());
                ordenMedica.setFechaEmision(ordenDTO.getFechaEmision());
                ordenMedica.setObservaciones(ordenDTO.getObservaciones());

                ordenMedica = this.ordenMedicaRepository.save(ordenMedica);
                ordenMedica.setTokenIdentificador(ordenMedica.getTokenIdentificador());

                List<OrdenMedicaDetalle> detallesActuales = this.ordenMedicaDetalleRepository.findAllByOrdenMedica_TokenIdentificadorAndRemovido(ordenMedica.getTokenIdentificador(), false);

                if(!detallesActuales.isEmpty()){
                    detallesActuales.forEach(det -> det.setRemovido(true));
                }

                if(ordenDTO.getDetalles() != null){
                    for(OrdenMedicaDetalleDTO detalleDTO : ordenDTO.getDetalles()){
                        OrdenMedicaDetalle detalleDb = null;
                        // Si el detalle no tiene token, es uno nuevo
                        // Si tiene token, intentamos encontrarlo entre los detalles actuales
                        if(!ObjectUtils.isEmpty(detalleDTO.getTokenIdentificador())){
                            detalleDb = detallesActuales.stream()
                                    .filter(d -> d.getTokenIdentificador().equals(detalleDTO.getTokenIdentificador()))
                                    .findFirst()
                                    .orElse(null);
                        }

                        if(detalleDb == null){
                            // Crear nuevo detalle
                            detalleDb = new OrdenMedicaDetalle();
                            detalleDb.setOrdenMedica(ordenMedica);
                            detalleDb.setIpCrea(ip);
                            detalleDb.setFechaCreacion(new Date());
                            detalleDb.setUsuarioSistemaCrea(usuarioSistema);
                        } else {
                            // Actualizar detalle existente
                            detalleDb.setIpEdita(ip);
                            detalleDb.setFechaEdicion(new Date());
                            detalleDb.setUsuarioSistemaEdita(usuarioSistema);
                        }

                        if (detalleDTO.getEspecialidadProducto() != null) {
                            detalleDb.setEspecialidadProducto(this.especialidadProductoRepository.findByTokenIdentificadorAndRemovido(detalleDTO.getEspecialidadProducto().getTokenIdentificador(), false));
                        }

                        detalleDb.setRemovido(false);

                        detalleDb = this.ordenMedicaDetalleRepository.save(detalleDb);
                        detalleDTO.setTokenIdentificador(detalleDb.getTokenIdentificador());
                    }
                }

                // Guardar los que quedaron marcados como removidos (los que no se actualizaron)
                for(OrdenMedicaDetalle detActual : detallesActuales){
                    if(detActual.getRemovido()){
                        detActual.setUsuarioSistemaElimina(usuarioSistema);
                        detActual.setFechaEliminacion(new Date());
                        detActual.setIpElimina(ip);
                        this.ordenMedicaDetalleRepository.save(detActual);
                    }
                }
            }

            // Preparar respuesta
            respuesta.setData(consultaDTO);
            respuesta.llenarRespuestaExitosa("Consulta de atención integral creada exitosamente.",consultaDTO);

        }catch (Exception e) {
            respuesta.llenarConDatosDeException(e);
        }

        return respuesta;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<ConsultaAtencionIntegralDTO>> getConsultaAtencionByIdFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<ConsultaAtencionIntegralDTO>> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> jwtResponse = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!jwtResponse.isExito()) {
                df.setMensaje(jwtResponse.getMensaje());
                df.setMensajeErrorReal(jwtResponse.getMensajeErrorReal());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyDesencriptado, PaginacionRequest.class);
            String tokenIdFichaMedica = paginacionRequest.getTokenIdentificador();

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize()
            );

            Page<ConsultaAtencionIntegral> consultaPage = this.consultaAtencionIntegralRepository.findByFichaMedica_TokenIdentificadorAndRemovidoOrderByFechaInicioDesc(
                    tokenIdFichaMedica, false, pageable);

            PaginacionResponse<ConsultaAtencionIntegralDTO> paginacionResponse = new PaginacionResponse<>();
            List<ConsultaAtencionIntegralDTO> consultaDTOS = consultaPage.stream().map(
                    consulta -> {
                        ConsultaAtencionIntegralDTO dto = new ConsultaAtencionIntegralDTO();
                        dto.setTokenIdentificador(consulta.getTokenIdentificador());
                        dto.setFechaInicio(consulta.getFechaInicio());
                        dto.setObservaciones(consulta.getObservaciones());
                        dto.setMotivoConsulta(consulta.getMotivoConsulta());
                        dto.setEstadoDeAnimo(consulta.getEstadoDeAnimo());


                        if (consulta.getFichaMedica() != null) {
                            dto.setTokenIdFichaMedica(consulta.getFichaMedica().getTokenIdentificador());
                        }

                        return dto;
                    }
            ).toList();

            paginacionResponse.setData(consultaDTOS);
            paginacionResponse.setTotalItems(consultaPage.getTotalElements());

            df.llenarRespuestaExitosa("Evaluaciones médica progresos obtenidas con éxito", paginacionResponse);

        }catch (Exception e) {
            df.llenarConDatosDeException(e);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ConsultaAtencionIntegralDTO> getConsultaActividadIntegralByIdTokenId(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<ConsultaAtencionIntegralDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> jwtResponse = jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!jwtResponse.isExito()) {
                df.setMensaje(jwtResponse.getMensaje());
                df.setMensajeErrorReal(jwtResponse.getMensajeErrorReal());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();
            String tokenId = new Gson().fromJson(bodyDesencriptado, String.class);

            ConsultaAtencionIntegral consulta = this.consultaAtencionIntegralRepository.findByTokenIdentificadorAndRemovido(tokenId, false);

            if (consulta == null) {
                df.setMensaje("No se encontró ninguna consulta de atención integral con el token proporcionado o ya fue eliminada.");
                return df;
            }

            ConsultaAtencionIntegralDTO dto = new ConsultaAtencionIntegralDTO();
            if (!ObjectUtils.isEmpty(consulta.getTokenIdentificador())) {
                dto.setTokenIdentificador(consulta.getTokenIdentificador());
            }
            if (!ObjectUtils.isEmpty(consulta.getFechaInicio())) {
                dto.setFechaInicio(consulta.getFechaInicio());
            }
            if (!ObjectUtils.isEmpty(consulta.getObservaciones())) {
                dto.setObservaciones(consulta.getObservaciones());
            }
            if (!ObjectUtils.isEmpty(consulta.getMotivoConsulta())) {
                dto.setMotivoConsulta(consulta.getMotivoConsulta());
            }
            if (!ObjectUtils.isEmpty(consulta.getEdad())) {
                dto.setEdad(consulta.getEdad());
            }
            if (!ObjectUtils.isEmpty(consulta.getTipoEnfermedad())) {
                dto.setTipoEnfermedad(consulta.getTipoEnfermedad());
            }
            if (!ObjectUtils.isEmpty(consulta.getFormaDeInicio())) {
                dto.setFormaDeInicio(consulta.getFormaDeInicio());
            }
            if (!ObjectUtils.isEmpty(consulta.getEstadoDeAnimo())) {
                dto.setEstadoDeAnimo(consulta.getEstadoDeAnimo());
            }
            if (!ObjectUtils.isEmpty(consulta.getSed())) {
                dto.setSed(consulta.getSed());
            }
            if (!ObjectUtils.isEmpty(consulta.getSueno())) {
                dto.setSueno(consulta.getSueno());
            }
            if (!ObjectUtils.isEmpty(consulta.getApetito())) {
                dto.setApetito(consulta.getApetito());
            }
            if (!ObjectUtils.isEmpty(consulta.getOrina())) {
                dto.setOrina(consulta.getOrina());
            }
            if (!ObjectUtils.isEmpty(consulta.getDeposiciones())) {
                dto.setDeposiciones(consulta.getDeposiciones());
            }
            if (!ObjectUtils.isEmpty(consulta.getFiebre15dias())) {
                dto.setFiebre15dias(consulta.getFiebre15dias());
            }
            if (!ObjectUtils.isEmpty(consulta.getTos15dias())) {
                dto.setTos15dias(consulta.getTos15dias());
            }
            if (!ObjectUtils.isEmpty(consulta.getSecrecionGenitales())) {
                dto.setSecrecionGenitales(consulta.getSecrecionGenitales());
            }
            if (!ObjectUtils.isEmpty(consulta.getPerdidaPeso())) {
                dto.setPerdidaPeso(consulta.getPerdidaPeso());
            }
            if (!ObjectUtils.isEmpty(consulta.getPeso())) {
                dto.setPeso(consulta.getPeso());
            }
            if (!ObjectUtils.isEmpty(consulta.getTalla())) {
                dto.setTalla(consulta.getTalla());
            }
            if (!ObjectUtils.isEmpty(consulta.getPresion())) {
                dto.setPresion(consulta.getPresion());
            }
            if (!ObjectUtils.isEmpty(consulta.getIMC())) {
                dto.setImc(consulta.getIMC());
            }
            if (!ObjectUtils.isEmpty(consulta.getTemperatura())) {
                dto.setTemperatura(consulta.getTemperatura());
            }
            if (!ObjectUtils.isEmpty(consulta.getDiagnostico())) {
                dto.setDiagnostico(consulta.getDiagnostico());
            }
            if (!ObjectUtils.isEmpty(consulta.getTratamiento())) {
                dto.setTratamiento(consulta.getTratamiento());
            }
            if (!ObjectUtils.isEmpty(consulta.getExamenesAuxiliares())) {
                dto.setExamenesAuxiliares(consulta.getExamenesAuxiliares());
            }
            if (!ObjectUtils.isEmpty(consulta.getFechaProximaCita())) {
                dto.setFechaProximaCita(consulta.getFechaProximaCita());
            }
            if (!ObjectUtils.isEmpty(consulta.getTiempoEnfermedad())) {
                dto.setTiempoEnfermedad(consulta.getTiempoEnfermedad());
            }

//            if (!ObjectUtils.isEmpty(consulta.getDoctorAtencion())) {
//                dto.setDoctorAtencion(consulta.getDoctorAtencion());
//            }
//
//            if (!ObjectUtils.isEmpty(consulta.getLugarAtencion())) {
//                dto.setLugarAtencion(consulta.getLugarAtencion());
//            }

            if (consulta.getFichaMedica() != null) {
                dto.setTokenIdFichaMedica(consulta.getFichaMedica().getTokenIdentificador());
            }

            Receta receta = this.recetaRepository.findRecetaConsultaSinDetalles(consulta.getTokenIdentificador());
            if (receta != null) {
                RecetaDTO recetaDTO = recetaToDTO(receta);
                dto.setReceta(recetaDTO);
            }

            OrdenMedica ordenMedica = this.ordenMedicaRepository.findOrdenMedicaConsultaSinDetalles(consulta.getTokenIdentificador());
            if (ordenMedica != null) {
                OrdenMedicaDTO ordenMedicaDTO = ordenMedicaToDTO(ordenMedica);
                dto.setOrden(ordenMedicaDTO);
            }

            df.llenarRespuestaExitosa("Consulta de atención integral obtenida con éxito", dto);

        }catch (Exception e) {
            df.llenarConDatosDeException(e);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> deleteConsultaActividadIntegral(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> dfJwt = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!dfJwt.isExito()) {
                df.setMensaje(dfJwt.getMensaje());
                df.setMensajeErrorReal(dfJwt.getMensajeErrorReal());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = dfJwt.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(bodyJwtValido.getEmpresa().getTokenIdentificador());

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyDesencriptado = df22.getData();

            ConsultaAtencionIntegralDTO consultaDTO = new Gson().fromJson(bodyDesencriptado, ConsultaAtencionIntegralDTO.class);

            ConsultaAtencionIntegral consulta = this.consultaAtencionIntegralRepository
                    .findByTokenIdentificadorAndRemovido(consultaDTO.getTokenIdentificador(), false);

            if (consulta == null) {
                df.setMensaje("No se encontró la consulta de atención integral o ya fue eliminada.");
                return df;
            }

            consulta.setRemovido(true);
            consulta.setFechaEliminacion(new Date());
            consulta.setIpElimina(httpServletRequest.getRemoteAddr());
            consulta.setUsuarioSistemaElimina(usuarioSistema);

            this.consultaAtencionIntegralRepository.save(consulta);

            df.llenarRespuestaExitosa("La consulta de atención integral ha sido eliminada correctamente.", true);


        }catch (Exception e) {
            df.llenarConDatosDeException(e);
        }
        return df;
    }

    private CatalogoDTO catalogoToDTO(Catalogo catalogo){
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

    private Catalogo dtoToCatalogo(CatalogoDTO catalogoDTO){
        if (catalogoDTO == null) {
            return null;
        }

        Catalogo catalogo = this.catalogoRepository.findByTokenIdentificadorAndRemovido(catalogoDTO.getTokenIdentificador(), false);

        return catalogo;
    }

    private RecetaDTO recetaToDTO(Receta receta){
        RecetaDTO dto = new RecetaDTO();
        dto.setTokenIdentificador(receta.getTokenIdentificador());
        dto.setNumeroReceta(receta.getNumeroReceta());
        dto.setFechaEmision(receta.getFechaEmision());
        dto.setObservaciones(receta.getObservaciones());
        dto.setEspecialidad(catalogoToDTO(receta.getEspecialidad()));

        List<DetalleReceta> detalles = this.detalleRecetaRepository.findAllByReceta_TokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

        if(detalles != null && !detalles.isEmpty()){
            List<DetalleRecetaDTO> detalleDTOS = detalles.stream().map(this::detalleRecetaToDTO).toList();
            dto.setDetalles(new java.util.ArrayList<>(detalleDTOS));
        }

        return dto;
    }

    private DetalleRecetaDTO detalleRecetaToDTO(DetalleReceta detalle){
        DetalleRecetaDTO dto = new DetalleRecetaDTO();
        dto.setTokenIdentificador(detalle.getTokenIdentificador());
        dto.setMedicamento(detalle.getMedicamento());

        if (detalle.getMedicamentoCompleto() != null) {
            dto.setMedicamentoCompleto(detalle.getMedicamentoCompleto().convertirADTO());
        }

        dto.setDosis(detalle.getDosis());
        dto.setFrecuencia(detalle.getFrecuencia());
        dto.setIndicaciones(detalle.getIndicaciones());
        dto.setConcentracion(detalle.getConcentracion());
        dto.setFormaFarmaceutica(catalogoToDTO(detalle.getFormaFarmaceutica()));
        return dto;
    }

    private OrdenMedicaDTO ordenMedicaToDTO(OrdenMedica ordenMedica){
        OrdenMedicaDTO dto = new OrdenMedicaDTO();
        dto.setTokenIdentificador(ordenMedica.getTokenIdentificador());
        dto.setNumeroOrden(ordenMedica.getNumeroOrden());
        dto.setFechaEmision(ordenMedica.getFechaEmision());
        dto.setObservaciones(ordenMedica.getObservaciones());

        List<OrdenMedicaDetalle> detalles = this.ordenMedicaDetalleRepository.findAllByOrdenMedica_TokenIdentificadorAndRemovido(dto.getTokenIdentificador(), false);

        if(detalles != null && !detalles.isEmpty()){
            List<OrdenMedicaDetalleDTO> detalleDTOS = detalles.stream().map(this::detalleOrdenMedicaToDTO).toList();
            dto.setDetalles(new java.util.ArrayList<>(detalleDTOS));
        }

        return dto;
    }

    private OrdenMedicaDetalleDTO detalleOrdenMedicaToDTO(OrdenMedicaDetalle detalle){
        OrdenMedicaDetalleDTO dto = new OrdenMedicaDetalleDTO();
        dto.setTokenIdentificador(detalle.getTokenIdentificador());

        if (detalle.getEspecialidadProducto() != null) {
            dto.setEspecialidadProducto(detalle.getEspecialidadProducto().convertirADTO());
        }

        return dto;
    }
}
