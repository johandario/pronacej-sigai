package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.Diagnostico;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.DiagnosticoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico.DiagnosticoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico.EvaluacionMedicaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class DiagnosticoServiceImpl implements DiagnosticoService{

    private CatalogoRepository catalogoRepository;
    private EvaluacionMedicaRepository evaluacionMedicaRepository;
    private DiagnosticoRepository diagnosticoRepository;
    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<DiagnosticoDTO>> getDiagnosticoByIdEvaluacionMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<DiagnosticoDTO>> df = new RespuestaPorDefectoAuditoria<>();
        try{
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

            Page<Diagnostico> diagnosticosPage = this.diagnosticoRepository.findByEvaluacionMedica_TokenIdentificadorAndRemovido(tokenIdFichaMedica, false, pageable);
            PaginacionResponse<DiagnosticoDTO> paginacionResponse = new PaginacionResponse<>();

            List<DiagnosticoDTO> diagnosticoDTOS = diagnosticosPage.stream()
                    .map(diagnostico -> {
                        DiagnosticoDTO dto = new DiagnosticoDTO();
                        dto.setTokenIdentificador(diagnostico.getTokenIdentificador());
                        dto.setTokenIdEvaluacionMedica(diagnostico.getEvaluacionMedica().getTokenIdentificador());
                        dto.setTipoDiagnostico(catalogoToDTO(diagnostico.getTipoDiagnostico()));
                        dto.setCodDiagnostico(diagnostico.getCodDiagnostico());
                        dto.setDiagnostico(diagnostico.getDiagnostico());
                        dto.setTratamiento(diagnostico.getTratamiento());
                        dto.setIndicaciones(diagnostico.getIndicaciones());
                        dto.setExamenes(diagnostico.getExamenes());
                        dto.setMedicamentos(diagnostico.getMedicamentos());
                        return dto;
                    }).toList();
            paginacionResponse.setData(diagnosticoDTOS);
            paginacionResponse.setTotalItems(diagnosticosPage.getTotalElements());

            df.llenarRespuestaExitosa("Diagnosticos obtenidos con éxito", paginacionResponse);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<DiagnosticoDTO> postDiagnostico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<DiagnosticoDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            String bodyDesencriptado = df22.getData();
            DiagnosticoDTO diagnosticoDTO = new Gson().fromJson(bodyDesencriptado, DiagnosticoDTO.class);

            String ip = httpServletRequest.getRemoteAddr();

            Date fecha = new Date();

            Diagnostico diagnosticoDb = new Diagnostico();

            diagnosticoDb.setIpCrea(ip);
            diagnosticoDb.setFechaCreacion(fecha);
            diagnosticoDb.setUsuarioSistemaCrea(usuarioSistema);

            diagnosticoDb.setEvaluacionMedica(this.evaluacionMedicaRepository.findByTokenIdentificadorAndRemovido(diagnosticoDTO.getTokenIdEvaluacionMedica(), false));
            diagnosticoDb.setTipoDiagnostico(dtoToCatalogo(diagnosticoDTO.getTipoDiagnostico()));
            diagnosticoDb.setCodDiagnostico(diagnosticoDTO.getCodDiagnostico());
            diagnosticoDb.setDiagnostico(diagnosticoDTO.getDiagnostico());
            diagnosticoDb.setTratamiento(diagnosticoDTO.getTratamiento());
            diagnosticoDb.setIndicaciones(diagnosticoDTO.getIndicaciones());
            diagnosticoDb.setExamenes(diagnosticoDTO.getExamenes());
            diagnosticoDb.setMedicamentos(diagnosticoDTO.getMedicamentos());

            diagnosticoDb = this.diagnosticoRepository.save(diagnosticoDb);

            this.diagnosticoRepository.save(diagnosticoDb);

            diagnosticoDTO.setTokenIdentificador(diagnosticoDb.getTokenIdentificador());
            df.llenarRespuestaExitosa("Diagnostico creado con éxito", diagnosticoDTO);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<DiagnosticoDTO> updateDiagnostico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<DiagnosticoDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            String bodyDesencriptado = df22.getData();
            DiagnosticoDTO diagnosticoDTO = new Gson().fromJson(bodyDesencriptado, DiagnosticoDTO.class);

            String ip = httpServletRequest.getRemoteAddr();
            Date fecha = new Date();

            Diagnostico diagnosticoDb = this.diagnosticoRepository.findByTokenIdentificadorAndRemovido(diagnosticoDTO.getTokenIdentificador(), false);
            if (diagnosticoDb == null) {
                df.setMensaje("El diagnostico con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            diagnosticoDb.setIpEdita(ip);
            diagnosticoDb.setFechaEdicion(fecha);
            diagnosticoDb.setUsuarioSistemaEdita(usuarioSistema);

            diagnosticoDb.setTipoDiagnostico(dtoToCatalogo(diagnosticoDTO.getTipoDiagnostico()));
            diagnosticoDb.setCodDiagnostico(diagnosticoDTO.getCodDiagnostico());
            diagnosticoDb.setDiagnostico(diagnosticoDTO.getDiagnostico());
            diagnosticoDb.setTratamiento(diagnosticoDTO.getTratamiento());
            diagnosticoDb.setIndicaciones(diagnosticoDTO.getIndicaciones());
            diagnosticoDb.setExamenes(diagnosticoDTO.getExamenes());
            diagnosticoDb.setMedicamentos(diagnosticoDTO.getMedicamentos());

            diagnosticoDb = this.diagnosticoRepository.save(diagnosticoDb);

            this.diagnosticoRepository.save(diagnosticoDb);

            diagnosticoDTO.setTokenIdentificador(diagnosticoDb.getTokenIdentificador());
            df.llenarRespuestaExitosa("Diagnostico actualizado con éxito", diagnosticoDTO);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> deleteDiagnostico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            String bodyDesencriptado = df22.getData();
            DiagnosticoDTO diagnosticoDTO = new Gson().fromJson(bodyDesencriptado, DiagnosticoDTO.class);

            String ip = httpServletRequest.getRemoteAddr();

            Diagnostico diagnosticoDb = this.diagnosticoRepository.findByTokenIdentificadorAndRemovido(diagnosticoDTO.getTokenIdentificador(), false);
            if (diagnosticoDb == null) {
                df.setMensaje("El diagnóstico con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            Date fecha = new Date();

            diagnosticoDb.setIpElimina(ip);
            diagnosticoDb.setFechaEliminacion(fecha);
            diagnosticoDb.setUsuarioSistemaElimina(usuarioSistema);

            diagnosticoDb.setRemovido(true);

            this.diagnosticoRepository.save(diagnosticoDb);

            df.llenarRespuestaExitosa("Diagnostico eliminado con exito", diagnosticoDb.getRemovido());
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
