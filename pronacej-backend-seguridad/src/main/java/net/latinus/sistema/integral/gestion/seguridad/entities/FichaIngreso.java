package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

@Entity
@Data
@Table(name = "ia_ficha_ingreso")
@EqualsAndHashCode(of = {"idFichaIngreso"}, callSuper = true)
public class FichaIngreso extends EntidadBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idFichaIngreso;

    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;

    @Column(columnDefinition = "timestamp")
    @Comment("fecha de ingreso")
    private Date fechaIngreso;

    @Comment("centro")
    @JoinColumn(name = "id_centro", referencedColumnName = "idJerarquia")
    @ManyToOne(fetch = FetchType.LAZY)
    private Jerarquia centro;

    @Comment("atencion salud")
    private Boolean atencionSalud;

    @Comment("motivo")
    private String motivo;

    @Comment("observaciones")
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Comment("responsable inscripcion")
    @Column(columnDefinition = "TEXT")
    private String responsableInscripcion;

    @Comment("caracteristicas particulares")
    private String caracteristicasParticulares;

    @Comment("programa derivado")
    @JoinColumn(name = "programa_derivado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo programaDerivado;

    @Comment("tuto")
    @JoinColumn(name = "tutor", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo tutor;
    
    @Comment("lesiones")
    private Boolean lesiones;

    @Comment("especificar zona moretones")
    private String especificarZonaLesiones;

    @Comment("moretones")
    private Boolean moretones;

    @Comment("especificar zona moretones")
    private String especificarZonaMoretones;

    @Comment("cicatrices")
    private Boolean cicatrices;

    @Comment("especificar zona cicatrices")
    private String especificarZonaCicatrices;

    @Comment("tatuajes")
    private Boolean tatuajes;

    @Comment("especificar zona tatuajes")
    private String especificarZonaTatuajes;

    @Comment("piercing")
    private Boolean piercing;

    @Comment("especificar zona piercing")
    private String especificarZonaPiercing;
    
    @Comment("otros")
    private Boolean otros;

    @Comment("especificar zona piercing")
    private String especificarZonaOtros;

    @Comment("victima agresion")
    private Boolean victimaAgresion;

    @Comment("especificar agresion")
    private String especificarAgresion;
    
    @Comment("seguro de salud")
    @JoinColumn(name = "seguro_salud", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo seguroSalud;

    @Comment("forma cabeza")
    @JoinColumn(name = "forma_cabeza", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo formaCabeza;

    @Comment("forma nariz")
    @JoinColumn(name = "forma_nariz", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo formaNariz;

    @Comment("forma labios")
    @JoinColumn(name = "forma_labios", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo formaLabios;

    @Comment("forma cuerpo")
    @JoinColumn(name = "forma_cuerpo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo formaCuerpo;

    @Comment("anomalia ojos")
    @JoinColumn(name = "anomalia_ojos", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo anomaliaOjos;

    @Comment("es embarazada")
    private Boolean esEmbarazada;

    @Comment("meses embarazo")
    private Integer mesesEmbarazo;

    @Comment("ingresa con hijo")
    private Boolean ingresaConHijo;

//    private String hijoApellidoPaterno;
//    private String hijoApellidoMaterno;
//    private String hijoNombresCompletos;
//    private Integer hijoEdad;
//    private String hijoDNI;
//    private Boolean hijoVictimaAgresion;
//    private String hijoEspecificarAgresion;
//    private Boolean hijoMoretones;
//    private String hijoEspecificarZonaMoretones;
//    private Boolean hijoCicatrices;
//    private String hijoEspecificarZonaCicatrices;
//    private Boolean hijoTatuajes;
//    private String hijoEspecificarZonaTatuajes;
//    private String hijoOtroEspecificar;
//    private String hijoObservaciones;

    @Comment("id del estado")
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estado;

    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    @Comment("numero de documentos fojas al ingreso")
    @Column(columnDefinition = "int")
    private Long numeroFojas;

    @Comment("juez encargado")
    private String juez;

    @Comment("juzgado encargado")
    private String juzgado;

    @Comment("activo")
    private Boolean activo = false;

    @Comment("Fecha de Inactividad")
    private Date fechaInactividad;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
