/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.latinus.sistema.integral.gestion.seguridad.model.response;

/**
 *
 * @author New
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa la respuesta de la operación de carga masiva
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargaMasivaResponse {
    
    private boolean exito;
    private String mensaje;
    private int totalProcesados;
    private int registrosExitosos;
    private int registrosFallidos;
    
    @Builder.Default
    private List<String> errores = new ArrayList<>();
}