package net.latinus.sistema.integral.gestion.seguridad.model.response.doc;

import lombok.Data;

@Data
public class RutaContenidoCarpetaResponse {

    private String tokenIdentificadorFichaPrincipalCarpeta;
    private String tokenCarpeta;
    private String nombre;
}
