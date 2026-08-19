package net.latinus.sistema.integral.gestion.seguridad.service.EJE.seguimiento_medico;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.Diagnostico;
import net.latinus.sistema.integral.gestion.seguridad.entities.EJE.seguimiento_medico.EstadoNutricional;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.DiagnosticoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.EJE.seguimiento_medico.EstadoNutricionalDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.EJE.seguimiento_medico.EstadoNutricionalRepository;
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
public class EstadoNutricionalServiceImpl implements EstadoNutricionalService{

    private CatalogoRepository catalogoRepository;
    private EvaluacionMedicaRepository evaluacionMedicaRepository;
    private EstadoNutricionalRepository estadoNutricionalRepository;
    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<EstadoNutricionalDTO>> getEstadoNutricionalByIdEvaluacionMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<EstadoNutricionalDTO>> df = new RespuestaPorDefectoAuditoria<>();
        try{
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if(!df22.isExito()){
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(body, PaginacionRequest.class);
            String tokenIdFichaMedica = paginacionRequest.getTokenIdentificador();

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize()
            );

            Page<EstadoNutricional> estadoNutricionalPage = this.estadoNutricionalRepository.findByEvaluacionMedica_TokenIdentificadorAndRemovido(tokenIdFichaMedica, false, pageable);
            PaginacionResponse<EstadoNutricionalDTO> paginacionResponse = new PaginacionResponse<>();

            List<EstadoNutricionalDTO> estadoNutricionalDTOS = estadoNutricionalPage.stream()
                    .map(estadoNutricional -> {
                        EstadoNutricionalDTO dto = new EstadoNutricionalDTO();
                        dto.setTokenIdentificador(estadoNutricional.getTokenIdentificador());
                        dto.setTokenIdEvaluacionMedica(estadoNutricional.getEvaluacionMedica().getTokenIdentificador());

                        dto.setCriterio(catalogoToDTO(estadoNutricional.getCriterio()));
                        dto.setGrado(catalogoToDTO(estadoNutricional.getGrado()));

                        return dto;
                    }).toList();
            paginacionResponse.setData(estadoNutricionalDTOS);
            paginacionResponse.setTotalItems(estadoNutricionalPage.getTotalElements());

            df.llenarRespuestaExitosa("Estados nutricionales obtenidos con éxito", paginacionResponse);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EstadoNutricionalDTO> postEstadoNutricional(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EstadoNutricionalDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            EstadoNutricionalDTO estadoNutricionalDTO = new Gson().fromJson(body, EstadoNutricionalDTO.class);

            String ip = httpServletRequest.getRemoteAddr();

            Date fecha = new Date();

            EstadoNutricional estadoNutricionalDb = new EstadoNutricional();

            estadoNutricionalDb.setIpCrea(ip);
            estadoNutricionalDb.setFechaCreacion(fecha);
            estadoNutricionalDb.setUsuarioSistemaCrea(usuarioSistema);

            estadoNutricionalDb.setEvaluacionMedica(this.evaluacionMedicaRepository.findByTokenIdentificadorAndRemovido(estadoNutricionalDTO.getTokenIdEvaluacionMedica(), false));
            estadoNutricionalDb.setCriterio(dtoToCatalogo(estadoNutricionalDTO.getCriterio()));
            estadoNutricionalDb.setGrado(dtoToCatalogo(estadoNutricionalDTO.getGrado()));


            estadoNutricionalDb = this.estadoNutricionalRepository.save(estadoNutricionalDb);

            this.estadoNutricionalRepository.save(estadoNutricionalDb);

            estadoNutricionalDTO.setTokenIdentificador(estadoNutricionalDb.getTokenIdentificador());
            df.llenarRespuestaExitosa("Estado nutricional creado con éxito", estadoNutricionalDTO);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<EstadoNutricionalDTO> updateEstadoNutricional(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<EstadoNutricionalDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            EstadoNutricionalDTO estadoNutricionalDTO = new Gson().fromJson(body, EstadoNutricionalDTO.class);

            String ip = httpServletRequest.getRemoteAddr();
            Date fecha = new Date();

            EstadoNutricional estadoNutricionalDb = this.estadoNutricionalRepository.findByTokenIdentificadorAndRemovido(estadoNutricionalDTO.getTokenIdentificador(), false);
            if (estadoNutricionalDb == null) {
                df.setMensaje("El estado nutricional con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            estadoNutricionalDb.setIpEdita(ip);
            estadoNutricionalDb.setFechaEdicion(fecha);
            estadoNutricionalDb.setUsuarioSistemaEdita(usuarioSistema);

            estadoNutricionalDb.setCriterio(dtoToCatalogo(estadoNutricionalDTO.getCriterio()));
            estadoNutricionalDb.setGrado(dtoToCatalogo(estadoNutricionalDTO.getGrado()));


            estadoNutricionalDb = this.estadoNutricionalRepository.save(estadoNutricionalDb);

            this.estadoNutricionalRepository.save(estadoNutricionalDb);

            estadoNutricionalDTO.setTokenIdentificador(estadoNutricionalDb.getTokenIdentificador());
            df.llenarRespuestaExitosa("Estado nutricional actualizado con éxito", estadoNutricionalDTO);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> deleteEstadoNutricional(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            EstadoNutricionalDTO estadoNutricionalDTO = new Gson().fromJson(body, EstadoNutricionalDTO.class);

            String ip = httpServletRequest.getRemoteAddr();

            EstadoNutricional estadoNutricionalDb = this.estadoNutricionalRepository.findByTokenIdentificadorAndRemovido(estadoNutricionalDTO.getTokenIdentificador(), false);
            if (estadoNutricionalDb == null) {
                df.setMensaje("El estado nutricional con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            Date fecha = new Date();

            estadoNutricionalDb.setIpElimina(ip);
            estadoNutricionalDb.setFechaEliminacion(fecha);
            estadoNutricionalDb.setUsuarioSistemaElimina(usuarioSistema);

            estadoNutricionalDb.setRemovido(true);

            this.estadoNutricionalRepository.save(estadoNutricionalDb);

            df.llenarRespuestaExitosa("Estado nutricional eliminado con exito", estadoNutricionalDb.getRemovido());
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
