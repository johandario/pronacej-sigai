package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.*;
import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.CriterioEvaluacionMedicaSeguimiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.EvaluacionMedica;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.CriterioEvaluacionMedicaProgresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.CriterioEvaluacionMedicaSeguimientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.EvaluacionMedicaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.EvaluacionMedicaProgresoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico.CriterioEvaluacionMedicaProgresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico.EvaluacionMedicaProgresoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.FichaMedicaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EvaluacionMedicaProgresoCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class EvaluacionMedicaProgresoServiceImpl implements EvaluacionMedicaProgresoService{

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private CatalogoRepository catalogoRepository;
    private FichaMedicaRepository fichaMedicaRepository;
    private JwtProviderService jwtProviderService;
    private EvaluacionMedicaProgresoRepository evaluacionMedicaProgresoRepository;
    private CriterioEvaluacionMedicaProgresoRepository criterioEvaluacionMedicaProgresoRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;
    private EvaluacionMedicaProgresoCarpetaRepository evaluacionMedicaProgresoCarpetaRepository;


    @Override
    public RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> getEvaluacionMedicaProgresoByIdTokenId(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            String tokenIdFichaIdentificacion = new Gson().fromJson(body, String.class);

            EvaluacionMedicaProgreso evaluacionMedicaProgreso = this.evaluacionMedicaProgresoRepository.findByTokenIdentificadorAndRemovido(tokenIdFichaIdentificacion,  false);

            if(evaluacionMedicaProgreso == null){
                df.setMensaje("No existe una evaluacion médica progreso asociada al token proporcionado");
                df.setExito(false);
                return df;
            }

            EvaluacionMedicaProgresoDTO evaluacionMedicaProgresoDTO = new EvaluacionMedicaProgresoDTO();
            evaluacionMedicaProgresoDTO.setFecha(evaluacionMedicaProgreso.getFecha());
            evaluacionMedicaProgresoDTO.setTokenIdFichaMedica(evaluacionMedicaProgreso.getFichaMedica().getTokenIdentificador());
            evaluacionMedicaProgresoDTO.setTipoEvaluacionProgreso(catalogoToDTO(evaluacionMedicaProgreso.getTipoEvaluacionProgreso()));
            evaluacionMedicaProgresoDTO.setEstadoNutricional(catalogoToDTO(evaluacionMedicaProgreso.getEstadoNutricional()));
            evaluacionMedicaProgresoDTO.setTokenIdentificador(evaluacionMedicaProgreso.getTokenIdentificador());

            if (!ObjectUtils.isEmpty(evaluacionMedicaProgreso.getTipoDesnutricion())) {
                evaluacionMedicaProgresoDTO.setTipoDesnutricion(catalogoToDTO(evaluacionMedicaProgreso.getTipoDesnutricion()));
            }

            if (!ObjectUtils.isEmpty(evaluacionMedicaProgreso.getGrado())) {
                evaluacionMedicaProgresoDTO.setGrado(evaluacionMedicaProgreso.getGrado());
            }

            if (!ObjectUtils.isEmpty(evaluacionMedicaProgreso.getTalla())) {
                evaluacionMedicaProgresoDTO.setTalla(evaluacionMedicaProgreso.getTalla());
            }

            if (!ObjectUtils.isEmpty(evaluacionMedicaProgreso.getPeso())) {
                evaluacionMedicaProgresoDTO.setPeso(evaluacionMedicaProgreso.getPeso());
            }

            if (!ObjectUtils.isEmpty(evaluacionMedicaProgreso.getImc())) {
                evaluacionMedicaProgresoDTO.setImc(evaluacionMedicaProgreso.getImc());
            }

            if (!ObjectUtils.isEmpty(evaluacionMedicaProgreso.getImpresionDiagnostico())) {
                evaluacionMedicaProgresoDTO.setImpresionDiagnostico(evaluacionMedicaProgreso.getImpresionDiagnostico());
            }

            if (!ObjectUtils.isEmpty(evaluacionMedicaProgreso.getManejoTerapeutico())) {
                evaluacionMedicaProgresoDTO.setManejoTerapeutico(evaluacionMedicaProgreso.getManejoTerapeutico());
            }

            if (!ObjectUtils.isEmpty(evaluacionMedicaProgreso.getClinicamenteSano())) {
                evaluacionMedicaProgresoDTO.setClinicamenteSano(evaluacionMedicaProgreso.getClinicamenteSano());
            }

            if (!ObjectUtils.isEmpty(evaluacionMedicaProgreso.getEnfermo())) {
                evaluacionMedicaProgresoDTO.setEnfermo(evaluacionMedicaProgreso.getEnfermo());
            }

            List<CriterioEvaluacionMedicaProgreso> listadoCriterios = this.criterioEvaluacionMedicaProgresoRepository.findByEvaluacionMedicaProgreso_TokenIdentificadorAndRemovido(tokenIdFichaIdentificacion,false);
            ArrayList<CriterioEvaluacionMedicaProgresoDTO> criteriosDTO = new ArrayList<>();

            if(!listadoCriterios.isEmpty()){
                for(CriterioEvaluacionMedicaProgreso criterio: listadoCriterios){
                    CriterioEvaluacionMedicaProgresoDTO dto = new CriterioEvaluacionMedicaProgresoDTO();
                    if(!ObjectUtils.isEmpty(criterio.getPresente())){
                        dto.setPresente(criterio.getPresente());
                    }
                    dto.setCriterioPadre(catalogoToDTO(criterio.getTipoSignoAlteracion()));
                    dto.setCriterioHijo(catalogoToDTO(criterio.getTipoSignoAlteracionHijo()));
                    if(!ObjectUtils.isEmpty(criterio.getLadoSigno())){
                        dto.setLadoSigno(catalogoToDTO(criterio.getLadoSigno()));
                    }
                    if(!ObjectUtils.isEmpty(criterio.getUbicacionSigno())){
                        dto.setUbiacionSigno(catalogoToDTO(criterio.getUbicacionSigno()));
                    }
                    if(!ObjectUtils.isEmpty(criterio.getDetalle())){
                        dto.setDetalle(criterio.getDetalle());
                    }
                    dto.setTokenIdentificador(criterio.getTokenIdentificador());
                    criteriosDTO.add(dto);
                }
            }

            evaluacionMedicaProgresoDTO.setCriteriosEvaluacionProgresoAsociados(criteriosDTO);

            df.llenarRespuestaExitosa("Evaluacion médica progreso obtenida con éxito. ", evaluacionMedicaProgresoDTO);

        }catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionMedicaProgresoDTO>> getEvaluacionMedicaProgresoByIdFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionMedicaProgresoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try{

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            String tokenIdFichaMedica = paginacionRequest.getTokenIdentificador();

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize()
            );

            Page<EvaluacionMedicaProgreso> evaluacionMedicaPage = this.evaluacionMedicaProgresoRepository.findByFichaMedica_TokenIdentificadorAndRemovido(tokenIdFichaMedica, false, pageable);
            PaginacionResponse<EvaluacionMedicaProgresoDTO> paginacionResponse = new PaginacionResponse<>();

            List<EvaluacionMedicaProgresoDTO> evaluacionMedicaDTOS = evaluacionMedicaPage.stream().map(
                    evaluacionMedicaProgreso -> {
                        EvaluacionMedicaProgresoDTO dto = new EvaluacionMedicaProgresoDTO();
                        dto.setEstadoNutricional(catalogoToDTO(evaluacionMedicaProgreso.getEstadoNutricional()));
                        dto.setTokenIdFichaMedica(evaluacionMedicaProgreso.getFichaMedica().getTokenIdentificador());
                        dto.setFecha(evaluacionMedicaProgreso.getFecha());
                        dto.setTokenIdentificador(evaluacionMedicaProgreso.getTokenIdentificador());
                        dto.setTipoEvaluacionProgreso(catalogoToDTO(evaluacionMedicaProgreso.getTipoEvaluacionProgreso()));

                        return dto;
                    }
            ).toList();

            paginacionResponse.setData(evaluacionMedicaDTOS);
            paginacionResponse.setTotalItems(evaluacionMedicaPage.getTotalElements());

            df.llenarRespuestaExitosa("Evaluaciones médica progresos obtenidas con éxito", paginacionResponse);

        }catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;

    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> postEvaluacionMedicaProgreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            EvaluacionMedicaProgresoDTO evaluacionMedicaDTO = new Gson().fromJson(body, EvaluacionMedicaProgresoDTO.class);

            String ip = httpServletRequest.getRemoteAddr();
            Date fecha = new Date();

            EvaluacionMedicaProgreso evaluacionMedicaProgreso = new EvaluacionMedicaProgreso();
            evaluacionMedicaProgreso.setFichaMedica(this.fichaMedicaRepository.findByTokenIdentificadorAndRemovido(evaluacionMedicaDTO.getTokenIdFichaMedica(), false));

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getEstadoNutricional())){
                evaluacionMedicaProgreso.setEstadoNutricional(dtoToCatalogo(evaluacionMedicaDTO.getEstadoNutricional()));
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getTipoEvaluacionProgreso())){
                evaluacionMedicaProgreso.setTipoEvaluacionProgreso(dtoToCatalogo(evaluacionMedicaDTO.getTipoEvaluacionProgreso()));
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getTipoDesnutricion())){
                evaluacionMedicaProgreso.setTipoDesnutricion(dtoToCatalogo(evaluacionMedicaDTO.getTipoDesnutricion()));
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getGrado())){
                evaluacionMedicaProgreso.setGrado(evaluacionMedicaDTO.getGrado());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getTalla())){
                evaluacionMedicaProgreso.setTalla(evaluacionMedicaDTO.getTalla());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getPeso())){
                evaluacionMedicaProgreso.setPeso(evaluacionMedicaDTO.getPeso());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getImc())){
                evaluacionMedicaProgreso.setImc(evaluacionMedicaDTO.getImc());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getImpresionDiagnostico())){
                evaluacionMedicaProgreso.setImpresionDiagnostico(evaluacionMedicaDTO.getImpresionDiagnostico());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getManejoTerapeutico())){
                evaluacionMedicaProgreso.setManejoTerapeutico(evaluacionMedicaDTO.getManejoTerapeutico());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getClinicamenteSano())){
                evaluacionMedicaProgreso.setClinicamenteSano(evaluacionMedicaDTO.getClinicamenteSano());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getEnfermo())){
                evaluacionMedicaProgreso.setEnfermo(evaluacionMedicaDTO.getEnfermo());
            }

            evaluacionMedicaProgreso.setFecha(new Date());
            evaluacionMedicaProgreso.setIpEdita(ip);
            evaluacionMedicaProgreso.setFechaEdicion(fecha);
            evaluacionMedicaProgreso.setUsuarioSistemaEdita(usuarioSistema);

            this.evaluacionMedicaProgresoRepository.save(evaluacionMedicaProgreso);

            for(CriterioEvaluacionMedicaProgresoDTO criterio: evaluacionMedicaDTO.getCriteriosEvaluacionProgresoAsociados()){
                CriterioEvaluacionMedicaProgreso criterioEva = null;
                if(ObjectUtils.isEmpty(criterio.getTokenIdentificador())){
                    criterioEva = new CriterioEvaluacionMedicaProgreso();
                    if(!ObjectUtils.isEmpty(criterio.getPresente())){
                        criterioEva.setPresente(criterio.getPresente());
                    }
                    criterioEva.setTipoSignoAlteracion(dtoToCatalogo(criterio.getCriterioPadre()));
                    criterioEva.setTipoSignoAlteracionHijo(dtoToCatalogo(criterio.getCriterioHijo()));
                    if(!ObjectUtils.isEmpty(criterio.getUbiacionSigno())){
                        criterioEva.setUbicacionSigno(dtoToCatalogo(criterio.getUbiacionSigno()));
                    }
                    if(!ObjectUtils.isEmpty(criterio.getLadoSigno())){
                        criterioEva.setLadoSigno(dtoToCatalogo(criterio.getLadoSigno()));
                    }
                    if(!ObjectUtils.isEmpty(criterio.getDetalle())){
                        criterioEva.setDetalle(criterio.getDetalle());
                    }

                    criterioEva.setEvaluacionMedicaProgreso(evaluacionMedicaProgreso);
                    this.criterioEvaluacionMedicaProgresoRepository.save(criterioEva);
                }
            }

            String nemonicoEvaluacionMedicaProgreso = EtiquetaNemonico.CARPETA_GESTION_EVALUACION_MEDICA_PROGRESO;
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaEvaluacionProgreso = this.fichaIdentificacionCarpetaRepository.
                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(evaluacionMedicaDTO.getTokenIdentificadorFichaIdentificacion(), nemonicoEvaluacionMedicaProgreso, false);

            if (fichaIdentificacionCarpetaEvaluacionProgreso == null) {
                FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.
                        findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(evaluacionMedicaDTO.getTokenIdentificadorFichaIdentificacion(), null, false);
                Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();
                FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(evaluacionMedicaDTO.getTokenIdentificadorFichaIdentificacion(), false);

                String nombreCarpetaPrincipal = "Evaluacion Medica Progreso";

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpetaPrincipal);
                carpetaDTO.setDescripcion("Carpeta de evaluacion progreso medico");
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadrePrincipal.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardadaRecientemente = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                fichaIdentificacionCarpetaEvaluacionProgreso = new FichaIdentificacionCarpeta();
                fichaIdentificacionCarpetaEvaluacionProgreso.setCarpeta(carpetaGuardadaRecientemente);
                fichaIdentificacionCarpetaEvaluacionProgreso.setFichaIdentificacion(ficha);
                Catalogo catalogoTipoGestionAdolescente = this.catalogoRepository.findByNemonicoAndRemovido(nemonicoEvaluacionMedicaProgreso, false);
                fichaIdentificacionCarpetaEvaluacionProgreso.setTipoDeGestionDeAdolescente(catalogoTipoGestionAdolescente);
                fichaIdentificacionCarpetaEvaluacionProgreso.setFechaCreacion(new Date());
                fichaIdentificacionCarpetaEvaluacionProgreso.setIpCrea(httpServletRequest.getRemoteAddr());
                fichaIdentificacionCarpetaEvaluacionProgreso.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                this.fichaIdentificacionCarpetaRepository.save(fichaIdentificacionCarpetaEvaluacionProgreso);
            }

            // CREACIÓN DE CARPETA FICHA INGRESO

            String nemonico = EtiquetaNemonico.CARPETA_GESTION_EVALUACION_MEDICA_PROGRESO;
            FichaIdentificacionCarpeta fichaIdentificacionCarpeta = this.fichaIdentificacionCarpetaRepository.
                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido
                            (evaluacionMedicaDTO.getTokenIdentificadorFichaIdentificacion(), nemonico, false);
            Carpeta carpetaPadreIngreso= fichaIdentificacionCarpeta.getCarpeta();

            EvaluacionMedicaProgresoCarpeta evaluacionMedicaProgresoCarpeta = this.evaluacionMedicaProgresoCarpetaRepository.
                    findFirstByEvaluacionMedicaProgresoTokenIdentificadorAndRemovido(evaluacionMedicaProgreso.getTokenIdentificador(), false);

            if (evaluacionMedicaProgresoCarpeta == null) {

                String nombreCarpeta = "eva_med_prog" +evaluacionMedicaProgreso.getTokenIdentificador();

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpeta);
                carpetaDTO.setDescripcion("Carpeta de evaluacion medica progreso relacionado a: " + evaluacionMedicaProgreso.getTokenIdentificador());
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaPadreIngreso.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                evaluacionMedicaProgresoCarpeta = new EvaluacionMedicaProgresoCarpeta();
                evaluacionMedicaProgresoCarpeta.setCarpeta(carpetaGuardada);
                evaluacionMedicaProgresoCarpeta.setEvaluacionMedicaProgreso(evaluacionMedicaProgreso);
                evaluacionMedicaProgresoCarpeta.setFechaCreacion(new Date());
                evaluacionMedicaProgresoCarpeta.setIpCrea(httpServletRequest.getRemoteAddr());
                evaluacionMedicaProgresoCarpeta.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                evaluacionMedicaProgresoCarpeta.setRemovido(false);
                this.evaluacionMedicaProgresoCarpetaRepository.save(evaluacionMedicaProgresoCarpeta);
            }

            df.llenarRespuestaExitosa("Evaluacion médica progreso creada con éxito. ", evaluacionMedicaDTO);

        }catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> updateEvaluacionMedicaProgreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EvaluacionMedicaProgresoDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            EvaluacionMedicaProgresoDTO evaluacionMedicaDTO = new Gson().fromJson(body, EvaluacionMedicaProgresoDTO.class);

            String ip = httpServletRequest.getRemoteAddr();
            Date fecha = new Date();

            EvaluacionMedicaProgreso evaluacionMedicaProgreso = this.evaluacionMedicaProgresoRepository.
                    findByTokenIdentificadorAndRemovido(evaluacionMedicaDTO.getTokenIdentificador(), false);

            if(evaluacionMedicaProgreso == null){
                df.setMensaje("La evaluacion médica con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getEstadoNutricional())){
                evaluacionMedicaProgreso.setEstadoNutricional(dtoToCatalogo(evaluacionMedicaDTO.getEstadoNutricional()));
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getTipoEvaluacionProgreso())){
                evaluacionMedicaProgreso.setTipoEvaluacionProgreso(dtoToCatalogo(evaluacionMedicaDTO.getTipoEvaluacionProgreso()));
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getTipoDesnutricion())){
                evaluacionMedicaProgreso.setTipoDesnutricion(dtoToCatalogo(evaluacionMedicaDTO.getTipoDesnutricion()));
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getGrado())){
                evaluacionMedicaProgreso.setGrado(evaluacionMedicaDTO.getGrado());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getTalla())){
                evaluacionMedicaProgreso.setTalla(evaluacionMedicaDTO.getTalla());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getPeso())){
                evaluacionMedicaProgreso.setPeso(evaluacionMedicaDTO.getPeso());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getImc())){
                evaluacionMedicaProgreso.setImc(evaluacionMedicaDTO.getImc());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getImpresionDiagnostico())){
                evaluacionMedicaProgreso.setImpresionDiagnostico(evaluacionMedicaDTO.getImpresionDiagnostico());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getManejoTerapeutico())){
                evaluacionMedicaProgreso.setManejoTerapeutico(evaluacionMedicaDTO.getManejoTerapeutico());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getClinicamenteSano())){
                evaluacionMedicaProgreso.setClinicamenteSano(evaluacionMedicaDTO.getClinicamenteSano());
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getEnfermo())){
                evaluacionMedicaProgreso.setEnfermo(evaluacionMedicaDTO.getEnfermo());
            }

            evaluacionMedicaProgreso.setIpEdita(ip);
            evaluacionMedicaProgreso.setFechaEdicion(fecha);
            evaluacionMedicaProgreso.setUsuarioSistemaEdita(usuarioSistema);
            evaluacionMedicaProgreso.setFecha(new Date());

            this.evaluacionMedicaProgresoRepository.save(evaluacionMedicaProgreso);

            for(CriterioEvaluacionMedicaProgresoDTO criterio: evaluacionMedicaDTO.getCriteriosEvaluacionProgresoAsociados()){
                CriterioEvaluacionMedicaProgreso criterioEva = null;
                if(ObjectUtils.isEmpty(criterio.getTokenIdentificador())){
                    criterioEva = new CriterioEvaluacionMedicaProgreso();
                    if(!ObjectUtils.isEmpty(criterio.getPresente())){
                        criterioEva.setPresente(criterio.getPresente());
                    }

                    criterioEva.setTipoSignoAlteracion(dtoToCatalogo(criterio.getCriterioPadre()));
                    criterioEva.setTipoSignoAlteracionHijo(dtoToCatalogo(criterio.getCriterioHijo()));
                    criterioEva.setLadoSigno(dtoToCatalogo(criterio.getLadoSigno()));
                    criterioEva.setUbicacionSigno(dtoToCatalogo(criterio.getUbiacionSigno()));
                    if(!ObjectUtils.isEmpty(criterio.getDetalle())){
                        criterioEva.setDetalle(criterio.getDetalle());
                    }
                    criterioEva.setEvaluacionMedicaProgreso(evaluacionMedicaProgreso);
                    this.criterioEvaluacionMedicaProgresoRepository.save(criterioEva);
                }else{
                    criterioEva = this.criterioEvaluacionMedicaProgresoRepository.findByTokenIdentificadorAndRemovido(criterio.getTokenIdentificador(),false);
                    criterioEva.setTipoSignoAlteracionHijo(dtoToCatalogo(criterio.getCriterioHijo()));
                    criterioEva.setLadoSigno(dtoToCatalogo(criterio.getLadoSigno()));
                    criterioEva.setUbicacionSigno(dtoToCatalogo(criterio.getUbiacionSigno()));
                    if(!ObjectUtils.isEmpty(criterio.getPresente())){
                        criterioEva.setPresente(criterio.getPresente());
                    }
                    if(!ObjectUtils.isEmpty(criterio.getDetalle())){
                        criterioEva.setDetalle(criterio.getDetalle());
                    }
                    this.criterioEvaluacionMedicaProgresoRepository.save(criterioEva);
                }
            }

            if(!ObjectUtils.isEmpty(evaluacionMedicaDTO.getTokensCriteriosEliminar())){
                for(String token: evaluacionMedicaDTO.getTokensCriteriosEliminar()){
                    CriterioEvaluacionMedicaProgreso criterioEva = this.criterioEvaluacionMedicaProgresoRepository.findByTokenIdentificadorAndRemovido(token,false);
                    criterioEva.setRemovido(true);
                    this.criterioEvaluacionMedicaProgresoRepository.save(criterioEva);
                }
            }

            df.llenarRespuestaExitosa("Evaluacion médica progreso creada con éxito. ", evaluacionMedicaDTO);
        }catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> deleteEvaluacionMedicaProgreso(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            EvaluacionMedicaProgresoDTO evaluacionMedicaDTO = new Gson().fromJson(body, EvaluacionMedicaProgresoDTO.class);

            String ip = httpServletRequest.getRemoteAddr();
            Date fecha = new Date();

            EvaluacionMedicaProgreso evaluacionMedicaProgreso = this.evaluacionMedicaProgresoRepository.
                    findByTokenIdentificadorAndRemovido(evaluacionMedicaDTO.getTokenIdentificador(), false);

            if (evaluacionMedicaProgreso == null) {
                df.setMensaje("La evaluación médica con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            evaluacionMedicaProgreso.setIpElimina(ip);
            evaluacionMedicaProgreso.setFechaEliminacion(fecha);
            evaluacionMedicaProgreso.setUsuarioSistemaElimina(usuarioSistema);

            evaluacionMedicaProgreso.setRemovido(true);

            this.evaluacionMedicaProgresoRepository.save(evaluacionMedicaProgreso);

            df.llenarRespuestaExitosa("Evaluación médica progreso eliminado con exito", evaluacionMedicaProgreso.getRemovido());

        }catch (Exception ex){
            df.llenarConDatosDeException(ex);
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
        catalogoDTO.setIdCatalogo(catalogo.getIdCatalogo());
        catalogoDTO.setTokenIdentificadorEmpresa(catalogo.getEmpresa().getTokenIdentificador());

        return catalogoDTO;
    }

    private Catalogo dtoToCatalogo(CatalogoDTO catalogoDTO){
        if (catalogoDTO == null) {
            return null;
        }

        Catalogo catalogo = this.catalogoRepository.findByTokenIdentificadorAndRemovido(catalogoDTO.getTokenIdentificador(), false);

        return catalogo;
    }
}
