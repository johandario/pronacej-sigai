package net.latinus.sistema.integral.gestion.seguridad.model.both.fuga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.InstanciaProcesoDTO;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idFuga"}, callSuper = true)
public class EventoFugaDTO extends CamposDTO {
    private Long idFuga;
    private Long tokenFichaIdentificacion; // Token identificador de la Ficha Identificación
    private CatalogoDTO parentesco; // DTO del Catalogo para parentesco
    private Date fechaRegistro;
    private Date fechaFuga;
    private Date fechaInformeDirector;
    private Date fechaInformeApoderado;
    private String descripcionHechos;
    private String accionesRealizadas;
    private String presenciaDe;
    private String dirigidoA;
    private String asunto;
    private String de;
    private String apoderado;
    private InstanciaProcesoDTO instanciaProcesoDTO;
    private String tokenProceso;
    private String dni;
    private String numFuga;
    private String html;
    private Boolean isComplete;
    private CatalogoDTO estadoEvento;
    private JerarquiaDTO centro;
    private String nombreAdolescente;
    private String numeroIdentificacion;
    private Boolean ultimoPaso;
    private Date fechaNacimiento;
}
