package net.latinus.sistema.integral.gestion.seguridad.model.request.ia;

import lombok.Data;

@Data
public class FichaIdentificacionCarpetaRequest {

    private String tokenIdentificadorFichaPrincipal;
    private String tokenIdentificadorFichaPrincipalCarpeta;

    private String tokenIdentificadorCarpeta;
}
