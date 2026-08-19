package net.latinus.sistema.integral.gestion.seguridad.service.IA.ficha_medica;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ficha_medica.AntecedenteFamiliar;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica.AntecedenteFamiliarDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ficha_medica.AntecedenteFamiliarRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class AntecedenteFamiliarServiceImpl implements AntecedenteFamiliarService {

    private AntecedenteFamiliarRepository antecedenteFamiliarRepository;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private CatalogoRepository catalogoRepository;
    private JwtProviderService jwtProviderService;

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private static final String ORDENAR_POR_NOMBRE = "nombre";

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<AntecedenteFamiliarDTO>> getAntecedenteFamiliarByTokenIdFichaMedica(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<AntecedenteFamiliarDTO>> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
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

            Page<AntecedenteFamiliar> antecedentesPage = this.antecedenteFamiliarRepository.findByFichaIdentificacion_TokenIdentificadorAndRemovido(tokenIdFichaMedica, false, pageable);
            PaginacionResponse<AntecedenteFamiliarDTO> paginacionResponse = new PaginacionResponse<>();

            List<AntecedenteFamiliarDTO> antecedentesDTOs = antecedentesPage.stream()
                    .map(antecedente -> {
                        AntecedenteFamiliarDTO dto = new AntecedenteFamiliarDTO();
                        dto.setTokenIdentificador(antecedente.getTokenIdentificador());
                        dto.setTokenIdFichaIdentificacion(antecedente.getFichaIdentificacion().getTokenIdentificador());
                        dto.setEnfermedad(catalogoToDTO(antecedente.getEnfermedad()));
                        dto.setParentesco(catalogoToDTO(antecedente.getParentezco()));
                        return dto;
                    }).toList();
            paginacionResponse.setData(antecedentesDTOs);
            paginacionResponse.setTotalItems(antecedentesPage.getTotalElements());

            df.llenarRespuestaExitosa("Antecedentes familiares obtenidos con éxito", paginacionResponse);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<AntecedenteFamiliarDTO> postAntecedenteFamiliar(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<AntecedenteFamiliarDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            AntecedenteFamiliarDTO antecedenteFamiliarDTO = new Gson().fromJson(body, AntecedenteFamiliarDTO.class);

            String ip = httpServletRequest.getRemoteAddr();

            Date fecha = new Date();

            AntecedenteFamiliar antecedenteDb = new AntecedenteFamiliar();

            antecedenteDb.setIpCrea(ip);
            antecedenteDb.setFechaCreacion(fecha);
            antecedenteDb.setUsuarioSistemaCrea(usuarioSistema);

            antecedenteDb.setFichaIdentificacion(this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(antecedenteFamiliarDTO.getTokenIdFichaIdentificacion(), false));

            antecedenteDb.setParentezco(dtoToCatalogo(antecedenteFamiliarDTO.getParentesco()));
            antecedenteDb.setEnfermedad(dtoToCatalogo(antecedenteFamiliarDTO.getEnfermedad()));

            antecedenteDb = this.antecedenteFamiliarRepository.save(antecedenteDb);

            this.antecedenteFamiliarRepository.save(antecedenteDb);

            antecedenteFamiliarDTO.setTokenIdentificador(antecedenteDb.getTokenIdentificador());
            df.llenarRespuestaExitosa("Antecedente familiar creado con éxito", antecedenteFamiliarDTO);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<AntecedenteFamiliarDTO> updateAntecedenteFamiliar(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<AntecedenteFamiliarDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            AntecedenteFamiliarDTO antecedenteFamiliarDTO = new Gson().fromJson(body, AntecedenteFamiliarDTO.class);

            String ip = httpServletRequest.getRemoteAddr();
            Date fecha = new Date();

            AntecedenteFamiliar antecedenteDb = this.antecedenteFamiliarRepository.findByTokenIdentificadorAndRemovido(antecedenteFamiliarDTO.getTokenIdentificador(), false);
            if (antecedenteDb == null) {
                df.setMensaje("El antecedente familiar con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            antecedenteDb.setIpEdita(ip);
            antecedenteDb.setFechaEdicion(fecha);
            antecedenteDb.setUsuarioSistemaEdita(usuarioSistema);

            antecedenteDb.setParentezco(dtoToCatalogo(antecedenteFamiliarDTO.getParentesco()));
            antecedenteDb.setEnfermedad(dtoToCatalogo(antecedenteFamiliarDTO.getEnfermedad()));

            antecedenteDb = this.antecedenteFamiliarRepository.save(antecedenteDb);

            this.antecedenteFamiliarRepository.save(antecedenteDb);

            antecedenteFamiliarDTO.setTokenIdentificador(antecedenteDb.getTokenIdentificador());
            df.llenarRespuestaExitosa("Antecedente familiar actualizado con éxito", antecedenteFamiliarDTO);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> deleteAntecedenteFamiliar(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            UsuarioSistema usuarioSistema = bodyJwtValido.getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            AntecedenteFamiliarDTO antecedenteFamiliarDTO = new Gson().fromJson(body, AntecedenteFamiliarDTO.class);

            String ip = httpServletRequest.getRemoteAddr();

            AntecedenteFamiliar antecedenteDb = this.antecedenteFamiliarRepository.findByTokenIdentificadorAndRemovido(antecedenteFamiliarDTO.getTokenIdentificador(), false);
            if (antecedenteDb == null) {
                df.setMensaje("El antecedente familiar con el token proporcionado no existe.");
                df.setExito(false);
                return df;
            }

            Date fecha = new Date();

            antecedenteDb.setIpElimina(ip);
            antecedenteDb.setFechaEliminacion(fecha);
            antecedenteDb.setUsuarioSistemaElimina(usuarioSistema);

            antecedenteDb.setRemovido(true);

            this.antecedenteFamiliarRepository.save(antecedenteDb);

            df.llenarRespuestaExitosa("Antecedente familiar eliminado con exito", antecedenteDb.getRemovido());
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    private Catalogo dtoToCatalogo(CatalogoDTO catalogoDTO) {
        if (catalogoDTO == null) {
            return null;
        }

        Catalogo catalogo = this.catalogoRepository.findByTokenIdentificadorAndRemovido(catalogoDTO.getTokenIdentificador(), false);

        return catalogo;
    }

    private CatalogoDTO catalogoToDTO(Catalogo catalogo) {
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
