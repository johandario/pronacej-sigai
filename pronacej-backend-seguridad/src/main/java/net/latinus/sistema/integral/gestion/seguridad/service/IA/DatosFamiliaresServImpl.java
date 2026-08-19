package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.DatosFamiliares;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DatosFamiliaresDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.DatosFamiliaresRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class DatosFamiliaresServImpl implements DatosFamiliaresService{

    @Autowired
    private CatalogoRepository catalogoRepository;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;

    private DatosFamiliaresRepository datosFamiliaresRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private Map<String, Long> solicitudesEnProcesamiento = new ConcurrentHashMap<>();

    @Override
    public RespuestaPorDefectoAuditoria<DatosFamiliaresDTO> crearDatosFamiliares(HttpServletRequest httpServletRequest, DatosFamiliaresDTO datosFamiliaresDTO) {
        RespuestaPorDefectoAuditoria<DatosFamiliaresDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            String idSolicitud = datosFamiliaresDTO.getTokenIdentificadorFicha() + "-datosFamiliares";

            Long tiempoProcesamiento = solicitudesEnProcesamiento.get(idSolicitud);
            if (tiempoProcesamiento != null) {
                if (System.currentTimeMillis() - tiempoProcesamiento < 5000) {
                    df.setExito(false);
                    df.setMensaje("Una solicitud similar ya está siendo procesada. Por favor, espere unos segundos antes de intentar nuevamente.");
                    return df;
                }
            }

            solicitudesEnProcesamiento.put(idSolicitud, System.currentTimeMillis());

            try {
                DatosFamiliares dato;
                FichaIdentificacion ficha = fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(datosFamiliaresDTO.getTokenIdentificadorFicha(),
                        Boolean.FALSE);

                dato = datosFamiliaresRepository.encontrarDatosPersonales(datosFamiliaresDTO.getTokenIdentificadorFicha());
                boolean esEdicion = dato != null;
                
                if (dato == null) {
                    dato = new DatosFamiliares();
                }

                if (datosFamiliaresDTO.getEntornoFamiliar() != null) {
                    dato.setEntornoFamiliar(datosFamiliaresDTO.getEntornoFamiliar());
                }
                if (datosFamiliaresDTO.getEjercicioAutoridad() != null) {
                    dato.setEjercicioAutoridad(datosFamiliaresDTO.getEjercicioAutoridad());
                }
                if (datosFamiliaresDTO.getOrganizacionFamiliar() != null) {
                    dato.setOrganizacionFamiliar(
                            this.catalogoRepository.findByTokenIdentificadorAndRemovido(datosFamiliaresDTO.getOrganizacionFamiliar(), false)
                    );
                }
                if (datosFamiliaresDTO.getTipoFamilia() != null) {
                    dato.setTipoFamilia(
                            this.catalogoRepository.findByTokenIdentificadorAndRemovido(datosFamiliaresDTO.getTipoFamilia(), false)
                    );
                }
                if (datosFamiliaresDTO.getRelacionIntraFamiliarPareja() != null) {
                    dato.setRelacionIntraFamiliarPareja(datosFamiliaresDTO.getRelacionIntraFamiliarPareja().equals("S"));
                }
                if (datosFamiliaresDTO.getObservacionesRelacionIntrafamiliar()!= null) {
                    dato.setObservacionesRelacionIntrafamiliar(datosFamiliaresDTO.getObservacionesRelacionIntrafamiliar());
                }
                if (datosFamiliaresDTO.getRelacionIntraFamiliarFilial() != null) {
                    dato.setRelacionIntraFamiliarFilial(datosFamiliaresDTO.getRelacionIntraFamiliarFilial().equals("S"));
                }
                if (datosFamiliaresDTO.getRelacionIntraFamiliarParentales() != null) {
                    dato.setRelacionIntraFamiliarParentales(datosFamiliaresDTO.getRelacionIntraFamiliarParentales().equals("S"));
                }
                if (datosFamiliaresDTO.getRelacionIntraFamiliarPadres() != null) {
                    dato.setRelacionIntraFamiliarPadres(datosFamiliaresDTO.getRelacionIntraFamiliarPadres().equals("S"));
                }
                if (datosFamiliaresDTO.getCausaAusenciaPadres() != null) {
                    dato.setCausaAusenciaPadres(datosFamiliaresDTO.getCausaAusenciaPadres());
                }
                if (datosFamiliaresDTO.getPartidaNacimiento() != null) {
                    dato.setPartidaNacimiento(datosFamiliaresDTO.getPartidaNacimiento().equals("S"));
                }
                if (datosFamiliaresDTO.getOtroSacramento() != null) {
                    dato.setOtroSacramento(datosFamiliaresDTO.getOtroSacramento());
                }
                if (datosFamiliaresDTO.getReligion() != null) {
                    dato.setReligion(datosFamiliaresDTO.getReligion());
                }
                if (datosFamiliaresDTO.getTipoSacramento() != null) {
                    dato.setTipoSacramento(this.catalogoRepository.findByTokenIdentificadorAndRemovido(datosFamiliaresDTO.getTipoSacramento(), false));
                }

                dato.setRemovido(false);
                dato.setFichaIdentificacion(ficha);

                this.datosFamiliaresRepository.save(dato);

                // Obtener nombres completos para los mensajes
                String nombresCompletos = obtenerNombresCompletos(ficha);

                // Mensaje para el usuario
                String mensajeUsuario = esEdicion ? 
                    "Se editó con éxito los datos familiares de " + nombresCompletos :
                    "Se creó con éxito los datos familiares de " + nombresCompletos;

                // Mensaje para auditoría
                String identificacionPersona = obtenerIdentificacionPersona(ficha);
                String mensajeAuditoria = esEdicion ?
                    "Se editó con éxito los datos familiares de la persona con identificación: " + identificacionPersona :
                    "Se creó con éxito los datos familiares de la persona con identificación: " + identificacionPersona;

                df.llenarRespuestaExitosa(mensajeUsuario, datosFamiliaresDTO, mensajeAuditoria);

            } finally {
                solicitudesEnProcesamiento.remove(idSolicitud);
            }

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<DatosFamiliaresDTO> obtenerDatosFamiliaresToken(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<DatosFamiliaresDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            String tokenIdentificador = new Gson().fromJson(bodyString, String.class);
            DatosFamiliaresDTO datoDTO = new DatosFamiliaresDTO();
            DatosFamiliares dato = this.datosFamiliaresRepository.encontrarDatosPersonales(tokenIdentificador);
            
            if(dato==null){
                // Mensaje para el usuario
                String mensajeUsuario = "Obteniendo los datos familiares";
                
                // Mensaje para auditoría
                String mensajeAuditoria = "No existe situacion familiar para la ficha: token-" + tokenIdentificador;
                
                df.llenarRespuestaExitosa(mensajeUsuario, datoDTO, mensajeAuditoria);
                return df;
            }

            if (dato.getOtroSacramento() != null) {
                datoDTO.setOtroSacramento(dato.getOtroSacramento());
            }
            if (dato.getTipoFamilia() != null && dato.getTipoFamilia().getTokenIdentificador() != null) {
                datoDTO.setTipoFamilia(dato.getTipoFamilia().getTokenIdentificador());
            }
            if (dato.getRelacionIntraFamiliarPadres() != null) {
                datoDTO.setRelacionIntraFamiliarPadres(dato.getRelacionIntraFamiliarPadres() ? "S" : "N");
            }
            if (dato.getRelacionIntraFamiliarFilial() != null) {
                datoDTO.setRelacionIntraFamiliarFilial(dato.getRelacionIntraFamiliarFilial() ? "S" : "N");
            }
            if (dato.getRelacionIntraFamiliarPareja() != null) {
                datoDTO.setRelacionIntraFamiliarPareja(dato.getRelacionIntraFamiliarPareja() ? "S" : "N");
            }
            if (dato.getRelacionIntraFamiliarParentales() != null) {
                datoDTO.setRelacionIntraFamiliarParentales(dato.getRelacionIntraFamiliarParentales() ? "S" : "N");
            }
            if (dato.getEntornoFamiliar() != null) {
                datoDTO.setEntornoFamiliar(dato.getEntornoFamiliar());
            }
            if (dato.getOrganizacionFamiliar() != null && dato.getOrganizacionFamiliar().getTokenIdentificador() != null) {
                datoDTO.setOrganizacionFamiliar(dato.getOrganizacionFamiliar().getTokenIdentificador());
            }
            if (dato.getCausaAusenciaPadres() != null) {
                datoDTO.setCausaAusenciaPadres(dato.getCausaAusenciaPadres());
            }
            if (dato.getObservacionesRelacionIntrafamiliar() != null) {
                datoDTO.setObservacionesRelacionIntrafamiliar(dato.getObservacionesRelacionIntrafamiliar());
            }
            if (dato.getPartidaNacimiento() != null) {
                datoDTO.setPartidaNacimiento(dato.getPartidaNacimiento() ? "S" : "N");
            }
            if (dato.getReligion() != null) {
                datoDTO.setReligion(dato.getReligion());
            }
            if (dato.getEjercicioAutoridad() != null) {
                datoDTO.setEjercicioAutoridad(dato.getEjercicioAutoridad());
            }
            if (dato.getTipoSacramento() != null) {
                datoDTO.setTipoSacramento(dato.getTipoSacramento().getTokenIdentificador());
            }

            // Obtener nombres completos para los mensajes
            String nombresCompletos = obtenerNombresCompletos(dato.getFichaIdentificacion());
            
            // Mensaje para el usuario
            String mensajeUsuario = "Obteniendo los datos familiares de " + nombresCompletos;

            // Mensaje para auditoría
            String identificacionPersona = obtenerIdentificacionPersona(dato.getFichaIdentificacion());
            String mensajeAuditoria = "Se obtuvo con éxito los datos familiares de la persona con identificación: " + identificacionPersona;

            df.llenarRespuestaExitosa(mensajeUsuario, datoDTO, mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    /**
     * Método auxiliar para obtener nombres completos de una ficha
     */
    private String obtenerNombresCompletos(FichaIdentificacion ficha) {
        if (ficha == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (ficha.getNombres() != null && !ficha.getNombres().trim().isEmpty()) {
            nombreCompleto.append(ficha.getNombres());
        }
        if (ficha.getApellidoPaterno() != null && !ficha.getApellidoPaterno().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(ficha.getApellidoPaterno());
        }
        if (ficha.getApellidoMaterno() != null && !ficha.getApellidoMaterno().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(ficha.getApellidoMaterno());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }

    /**
     * Método auxiliar para obtener la identificación de una ficha (para auditoría)
     */
    private String obtenerIdentificacionPersona(FichaIdentificacion ficha) {
        if (ficha == null) {
            return "N/A";
        }

        String identificacion = "N/A";
        
        // Primero intentar con el campo dni
        if (ficha.getDni() != null && !ficha.getDni().trim().isEmpty()) {
            identificacion = ficha.getDni();
        } 
        // Si no hay dni, intentar con numeroIdentificacion
        else if (ficha.getNumeroIdentificacion() != null && !ficha.getNumeroIdentificacion().trim().isEmpty()) {
            identificacion = ficha.getNumeroIdentificacion();
        }
        // Si no hay ninguno, usar nombres y apellidos como identificación
        else {
            String nombresCompletos = obtenerNombresCompletos(ficha);
            if (!"N/A".equals(nombresCompletos)) {
                identificacion = nombresCompletos;
            }
        }

        return identificacion;
    }
}