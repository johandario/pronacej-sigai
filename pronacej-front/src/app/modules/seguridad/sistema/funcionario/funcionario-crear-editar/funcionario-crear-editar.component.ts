import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';

@Component({
  selector: 'app-funcionario-crear-editar',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './funcionario-crear-editar.component.html',
  styleUrl: './funcionario-crear-editar.component.scss'
})
export class FuncionarioCrearEditarComponent {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_FUNCIONARIO;

  crearFuncionarioForm: FormGroup;
  funcionarioEdicion: FuncionarioDTO;

  esEdicion = false;

  @Output() completoOperacion = new EventEmitter<boolean>();
  @Output() canceloEdicion = new EventEmitter<boolean>();

  constructor(
    private fb: FormBuilder,
    private funcionarioService: FuncionarioService,
    private dialogMensajeService: DialogMensajeService
  ) {
    this.construirForm();

  } 

  construirForm() {
    this.crearFuncionarioForm = this.fb.group(
      {
        nombres: [null, [Validators.required, this.noWhitespaceValidator]],
        apellidos: [null, [Validators.required, this.noWhitespaceValidator]],
        email: [null, [Validators.required, this.noWhitespaceValidator, Validators.email]],
        telefono: [null, [Validators.required, this.noWhitespaceValidator]],
        numeroDeDocumento: [null, [Validators.required, this.noWhitespaceValidator]],
        numeroDeCelular: [null, [Validators.required, this.noWhitespaceValidator]],
      }
    );
  }

  private obtenerValor(key: string) {
    return this.crearFuncionarioForm.get(key)?.value;
  }

  empezarEdicion(creacionDeFuncionarioEditar: FuncionarioDTO) {
    this.esEdicion = true;
    this.funcionarioEdicion = creacionDeFuncionarioEditar;
    this.crearFuncionarioForm.get("nombres")?.setValue(creacionDeFuncionarioEditar.nombres);
    this.crearFuncionarioForm.get("apellidos")?.setValue(creacionDeFuncionarioEditar.apellidos);
    this.crearFuncionarioForm.get("email")?.setValue(creacionDeFuncionarioEditar.email);
    this.crearFuncionarioForm.get("telefono")?.setValue(creacionDeFuncionarioEditar.telefono);
    this.crearFuncionarioForm.get("numeroDeDocumento")?.setValue(creacionDeFuncionarioEditar.numeroDeDocumento);
    this.crearFuncionarioForm.get("numeroDeCelular")?.setValue(creacionDeFuncionarioEditar.numeroDeCelular);
  }

  cancelarEdicion() {
    this.esEdicion = false;
    this.crearFuncionarioForm.reset();
    this.funcionarioEdicion = null;

    this.canceloEdicion.emit(true);
  }

  ejecutarAccion() {

    if (this.crearFuncionarioForm.invalid) {
      return;
    }    

    if (this.obtenerValor("apellidos") == ' ' ||  this.obtenerValor("nombres") == ' ') {
      this.crearFuncionarioForm.get("nombres")?.reset();
      this.crearFuncionarioForm.get("apellidos")?.reset();
      return;
    }

    this.crearFuncionarioForm.disable();

    let funcionarioCreacion = new FuncionarioDTO();
    funcionarioCreacion.apellidos = this.obtenerValor("apellidos");
    funcionarioCreacion.nombres = this.obtenerValor("nombres");
    funcionarioCreacion.email = this.obtenerValor("email");
    funcionarioCreacion.telefono = this.obtenerValor("telefono");
    funcionarioCreacion.numeroDeDocumento = this.obtenerValor("numeroDeDocumento");
    funcionarioCreacion.numeroDeCelular = this.obtenerValor("numeroDeCelular");
    funcionarioCreacion.esEdicion = this.esEdicion;

    this.funcionarioService.crearFuncionario(funcionarioCreacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {
          this.crearFuncionarioForm.enable();

          this.completoOperacion.emit(response.exito);
          if (!response.exito) {
            this.funcionarioService.checkError(response);

            return;
          }
          this.cancelarEdicion();
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
        },
        error: (error: any) => {
          this.funcionarioService.checkError(error);
          this.crearFuncionarioForm.enable();
        }
      }
    );
  }  

  limpiarCaracteresEspeciales(event: any) {
    let valor: string = event.target.value;
    valor = valor.trim()
    valor = valor.replace(/[^a-zA-Z0-9]/g, '');
    event.target.value = valor.toUpperCase();
  }

  limpiarCaracteresEspecialesConEspacio(event: any) {
    let valor: string = event.target.value;
    valor = valor.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ ]/g, '');   
    valor = valor.replace(/\s+/g, ' '); 
    event.target.value = valor;
  }

  limpiarEspaciosBlanco(event: any) {
    let valor: string = event.target.value;    
    event.target.value = valor.trim();    
  }

  validarCaracteresCorreo(event: any) {
    let valor = event.target.value;
    // valor = valor.trim();
    valor = valor.replace(/[^a-zA-Z0-9@._-]/g, '');
    event.target.value = valor;
  }  

  validarNumeros(event: any) {
    let valor = event.target.value;
    valor = valor.trim()
    valor = valor.replace(/[^0-9]/g, '');
    event.target.value = valor;     
  }  

  public noWhitespaceValidator(control: FormControl) {
    return (control.value || '').trim().length? null : { 'whitespace': true };       
  }
}
