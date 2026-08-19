package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "par_alerta")
@EqualsAndHashCode(of = {"idAlerta"}, callSuper = true)
@Comment("Tabla de alertas")
public class Alerta extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de alerta")
    private Long idAlerta;

    @Column(name = "descripcion")
    @Comment("Descripción de la alerta")
    private String descripcion;

    @Column(name = "mensaje")
    @Comment("Mensaje de la alerta")
    private String mensaje;

    @Column(name = "tabla")
    @Comment("Tabla para comparar")
    private String tabla;

    @Column(name = "campo")
    @Comment("Campo para comparar")
    private String campo;

    @Column(name = "ruta")
    @Comment("Ruta a la que redirige la alerta")
    private String ruta;

    @Column(name = "prioridad")
    @Comment("Prioridad de la alerta")
    private String prioridad;

//    @Column(name = "fecha_limite", nullable = false)
//    @Comment("Fecha límite para la tarea")
//    private Date fechaLimite;

    @Column(name = "unidadTiempo")
    @Comment("Unidad de tiempo")
    private String unidadTiempo;

    @Column(name = "tiempo")
    @Comment("Cantidad de tiempo")
    private Long tiempo;

    @Column(name = "activo")
    @Comment("Bandera para saber si está activo")
    private Boolean activo;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id_ficha_identificacion")
//    @Comment("Adolescente al que pertenece la alerta")
//    private FichaIdentificacion fichaIdentificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_centro", nullable = false)
    @Comment("Centro al que pertenece la alerta")
    private Jerarquia centro;

    @Comment("Id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;
}
