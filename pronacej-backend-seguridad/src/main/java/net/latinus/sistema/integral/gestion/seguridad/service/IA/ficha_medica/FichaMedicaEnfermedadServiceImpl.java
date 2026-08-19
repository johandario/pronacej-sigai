package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaMedicaEnfermedad;
import net.latinus.sistema.integral.gestion.seguridad.entities.PersonaRelacionadaEnfermedad;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.FichaMedica;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.FichaMedicaEnfermedadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.FichaMedicaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaMedicaEnfermedadRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class FichaMedicaEnfermedadServiceImpl implements FichaMedicaEnfermedadService{

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private FichaMedicaRepository fichaMedicaRepository;

    private FichaMedicaEnfermedadRepository fichaMedicaEnfermedadRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<FichaMedicaEnfermedadDTO>> getFichaMedicaEnfermedades(HttpServletRequest httpServletRequest,
                                                                                                                 BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<PaginacionResponse<FichaMedicaEnfermedadDTO>> df = new RespuestaPorDefectoAuditoria<>();

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
                    Sort.by("idFichaMedicaEnfermedad").descending()
            );

            Page<FichaMedicaEnfermedad> listaEnfermedades = this.fichaMedicaEnfermedadRepository.
                    encontrarEnfermedadesFichaMedica(ficha.getTokenIdentificador(),pageable);
            List<FichaMedicaEnfermedadDTO> listadoFichaMedicaEnfermedadDTO= new ArrayList<>();

            PaginacionResponse<FichaMedicaEnfermedadDTO> enfermedaFichaMedicaPage = new PaginacionResponse<>();

            for(FichaMedicaEnfermedad fichaMedicaEnfermedad: listaEnfermedades.getContent()){
                FichaMedicaEnfermedadDTO fichaME = new FichaMedicaEnfermedadDTO();
                fichaME.setEnfermedadActiva(fichaMedicaEnfermedad.getEnfermedadActual());

                if (!ObjectUtils.isEmpty(fichaMedicaEnfermedad.getTipoEnfermedad())) {
                    fichaME.setTokenTipoEnfermedad(fichaMedicaEnfermedad.getTipoEnfermedad().getTokenIdentificador());
                    fichaME.setNombreEnfermedad(fichaMedicaEnfermedad.getTipoEnfermedad().getNombre());
                }

                if (!ObjectUtils.isEmpty(fichaMedicaEnfermedad.getClasificacionEnfermedad())) {
                    fichaME.setClasificacionEnfermedad(fichaMedicaEnfermedad.getClasificacionEnfermedad().convertirADTO());
                }

                if (!ObjectUtils.isEmpty(fichaMedicaEnfermedad.getDetalle())) {
                    fichaME.setDetalle(fichaMedicaEnfermedad.getDetalle());
                }

                if (!ObjectUtils.isEmpty(fichaMedicaEnfermedad.getTokenIdentificador())) {
                    fichaME.setTokenIdentificador(fichaMedicaEnfermedad.getTokenIdentificador());
                }

                if (!ObjectUtils.isEmpty(fichaMedicaEnfermedad.getEdadPresente())) {
                    fichaME.setEdadPresente(fichaMedicaEnfermedad.getEdadPresente());
                }

                if (!ObjectUtils.isEmpty(fichaMedicaEnfermedad.getTratamiento())) {
                    fichaME.setTratamiento(fichaMedicaEnfermedad.getTratamiento());
                }
                if (!ObjectUtils.isEmpty(fichaMedicaEnfermedad.getFechaAparicion())) {
                    fichaME.setFechaAparicion(fichaMedicaEnfermedad.getFechaAparicion());
                }
                listadoFichaMedicaEnfermedadDTO.add(fichaME);
            }

            enfermedaFichaMedicaPage.setData(listadoFichaMedicaEnfermedadDTO);
            enfermedaFichaMedicaPage.setTotalItems(listaEnfermedades.getTotalElements());

            df.llenarRespuestaExitosa("Se han encontrado un total de: "
                            + listadoFichaMedicaEnfermedadDTO.size() + " de: " + listaEnfermedades.getTotalElements() + " elementos disponibles",
                    enfermedaFichaMedicaPage);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
