package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.InformacionUbicacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionada;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformacionUbicacionDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.InformacionUbicacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.PersonaRelacionadaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class InformacionUbicacionServiceImpl implements InformacionUbicacionService {

    private PersonaRelacionadaRepository personaRelacionadaRepository;
    private CatalogoRepository catalogoRepository;
    private InformacionUbicacionRepository informacionUbicacionRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private JwtProviderService jwtProviderService;

    @Override
    @Transactional
    public RespuestaPorDefectoAuditoria<InformacionUbicacionDTO> crearInformacionUbicacion(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<InformacionUbicacionDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            InformacionUbicacionDTO informacionUbicacionDTO = new Gson().fromJson(bodyString, InformacionUbicacionDTO.class);

            PersonaRelacionada newPersonaRelacionada;
            newPersonaRelacionada = this.personaRelacionadaRepository.findByIdPersonasRelacionadasAndRemovido(informacionUbicacionDTO.getIdPersonaRelacionada(), Boolean.FALSE);

            if (newPersonaRelacionada == null) {
                df.setMensaje("La persona relacionada no existe.");
                return df;
            }

            InformacionUbicacion infoUbicacion;
            if(informacionUbicacionDTO.getEsEdicion()){

                infoUbicacion = this.informacionUbicacionRepository.findByTokenIdentificadorAndRemovido(informacionUbicacionDTO.getTokenIdentificador(),false);
                if (infoUbicacion == null) {
                    df.setMensaje("La información a editar no existe o ya fue eliminada anteriormente");
                    return df;
                }

            }else{
                infoUbicacion = new InformacionUbicacion();
            }

            infoUbicacion.setIdPersonasRelacionadas(newPersonaRelacionada);
            infoUbicacion.setTipoInformacionUbicacion(catalogoRepository.findByNemonicoAndRemovido(informacionUbicacionDTO.getTipoInformacionUbicacion(),false));
            infoUbicacion.setValor(informacionUbicacionDTO.getValor());
            infoUbicacion.setRemovido(false);

            this.informacionUbicacionRepository.save(infoUbicacion);

            df.llenarRespuestaExitosa("Se " + (informacionUbicacionDTO.getEsEdicion() ? "editó" : "creó") + " con éxito la ubicacion de la persona relacionada.",
                    informacionUbicacionDTO);


        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<InformacionUbicacionDTO>> obtenerInformacionUbicaciones(
            HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<InformacionUbicacionDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validación JWT (mantengo código existente)
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar body (mantengo código existente)
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            Long idPersonaRelacionada = new Gson().fromJson(bodyString, Long.class);

            // Verificar que la persona relacionada existe
            PersonaRelacionada persona = this.personaRelacionadaRepository.findByIdPersonasRelacionadasAndRemovido(idPersonaRelacionada, Boolean.FALSE);
            if (persona == null) {
                df.setMensaje("La persona relacionada no existe o fue eliminada anteriormente");
                return df;
            }

            // Obtener información de ubicaciones en una sola consulta optimizada con JOIN FETCH
            List<InformacionUbicacion> listaInformaciones = this.informacionUbicacionRepository.encontrarInformacionUbicaciones(idPersonaRelacionada);

            // Optimización: pre-calcular el tamaño de la lista para evitar crecimiento dinámico
            List<InformacionUbicacionDTO> listadoInformacionUbicacionDTO = new ArrayList<>(listaInformaciones.size());

            // Mapeo optimizado de entidades a DTOs
            for(InformacionUbicacion informacionUbicacion: listaInformaciones){
                InformacionUbicacionDTO infoDto = new InformacionUbicacionDTO();
                infoDto.setValor(informacionUbicacion.getValor());
                infoDto.setTipoInformacionUbicacion(informacionUbicacion.getTipoInformacionUbicacion().getNemonico());
                infoDto.setTokenIdentificador(informacionUbicacion.getTokenIdentificador());
                infoDto.setNombreTipoInformacion(informacionUbicacion.getTipoInformacionUbicacion().getNombre());
                infoDto.setIdPersonaRelacionada(idPersonaRelacionada);
                listadoInformacionUbicacionDTO.add(infoDto);
            }

            // Construcción de la respuesta paginada
            PaginacionResponse<InformacionUbicacionDTO> informacionUbicacionDTOPaginacionResponse = new PaginacionResponse<>();
            informacionUbicacionDTOPaginacionResponse.setData(listadoInformacionUbicacionDTO);
            // No usamos setPage ni setTotalPages que parece que no existen
            // Solo lo que sabemos que existe:
            informacionUbicacionDTOPaginacionResponse.setTotalItems((long) listadoInformacionUbicacionDTO.size());

            df.llenarRespuestaExitosa("Se han encontrado un total de: " 
                    + listadoInformacionUbicacionDTO.size() + " elementos disponibles",
                    informacionUbicacionDTOPaginacionResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
    
    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarInformacionUbicacion(HttpServletRequest httpServletRequest,
                                                                              BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            InformacionUbicacionDTO infoUbicacionDTO = new Gson().fromJson(bodyString, InformacionUbicacionDTO.class);

            InformacionUbicacion infoUbicacion = this.informacionUbicacionRepository.findByTokenIdentificadorAndRemovido(infoUbicacionDTO.getTokenIdentificador(),false);
            if (infoUbicacion == null) {
                df.setMensaje("La información a editar no existe o ya fue eliminada anteriormente");
                return df;
            }

            Date fecha = new Date();
            infoUbicacion.setRemovido(true);
            this.informacionUbicacionRepository.save(infoUbicacion);
            df.llenarRespuestaExitosa("Se ha eliminado con exito del sistema la información de la persona: "
                    + infoUbicacion.getIdPersonasRelacionadas().getIdentificacion(), true);


        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
