package net.latinus.sistema.integral.gestion.seguridad.model.request;

import lombok.Data;

import java.util.List;

@Data
public class JerarquiasPorNemonicosPadreRequest {
    private List<String> nemonicosPadre;
}

