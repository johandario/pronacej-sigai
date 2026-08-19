/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.latinus.sistema.integral.gestion.seguridad.model.request;

/**
 *
 * @author New
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 
 Clase que representa la estructura detallada de un usuario en el request de carga masiva
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioCargaMasivaRequest {
        
    @JsonProperty("Item")
    private Integer item;
    
    @JsonProperty("Nombres")
    private String nombres;
    
    @JsonProperty("Apellidos")
    private String apellidos;
    
    @JsonProperty("Email")
    private String email;
    
    @JsonProperty("Celular")
    private String celular;
    
    @JsonProperty("Tipo de Documento")
    private String tipoDocumento;
    
    @JsonProperty("Documento")
    private String documento;
    
    @JsonProperty("Departamento/Centro/SOA")
    private String departamentoCentroSoa;
    
    @JsonProperty("Cargo")
    private String cargo;
    
    @JsonProperty("Nombre de usuario")
    private String nombreUsuario;
    
    @JsonProperty("Rol")
    private String rol;
    
    @JsonProperty("Tipo de contrato")
    private String tipoContrato;
    
    @JsonProperty("Es formador (si/no)")
    private String esFormador;
    
    @JsonProperty("CENTRO")
    private String centro;
}