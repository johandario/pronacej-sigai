package net.latinus.sistema.integral.gestion.seguridad.model.both.informe;

import lombok.Data;

@Data
public class CampoInformeDTO {
    private long idCampo;
    private String etiqueta;
    private String tipo;
    private String valor;
}
