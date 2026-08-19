package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaMedicaEnfermedad;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionada;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionadaEnfermedad;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.*;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.FichaMedicaEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.PersonaRelacionadaEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.FichaMedicaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico.PersonaRelacionadaEnfermedadRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.FichaMedicaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ClasificacionEnfermedadRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaMedicaEnfermedadRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.PersonaRelacionadaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class FichaMedicaServiceImpl implements FichaMedicaService {

    private JwtProviderService jwtProviderService;
    private FichaMedicaRepository fichaMedicaRepository;
    private CatalogoRepository catalogoRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private PersonaRelacionadaEnfermedadRepository personaRelacionadaEnfermedadRepository;
    private PersonaRelacionadaRepository personaRelacionadaRepository;
    private FichaMedicaEnfermedadRepository fichaMedicaEnfermedadRepository;
    private ClasificacionEnfermedadRepository clasificacionEnfermedadRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private static final String ORDENAR_POR_NOMBRE = "nombre";

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FichaMedicaDTO>>getFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaMedicaDTO>> df = new RespuestaPorDefectoAuditoria<>();
        try{
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by(ORDENAR_POR_NOMBRE).ascending()
            );

            Page<FichaMedica> fichaMedicaPage = this.fichaMedicaRepository.findByRemovido(false, pageable);
            PaginacionResponse<FichaMedicaDTO> paginacionResponse = new PaginacionResponse<>();

            List<FichaMedicaDTO> fichasDTOs = fichaMedicaPage.toList().stream()
                    .map(ficha -> {
                        FichaMedicaDTO dto = new FichaMedicaDTO();
                        dto.setTokenIdentificador(ficha.getTokenIdentificador());
                        dto.setTokenIdFichaIdentificacion(ficha.getFichaIdentificacion().getTokenIdentificador());
                        dto.setEstadoSalud(ficha.getEstadoSalud());
                        dto.setLesiones(ficha.getLesiones());
                        dto.setEnfermedades(ficha.getEnfermedades());
                        dto.setMedicamentos(ficha.getMedicamentos());
                        dto.setSeguroMedico(ficha.getSeguroMedico());
                        dto.setInstitucionAcude(ficha.getInstitucionAcude());
                        dto.setInternadoHospital(ficha.getInternadoHospital());
                        dto.setTipoSangre(catalogoToDTO(ficha.getTipoSangre()));
                        return dto;
                    })
                    .toList();

            paginacionResponse.setData(fichasDTOs);
            paginacionResponse.setTotalItems(fichaMedicaPage.getTotalElements());

            df.llenarRespuestaExitosa("Fichas médicas obtenidas con éxito. ", paginacionResponse);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaMedicaDTO> getFichaMedicaByIdFichaIdentificacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<FichaMedicaDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            String tokenIdFichaIdentificacion = new Gson().fromJson(body, String.class);

           FichaMedica ficha = this.fichaMedicaRepository.findByFichaIdentificacion_TokenIdentificadorAndRemovido(tokenIdFichaIdentificacion,  false);

           if(ficha == null){
               df.setMensaje("No existe una ficha médica asociada al token proporcionado");
               df.setExito(true);
               df.setData(new FichaMedicaDTO());
               return df;
           }

            FichaMedicaDTO dto = new FichaMedicaDTO();
            dto.setTokenIdentificador(ficha.getTokenIdentificador());
            dto.setTokenIdFichaIdentificacion(ficha.getFichaIdentificacion().getTokenIdentificador());
            dto.setEstadoSalud(ficha.getEstadoSalud());
            dto.setLesiones(ficha.getLesiones());
            dto.setEnfermedades(ficha.getEnfermedades());
            dto.setMedicamentos(ficha.getMedicamentos());
            dto.setSeguroMedico(ficha.getSeguroMedico());
            dto.setInstitucionAcude(ficha.getInstitucionAcude());
            dto.setInternadoHospital(ficha.getInternadoHospital());
            dto.setTipoSangre(catalogoToDTO(ficha.getTipoSangre()));

            if (!ObjectUtils.isEmpty(ficha.getLesiones())) {
                dto.setLesiones(ficha.getLesiones());
            }

            if (!ObjectUtils.isEmpty(ficha.getEnfermedades())) {
                dto.setEnfermedades(ficha.getEnfermedades());
            }

            if (!ObjectUtils.isEmpty(ficha.getMedicamentos())) {
                dto.setMedicamentos(ficha.getMedicamentos());
            }

            if (!ObjectUtils.isEmpty(ficha.getSeguroMedico())) {
                dto.setSeguroMedico(ficha.getSeguroMedico());
            }

            if (!ObjectUtils.isEmpty(ficha.getInstitucionAcude())) {
                dto.setInstitucionAcude(ficha.getInstitucionAcude());
            }

            if (!ObjectUtils.isEmpty(ficha.getInternadoHospital())) {
                dto.setInternadoHospital(ficha.getInternadoHospital());
            }

            if (!ObjectUtils.isEmpty(ficha.getAlergiaMedicamentos())) {
                dto.setAlergiaMedicamentos(ficha.getAlergiaMedicamentos());
            }

            if (!ObjectUtils.isEmpty(ficha.getMedicamentosAlergicos())) {
                dto.setMedicamentosAlergicos(ficha.getMedicamentosAlergicos());
            }

            if (!ObjectUtils.isEmpty(ficha.getAlergiaAlimentos())) {
                dto.setAlergiaAlimentos(ficha.getAlergiaAlimentos());
            }

            if (!ObjectUtils.isEmpty(ficha.getDetalleAlergiasAlimentos())) {
                dto.setDetalleAlergiasAlimentos(ficha.getDetalleAlergiasAlimentos());
            }

            if (!ObjectUtils.isEmpty(ficha.getCirugiaQuirurgica())) {
                dto.setCirugiaQuirurgica(ficha.getCirugiaQuirurgica());
            }

            if (!ObjectUtils.isEmpty(ficha.getDetalleCirugias())) {
                dto.setDetalleCirugias(ficha.getDetalleCirugias());
            }

            if (!ObjectUtils.isEmpty(ficha.getFracturas())) {
                dto.setFracturas(ficha.getFracturas());
            }

            if (!ObjectUtils.isEmpty(ficha.getDetalleFracturas())) {
                dto.setDetalleFracturas(ficha.getDetalleFracturas());
            }

            if (!ObjectUtils.isEmpty(ficha.getIRS())) {
                dto.setIrs(ficha.getIRS());
            }

            if (!ObjectUtils.isEmpty(ficha.getUsoDePreservativo())) {
                dto.setUsoDePreservativo(ficha.getUsoDePreservativo());
            }

            if (!ObjectUtils.isEmpty(ficha.getGenero())) {
                dto.setRelacionGenero(ficha.getGenero().getTokenIdentificador());
            }

            if (!ObjectUtils.isEmpty(ficha.getICD())) {
                dto.setIcd(ficha.getICD());
            }

            if (!ObjectUtils.isEmpty(ficha.getHabitosNocivos())) {
                dto.setHabitosNocivos(ficha.getHabitosNocivos());
            }

            if (!ObjectUtils.isEmpty(ficha.getTabaco())) {
                dto.setTabaco(ficha.getTabaco());
            }

            if (!ObjectUtils.isEmpty(ficha.getTomaAlcohol())) {
                dto.setTomaAlcohol(ficha.getTomaAlcohol());
            }

            if (!ObjectUtils.isEmpty(ficha.getEdadAlcohol())) {
                dto.setEdadAlcohol(ficha.getEdadAlcohol());
            }

            if (!ObjectUtils.isEmpty(ficha.getEdadTabaco())) {
                dto.setEdadTabaco(ficha.getEdadTabaco());
            }

            if (!ObjectUtils.isEmpty(ficha.getPresion())) {
                dto.setPresion(ficha.getPresion());
            }

            if (!ObjectUtils.isEmpty(ficha.getTalla())) {
                dto.setTalla(ficha.getTalla());
            }

            if (!ObjectUtils.isEmpty(ficha.getAspectoGeneralFisico())) {
                dto.setAspectoGeneralFisico(ficha.getAspectoGeneralFisico());
            }

            if (!ObjectUtils.isEmpty(ficha.getInspeccion())) {
                dto.setInspeccion(ficha.getInspeccion());
            }

            if (!ObjectUtils.isEmpty(ficha.getPielFaneras())) {
                dto.setPielFaneras(ficha.getPielFaneras());
            }

            if (!ObjectUtils.isEmpty(ficha.getPeso())) {
                dto.setPeso(ficha.getPeso());
            }

            if (!ObjectUtils.isEmpty(ficha.getSaturacionOxigeno())) {
                dto.setSaturacionOxigeno(ficha.getSaturacionOxigeno());
            }

            if (!ObjectUtils.isEmpty(ficha.getIndiceMasaCorporal())) {
                dto.setIndiceMasaCorporal(ficha.getIndiceMasaCorporal());
            }

            if (!ObjectUtils.isEmpty(ficha.getDrogaInicio())) {
                dto.setDrogaInicio(ficha.getDrogaInicio());
            }

            if (!ObjectUtils.isEmpty(ficha.getCabezaDetalle())) {
                dto.setCabezaDetalle(ficha.getCabezaDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getOjosDetalle())) {
                dto.setOjosDetalle(ficha.getOjosDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getOidoDetalle())) {
                dto.setOidoDetalle(ficha.getOidoDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getNarizDetalle())) {
                dto.setNarizDetalle(ficha.getNarizDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getBocaDetalle())) {
                dto.setBocaDetalle(ficha.getBocaDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getOrofaringeDetalle())) {
                dto.setOrofaringeDetalle(ficha.getOrofaringeDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getCorazonDetalle())) {
                dto.setCorazonDetalle(ficha.getCorazonDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getPulmonesDetalle())) {
                dto.setPulmonesDetalle(ficha.getPulmonesDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getAbdomenDetalle())) {
                dto.setAbdomenDetalle(ficha.getAbdomenDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getUrinarioDetalle())) {
                dto.setUrinarioDetalle(ficha.getUrinarioDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getPplDetalle())) {
                dto.setPplDetalle(ficha.getPplDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getPruDetalle())) {
                dto.setPruDetalle(ficha.getPruDetalle());
            }

            if (!ObjectUtils.isEmpty(ficha.getImpresionDiagnostico())) {
                dto.setImpresionDiagnostico(ficha.getImpresionDiagnostico());
            }


            df.llenarRespuestaExitosa("Ficha médica obtenida con éxito. ", dto);
        } catch (Exception ex){
            df.setData(null);
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<FichaMedicaDTO> postFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<FichaMedicaDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            FichaMedicaDTO fichaMedicaDTO = new Gson().fromJson(body, FichaMedicaDTO.class);

            String ip = httpServletRequest.getRemoteAddr();
            Date fecha = new Date();
            FichaMedica fichaMedicaDb = null;

            fichaMedicaDb = this.fichaMedicaRepository.encontrarFichaMedicaPorFichaIdentificacion(fichaMedicaDTO.getTokenIdFichaIdentificacion());
            if(fichaMedicaDb==null){
                fichaMedicaDb = new FichaMedica();
                fichaMedicaDb.setFichaIdentificacion(this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(fichaMedicaDTO.getTokenIdFichaIdentificacion(), false));

            }

//            fichaMedicaDb.setLesiones(fichaMedicaDTO.getLesiones());
//            fichaMedicaDb.setEnfermedades(fichaMedicaDTO.getEnfermedades());
//            fichaMedicaDb.setMedicamentos(fichaMedicaDTO.getMedicamentos());
//            fichaMedicaDb.setSeguroMedico(fichaMedicaDTO.getSeguroMedico());
//            fichaMedicaDb.setInstitucionAcude(fichaMedicaDTO.getInstitucionAcude());
//            fichaMedicaDb.setInternadoHospital(fichaMedicaDTO.getInternadoHospital());
//            fichaMedicaDb.setTipoSangre(dtoToCatalogo(fichaMedicaDTO.getTipoSangre()));
            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getLesiones())) {
                fichaMedicaDb.setLesiones(fichaMedicaDTO.getLesiones());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getEstadoSalud())) {
                fichaMedicaDb.setEstadoSalud(fichaMedicaDTO.getEstadoSalud());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getEnfermedades())) {
                fichaMedicaDb.setEnfermedades(fichaMedicaDTO.getEnfermedades());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getMedicamentos())) {
                fichaMedicaDb.setMedicamentos(fichaMedicaDTO.getMedicamentos());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getSeguroMedico())) {
                fichaMedicaDb.setSeguroMedico(fichaMedicaDTO.getSeguroMedico());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getInstitucionAcude())) {
                fichaMedicaDb.setInstitucionAcude(fichaMedicaDTO.getInstitucionAcude());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getInternadoHospital())) {
                fichaMedicaDb.setInternadoHospital(fichaMedicaDTO.getInternadoHospital());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getAlergiaMedicamentos())) {
                fichaMedicaDb.setAlergiaMedicamentos(fichaMedicaDTO.getAlergiaMedicamentos());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getMedicamentosAlergicos())) {
                fichaMedicaDb.setMedicamentosAlergicos(fichaMedicaDTO.getMedicamentosAlergicos());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getAlergiaAlimentos())) {
                fichaMedicaDb.setAlergiaAlimentos(fichaMedicaDTO.getAlergiaAlimentos());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getDetalleAlergiasAlimentos())) {
                fichaMedicaDb.setDetalleAlergiasAlimentos(fichaMedicaDTO.getDetalleAlergiasAlimentos());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getCirugiaQuirurgica())) {
                fichaMedicaDb.setCirugiaQuirurgica(fichaMedicaDTO.getCirugiaQuirurgica());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getDetalleCirugias())) {
                fichaMedicaDb.setDetalleCirugias(fichaMedicaDTO.getDetalleCirugias());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getFracturas())) {
                fichaMedicaDb.setFracturas(fichaMedicaDTO.getFracturas());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getDetalleFracturas())) {
                fichaMedicaDb.setDetalleFracturas(fichaMedicaDTO.getDetalleFracturas());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getIrs())) {
                fichaMedicaDb.setIRS(fichaMedicaDTO.getIrs());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getUsoDePreservativo())) {
                fichaMedicaDb.setUsoDePreservativo(fichaMedicaDTO.getUsoDePreservativo());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getRelacionGenero())) {
                fichaMedicaDb.setGenero(catalogoRepository.findByTokenIdentificadorAndRemovido(fichaMedicaDTO.getRelacionGenero(),false));
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getIcd())) {
                fichaMedicaDb.setICD(fichaMedicaDTO.getIcd());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getHabitosNocivos())) {
                fichaMedicaDb.setHabitosNocivos(fichaMedicaDTO.getHabitosNocivos());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getTabaco())) {
                fichaMedicaDb.setTabaco(fichaMedicaDTO.getTabaco());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getTomaAlcohol())) {
                fichaMedicaDb.setTomaAlcohol(fichaMedicaDTO.getTomaAlcohol());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getEdadAlcohol())) {
                fichaMedicaDb.setEdadAlcohol(fichaMedicaDTO.getEdadAlcohol());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getEdadTabaco())) {
                fichaMedicaDb.setEdadTabaco(fichaMedicaDTO.getEdadTabaco());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getPresion())) {
                fichaMedicaDb.setPresion(fichaMedicaDTO.getPresion());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getTalla())) {
                fichaMedicaDb.setTalla(fichaMedicaDTO.getTalla());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getAspectoGeneralFisico())) {
                fichaMedicaDb.setAspectoGeneralFisico(fichaMedicaDTO.getAspectoGeneralFisico());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getInspeccion())) {
                fichaMedicaDb.setInspeccion(fichaMedicaDTO.getInspeccion());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getPielFaneras())) {
                fichaMedicaDb.setPielFaneras(fichaMedicaDTO.getPielFaneras());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getPeso())) {
                fichaMedicaDb.setPeso(fichaMedicaDTO.getPeso());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getSaturacionOxigeno())) {
                fichaMedicaDb.setSaturacionOxigeno(fichaMedicaDTO.getSaturacionOxigeno());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getIndiceMasaCorporal())) {
                fichaMedicaDb.setIndiceMasaCorporal(fichaMedicaDTO.getIndiceMasaCorporal());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getDrogaInicio())) {
                fichaMedicaDb.setDrogaInicio(fichaMedicaDTO.getDrogaInicio());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getCabezaDetalle())) {
                fichaMedicaDb.setCabezaDetalle(fichaMedicaDTO.getCabezaDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getOjosDetalle())) {
                fichaMedicaDb.setOjosDetalle(fichaMedicaDTO.getOjosDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getOidoDetalle())) {
                fichaMedicaDb.setOidoDetalle(fichaMedicaDTO.getOidoDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getNarizDetalle())) {
                fichaMedicaDb.setNarizDetalle(fichaMedicaDTO.getNarizDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getBocaDetalle())) {
                fichaMedicaDb.setBocaDetalle(fichaMedicaDTO.getBocaDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getOrofaringeDetalle())) {
                fichaMedicaDb.setOrofaringeDetalle(fichaMedicaDTO.getOrofaringeDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getCorazonDetalle())) {
                fichaMedicaDb.setCorazonDetalle(fichaMedicaDTO.getCorazonDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getPulmonesDetalle())) {
                fichaMedicaDb.setPulmonesDetalle(fichaMedicaDTO.getPulmonesDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getAbdomenDetalle())) {
                fichaMedicaDb.setAbdomenDetalle(fichaMedicaDTO.getAbdomenDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getUrinarioDetalle())) {
                fichaMedicaDb.setUrinarioDetalle(fichaMedicaDTO.getUrinarioDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getPplDetalle())) {
                fichaMedicaDb.setPplDetalle(fichaMedicaDTO.getPplDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getPruDetalle())) {
                fichaMedicaDb.setPruDetalle(fichaMedicaDTO.getPruDetalle());
            }

            if (!ObjectUtils.isEmpty(fichaMedicaDTO.getImpresionDiagnostico())) {
                fichaMedicaDb.setImpresionDiagnostico(fichaMedicaDTO.getImpresionDiagnostico());
            }

// Asumimos que tipoSangre es un objeto y no una cadena:
            if (fichaMedicaDTO.getTipoSangre() != null) {
                fichaMedicaDb.setTipoSangre(dtoToCatalogo(fichaMedicaDTO.getTipoSangre()));
            }
            fichaMedicaDb.setIpCrea(ip);
            fichaMedicaDb.setFechaCreacion(fecha);
            fichaMedicaDb.setUsuarioSistemaCrea(usuarioSistema);
            fichaMedicaDb = this.fichaMedicaRepository.save(fichaMedicaDb);
            this.fichaMedicaRepository.save(fichaMedicaDb);
            fichaMedicaDb.setTokenIdentificador(fichaMedicaDb.getTokenIdentificador());

            for(PersonaRelacionadaEnfermedadDTO personaRelacionadaEnfermedadDTO: fichaMedicaDTO.getEnfermedadesPersonasRelacionada()){
                PersonaRelacionadaEnfermedad personaEnfermedad = null;
                if(ObjectUtils.isEmpty(personaRelacionadaEnfermedadDTO.getTokenIdentificador())){
                    personaEnfermedad = new PersonaRelacionadaEnfermedad();

                    personaEnfermedad.setIdFichaMedica(fichaMedicaDb);
                    personaEnfermedad.setUsuarioSistemaCrea(usuarioSistema);
                    personaEnfermedad.setFechaCreacion(new Date());
                    personaEnfermedad.setIpCrea(ip);

                    personaEnfermedad.setRemovido(false);

                }else{
                    personaEnfermedad = this.personaRelacionadaEnfermedadRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaEnfermedadDTO.getTokenIdentificador(),false);
                    personaEnfermedad.setUsuarioSistemaEdita(usuarioSistema);
                    personaEnfermedad.setFechaEdicion(new Date());
                    personaEnfermedad.setIpEdita(ip);
                }
                if (personaRelacionadaEnfermedadDTO.getTipoParentesco() != null) {
                    personaEnfermedad.setTipoParentesco(catalogoRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaEnfermedadDTO.getTipoParentesco().getTokenIdentificador(),false));
                }

                if (personaRelacionadaEnfermedadDTO.getSexoParentesco() != null) {
                    personaEnfermedad.setSexoParentesco(catalogoRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaEnfermedadDTO.getSexoParentesco().getTokenIdentificador(),false));
                }

                personaEnfermedad.setTipoEnfermedad(catalogoRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaEnfermedadDTO.getTokenTipoEnfermedad(),false));
                personaEnfermedad.setEnfermedadActual(personaRelacionadaEnfermedadDTO.getEnfermedadActiva());
                personaEnfermedad.setDetalle(personaRelacionadaEnfermedadDTO.getDetalle());

                if (personaRelacionadaEnfermedadDTO.getClasificacionEnfermedad() != null) {
                    personaEnfermedad.setClasificacionEnfermedad(this.clasificacionEnfermedadRepository.findByTokenIdentificadorAndRemovido(personaRelacionadaEnfermedadDTO.getClasificacionEnfermedad().getTokenIdentificador(), false));
                }
                /*PersonaRelacionada newPersonaRelacionada = this.personaRelacionadaRepository.findByTokenIdentificadorAndRemovido
                        (personaRelacionadaEnfermedadDTO.getTokenIdentificadorPersona(), Boolean.FALSE);
                personaEnfermedad.setIdPersonasRelacionadas(newPersonaRelacionada);*/
                this.personaRelacionadaEnfermedadRepository.save(personaEnfermedad);
            }

            for(String tokenEnfermedad: fichaMedicaDTO.getTokensEnfermedadEliminar()){
                PersonaRelacionadaEnfermedad personaEnfermedad = this.personaRelacionadaEnfermedadRepository.findByTokenIdentificadorAndRemovido(tokenEnfermedad,false);
                if(personaEnfermedad!=null){
                    personaEnfermedad.setRemovido(true);
                    personaEnfermedad.setUsuarioSistemaElimina(usuarioSistema);
                    personaEnfermedad.setFechaEliminacion(new Date());
                    personaEnfermedad.setIpElimina(ip);
                    this.personaRelacionadaEnfermedadRepository.save(personaEnfermedad);
                }
            }

            for(FichaMedicaEnfermedadDTO fichaMeditaEnfermedadDTO: fichaMedicaDTO.getEnfermedadesRelacionadas()){
                FichaMedicaEnfermedad fichaEnfermedad = null;
                if(ObjectUtils.isEmpty(fichaMeditaEnfermedadDTO.getTokenIdentificador())){
                    fichaEnfermedad = new FichaMedicaEnfermedad();
                    fichaEnfermedad.setIdFichaMedica(fichaMedicaDb);
                    fichaEnfermedad.setUsuarioSistemaCrea(usuarioSistema);
                    fichaEnfermedad.setFechaCreacion(new Date());
                    fichaEnfermedad.setIpCrea(ip);
                    fichaEnfermedad.setRemovido(false);

                }else{
                    fichaEnfermedad = this.fichaMedicaEnfermedadRepository.findByTokenIdentificadorAndRemovido(fichaMeditaEnfermedadDTO.getTokenIdentificador(),false);
                    fichaEnfermedad.setUsuarioSistemaEdita(usuarioSistema);
                    fichaEnfermedad.setFechaEdicion(new Date());
                    fichaEnfermedad.setIpEdita(ip);
                }
                fichaEnfermedad.setTipoEnfermedad(catalogoRepository.findByTokenIdentificadorAndRemovido(fichaMeditaEnfermedadDTO.getTokenTipoEnfermedad(),false));
                if (!ObjectUtils.isEmpty(fichaMeditaEnfermedadDTO.getEnfermedadActiva())) {
                    fichaEnfermedad.setEnfermedadActual(fichaMeditaEnfermedadDTO.getEnfermedadActiva());
                }

                if (!ObjectUtils.isEmpty(fichaMeditaEnfermedadDTO.getClasificacionEnfermedad())) {
                    fichaEnfermedad.setClasificacionEnfermedad(this.clasificacionEnfermedadRepository.findByTokenIdentificadorAndRemovido(fichaMeditaEnfermedadDTO.getClasificacionEnfermedad().getTokenIdentificador(),false));
                }

                if (!ObjectUtils.isEmpty(fichaMeditaEnfermedadDTO.getDetalle())) {
                    fichaEnfermedad.setDetalle(fichaMeditaEnfermedadDTO.getDetalle());
                }

                if (!ObjectUtils.isEmpty(fichaMeditaEnfermedadDTO.getEdadPresente())) {
                    fichaEnfermedad.setEdadPresente(fichaMeditaEnfermedadDTO.getEdadPresente());
                }

                if (!ObjectUtils.isEmpty(fichaMeditaEnfermedadDTO.getTratamiento())) {
                    fichaEnfermedad.setTratamiento(fichaMeditaEnfermedadDTO.getTratamiento());
                }

                if (!ObjectUtils.isEmpty(fichaMeditaEnfermedadDTO.getFechaAparicion())) {
                    fichaEnfermedad.setFechaAparicion(fichaMeditaEnfermedadDTO.getFechaAparicion());
                }
                this.fichaMedicaEnfermedadRepository.save(fichaEnfermedad);
            }

            for(String tokenFichaEnfermedad: fichaMedicaDTO.getTokensEnfermedadesFichaEliminar()){
                FichaMedicaEnfermedad fichaEnfermedad  = this.fichaMedicaEnfermedadRepository.findByTokenIdentificadorAndRemovido(tokenFichaEnfermedad,false);
                if(fichaEnfermedad!=null){
                    fichaEnfermedad.setRemovido(true);
                    fichaEnfermedad.setUsuarioSistemaElimina(usuarioSistema);
                    fichaEnfermedad.setFechaEliminacion(new Date());
                    fichaEnfermedad.setIpElimina(ip);
                    this.fichaMedicaEnfermedadRepository.save(fichaEnfermedad);
                }
            }

            df.llenarRespuestaExitosa("Ficha médica creada con éxito. ", fichaMedicaDTO);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<FichaMedicaDTO> updateFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<FichaMedicaDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            FichaMedicaDTO fichaMedicaDTO = new Gson().fromJson(body, FichaMedicaDTO.class);

            String ip = httpServletRequest.getRemoteAddr();
            Date fecha = new Date();

            FichaMedica fichaMedicaDb = this.fichaMedicaRepository.findByTokenIdentificadorAndRemovido(fichaMedicaDTO.getTokenIdentificador(), false);
            if(fichaMedicaDb == null){
                df.setMensaje("La ficha médica con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            fichaMedicaDb.setEstadoSalud(fichaMedicaDTO.getEstadoSalud());
            fichaMedicaDb.setLesiones(fichaMedicaDTO.getLesiones());
            fichaMedicaDb.setEnfermedades(fichaMedicaDTO.getEnfermedades());
            fichaMedicaDb.setMedicamentos(fichaMedicaDTO.getMedicamentos());
            fichaMedicaDb.setSeguroMedico(fichaMedicaDTO.getSeguroMedico());
            fichaMedicaDb.setInstitucionAcude(fichaMedicaDTO.getInstitucionAcude());
            fichaMedicaDb.setInternadoHospital(fichaMedicaDTO.getInternadoHospital());
            fichaMedicaDb.setTipoSangre(dtoToCatalogo(fichaMedicaDTO.getTipoSangre()));
            fichaMedicaDb.setIpEdita(ip);
            fichaMedicaDb.setFechaEdicion(fecha);
            fichaMedicaDb.setUsuarioSistemaEdita(usuarioSistema);

            this.fichaMedicaRepository.save(fichaMedicaDb);
            fichaMedicaDb.setTokenIdentificador(fichaMedicaDb.getTokenIdentificador());

            df.llenarRespuestaExitosa("Ficha médica actualizada con éxito. ", fichaMedicaDTO);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> deleteFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            FichaMedicaDTO fichaMedicaDTO = new Gson().fromJson(body, FichaMedicaDTO.class);

            String ip = httpServletRequest.getRemoteAddr();

            FichaMedica fichaMedicaDb = this.fichaMedicaRepository.findByTokenIdentificadorAndRemovido(fichaMedicaDTO.getTokenIdentificador(), false);
            if (fichaMedicaDb == null) {
                df.setMensaje("La ficha médica con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            Date fecha = new Date();

            fichaMedicaDb.setIpElimina(ip);
            fichaMedicaDb.setFechaEliminacion(fecha);
            fichaMedicaDb.setUsuarioSistemaElimina(usuarioSistema);

            fichaMedicaDb.setRemovido(true);

            this.fichaMedicaRepository.save(fichaMedicaDb);

            df.llenarRespuestaExitosa("Ficha medica eliminada con exito", fichaMedicaDb.getRemovido());
        } catch (Exception ex){
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
