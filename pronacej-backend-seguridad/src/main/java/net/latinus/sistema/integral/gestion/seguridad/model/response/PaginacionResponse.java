package net.latinus.sistema.integral.gestion.seguridad.model.response;

import lombok.Data;
import java.util.List;

@Data
public class PaginacionResponse<T> {

    private Long totalItems;
    private List<T> data;
}
