package net.latinus.sistema.integral.gestion.seguridad.service;


import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class ServiciosRestService {

    private final LogService logService = new LogService(this.getClass());

    /**
     * Crea un consumo a un servicio post con respuesta json
     *
     * @param url  string email al que le va a llegar el correo.
     * @param body String razon del correo.
     * @return ResponseEntity<String>
     */
    public ResponseEntity<String> postJson(String url, String body, String username, String password) {
        return this.ejecutarExchangeJson(url, body, HttpMethod.POST, username, password);
    }

    /**
     * Crea un consumo a un servicio put con respuesta json
     *
     * @param url  string email al que le va a llegar el correo.
     * @param body String razon del correo.
     * @return ResponseEntity<String>
     */
    public ResponseEntity<String> putJson(String url, String body, String username, String password) {
        return this.ejecutarExchangeJson(url, body, HttpMethod.PUT, username, password);
    }

    /**
     * Crea un consumo a un servicio post con respuesta json
     *
     * @param url    string email al que le va a llegar el correo.
     * @param params Map<String, String> query params de la url.
     * @return ResponseEntity<String>
     */
    public ResponseEntity<String> getJson(String url, Map<String, String> params, String username, String password) {
        if (params != null && !params.isEmpty()) {
            Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator();
            url = url + "?";
            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                url = url + entry.getKey() + "=" + entry.getValue();
                if (itr.hasNext()) {
                    url = url + "&";
                }
            }

        }
        return this.ejecutarExchangeJson(url, null, HttpMethod.GET, username, password);
    }

    /**
     * Crea un consumo a un servicio post con respuesta Object
     *
     * @param url    string email al que le va a llegar el correo.
     * @param params Map<String, String> query params de la url.
     * @return ResponseEntity<Object>
     */
    public ResponseEntity<Resource> getObject(String url, Map<String, String> params, String username, String password) {
        if (params != null && !params.isEmpty()) {
            Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator();
            url = url + "?";
            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                url = url + entry.getKey() + "=" + entry.getValue();
                if (itr.hasNext()) {
                    url = url + "&";
                }
            }

        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();

        HttpMethod httpMethod = HttpMethod.GET;
        this.logService.info("url (" + httpMethod.name() + "): " + url + " ...");
        ResponseEntity<Resource> resp = restTemplate.exchange(url, httpMethod, httpEntity, Resource.class);
        this.logService.info("url (" + httpMethod.name() + "): " + url + ", response: " + resp);

        return resp;
    }

    /**
     * Crea un consumo a un servicio post con respuesta json y body form Data
     *
     * @param url      string email al que le va a llegar el correo.
     * @param body     MultiValueMap body formData del request.
     * @param username String username del auth basic
     * @param password String password del auth basic
     * @return ResponseEntity<String>
     */
    public ResponseEntity<String> postFormData(String url, MultiValueMap<String, Object> body, String username, String password) {
        return this.ejecutarExchangeFormData(url, HttpMethod.POST, body, username, password);
    }

    private ResponseEntity<String> ejecutarExchangeJson(String url, String body,
                                                        HttpMethod httpMethod, String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));   //String a objeto json
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (username != null && password != null) {
            headers.setBasicAuth(username, password);
        }

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = this.ejecutarExchange(url, httpMethod, httpEntity);
        return resp;
    }

    private ResponseEntity<String> ejecutarExchangeFormData(String url, HttpMethod httpMethod,
                                                            MultiValueMap<String, Object> body, String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBasicAuth(username, password);

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = this.ejecutarExchange(url, httpMethod, requestEntity);

        return resp;
    }

    private <T> ResponseEntity<String> ejecutarExchange(String url, HttpMethod httpMethod, HttpEntity<T> requestEntity) {
        RestTemplate restTemplate = new RestTemplate();

        this.logService.info("url (" + httpMethod.name() + "): " + url + ", request: " + requestEntity + " ...");
        ResponseEntity<String> resp = restTemplate.exchange(url, httpMethod, requestEntity, String.class);
        this.logService.info("url (" + httpMethod.name() + "): " + url + ", response: " + resp);

        return resp;
    }
}
