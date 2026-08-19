package net.latinus.sistema.integral.gestion.seguridad.service.documentos;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.FichaIdentificacionCarpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CarpetaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ia.FichaIdentificacionCarpetaRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.alfresco.EntryNodeResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.alfresco.ErrorBody;
import net.latinus.sistema.integral.gestion.seguridad.model.response.alfresco.NodeResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.doc.ContenidoCarpetaResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.doc.RutaContenidoCarpetaResponse;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.CarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.documento.DocumentoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.FichaIdentificacionCarpetaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class CarpetaServiceImpl implements CarpetaService {

    private JwtProviderService jwtProviderService;
    private AlfrescoService alfrescoService;

    private CarpetaRepository carpetaRepository;
    private EmpresaRepository empresaRepository;

    private DocumentoRepository documentoRepository;
    private FichaIdentificacionCarpetaRepository fichaIdentificacionCarpetaRepository;

    private final LogService logService = new LogService(CarpetaService.class);


    @Override
    public RespuestaPorDefectoAuditoria<CarpetaDTO> crearCarpeta(HttpServletRequest httpServletRequest,
                                                                 boolean validarSesion,
                                                                 CarpetaDTO carpetaDTO) {

        RespuestaPorDefectoAuditoria<CarpetaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            String tokenEmpresa;
            Empresa empresa = null;
            UsuarioSistema usuarioSistema = null;
            if (validarSesion) {
                RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

                if (!df2.isExito()) {
                    df.setMensaje(df2.getMensaje());
                    df.setLogOut(true);
                    return df;
                }

                BodyJwtValido bodyJwtValido = df2.getData();
                empresa = bodyJwtValido.getEmpresa();
                usuarioSistema = bodyJwtValido.getUsuarioSistema();
                tokenEmpresa = empresa.getTokenIdentificador();
            } else {
                tokenEmpresa = carpetaDTO.getTokenIdentificadorEmpresa();
                empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(
                        tokenEmpresa, false
                );
            }

            df.setTokenIdentificadorEmpresa(tokenEmpresa);

            CarpetaDTO carpetaPadreDTO = carpetaDTO.getCarpetaDTOPadre();

            Carpeta carpetaPadre = null;

            if (carpetaPadreDTO != null) {
                carpetaPadre = this.carpetaRepository.findByTokenIdentificadorAndRemovido(
                        carpetaPadreDTO.getTokenIdentificador(), false
                );
            }

            if (carpetaPadre == null) {
                this.logService.warn("La carpeta padre es nula");
                df.setMensaje("Se debe de enviar una carpeta padre valida para crear la carpeta hija");
                return df;
            }

            String idNode = carpetaPadre.getIdentificadorAlfresco();

            if (idNode == null) {
                this.logService.error("La carpeta: " + carpetaPadre.getIdCarpeta() + " tiene un identificador de id de alfresco nulo o vacio");
                df.setMensaje("El identificador de la carpeta padre no estar vacio o ser nulo");
                return df;
            }

            String nombreAlfresco = UUID.randomUUID().toString();   //nombre unico a la carpeta por crear

            RespuestaPorDefectoAuditoria<NodeResponse> df3 = this.alfrescoService.crearCarpeta(
                    tokenEmpresa,
                    idNode, nombreAlfresco, "Creación de carpeta: "
                            + nombreAlfresco, carpetaDTO.getDescripcion()
            );

            if (!df3.isExito()) {
                df.setMensaje(df3.getMensaje());
                return df;
            }

            NodeResponse nodeResponse = df3.getData();

            ErrorBody error = nodeResponse.getError();
            if (error != null && error.getErrorKey() != null) {
                df.setMensaje(error.getBriefSummary());
                return df;
            }

            EntryNodeResponse entryNodeResponse = nodeResponse.getEntry();
            Carpeta carpeta = new Carpeta();
            carpeta.setCarpetaPadre(carpetaPadre);
            carpeta.setNombreAlfresco(nombreAlfresco);
            carpeta.setNombreCliente(carpetaDTO.getNombreCliente());
            carpeta.setEmpresa(empresa);
            carpeta.setIdentificadorAlfresco(entryNodeResponse.getId());
            carpeta.setUsuarioSistemaCrea(usuarioSistema);
            carpeta.setDescripcion(carpetaDTO.getDescripcion());
            carpeta.setIpCrea(httpServletRequest.getRemoteAddr());

            this.carpetaRepository.save(carpeta);

            carpetaDTO.setTokenIdentificador(carpeta.getTokenIdentificador());
            carpetaDTO.setTokenIdentificadorEmpresa(tokenEmpresa);
            carpetaDTO.setFechaCreacion(carpeta.getFechaCreacion());

            df.llenarRespuestaExitosa("Se ha creado la carpeta con nombre: "
                    + carpeta.getNombreCliente() + " correctamente", carpetaDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<ContenidoCarpetaResponse> obterInformacionDeCarpetaDesdeLaFichaPrincipal(HttpServletRequest httpServletRequest,
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

            Carpeta carpeta = this.carpetaRepository.findByTokenIdentificadorAndRemovido(
                    fichaIdentificacionCarpetaRequest.getTokenIdentificadorCarpeta(), false
            );

            if (carpeta == null) {
                df.setMensaje("La carpeta a consultar no existe");
                return df;
            }

            ContenidoCarpetaResponse contenidoCarpetaResponse = this.crearConCarpeta(carpeta);

            List<Carpeta> carpetasHijas = this.carpetaRepository.findByCarpetaPadreTokenIdentificadorAndRemovidoOrderByNombreCliente(
                    carpeta.getTokenIdentificador(),
                    false
            );

            if (!carpetasHijas.isEmpty()) {
                List<ContenidoCarpetaResponse> contenidoCarpetaResponseCarpetaHijas = new ArrayList<>();

                for (Carpeta carpetaHija : carpetasHijas) {
                    ContenidoCarpetaResponse contenidoCarpetaResponse2 = this.crearConCarpeta(carpetaHija);
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
                    this.contruirRutaDeCarpetasDeFichaPrincipal(carpeta, fichaIdentificacionCarpetaRequest.getTokenIdentificadorFichaPrincipal())
            );

            df.llenarRespuestaExitosa("Se ha encontrado con éxito la información de la carpeta: "
                    + carpeta.getNombreCliente(), contenidoCarpetaResponse);


        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private List<RutaContenidoCarpetaResponse> contruirRutaDeCarpetasDeFichaPrincipal(Carpeta carpeta, String tokenFichaPrincipal) {
        List<RutaContenidoCarpetaResponse> rutaContenidoCarpetaResponseList = null;

        if (carpeta == null) {
            return rutaContenidoCarpetaResponseList;
        }

        rutaContenidoCarpetaResponseList = new ArrayList<>();

        RutaContenidoCarpetaResponse rutaContenidoCarpetaResponse = new RutaContenidoCarpetaResponse();
        rutaContenidoCarpetaResponse.setTokenCarpeta(carpeta.getTokenIdentificador());
        rutaContenidoCarpetaResponse.setNombre(carpeta.getNombreCliente());

        rutaContenidoCarpetaResponseList.add(rutaContenidoCarpetaResponse);

        Carpeta carpetaPadre = carpeta.getCarpetaPadre();

        Page<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaPage =
                this.fichaIdentificacionCarpetaRepository
                        .findByFichaIdentificacionTokenIdentificadorAndTipoDeGestionDeAdolescenteNemonicoAndRemovido(
                                tokenFichaPrincipal,
                                null,
                                false,
                                PageRequest.of(0, 2, Sort.by("idFichaIdentificacionCarpeta").descending())
                        );


        List<FichaIdentificacionCarpeta> fichaIdentificacionCarpetaList = fichaIdentificacionCarpetaPage.toList();
       Carpeta carpetaInicial = null;
        if(!fichaIdentificacionCarpetaList.isEmpty()){
            carpetaInicial = fichaIdentificacionCarpetaList.get(0).getCarpeta();
        }

        //si el tipo de gestion adolescente es nulo se asume que es una carpeta principal de un infractor adolescente
        while (carpetaPadre != null) {

            RutaContenidoCarpetaResponse rutaContenidoCarpetaResponse2 = new RutaContenidoCarpetaResponse();

            rutaContenidoCarpetaResponse2.setTokenCarpeta( carpetaPadre.getTokenIdentificador());
            rutaContenidoCarpetaResponse2.setNombre(carpetaPadre.getNombreCliente());

            rutaContenidoCarpetaResponseList.add(rutaContenidoCarpetaResponse2);

            assert carpetaInicial != null;
            if (carpetaPadre.getIdCarpeta().equals(carpetaInicial.getIdCarpeta())) {
                break;
            }

            carpetaPadre = carpetaPadre.getCarpetaPadre();

        }
        Collections.reverse(rutaContenidoCarpetaResponseList);

        return rutaContenidoCarpetaResponseList;
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
}
