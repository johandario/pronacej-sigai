package net.latinus.sistema.integral.gestion.seguridad.service.param;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ParametroDelSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.RSA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class ParametroDelSistemaServiceImpl implements ParametroDelSistemaService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private JwtProviderService jwtProviderService;
    private EmpresaRepository empresaRepository;

    private final LogService logService = new LogService(ParametroDelSistemaServiceImpl.class);

    @Override
    public ParametroDelSistema encontrarPorNemonicoYEmpresa(String nemonico, Long idEmpresa) {

        Pageable pageable = PageRequest.of(0, 3, Sort.by("idParametroDelSistema").descending());
        Page<ParametroDelSistema> parametroDelSistemaPage = this.parametroDelSistemaRepository.findByNemonicoAndRemovidoAndEmpresaIdEmpresa(
                nemonico, false, idEmpresa, pageable
        );
        return parametroDelSistemaPage.stream().findFirst().orElse(null);
    }

    @Override
    public RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO> encontrarPorNemonicoYEmpresa2(String nemonico, String tokenIdentificadorEmpresa) {
        RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(tokenIdentificadorEmpresa, false);

            ParametroDelSistema parametroDelSistema = this.encontrarPorNemonicoYEmpresa(nemonico, empresa != null ? empresa.getIdEmpresa() : null);

            if (parametroDelSistema == null) {
                df.setMensaje("No se encontro el parametro del sistema");
                return df;
            }

            ParametroDelSistemaDTO parametroDelSistemaDTO = new ParametroDelSistemaDTO();
            parametroDelSistemaDTO.setTokenIdentificador(parametroDelSistema.getTokenIdentificador());
            parametroDelSistemaDTO.setNemonico(parametroDelSistema.getNemonico());
            parametroDelSistemaDTO.setValor(parametroDelSistema.getValor());
            parametroDelSistemaDTO.setTokenIdentificadorEmpresa(empresa != null ? empresa.getTokenIdentificador() : null);
            parametroDelSistemaDTO.setDescripcion(parametroDelSistema.getDescripcion());
            parametroDelSistemaDTO.setValorExterno(parametroDelSistema.getValorExterno());
            parametroDelSistemaDTO.setCodigoExterno(parametroDelSistema.getCodigoExterno());
            parametroDelSistemaDTO.setNombre(parametroDelSistema.getNombre());
            ParametroDelSistema padre = parametroDelSistema.getParametroDelSistemaPadre();
            parametroDelSistemaDTO.setTokenIdentificadorPadre(padre != null ? padre.getTokenIdentificador() : null);

            df.llenarRespuestaExitosa("Se encontro con exito", parametroDelSistemaDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


    @Override
    public RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO>>> crearVariosDirecto(
            HttpServletRequest httpServletRequest, List<ParametroDelSistemaDTO> parametroDelSistemaDTOList) {
        RespuestaPorDefectoAuditoria<List<RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO>>> df =
                new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            String ip = httpServletRequest.getRemoteAddr();

            List<RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO>> respuestaPorDefectoAuditoriaList = new ArrayList<>();

            for (ParametroDelSistemaDTO parametroDelSistemaDTO : parametroDelSistemaDTOList) {
                RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO> respuestaPorDefectoAuditoria = new RespuestaPorDefectoAuditoria<>();

                ParametroDelSistema parametroDelSistema = new ParametroDelSistema();
                parametroDelSistema.setParametroDelSistemaPadre(this.parametroDelSistemaRepository.findByTokenIdentificadorAndRemovido(
                        parametroDelSistemaDTO.getTokenIdentificadorPadre(), false));
                parametroDelSistema.setEmpresa(this.empresaRepository.findByTokenIdentificadorAndRemovido(parametroDelSistemaDTO.getTokenIdentificadorEmpresa(), false));
                parametroDelSistema.setDescripcion(parametroDelSistemaDTO.getDescripcion());
                parametroDelSistema.setIpCrea(ip);
                parametroDelSistema.setNombre(parametroDelSistemaDTO.getNombre());
                parametroDelSistema.setCodigoExterno(parametroDelSistemaDTO.getCodigoExterno());
                parametroDelSistema.setDescripcion(parametroDelSistemaDTO.getDescripcion());
                parametroDelSistema.setNemonico(parametroDelSistemaDTO.getNemonico());
                parametroDelSistema.setValor(parametroDelSistemaDTO.getValor());
                parametroDelSistema.setValorExterno(parametroDelSistemaDTO.getValorExterno());
                parametroDelSistema = this.parametroDelSistemaRepository.save(parametroDelSistema);

                parametroDelSistemaDTO.setTokenIdentificador(parametroDelSistema.getTokenIdentificador());

                respuestaPorDefectoAuditoria.llenarRespuestaExitosa("Se ha creado con exito el catalogo: " +
                        parametroDelSistema.getNombre(), parametroDelSistemaDTO);

                respuestaPorDefectoAuditoriaList.add(respuestaPorDefectoAuditoria);
            }

            df.llenarRespuestaExitosa("Se han enviado a crear un totoal de: " +
                    respuestaPorDefectoAuditoriaList.size() + " parametro(s) del sistema", respuestaPorDefectoAuditoriaList);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


    @Override
    public RespuestaPorDefectoAuditoria<List<ParametroDelSistemaDTO>> obtenerParametrosDelSistemaGenerales(HttpServletRequest httpServletRequest,
                                                                                                           BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<List<ParametroDelSistemaDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RSA rsa = new RSA(
                    this.parametroDelSistemaRepository
            );
            Aes aes = new Aes();

            String body = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository,
                    null).getData();
            ParametroDelSistemaDTO parametroDelSistemaDTO = new Gson().fromJson(body, ParametroDelSistemaDTO.class);

            List<ParametroDelSistema> parametroDelSistemaList = this.parametroDelSistemaRepository.
                    findByEmpresaTokenIdentificadorAndParametroDelSistemaPadreNemonicoAndRemovido(
                            parametroDelSistemaDTO.getTokenIdentificadorEmpresa(),
                            parametroDelSistemaDTO.getNemonico(),
                            false
                    );
            List<ParametroDelSistemaDTO> parametroDelSistemaDTOList = new ArrayList<>();

            for (ParametroDelSistema parametroDelSistema : parametroDelSistemaList) {
                parametroDelSistemaDTOList.add(
                        this.obtenerParamDTO(parametroDelSistema));

            }

            df.llenarRespuestaExitosa("Se han encontrado un total de: " +
                    parametroDelSistemaList.size() + " parametros del sistema", parametroDelSistemaDTOList);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<String> obtenerValorParam(String nemonico, String tokenIdentificadorEmpresa, Boolean base64) {
        RespuestaPorDefectoAuditoria<String> df = new RespuestaPorDefectoAuditoria<>();

        try {
            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(tokenIdentificadorEmpresa, false);

            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(
                    nemonico, base64 != null && base64 ? null : empresa, false
            );

            if (parametroDelSistema == null) {
                df.setMensaje("No se ha encontrado la información");
                return df;
            }
            String valor = parametroDelSistema.getValor();

            if (base64 != null && base64) {
                valor = Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
            }
            df.llenarRespuestaExitosa(
                    "Se ha encontrado el valor correctamente",
                    valor
            );

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO> obtenerPorNemonico(HttpServletRequest httpServletRequest, String nemonico) {
        RespuestaPorDefectoAuditoria<ParametroDelSistemaDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(df2.getLogOut());
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();

            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(nemonico, empresa, false);

            if (parametroDelSistema == null) {
                df.setMensaje("No se encontro el parametro del sistema con nemonico: " + nemonico);
                return df;
            }

            df.llenarRespuestaExitosa("Parametro del sistema con nemonico: " +
                    nemonico + " encontrado con éxito", parametroDelSistema.convertirADTO());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private ParametroDelSistemaDTO obtenerParamDTO(ParametroDelSistema parametroDelSistema) {
        if (parametroDelSistema == null) {
            return null;
        }

        ParametroDelSistemaDTO parametroDelSistemaDTO = new ParametroDelSistemaDTO();
        parametroDelSistemaDTO.setTokenIdentificador(parametroDelSistema.getTokenIdentificador());
        parametroDelSistemaDTO.setNombre(parametroDelSistema.getNombre());
        parametroDelSistemaDTO.setDescripcion(parametroDelSistema.getDescripcion());
        parametroDelSistemaDTO.setCodigoExterno(parametroDelSistema.getCodigoExterno());
        parametroDelSistemaDTO.setNemonico(parametroDelSistema.getNemonico());

        ParametroDelSistema parametroDelSistemaPadre = parametroDelSistema.getParametroDelSistemaPadre();
        Empresa empresa = parametroDelSistema.getEmpresa();

        parametroDelSistemaDTO.setTokenIdentificadorEmpresa(empresa != null ? empresa.getTokenIdentificador() : null);
        parametroDelSistemaDTO.setValorExterno(parametroDelSistema.getValorExterno());
        parametroDelSistemaDTO.setValor(parametroDelSistema.getValor());
        parametroDelSistemaDTO.setFechaCreacion(parametroDelSistema.getFechaCreacion());
        parametroDelSistemaDTO.setIpCrea(parametroDelSistema.getIpCrea());

        if (parametroDelSistemaPadre != null) {
            parametroDelSistemaDTO.setTokenIdentificadorPadre(parametroDelSistemaPadre.getTokenIdentificador());
        }

        List<ParametroDelSistema> hijos = this.parametroDelSistemaRepository.
                findByParametroDelSistemaPadreTokenIdentificadorAndRemovido(parametroDelSistema.getTokenIdentificador(),
                        false);

        if (hijos != null && !hijos.isEmpty()) {
            List<ParametroDelSistemaDTO> hijosp = new ArrayList<>();
            parametroDelSistemaDTO.setHijos2(hijosp);

            for (ParametroDelSistema hijo : hijos) {
                parametroDelSistemaDTO.getHijos().add(
                        this.obtenerParamDTO(hijo)
                );
            }
        }

        return parametroDelSistemaDTO;
    }


}
