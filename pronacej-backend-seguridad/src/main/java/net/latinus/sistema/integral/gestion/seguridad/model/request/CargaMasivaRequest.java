/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.latinus.sistema.integral.gestion.seguridad.model.request;

/**
 *
 * @author New
 */
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase que representa la estructura del request para la carga masiva de usuarios
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CargaMasivaRequest {
    
    private Map<String, List<UsuarioCargaMasivaRequest>> cargas = new HashMap<>();
    
    @JsonAnySetter
    public void addCentro(String tipoCentro, List<UsuarioCargaMasivaRequest> usuarios) {
        cargas.put(tipoCentro, usuarios);
    }
}