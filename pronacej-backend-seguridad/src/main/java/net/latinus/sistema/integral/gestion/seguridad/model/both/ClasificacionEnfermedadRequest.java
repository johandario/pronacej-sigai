package net.latinus.sistema.integral.gestion.seguridad.model.both;

enum Sexo {
    MASCULINO, FEMENINO
}

public record ClasificacionEnfermedadRequest(String valor, Sexo sexo) {
}
