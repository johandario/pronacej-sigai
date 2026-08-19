package net.latinus.sistema.integral.gestion.seguridad.model.request.ia;

import lombok.Getter;

@Getter
public class ValidarIngresoFichaRequest {
    private String tokenIdentificadorFicha;
    private String nemonicoTipoIngreso;
}
