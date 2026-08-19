package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.IntervencionDiferenciadaCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.PlanTratamientoIndInterv;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.IntervencionDiferenciadaCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.PlanTratamientoIndIntervRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PlanTratamientoIndIntervServiceImpl implements PlanTratamientoIndIntervService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private PlanTratamientoIndIntervRepository planRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private CarpetaRepository carpetaRepository;
    private CarpetaService carpetaService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private IntervencionDiferenciadaCarpetaRepository intervencionDiferenciadaCarpetaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> getByTokenIdentificador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            String idPlanTratIndInterv = df22.getData();
            String tokenIdentificador = new Gson().fromJson(idPlanTratIndInterv, String.class);
            PlanTratamientoIndInterv planEntity = planRepository.findByIdPlanTratIndIntervAndRemovido(Long.parseLong(tokenIdentificador), false);

            if (planEntity == null) {
                df.setMensaje("No existe el documento solicitado.");
                return df;
            }

            PlanTratamientoIndIntervDTO planDTO = new PlanTratamientoIndIntervDTO();
            planDTO.setIdPlanTratIndInterv(planEntity.getIdPlanTratIndInterv());
            if (planEntity.getDimension() != null) planDTO.setDimension(entidadADtoCatalogo(planEntity.getDimension()));
            planDTO.setObjetivo(planEntity.getObjetivo());
            planDTO.setActividadPrograma(planEntity.getActividadPrograma());
            planDTO.setEquipoResponsable(planEntity.getEquipoResponsable());
            planDTO.setTiempoEstimado(planEntity.getTiempoEstimado());
            planDTO.setNumAtencionIndividual(planEntity.getNumAtencionIndividual());
            planDTO.setNumAtencionGrupal(planEntity.getNumAtencionGrupal());
            planDTO.setLugar(planEntity.getLugar());
            if (planEntity.getModalidad() != null) planDTO.setModalidad(entidadADtoCatalogo(planEntity.getModalidad()));
            if (planEntity.getFrecuencia() != null) planDTO.setFrecuencia(entidadADtoCatalogo(planEntity.getFrecuencia()));
            planDTO.setDescripcion(planEntity.getDescripcion());
            planDTO.setTokenIdentificador(planEntity.getTokenIdentificador());
            if (!ObjectUtils.isEmpty(planEntity.getFrecuencia())) {
                planDTO.setFrecuencia(entidadADtoCatalogo(planEntity.getFrecuencia()));
            }
            if (!ObjectUtils.isEmpty(planEntity.getFechaInicio())) {
                planDTO.setFechaInicio(planEntity.getFechaInicio());
            }
            if (!ObjectUtils.isEmpty(planEntity.getFechaFin())) {
                planDTO.setFechaFin(planEntity.getFechaFin());
            }

            df.llenarRespuestaExitosa("Se ha encontrado el documento: " + planDTO.getIdPlanTratIndInterv(), planDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;

    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> updatePlanTratamientoIndInterv(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PlanTratamientoIndIntervDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            PlanTratamientoIndIntervDTO planDTO = new Gson().fromJson(bodyString, PlanTratamientoIndIntervDTO.class);

            Optional<PlanTratamientoIndInterv> optionalPlan = planRepository.findByIdPlanTratIndInterv(planDTO.getIdPlanTratIndInterv());

            if (!optionalPlan.isPresent()) {
                df.setMensaje("No existe la intervencion solicitada.");
                return df;
            }

            PlanTratamientoIndInterv planEntity = optionalPlan.get();

            planEntity.setFrecuencia(this.catalogoRepository.findByTokenIdentificadorAndRemovido(planDTO.getFrecuencia().getTokenIdentificador(), false));
            planEntity.setFechaInicio(planDTO.getFechaInicio());
            planEntity.setFechaFin(planDTO.getFechaFin());
            planEntity.setUsuarioSistemaEdita(usuarioSistema);
            planEntity.setIpEdita(ip);
            planEntity.setFechaEdicion(new Date());

            planRepository.save(planEntity);

            FichaIdentificacion ficha = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(planDTO.getTokenFichaIdentificacion(), false);

            //CREACION DE CARPETA INTERVENCION
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaPrincipal = this.fichaIdentificacionCarpetaRepository.
                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(planDTO.getTokenFichaIdentificacion(), null, false);
            Carpeta carpetaPadrePrincipal = fichaIdentificacionCarpetaPrincipal.getCarpeta();

            String nemonicoPertenencia = EtiquetaNemonico.CARPETA_GESTION_ADOLES_INTERVENCION_DIF;
            FichaIdentificacionCarpeta fichaIdentificacionCarpetaPertenencia = this.fichaIdentificacionCarpetaRepository.
                    findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(planDTO.getTokenFichaIdentificacion(), nemonicoPertenencia, false);

            if (fichaIdentificacionCarpetaPertenencia == null) {
                String nombreCarpetaPrincipal = "Intervenciones";

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

            IntervencionDiferenciadaCarpeta carpetaEncontrada = this.intervencionDiferenciadaCarpetaRepository.
                    findFirstByPlanTratamientoIndIntervIdPlanTratIndIntervAndRemovido(planDTO.getIdPlanTratIndInterv().longValue(), false);

            if (carpetaEncontrada == null) {

                String nemonico = EtiquetaNemonico.CARPETA_GESTION_ADOLES_INTERVENCION_DIF;
                FichaIdentificacionCarpeta fichaIdentificacionCarpeta = this.fichaIdentificacionCarpetaRepository.
                        findFirstByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(planDTO.getTokenFichaIdentificacion(), nemonico, false);

                Carpeta carpetaIntervencion = fichaIdentificacionCarpeta.getCarpeta();

                String nombreCarpeta = "interv_dif_" + planEntity.getIdPlanTratIndInterv();

                CarpetaDTO carpetaDTO = new CarpetaDTO();
                carpetaDTO.setNombreCliente(nombreCarpeta);
                carpetaDTO.setDescripcion("Carpeta de intervencion diferenciada a: " + planEntity.getIdPlanTratIndInterv());
                CarpetaDTO carpetaPadreDTO = new CarpetaDTO();
                carpetaPadreDTO.setTokenIdentificador(carpetaIntervencion.getTokenIdentificador());
                carpetaDTO.setCarpetaDTOPadre(carpetaPadreDTO);

                this.carpetaService.crearCarpeta(httpServletRequest, true, carpetaDTO);

                Carpeta carpetaGuardada = this.carpetaRepository.findByTokenIdentificadorAndRemovido(carpetaDTO.getTokenIdentificador(), false);

                IntervencionDiferenciadaCarpeta carpetaDetalle = new IntervencionDiferenciadaCarpeta();
                carpetaDetalle.setCarpeta(carpetaGuardada);
                carpetaDetalle.setPlanTratamientoIndInterv(planEntity);
                carpetaDetalle.setFechaCreacion(new Date());
                carpetaDetalle.setIpCrea(httpServletRequest.getRemoteAddr());
                carpetaDetalle.setUsuarioSistemaCrea(df2.getData().getUsuarioSistema());
                this.intervencionDiferenciadaCarpetaRepository.save(carpetaDetalle);

            }


            df.llenarRespuestaExitosa("Se ha actualizado la intervencion: " + planDTO.getIdPlanTratIndInterv(), planDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
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
