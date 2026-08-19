package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionSocial;
import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionSocialArtefacto;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EvaluacionSocialArtefactoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EvaluacionSocialArtefactoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EvaluacionSocialRepository;

@Service
@Transactional
@AllArgsConstructor
public class EvaluacionSocialArtefactoServiceImpl implements EvaluacionSocialArtefactoService {
    
    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;
    private EvaluacionSocialArtefactoRepository evaluacionSocialArtefactoRepository;
    private EvaluacionSocialRepository evaluacionSocialRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSocialArtefactoDTO>> obtenerArtefactosPorEvaluacionSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EvaluacionSocialArtefactoDTO>> df = new RespuestaPorDefectoAuditoria<>();
        
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
            String body = df22.getData();

            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            Empresa empresa = df2.getData().getEmpresa();
            
            // Validar que el token no esté vacío
            if (paginacionRequest.getTokenIdentificador() == null || 
                paginacionRequest.getTokenIdentificador().trim().isEmpty() ||
                "0".equals(paginacionRequest.getTokenIdentificador())) {
                df.setMensaje("Token identificador de evaluación social no válido");
                return df;
            }
            
            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize(),
                    Sort.by("idEvaluacionSocialArtefacto").descending()
            );

            Page<EvaluacionSocialArtefacto> evaluacionSocialArtefactoPage = this.evaluacionSocialArtefactoRepository.findByEvaluacionSocialTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(
                    paginacionRequest.getTokenIdentificador(), empresa.getIdEmpresa(), false, pageable);
            
            PaginacionResponse<EvaluacionSocialArtefactoDTO> paginacionResponse = new PaginacionResponse<>();
            List<EvaluacionSocialArtefactoDTO> evaluacionSocialArtefactoDTOList = new ArrayList<>();
            
            for (EvaluacionSocialArtefacto evaluacionSocialArtefacto : evaluacionSocialArtefactoPage.toList()) {
                EvaluacionSocialArtefactoDTO evaluacionSocialArtefactoDTO = new EvaluacionSocialArtefactoDTO();
                evaluacionSocialArtefactoDTO.setTokenIdentificador(evaluacionSocialArtefacto.getTokenIdentificador());
                evaluacionSocialArtefactoDTO.setTokenIdentificadorEmpresa(evaluacionSocialArtefacto.getEmpresa().getTokenIdentificador());
                evaluacionSocialArtefactoDTO.setTokenIdentificadorEvaluacionSocial(evaluacionSocialArtefacto.getEvaluacionSocial().getTokenIdentificador());
                
                if(evaluacionSocialArtefacto.getArtefactosVivienda()!=null) {
                    evaluacionSocialArtefactoDTO.setTokenIdentificadorArtefactosVivienda(evaluacionSocialArtefacto.getArtefactosVivienda().getTokenIdentificador());
                }
                evaluacionSocialArtefactoDTO.setCantidad(evaluacionSocialArtefacto.getCantidad());
                evaluacionSocialArtefactoDTO.setFechaCreacion(evaluacionSocialArtefacto.getFechaCreacion());

                evaluacionSocialArtefactoDTOList.add(evaluacionSocialArtefactoDTO);
            }

            paginacionResponse.setData(evaluacionSocialArtefactoDTOList);
            paginacionResponse.setTotalItems(evaluacionSocialArtefactoPage.getTotalElements());
            
            df.llenarRespuestaExitosa("Se han encontrado un total de: "
                            + evaluacionSocialArtefactoDTOList.size() + " de: " + evaluacionSocialArtefactoPage.getTotalElements() + " artefactos disponibles",
                    paginacionResponse);
                    
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EvaluacionSocialArtefactoDTO> crearArtefactoPorEvaluacionSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EvaluacionSocialArtefactoDTO> df = new RespuestaPorDefectoAuditoria<>();

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
            String body = df22.getData();
            
            Empresa empresa = df2.getData().getEmpresa();

            EvaluacionSocialArtefactoDTO evaluacionSocialArtefactoDTO = new Gson().fromJson(body, EvaluacionSocialArtefactoDTO.class);
            
            evaluacionSocialArtefactoDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
            
            String ip = httpServletRequest.getRemoteAddr();
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();
            
            EvaluacionSocialArtefacto evaluacionSocialArtefacto;
            
            if(evaluacionSocialArtefactoDTO.getEsEdicion()){
                evaluacionSocialArtefacto = evaluacionSocialArtefactoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialArtefactoDTO.getTokenIdentificador(), Boolean.FALSE);
                if (evaluacionSocialArtefacto == null) {
                    df.setMensaje("El artefacto a editar no existe o ya fue eliminado anteriormente");
                    return df;
                }
                evaluacionSocialArtefacto.setFechaEdicion(new Date());
                evaluacionSocialArtefacto.setIpEdita(ip);
                evaluacionSocialArtefacto.setUsuarioSistemaEdita(usuarioLogin);
            } else {                
                evaluacionSocialArtefacto = new EvaluacionSocialArtefacto();
                evaluacionSocialArtefacto.setFechaCreacion(new Date());
                evaluacionSocialArtefacto.setIpCrea(ip);
                evaluacionSocialArtefacto.setUsuarioSistemaCrea(usuarioLogin);
                evaluacionSocialArtefacto.setEmpresa(empresa);
                
                EvaluacionSocial evaluacionSocial = evaluacionSocialRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialArtefactoDTO.getTokenIdentificadorEvaluacionSocial(), Boolean.FALSE);
                if (evaluacionSocial == null) {
                    df.setMensaje("La evaluación social asociada no existe");
                    return df;
                }
                evaluacionSocialArtefacto.setEvaluacionSocial(evaluacionSocial);
            }
            
            Catalogo artefactosVivienda = catalogoRepository.findByTokenIdentificadorAndRemovido(evaluacionSocialArtefactoDTO.getTokenIdentificadorArtefactosVivienda(), Boolean.FALSE);
            if (artefactosVivienda == null) {
                df.setMensaje("El tipo de artefacto seleccionado no existe");
                return df;
            }
            
            evaluacionSocialArtefacto.setArtefactosVivienda(artefactosVivienda);
            evaluacionSocialArtefacto.setCantidad(evaluacionSocialArtefactoDTO.getCantidad());

            evaluacionSocialArtefacto = this.evaluacionSocialArtefactoRepository.save(evaluacionSocialArtefacto);
            evaluacionSocialArtefactoDTO.setTokenIdentificador(evaluacionSocialArtefacto.getTokenIdentificador());

            df.llenarRespuestaExitosa("Se " + (evaluacionSocialArtefactoDTO.getEsEdicion() ? "editó" : "creó") + " con éxito el artefacto", evaluacionSocialArtefactoDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarArtefactoPorEvaluacionSocial(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioSistemaLogin = df2.getData().getUsuarioSistema();
            String ip = httpServletRequest.getRemoteAddr();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();

            EvaluacionSocialArtefactoDTO evaluacionSocialArtefactoDTO = new Gson().fromJson(bodyString, EvaluacionSocialArtefactoDTO.class);

            EvaluacionSocialArtefacto evaluacionSocialArtefacto = this.evaluacionSocialArtefactoRepository.findByTokenIdentificadorAndRemovido(
                    evaluacionSocialArtefactoDTO.getTokenIdentificador(), false
            );

            if (evaluacionSocialArtefacto == null) {
                df.setMensaje("El artefacto no fue encontrado o ya fue eliminado anteriormente");
                return df;
            }

            Date fecha = new Date();
            evaluacionSocialArtefacto.setRemovido(true);
            evaluacionSocialArtefacto.setIpElimina(ip);
            evaluacionSocialArtefacto.setUsuarioSistemaElimina(usuarioSistemaLogin);
            evaluacionSocialArtefacto.setFechaEliminacion(fecha);

            this.evaluacionSocialArtefactoRepository.save(evaluacionSocialArtefacto);

            df.llenarRespuestaExitosa("Se ha eliminado con éxito del sistema el artefacto", true);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}
