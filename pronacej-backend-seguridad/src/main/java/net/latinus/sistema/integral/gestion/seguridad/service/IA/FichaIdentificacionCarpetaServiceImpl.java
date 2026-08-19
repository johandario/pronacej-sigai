package net.latinus.sistema.integral.gestion.seguridad.service.IA;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaIdentificacionCarpetaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.doc.ContenidoCarpetaResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.doc.RutaContenidoCarpetaResponse;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class FichaIdentificacionCarpetaServiceImpl implements FichaIdentificacionCarpetaService {

    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;
    private JwtProviderService jwtProviderService;
    private FichaIdentificacionRepository fichaIdentificacionRepository;
    private DocumentoRepository documentoRepository;
    private CarpetaRepository carpetaRepository;
    private CatalogoRepository catalogoRepository;

    private final LogService logService = new LogService(this.getClass());

    @Override
    public RespuestaPorDefectoAuditoria<ContenidoCarpetaResponse>
    obtenerInformacionDeCarpetaPrincipalDeLaFichaDeIndentificacion(HttpServletRequest httpServletRequest,
                                                                   FichaIdentificacionCarpetaRequest fichaIdentificacionCarpetaRequest) {

        RespuestaPorDefectoAuditoria<ContenidoCarpetaResponse> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(df2.getLogOut());
                df.setSinAcceso(df2.getSinAcceso());
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            FichaIdentificacion fichaIdentificacion = this.fichaIdentificacionRepository.findByTokenIdentificadorAndRemovido(
                    fichaIdentificacionCarpetaRequest.getTokenIdentificadorFichaPrincipal(), false
            );

            if (fichaIdentificacion == null) {
                df.setMensaje("La ficha de identificación no existe");
                return df;
            }

            String tokenIdentificador = fichaIdentificacionCarpetaRequest.getTokenIdentificadorFichaPrincipalCarpeta();
            FichaIdentificacionCarpeta fichaIdentificacionCarpeta;

            if (tokenIdentificador == null || tokenIdentificador.isBlank()) {
                List<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaList = this.fichaIdentificacionCarpetaRepository.
                        findByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteAndRemovidoOrderByIdFichaIdentificacionCarpeta(
                                fichaIdentificacion.getTokenIdentificador(),
                                null, false
                        );

                if (fichaIdentificacionCarpetaList.isEmpty()) {
                    df.setMensaje("La ficha de identificación no tiene una carpeta principal asignada");
                    return df;
                }

                if (fichaIdentificacionCarpetaList.size() > 1) {
                    this.logService.info("La ficha de identificación: " + fichaIdentificacion.getIdFichaIdentificacion() + " tiene asociada más de una carpeta principal");
                }

                fichaIdentificacionCarpeta = fichaIdentificacionCarpetaList.get(0);
            } else {
                fichaIdentificacionCarpeta = this.fichaIdentificacionCarpetaRepository.
                        findByTokenIdentificadorAndFichaIdentificacionTokenIdentificadorAndRemovido(
                                tokenIdentificador, fichaIdentificacion.getTokenIdentificador(),
                                false
                        );
            }

            if (fichaIdentificacionCarpeta == null) {
                df.setMensaje("La ficha de identificación no tiene la carpeta asociada solicitada");
                return df;
            }

            Carpeta carpeta = fichaIdentificacionCarpeta.getCarpeta();

            if (carpeta == null) {
                df.setMensaje("La relación con la carpeta principal de la ficha de identificación no existe");
                return df;
            }

            ContenidoCarpetaResponse contenidoCarpetaResponse = this.crearConCarpeta(carpeta);
            contenidoCarpetaResponse.setTokenIdentificadorFichaPrincipalCarpeta(fichaIdentificacionCarpeta.getTokenIdentificador());

            List<Carpeta> carpetasHijas = this.carpetaRepository.findByCarpetaPadreTokenIdentificadorAndRemovidoOrderByNombreCliente(
                    carpeta.getTokenIdentificador(),
                    false
            );

            if (!carpetasHijas.isEmpty()) {
                List<ContenidoCarpetaResponse> contenidoCarpetaResponseCarpetaHijas = new ArrayList<>();

                for (Carpeta carpetaHija : carpetasHijas) {
                    ContenidoCarpetaResponse contenidoCarpetaResponse2 = this.crearConCarpeta(carpetaHija);
                    List<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaList =
                            this.fichaIdentificacionCarpetaRepository.findByCarpetaAndFichaIdentificacionAndRemovidoOrderByIdFichaIdentificacionCarpetaDesc(
                                    carpetaHija,
                                    fichaIdentificacion,
                                    false
                            );
                    if (!fichaIdentificacionCarpetaList.isEmpty()) {
                        FichaIdentificacionCarpeta fichaIdentificacionCarpeta1 = fichaIdentificacionCarpetaList.get(0);
                        contenidoCarpetaResponse2.setTokenIdentificadorFichaPrincipalCarpeta(fichaIdentificacionCarpeta1.getTokenIdentificador());
                    }
                    contenidoCarpetaResponseCarpetaHijas.add(contenidoCarpetaResponse2);
                }

                contenidoCarpetaResponse.setCarpetas(contenidoCarpetaResponseCarpetaHijas);
            }

            List<Documento> documentoList = this.documentoRepository.findByCarpetaTokenIdentificadorAndRemovido(
                    carpeta.getTokenIdentificador(),
                    false
            );

            if (!documentoList.isEmpty()) {
                List<ContenidoCarpetaResponse> contenidoDocumentos = new ArrayList<>();

                for (Documento documento : documentoList) {
                    contenidoDocumentos.add(this.crearConDocumento(documento));
                }

                contenidoCarpetaResponse.setDocumentos(contenidoDocumentos);
            }

            contenidoCarpetaResponse.setRutaContenidoCarpetaResponseList(
                    this.contruirRuta(fichaIdentificacionCarpeta)
            );

            df.llenarRespuestaExitosa("Se ha encontrado con éxito la información de la carpeta: "
                    + carpeta.getNombreCliente(), contenidoCarpetaResponse);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private ContenidoCarpetaResponse crearConCarpeta(Carpeta carpeta) {
        ContenidoCarpetaResponse contenidoCarpetaResponse = new ContenidoCarpetaResponse();
        contenidoCarpetaResponse.setTokenIdentificadorCarpeta(carpeta.getTokenIdentificador());
        contenidoCarpetaResponse.setDescripcion(carpeta.getDescripcion());

        contenidoCarpetaResponse.setCantidadDeDocumentos(this.documentoRepository.countByCarpetaTokenIdentificadorAndRemovido(carpeta.getTokenIdentificador()
                , false));
        contenidoCarpetaResponse.setNombre(carpeta.getNombreCliente());
        contenidoCarpetaResponse.setCantidadDeCarpetas(this.carpetaRepository.countByCarpetaPadreTokenIdentificadorAndRemovido(
                carpeta.getTokenIdentificador(),
                false
        ));

        contenidoCarpetaResponse.setFechaDeCreacion(carpeta.getFechaCreacion());
        UsuarioSistema usuarioSistema = carpeta.getUsuarioSistemaCrea();
        if (usuarioSistema != null) {
            contenidoCarpetaResponse.setUsuarioQueCreo(usuarioSistema.convertirADTO());
        }

        return contenidoCarpetaResponse;
    }

    private ContenidoCarpetaResponse crearConDocumento(Documento documento) {
        ContenidoCarpetaResponse contenidoCarpetaResponse = new ContenidoCarpetaResponse();
        contenidoCarpetaResponse.setTokenIdentificadorDocumento(documento.getTokenIdentificador());
        contenidoCarpetaResponse.setDescripcion(documento.getDescripcion());
        contenidoCarpetaResponse.setTipo(documento.getMimeType());
        contenidoCarpetaResponse.setNombre(documento.getNombreReal());
        contenidoCarpetaResponse.setSizeBytes(documento.getTamanioByteDocumento());

        contenidoCarpetaResponse.setFechaDeCreacion(documento.getFechaCreacion());
        UsuarioSistema usuarioSistema = documento.getUsuarioSistemaCrea();
        if (usuarioSistema != null) {
            contenidoCarpetaResponse.setUsuarioQueCreo(usuarioSistema.convertirADTO());
        }

        return contenidoCarpetaResponse;
    }

    private List<RutaContenidoCarpetaResponse> contruirRuta(FichaIdentificacionCarpeta fichaIdentificacionCarpeta) {
        List<RutaContenidoCarpetaResponse> rutaContenidoCarpetaResponseList = null;

        Catalogo tipoDeGestionDeAdoslescente = fichaIdentificacionCarpeta.getTipoDeGestionDeAdolescente();

        //Si no tiene tipo de festion de adoslecente se asume que la carpeta principal
        if (tipoDeGestionDeAdoslescente == null) {
            return rutaContenidoCarpetaResponseList;
        }


        Carpeta carpetaHija = fichaIdentificacionCarpeta.getCarpeta();
        if (carpetaHija == null) {
            this.logService.warn("La ficha de identificación carpeta: " + fichaIdentificacionCarpeta.getIdFichaIdentificacionCarpeta() + " tiene una carpeta nula");
            return rutaContenidoCarpetaResponseList;
        }
        rutaContenidoCarpetaResponseList = new ArrayList<>();

        RutaContenidoCarpetaResponse rutaContenidoCarpetaResponse = new RutaContenidoCarpetaResponse();
        rutaContenidoCarpetaResponse.setTokenIdentificadorFichaPrincipalCarpeta(fichaIdentificacionCarpeta.getTokenIdentificador());
        rutaContenidoCarpetaResponse.setNombre(carpetaHija.getNombreCliente());

        rutaContenidoCarpetaResponseList.add(rutaContenidoCarpetaResponse);
        FichaIdentificacion fichaIdentificacion = fichaIdentificacionCarpeta.getFichaIdentificacion();

        //si el tipo de gestion adolescente es nulo se asume que es una carpeta principal de un infractor adolescente
        while (tipoDeGestionDeAdoslescente != null) {
            Carpeta carpetaPadre = carpetaHija.getCarpetaPadre();

            List<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaList = this.fichaIdentificacionCarpetaRepository.
                    findByCarpetaAndFichaIdentificacionAndRemovidoOrderByIdFichaIdentificacionCarpetaDesc(
                            carpetaPadre,
                            fichaIdentificacion,
                            false
                    );

            if (fichaIdentificacionCarpetaList.isEmpty()) {
                this.logService.warn("La ficha de identificacion: " + fichaIdentificacion.getIdFichaIdentificacion() +
                        " no tiene una relacion con la carpeta padre: " + carpetaPadre.getIdCarpeta());
                return rutaContenidoCarpetaResponseList;
            }
            fichaIdentificacionCarpeta = fichaIdentificacionCarpetaList.get(0);
            tipoDeGestionDeAdoslescente = fichaIdentificacionCarpeta.getTipoDeGestionDeAdolescente();

            RutaContenidoCarpetaResponse rutaContenidoCarpetaResponse2 = new RutaContenidoCarpetaResponse();
            String token = fichaIdentificacionCarpeta.getTokenIdentificador();
            if (tipoDeGestionDeAdoslescente == null) {
                token = fichaIdentificacion.getTokenIdentificador();
            }
            rutaContenidoCarpetaResponse2.setTokenIdentificadorFichaPrincipalCarpeta(token);
            rutaContenidoCarpetaResponse2.setNombre(carpetaPadre.getNombreCliente());

            rutaContenidoCarpetaResponseList.add(rutaContenidoCarpetaResponse2);
        }
        Collections.reverse(rutaContenidoCarpetaResponseList);

        return rutaContenidoCarpetaResponseList;
    }
}
