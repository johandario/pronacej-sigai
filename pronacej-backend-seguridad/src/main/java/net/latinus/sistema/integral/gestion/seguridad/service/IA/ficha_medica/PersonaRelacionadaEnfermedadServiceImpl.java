package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionadaEnfermedad;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.PersonaRelacionadaEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.PersonaRelacionadaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico.PersonaRelacionadaEnfermedadRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.FichaMedicaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class PersonaRelacionadaEnfermedadServiceImpl implements PersonaRelacionadaEnfermedadService{

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;

    private FichaMedicaRepository fichaMedicaRepository;
    private PersonaRelacionadaEnfermedadRepository personaRelacionadaEnfermedadRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<PersonaRelacionadaEnfermedadDTO>> getPersonaRelacionadaEnfermedades(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<PersonaRelacionadaEnfermedadDTO>> df = new RespuestaPorDefectoAuditoria<>();
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
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyString, PaginacionRequest.class);

            FichaMedica ficha = this.fichaMedicaRepository.findByFichaIdentificacion_TokenIdentificadorAndRemovido(paginacionRequest.getTokenIdentificador(),  false);

            if(ficha == null){
                df.setMensaje("No existe una ficha médica asociada al token proporcionado");
                df.setExito(false);
                return df;
            }

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idPersonasRelacionadaEnfermedad").descending()
            );

            Page<PersonaRelacionadaEnfermedad> listaEnfermedades = this.personaRelacionadaEnfermedadRepository.
                    encontrarEnfermedadesPersonaFicha(ficha.getTokenIdentificador(),pageable);
            List<PersonaRelacionadaEnfermedadDTO> listadoPersonaRelacionadaEnfermedadDTO= new ArrayList<>();

            PaginacionResponse<PersonaRelacionadaEnfermedadDTO> enfermedaPersonaRelacionadaPage = new PaginacionResponse<>();

            for(PersonaRelacionadaEnfermedad enfermedad: listaEnfermedades.getContent() ){
                PersonaRelacionadaEnfermedadDTO personaRelacionadaEnfermedadDTO = getPersonaRelacionadaEnfermedadDTO(enfermedad);
                listadoPersonaRelacionadaEnfermedadDTO.add(personaRelacionadaEnfermedadDTO);
            }

            enfermedaPersonaRelacionadaPage.setData(listadoPersonaRelacionadaEnfermedadDTO);
            enfermedaPersonaRelacionadaPage.setTotalItems(listaEnfermedades.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado un total de: "
                            + listadoPersonaRelacionadaEnfermedadDTO.size() + " de: " + listaEnfermedades.getTotalElements() + " elementos disponibles",
                    enfermedaPersonaRelacionadaPage);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @NotNull
    private static PersonaRelacionadaEnfermedadDTO getPersonaRelacionadaEnfermedadDTO(PersonaRelacionadaEnfermedad enfermedad) {
        PersonaRelacionadaEnfermedadDTO personaRelacionadaEnfermedadDTO = new PersonaRelacionadaEnfermedadDTO();
        personaRelacionadaEnfermedadDTO.setEnfermedadActiva(enfermedad.getEnfermedadActual());
        personaRelacionadaEnfermedadDTO.setDetalle(enfermedad.getDetalle());

        if (enfermedad.getTipoEnfermedad() != null) {
            personaRelacionadaEnfermedadDTO.setTokenTipoEnfermedad(enfermedad.getTipoEnfermedad().getTokenIdentificador());
            personaRelacionadaEnfermedadDTO.setNombreEnfermedad(enfermedad.getTipoEnfermedad().getNombre());
        }

        personaRelacionadaEnfermedadDTO.setTokenIdentificador(enfermedad.getTokenIdentificador());

        if (enfermedad.getClasificacionEnfermedad() != null) {
            personaRelacionadaEnfermedadDTO.setClasificacionEnfermedad(enfermedad.getClasificacionEnfermedad().convertirADTO());
        }

        if (enfermedad.getTipoParentesco() != null) {
            personaRelacionadaEnfermedadDTO.setTipoParentesco(enfermedad.getTipoParentesco().convertirADTO());
        }

        if (enfermedad.getSexoParentesco() != null) {
            personaRelacionadaEnfermedadDTO.setSexoParentesco(enfermedad.getSexoParentesco().convertirADTO());
        }

        personaRelacionadaEnfermedadDTO.setParentescoPersona(
                enfermedad.getIdPersonasRelacionadas() != null
                ? enfermedad.getIdPersonasRelacionadas().getParentesco().getDescripcion()
                : (
                        enfermedad.getTipoParentesco() != null
                        ? enfermedad.getTipoParentesco().getNombre()
                        : null
                )
        );
        personaRelacionadaEnfermedadDTO.setNombrePersona(
                enfermedad.getIdPersonasRelacionadas() != null
                        ? enfermedad.getIdPersonasRelacionadas().getNombresCompletos()
                        : null

        );
        personaRelacionadaEnfermedadDTO.setTokenIdentificadorPersona(
                enfermedad.getIdPersonasRelacionadas() != null
                        ? enfermedad.getIdPersonasRelacionadas().getTokenIdentificador()
                        : null
        );
        return personaRelacionadaEnfermedadDTO;
    }
}
