package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.EmpresaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.documentos.CarpetaService;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final LogService logService = new LogService(EmpresaServiceImpl.class);
    private EmpresaRepository empresaRepository;
    private JwtProviderService jwtProviderService;
    private CarpetaService carpetaService;
    private CarpetaRepository carpetaRepository;


    @Override
    public Empresa encontrarPorTokenIdentificador(String tokenIdentificador) {
        return this.empresaRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador, false);
    }

    @Override
    public RespuestaPorDefectoAuditoria<EmpresaDTO> crearEmpresaDirecto(HttpServletRequest httpServletRequest,
                                                                        EmpresaDTO empresaDTO) {

        RespuestaPorDefectoAuditoria<EmpresaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = new Empresa();
            empresa.setDescripcion(empresaDTO.getDescripcion());
            empresa.setColorPrimarioHex(empresaDTO.getColorPrimarioHex());
            empresa.setColorSecundarioHex(empresaDTO.getColorSecundarioHex());
            empresa.setIpCrea(httpServletRequest.getRemoteAddr());
            empresa.setNombre(empresaDTO.getNombre());
            empresa.setNombreCorto(empresaDTO.getNombreCorto());
            empresa.setUrlLogo(empresaDTO.getUrlLogo());
            empresa.setUrlPagina(empresaDTO.getUrlPagina());
            empresa.setUserNameAlfresco(empresaDTO.getUserNameAlfresco());
            empresa.setConstraseniaAlfresco(empresaDTO.getConstraseniaAlfresco());
            empresa = this.empresaRepository.save(empresa);

            empresaDTO.setTokenIdentificador(empresa.getTokenIdentificador());

            //Creando la carpeta de la empresa
            CarpetaDTO carpetaDTOEmpresa = new CarpetaDTO();
            carpetaDTOEmpresa.setDescripcion(empresaDTO.getDescripcion() + ". Carpeta principal");
            carpetaDTOEmpresa.setNombreCliente(empresaDTO.getNombreCorto());
            carpetaDTOEmpresa.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            RespuestaPorDefectoAuditoria<CarpetaDTO> df3 = this.carpetaService.crearCarpeta(
                    httpServletRequest, false, carpetaDTOEmpresa
            );

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                df.setMensajeErrorReal(df3.getMensajeErrorReal());
                return df;
            }


            carpetaDTOEmpresa = df3.getData();
            Carpeta carpetaEmpresa = this.carpetaRepository.findByTokenIdentificadorAndRemovido(
                    carpetaDTOEmpresa.getTokenIdentificador(), false
            );
            empresa.setIdCarpetaAlfresco(carpetaEmpresa.getIdentificadorAlfresco());

            //Creando la carpeta delas notificaciones
            CarpetaDTO carpetaDTONotificaciones = new CarpetaDTO();
            carpetaDTONotificaciones.setDescripcion("Notificaciones email");
            carpetaDTONotificaciones.setNombreCliente("Carpetas para los archivos de notificaciones de email");
            carpetaDTONotificaciones.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
            carpetaDTONotificaciones.setCarpetaDTOPadre(carpetaDTOEmpresa);

            RespuestaPorDefectoAuditoria<CarpetaDTO> df4 = this.carpetaService.crearCarpeta(
                    httpServletRequest, false, carpetaDTONotificaciones
            );

            if (!df4.isExito()) {
                df.setMensaje(df4.getMensaje());
                df.setMensajeErrorReal(df4.getMensajeErrorReal());
                return df;
            }

            carpetaDTONotificaciones = df4.getData();
            Carpeta carpetaNotificacionEmail = this.carpetaRepository.findByTokenIdentificadorAndRemovido(
                    carpetaDTONotificaciones.getTokenIdentificador(), false
            );
            empresa.setIdCarpetaAlfrescoNotificacionesEmail(carpetaNotificacionEmail.getIdentificadorAlfresco());
            empresa = this.empresaRepository.save(empresa);

            df.llenarRespuestaExitosa("Se creo con exito la empresa: " + empresa.getNombre(), empresaDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
