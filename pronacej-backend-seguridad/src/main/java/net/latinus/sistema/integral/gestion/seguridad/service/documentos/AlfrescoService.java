package net.latinus.sistema.integral.gestion.seguridad.service.documentos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.model.response.alfresco.Error;
import net.latinus.sistema.integral.gestion.seguridad.model.response.alfresco.NodeResponse;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.ServiciosRestService;
import net.latinus.sistema.integral.gestion.seguridad.utils.Aes;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class AlfrescoService {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private EmpresaRepository empresaRepository;
    private final Aes aes = new Aes();

    private final ServiciosRestService serviciosRestService = new ServiciosRestService();
    private final String nodePath = "/nodes/";

    @Value("${urlAlfresco}")
    private String urlApi;

    @Autowired
    public AlfrescoService(ParametroDelSistemaRepository parametroDelSistemaRepository,
                           EmpresaRepository empresaRepository) {
        this.parametroDelSistemaRepository = parametroDelSistemaRepository;
        this.empresaRepository = empresaRepository;
    }

    private final LogService logService = new LogService(this.getClass());

    private RespuestaPorDefectoAuditoria<String> obtenerValor(String tokenEmpresa, String nemonico) {
        RespuestaPorDefectoAuditoria<String> df = new RespuestaPorDefectoAuditoria<>();

        ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository.findByEmpresaTokenIdentificadorAndNemonicoAndRemovido(
                tokenEmpresa, nemonico, false
        );

        if (parametroDelSistema == null) {
            df.setMensaje("No se encontro el api del servicio de alfresco valor: " + nemonico);
            return df;
        }

        df.llenarRespuestaExitosa("Url de alfresco encontrada con exito, ",
                parametroDelSistema.getValor());

        return df;
    }

    /**
     * Ejecuta el servicio de subida de archivo de Alfresco
     *
     * @param tokenEmpresa string token identificador de la empresa.
     * @param idNode String id del nodo puede ser -root-, -shared-, -my- o un id de nodo.
     * @param resource Resource objeto file que se va a enviar en el servicio
     * @param titulo String titulo de la subida
     * @param descripcion String descripcion de la subida
     * @param pathRelativo String path relativo de la subida del archivo
     *
     * @return RespuestaPorDefectoAuditoria<NodeResponse>
     */
    public RespuestaPorDefectoAuditoria<NodeResponse> subirArchivo(String tokenEmpresa, String idNode,
                                                                   Resource resource,
                                                                   String titulo, String descripcion, String pathRelativo) {
        RespuestaPorDefectoAuditoria<NodeResponse> df = new RespuestaPorDefectoAuditoria<>();

        try {

            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(tokenEmpresa, false);

            if (empresa == null) {
                df.setMensaje("El identificador de la empresa en inválido");
                return df;
            }

            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(
                    EtiquetaNemonico.PARAM_AES_CLAVE, null, false);

            if (parametroDelSistema == null) {
                df.setMensaje("No se ha configurado la clave de desencriptación, comunicate con tu administrador");
                return df;
            }
            // Generar nombre único para el archivo
            String originalFilename = resource.getFilename();
            String fileExtension = "";
            String baseName = originalFilename;

            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            }

            String uniqueFileName = baseName + "_" + UUID.randomUUID().toString() + fileExtension;

            String clave = parametroDelSistema.getValor();
            String username = this.aes.decrypt(clave, empresa.getUserNameAlfresco());
            String password = this.aes.decrypt(clave, empresa.getConstraseniaAlfresco());

            MultiValueMap<String, Object> body
                    = new LinkedMultiValueMap<>();


            body.add("filedata", resource);

            body.add("name", uniqueFileName);
            body.add("nodeType", "cm:content");
            body.add("cm:title", titulo);
            body.add("cm:description", descripcion);
            if (pathRelativo != null) {
                body.add("relativePath", pathRelativo);
            }

            String endPointNodeFinal = this.nodePath + idNode + "/children";
            String url = this.urlApi + endPointNodeFinal;

            ResponseEntity<String> resp = this.serviciosRestService.postFormData(url, body,
                    username, password);

            NodeResponse nodeResponse = new Gson().fromJson(resp.getBody(), NodeResponse.class);
            df.llenarRespuestaExitosa("Se consumio con éxito el servicio de upload file", nodeResponse);


        } catch (HttpClientErrorException htEx) {
            logService.error("Ha ocurrido un error: {}", htEx.getMessage(), htEx);
            Error error = new Gson().fromJson(htEx.getResponseBodyAsString(), Error.class);
            df.setMensaje("Ha ocurrido el siguiente Error: " + error.getError().getErrorKey());
            df.setMensajeErrorReal(htEx.toString());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Ejecuta el servicio de creacion de una carpeta en Alfresco
     *
     * @param tokenEmpresa string token identificador de la empresa.
     * @param idNode String id del nodo puede ser -root-, -shared-, -my- o un id de nodo.
     * @param nombre String nombre de la carpeta
     * @param titulo String titulo de la subida
     * @param descripcion String descripcion de la subida
     *
     * @return RespuestaPorDefectoAuditoria<NodeResponse>
     */
    public RespuestaPorDefectoAuditoria<NodeResponse> crearCarpeta(String tokenEmpresa, String idNode,
                                                                   String nombre, String titulo, String descripcion) {

        RespuestaPorDefectoAuditoria<NodeResponse> df = new RespuestaPorDefectoAuditoria<>();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", nombre);
            jsonObject.put("nodeType", "cm:folder");

            JSONObject jsonProperties = new JSONObject();
            jsonProperties.put("cm:title", titulo);
            jsonProperties.put("cm:description", descripcion);

            jsonObject.put("properties", jsonProperties);

            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(tokenEmpresa, false);

            if (empresa == null) {
                df.setMensaje("El identificador de la empresa en inválido");
                return df;
            }

            String endPointNodeFinal = this.nodePath + idNode + "/children";
            String url = this.urlApi + endPointNodeFinal;

            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(
                    EtiquetaNemonico.PARAM_AES_CLAVE, null, false);

            if (parametroDelSistema == null) {
                df.setMensaje("No se ha configurado la clave de desencriptación, comunicate con tu administrador");
                return df;
            }
            String clave = parametroDelSistema.getValor();

            String username = this.aes.decrypt(clave, empresa.getUserNameAlfresco());
            String password = this.aes.decrypt(clave, empresa.getConstraseniaAlfresco());

            ResponseEntity<String> resp = this.serviciosRestService.postJson(url, jsonObject.toString(),
                    username, password);

            NodeResponse nodeResponse = new Gson().fromJson(resp.getBody(), NodeResponse.class);
            df.llenarRespuestaExitosa("Se consumio con éxito el servicio de creación de carpeta", nodeResponse);

        } catch (HttpClientErrorException htEx) {
            logService.error("Ha ocurrido un error: {}", htEx.getMessage(), htEx);
            Error error = new Gson().fromJson(htEx.getResponseBodyAsString(), Error.class);
            df.setMensaje("Ha ocurrido el siguiente Error: " + error.getError().getErrorKey());
            df.setMensajeErrorReal(htEx.toString());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Obten un archivo subido a alfresco o una respuesta en json NodeResponse en caso de fallos
     *
     * @param tokenEmpresa string token identificador de la empresa.
     * @param idNode String id del documento asociado a un nodo de Alfresco
     *
     * @return RespuestaPorDefectoAuditoria<Object>
     */
    public RespuestaPorDefectoAuditoria<Resource> obtenerDocumento(String tokenEmpresa, String idNode) {

        RespuestaPorDefectoAuditoria<Resource> df = new RespuestaPorDefectoAuditoria<>();
        try {

            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(tokenEmpresa, false);

            if (empresa == null) {
                df.setMensaje("El identificador de la empresa en inválido");
                return df;
            }

            String endPointNodeFinal = this.nodePath + idNode + "/content";
            String url = this.urlApi + endPointNodeFinal;

            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(
                    EtiquetaNemonico.PARAM_AES_CLAVE, null, false);

            if (parametroDelSistema == null) {
                df.setMensaje("No se ha configurado la clave de desencriptación, comunicate con tu administrador");
                return df;
            }
            String clave = parametroDelSistema.getValor();

            String username = this.aes.decrypt(clave, empresa.getUserNameAlfresco());
            String password = this.aes.decrypt(clave, empresa.getConstraseniaAlfresco());

            ResponseEntity<Resource> resp = this.serviciosRestService.getObject(url,
                    null, username, password);

            df.llenarRespuestaExitosa("Se consumio con éxito el servicio de creación de carpeta", resp.getBody());

        } catch (HttpClientErrorException htEx) {
            logService.error("Ha ocurrido un error: {}", htEx.getMessage(), htEx);
            this.logService.info(htEx.getResponseBodyAsString());
            Error error = new Gson().fromJson(htEx.getResponseBodyAsString(), Error.class);
            df.setMensaje("Ha ocurrido el siguiente Error: " + error.getError().getErrorKey());
            df.setMensajeErrorReal(htEx.toString());
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Actualiza un nombre o las propiedades de un nodo en Alfresco (titulo o descripcion)
     *
     * @param tokenEmpresa string token identificador de la empresa.
     * @param idNode String id del nodo puede ser -root-, -shared-, -my- o un id de nodo.
     * @param nombre String nombre de la carpeta
     * @param titulo String titulo de la subida
     * @param descripcion String descripcion de la subida
     *
     * @return RespuestaPorDefectoAuditoria<NodeResponse>
     */
    public RespuestaPorDefectoAuditoria<NodeResponse> actualizarMetadataNodo(String tokenEmpresa, String idNode,
                                                                             String nombre, String titulo, String descripcion) {

        RespuestaPorDefectoAuditoria<NodeResponse> df = new RespuestaPorDefectoAuditoria<>();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", nombre);

            JSONObject jsonProperties = new JSONObject();
            jsonProperties.put("cm:title", titulo);
            jsonProperties.put("cm:description", descripcion);

            jsonObject.put("properties", jsonProperties);

            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(tokenEmpresa, false);

            if (empresa == null) {
                df.setMensaje("El identificador de la empresa en inválido");
                return df;
            }

            String endPointNodeFinal = this.nodePath + idNode;
            String url = this.urlApi + endPointNodeFinal;

            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(
                    EtiquetaNemonico.PARAM_AES_CLAVE, null, false);

            if (parametroDelSistema == null) {
                df.setMensaje("No se ha configurado la clave de desencriptación, comunicate con tu administrador");
                return df;
            }
            String clave = parametroDelSistema.getValor();

            String username = this.aes.decrypt(clave, empresa.getUserNameAlfresco());
            String password = this.aes.decrypt(clave, empresa.getConstraseniaAlfresco());

            ResponseEntity<String> resp = this.serviciosRestService.putJson(url, jsonObject.toString(),
                    username, password);

            NodeResponse nodeResponse = new Gson().fromJson(resp.getBody(), NodeResponse.class);
            df.llenarRespuestaExitosa("Se consumio con éxito el servicio de creación de carpeta", nodeResponse);

        } catch (HttpClientErrorException htEx) {
            logService.error("Ha ocurrido un error: {}", htEx.getMessage(), htEx);
            Error error = new Gson().fromJson(htEx.getResponseBodyAsString(), Error.class);
            df.setMensaje("Ha ocurrido el siguiente Error: " + error.getError().getBriefSummary());
            df.setMensajeErrorReal(htEx.toString());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Obten la data de un nodo
     *
     * @param tokenEmpresa string token identificador de la empresa.
     * @param idNode String id del nodo puede ser -root-, -shared-, -my- o un id de nodo.
     *
     * @return RespuestaPorDefectoAuditoria<NodeResponse>
     */
    public RespuestaPorDefectoAuditoria<NodeResponse> getMetadaData(String tokenEmpresa, String idNode) {

        RespuestaPorDefectoAuditoria<NodeResponse> df = new RespuestaPorDefectoAuditoria<>();
        try {

            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(tokenEmpresa, false);

            if (empresa == null) {
                df.setMensaje("El identificador de la empresa en inválido");
                return df;
            }

            String endPointNodeFinal = this.nodePath + idNode;
            String url = this.urlApi + endPointNodeFinal;

            ParametroDelSistema parametroDelSistema = this.parametroDelSistemaRepository.findByNemonicoAndEmpresaAndRemovido(
                    EtiquetaNemonico.PARAM_AES_CLAVE, null, false);

            if (parametroDelSistema == null) {
                df.setMensaje("No se ha configurado la clave de desencriptación, comunicate con tu administrador");
                return df;
            }
            String clave = parametroDelSistema.getValor();

            String username = this.aes.decrypt(clave, empresa.getUserNameAlfresco());
            String password = this.aes.decrypt(clave, empresa.getConstraseniaAlfresco());

            ResponseEntity<String> resp = this.serviciosRestService.getJson(url, null,
                    username, password);

            NodeResponse nodeResponse = new Gson().fromJson(resp.getBody(), NodeResponse.class);
            df.llenarRespuestaExitosa("Obtención de la data del nodo exitoso", nodeResponse);

        } catch (HttpClientErrorException htEx) {
            logService.error("Ha ocurrido un error: {}", htEx.getMessage(), htEx);
            Error error = new Gson().fromJson(htEx.getResponseBodyAsString(), Error.class);
            df.setMensaje("Ha ocurrido el siguiente Error: " + error.getError().getBriefSummary());
            df.setMensajeErrorReal(htEx.toString());

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


    /*
    objeto {
    "id_carpeta": 72891,
    "nombre_alfresco": "79ad9720-0ec1-4dc8-8bb8-4d080f6be932",
    "descripcion": "Carpeta de mandato legal relacionado al detalle: 1a8308ab-71d8-418f-9806-f04bd27a949c",
    "identificador_alfresco": "4b3d4bb2-d16e-4ac6-bd4b-b2d16eeac6df",
    "identificador_padre": "edddab2c-f314-4bd1-9dab-2cf3147bd108"
  }
     */
    public RespuestaPorDefectoAuditoria<Boolean> ayuda(JSONArray jsonArray) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();
        try {

            List<CompletableFuture<String>> completableFutureList = new ArrayList<>();
            for (Object object : jsonArray) {
                JSONObject jsonObject = (JSONObject) object;

                CompletableFuture<String> tarea = CompletableFuture.supplyAsync(() -> this.mensaje(jsonObject));

                completableFutureList.add(tarea);
            }

            // Esperar a que todas las tareas terminen
            List<String> resultados = completableFutureList.stream()
                    .map(CompletableFuture::join)
                    .toList();
            ObjectMapper mapper = new ObjectMapper();

            mapper.writerWithDefaultPrettyPrinter()  // <-- formatea el JSON
                    .writeValue(new File("./respuestas.json"), resultados);

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return df;
    }

    private String mensaje(JSONObject jsonObject) {
        String mensaje;
        String username = "username";
        String password = "xxxx";
        String identificadorAlfresco = jsonObject.getString("identificador_alfresco");
        try {
            String endPointNodeFinal = this.nodePath + identificadorAlfresco;
            String url = this.urlApi + endPointNodeFinal;
            ResponseEntity<String> resp = this.serviciosRestService.getJson(url, null,
                    username, password);
            mensaje = resp.getBody();
        } catch (Exception e) {
            mensaje = e.getMessage();
        }

        return identificadorAlfresco + ":" + mensaje;
    }

}
