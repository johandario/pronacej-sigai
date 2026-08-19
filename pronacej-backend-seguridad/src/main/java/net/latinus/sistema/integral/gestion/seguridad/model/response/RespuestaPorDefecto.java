package net.latinus.sistema.integral.gestion.seguridad.model.response;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.jetbrains.annotations.NotNull;

@Data
public class RespuestaPorDefecto<T> {

    private String titulo = "Petición no exitosa";
    // Variable para mostrar el mensaje cliente.
    private String mensaje;
    // Variable para mostrar el mensaje para auditorías.
    private String mensajeAuditoria;
    private boolean exito = false;
    private T data;
    private Boolean sinAcceso = false;
    private Integer codigoEstado = 403;
    private String mensajeError;

    /**
     * Llena el objeto con los datos obtenidos en la exception
     *
     * @param ex Objeto Exception.
     *
     * @return void
     */
    public void llenarConDatosDeException(@NotNull Exception ex) {
        LogService logService = new LogService(ex.getClass());

        logService.error("Ha ocurrido un error: {}", ex.getMessage(), ex);
        this.codigoEstado = 500;
        this.mensaje = "No se pudo realizar la operación debido a que ha ocurrido un error. Consulte con su administrador.";
        this.mensajeError = ex.getMessage();
        this.titulo = "Petición fallida";
        this.exito = false;
    }

    /**
     * LLena los datos con una respuesta exito
     *
     * @param mensaje Mensaje final que se le envia al usuario.
     * @param data data final enviada al usuario.
     *
     * @return void.
     */
    public void llenarRespuestaExitosa(String mensaje, T data, String mensajeAuditoria) {
        this.exito = true;
        this.codigoEstado = 200;
        this.titulo = "Petición realizada con éxito";
        this.mensaje = mensaje;
        this.data = data;
        this.mensajeAuditoria = mensajeAuditoria;
        this.sinAcceso = false;
    }
    
    /**
     * LLena los datos con una respuesta exito
     *
     * @param mensaje Mensaje final que se le envia al usuario.
     * @param data data final enviada al usuario.
     *
     * @return void.
     */
    public void llenarRespuestaExitosa(String mensaje, T data) {
        this.exito = true;
        this.codigoEstado = 200;
        this.titulo = "Petición realizada con éxito";
        this.mensaje = mensaje;
        this.data = data;
        this.sinAcceso = false;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
