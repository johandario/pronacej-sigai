package net.latinus.sistema.integral.gestion.seguridad.service;

import jakarta.servlet.http.HttpServletRequest;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.AuditoriaServicioRestService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServiciosExternosImpl implements ServiciosExternos {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private AuditoriaServicioRestService auditoriaServicioRestService;

    private JwtProviderService jwtProviderService;

    private final ServiciosRestService serviciosRestService = new ServiciosRestService();

    //private final LogService logService = new LogService(this.getClass());

    @Value("${api.sunat}")
    private String apiSunat;

    @Value("${api.reniec}")
    private String apiReniec;

    @Autowired
    public ServiciosExternosImpl(ParametroDelSistemaRepository parametroDelSistemaRepository,
                                 AuditoriaServicioRestService auditoriaServicioRestService,
                                 JwtProviderService jwtProviderService) {
        this.parametroDelSistemaRepository = parametroDelSistemaRepository;
        this.auditoriaServicioRestService = auditoriaServicioRestService;
        this.jwtProviderService = jwtProviderService;
    }

    private RespuestaPorDefectoAuditoria<ResponseEntity<String>> requestApiGetHelp(HttpServletRequest httpServletRequest,
                                                                                   String nenomicoParam, String url,
                                                                                   Map<String, String> params) {
        RespuestaPorDefectoAuditoria<ResponseEntity<String>> df = new RespuestaPorDefectoAuditoria<>();

        try {


            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(df2.getLogOut());
                df.setSinAcceso(df2.getSinAcceso());
                return df;
            }



            Pageable pageable = PageRequest.of(0, 2, Sort.by("idParametroDelSistema").descending());
            Page<ParametroDelSistema> parametroDelSistemaPage = this.parametroDelSistemaRepository.findByNemonicoAndRemovido(
                    nenomicoParam,
                    false,
                    pageable
            );

            if (!parametroDelSistemaPage.hasContent()) {
                df.setMensaje("No se ha encontrado el parametro del token: " + nenomicoParam);
                return df;
            }

            ParametroDelSistema parametroDelSistema = parametroDelSistemaPage.toList().get(0);

            String valor = parametroDelSistema.getValor();

            if (valor == null || valor.isBlank()) {
                df.setMensaje("El valor del token no es vaálido");
                return df;
            }

            params.put("token", valor);
            Date fechaInicio = new Date();

            ResponseEntity<String> response = this.serviciosRestService.getJson(url,
                    params, null, null);

            df.llenarRespuestaExitosa("Servicio ejecutado con éxito", response);


            this.auditoriaServicioRestService.guardarServicioRest(httpServletRequest, params.toString(),
                    response.getBody(), fechaInicio, HttpMethod.GET, url);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ResponseEntity<String>> dataSunat(HttpServletRequest httpServletRequest, String ruc) {
        Map<String, String> params = new HashMap<>();
        params.put("ruc", ruc);
        String url = this.apiReniec + "/api_data_sunat.php";
        //this.logService.info(df.toString());

        return this.requestApiGetHelp(httpServletRequest, EtiquetaNemonico.PARAM_TOKEN_SUNAT, url,
                params);
    }

    @Override
    public RespuestaPorDefectoAuditoria<ResponseEntity<String>> data(HttpServletRequest httpServletRequest, String dni) {
        Map<String, String> params = new HashMap<>();
        params.put("dni", dni);
        String url = this.apiReniec + "/api_data.php";

        //this.logService.info(df.toString());
        return this.requestApiGetHelp(httpServletRequest, EtiquetaNemonico.PARAM_TOKEN_RENIEC, url,
                params);
    }
}
