package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.IngresoCentroJuvenil;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.IngresoCentroJuvenilDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionConParametrosRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.FichaMedicaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.IngresoCentroJuvenilRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class IngresoCentroJuvenilServiceImpl implements IngresoCentroJuvenilService{

    private IngresoCentroJuvenilRepository ingresoCentroJuvenilRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private CatalogoRepository catalogoRepository;
    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private static final String ORDENAR_POR_NOMBRE = "nombre";

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<IngresoCentroJuvenilDTO>> getCentrosJuvenilesByTokenIdFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<IngresoCentroJuvenilDTO>> df = new RespuestaPorDefectoAuditoria<>();
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

            Page<IngresoCentroJuvenil> ingresosPage = this.ingresoCentroJuvenilRepository.findByFichaIdentificacion_TokenIdentificadorAndRemovido(tokenIdFichaMedica, false, pageable);
            PaginacionResponse<IngresoCentroJuvenilDTO> paginacionResponse = new PaginacionResponse<>();

            List<IngresoCentroJuvenilDTO> ingresoDTOs = ingresosPage.stream()
                    .map(ingreso -> {
                        IngresoCentroJuvenilDTO dto = new IngresoCentroJuvenilDTO();
                        dto.setTokenIdentificador(ingreso.getTokenIdentificador());
                        dto.setTokenIdFichaIdentificacion(ingreso.getFichaIdentificacion().getTokenIdentificador());
                        dto.setCentro(ingreso.getCentro());
                        dto.setMotivo(catalogoToDTO(ingreso.getMotivo()));
                        dto.setFechaIngreso(ingreso.getFechaIngreso());
                        dto.setFechaEgreso(ingreso.getFechaEgreso());
                        return dto;
                    }).toList();
            paginacionResponse.setData(ingresoDTOs);
            paginacionResponse.setTotalItems(ingresosPage.getTotalElements());

            df.llenarRespuestaExitosa("Ingreso a centros juveniles obtenidos con éxito", paginacionResponse);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<IngresoCentroJuvenilDTO> postIngresoCentroJuvenil(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<IngresoCentroJuvenilDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            IngresoCentroJuvenilDTO ingresoCentroJuvenilDTO = new Gson().fromJson(body, IngresoCentroJuvenilDTO.class);

            String ip = httpServletRequest.getRemoteAddr();

            Date fecha = new Date();
            IngresoCentroJuvenil ingresoDb = new IngresoCentroJuvenil();

            ingresoDb.setIpCrea(ip);
            ingresoDb.setFechaCreacion(fecha);
            ingresoDb.setUsuarioSistemaCrea(usuarioSistema);

            ingresoDb.setFichaIdentificacion(this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(ingresoCentroJuvenilDTO.getTokenIdFichaIdentificacion(), false));
            ingresoDb.setCentro(ingresoCentroJuvenilDTO.getCentro());
            ingresoDb.setMotivo(dtoToCatalogo(ingresoCentroJuvenilDTO.getMotivo()));
            ingresoDb.setFechaIngreso(ingresoCentroJuvenilDTO.getFechaIngreso());
            ingresoDb.setFechaEgreso(ingresoCentroJuvenilDTO.getFechaEgreso());
            ingresoDb = this.ingresoCentroJuvenilRepository.save(ingresoDb);

            this.ingresoCentroJuvenilRepository.save(ingresoDb);

            ingresoCentroJuvenilDTO.setTokenIdentificador(ingresoDb.getTokenIdentificador());
            df.llenarRespuestaExitosa("Ingreso a centro creado con exito", ingresoCentroJuvenilDTO);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<IngresoCentroJuvenilDTO> updateIngresoCentroJuvenil(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<IngresoCentroJuvenilDTO> df = new RespuestaPorDefectoAuditoria<>();
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
            IngresoCentroJuvenilDTO ingresoCentroJuvenilDTO = new Gson().fromJson(body, IngresoCentroJuvenilDTO.class);

            String ip = httpServletRequest.getRemoteAddr();

            IngresoCentroJuvenil ingresoDb = this.ingresoCentroJuvenilRepository.findByTokenIdentificadorAndRemovido(ingresoCentroJuvenilDTO.getTokenIdentificador(), false);
            if (ingresoDb == null) {
                df.setMensaje("El ingreso a centro con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            Date fecha = new Date();

            ingresoDb.setIpEdita(ip);
            ingresoDb.setFechaEdicion(fecha);
            ingresoDb.setUsuarioSistemaEdita(usuarioSistema);

            ingresoDb.setCentro(ingresoCentroJuvenilDTO.getCentro());
            ingresoDb.setMotivo(dtoToCatalogo(ingresoCentroJuvenilDTO.getMotivo()));
            ingresoDb.setFechaIngreso(ingresoCentroJuvenilDTO.getFechaIngreso());
            ingresoDb.setFechaEgreso(ingresoCentroJuvenilDTO.getFechaEgreso());

            this.ingresoCentroJuvenilRepository.save(ingresoDb);

            ingresoCentroJuvenilDTO.setTokenIdentificador(ingresoDb.getTokenIdentificador());

            df.llenarRespuestaExitosa("Ingreso a centro actualizado con exito", ingresoCentroJuvenilDTO);
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> deleteIngresoCentroJuvenil(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
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
            IngresoCentroJuvenilDTO ingresoCentroJuvenilDTO = new Gson().fromJson(body, IngresoCentroJuvenilDTO.class);

            String ip = httpServletRequest.getRemoteAddr();

            IngresoCentroJuvenil ingresoDb = this.ingresoCentroJuvenilRepository.findByTokenIdentificadorAndRemovido(ingresoCentroJuvenilDTO.getTokenIdentificador(), false);
            if (ingresoDb == null) {
                df.setMensaje("El ingreso a centro con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            Date fecha = new Date();

            ingresoDb.setIpElimina(ip);
            ingresoDb.setFechaEliminacion(fecha);
            ingresoDb.setUsuarioSistemaElimina(usuarioSistema);

            ingresoDb.setRemovido(true);

            this.ingresoCentroJuvenilRepository.save(ingresoDb);

            df.llenarRespuestaExitosa("Ingreso a centro eliminado con exito", ingresoDb.getRemovido());
        } catch (Exception ex){
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    private Catalogo dtoToCatalogo(CatalogoDTO catalogoDTO){
        if (catalogoDTO == null) {
            return null;
        }

        Catalogo catalogo = this.catalogoRepository.findByTokenIdentificadorAndRemovido(catalogoDTO.getTokenIdentificador(), false);

        return catalogo;
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
}
