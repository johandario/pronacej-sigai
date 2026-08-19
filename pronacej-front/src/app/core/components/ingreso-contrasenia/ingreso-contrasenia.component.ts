import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import etiquetasModel from 'app/core/etiquetas.model';
import { ParametroDelSistemaDTO } from 'app/core/model/both/parametroDelSistemaDTO.model';
import { ContraseniaResponse } from 'app/core/model/internos/contraseniaResponse.model';
import { ValidacionContrasenia } from 'app/core/model/internos/validacionContrasenia.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { ParametroDelSistemaService } from 'app/core/services/parametroDelSistema.service';

@Component({
  selector: 'app-ingreso-contrasenia',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    MatInputModule,
    MatIconModule
  ],
  templateUrl: './ingreso-contrasenia.component.html',
  styleUrl: './ingreso-contrasenia.component.scss'
})
export class IngresoContraseniaComponent implements OnInit {

  @Input({ required: true }) declare nemonicoMenu: string;
  @Input() camposRequeridos = false;

  contraseniaForm: FormGroup;
  validacionContraseniaList: ValidacionContrasenia[] = [];

  private nemonicoConfParamPAssword = [
    "REGLA_CONTRASENIA_MINUSCULA",
    "REGLA_CONTRASENIA_MAYUSCULA",
    "REGLA_CONTRASENIA_NUMERO",
    "REGLA_CONTRASENIA_LONGUITUD",
    "REGLA_CONTRASENIA_CARACTER_NO_ALFANUMERICO"
  ];

  etiquetasModel = etiquetasModel;

  constructor(private parametroDelSistemaService: ParametroDelSistemaService,
    private fb: FormBuilder
  ) {

    this.contraseniaForm = this.fb.group(
      {
        contraseniaNueva: [null, this.camposRequeridos ? [Validators.required] : []],
        contraseniaNuevaConfirmada: [null, this.camposRequeridos ? [Validators.required] : []]
      }
    );
  }

  ngOnInit(): void {
    this.obtenerCondicionesDeContrasenia();
  }

  /**
* Obtiene todos los requisitos de la contraseña ingresada 
*
* @param callback Function callback con la data del servicio
*  
* @returns void
*/
  obtenerCondicionesDeContrasenia(callback?: Function) {
    let paraDTO = new ParametroDelSistemaDTO();
    paraDTO.nemonico = etiquetasModel.PARAM_REGLAS_CONTRASENIA;
    this.parametroDelSistemaService.obtenerParamHijos(
      paraDTO, this.nemonicoMenu
    ).subscribe(
      {
        next: (response: RespuestaPorDefecto<ParametroDelSistemaDTO[]>) => {
          if (!response.exito) {
            this.parametroDelSistemaService.checkError(response);
            return;
          }

          this.validacionContraseniaList = response.data.filter(
            (value) => value.nemonico != etiquetasModel.PARAM_REGLA_CONTRASENIA_CAMBIO_CADA_N_DIAS
          ).map(
            (param) => {
              let validacionContrasenia = new ValidacionContrasenia();
              validacionContrasenia.descripcion = param.descripcion;
              validacionContrasenia.id = param.tokenIdentificador;
              validacionContrasenia.valor = param.valor;
              validacionContrasenia.nemonico = param.nemonico;

              return validacionContrasenia;
            }
          );

          if (callback) {
            callback(response.data);
          }
        },
        error: (error: any) => {
          this.parametroDelSistemaService.checkError(error);
        }
      }
    );
  }


  /**
* verifica si el texto cumple con los requisitos 
*
* @param valorInput string texto ingreso a ser verificado
*  
* @returns void
*/
  ingresoDecontrasenia(valorInput: string) {
    this.validacionContraseniaList.forEach(
      (validacion) => {
        validacion.valida = false;
        if (validacion.nemonico == etiquetasModel.PARAM_REGLA_CONTRASENIA_LONGUITUD) {
          validacion.valida = valorInput.length >= +validacion.valor;
        } else {
          let i = 0;
          let regex = new RegExp(validacion.valor);

          //se verifica si al menos un chart del string cumple con la condicion
          while (!validacion.valida && valorInput.length > i) {
            let chart = valorInput.charAt(i);
            let resultTest = regex.test(chart);
            if (validacion.nemonico == etiquetasModel.PARAM_REGLA_CONTRASENIA_CARACTER_NO_ALFANUMERICO) {
              validacion.valida = !resultTest;
            } else {
              validacion.valida = resultTest;
            }
            i++;
          }
        }
      }
    );
  }

  verificarContraseniaRepetidaSiCoincide() {
    let contrasenia = this.contraseniaForm.get("contraseniaNueva").value;
    let contraseniaRepetida = this.contraseniaForm.get("contraseniaNuevaConfirmada").value;

    return contrasenia == contraseniaRepetida;
  }

  /**
* Retorna la contraseña ingresada por el usuario 
*
*  
* @returns ContraseniaResponse
*/
  obtenerContrasenia(): ContraseniaResponse {

    let contraseniaResponse = new ContraseniaResponse();

    contraseniaResponse.contrasenia = this.contraseniaForm.get("contraseniaNueva").value;
    contraseniaResponse.contraseniaRepetida = this.contraseniaForm.get("contraseniaNuevaConfirmada").value;
    contraseniaResponse.valida = !this.validacionContraseniaList.find((valid) => !valid.valida);
    
    return contraseniaResponse;
  }

}
