import { Component, ViewChild } from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatStepper, MatStepperModule } from '@angular/material/stepper';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionariosCrearEditarComponent } from 'app/modules/seguridad/sistema/funcionarios/funcionarios-crear-editar/funcionarios-crear-editar.component';
import { UsuariosCrearEditarComponent } from 'app/modules/seguridad/sistema/usuarios/usuarios-crear-editar/usuarios-crear-editar.component';

@Component({
  selector: 'app-crear-funcionario-usuario',
  standalone: true,
  imports: [
    MatButtonModule,
    MatStepperModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    FuncionariosCrearEditarComponent,
    UsuariosCrearEditarComponent,
  ],
  templateUrl: './crear-funcionario-usuario.component.html',
  styleUrl: './crear-funcionario-usuario.component.scss'
})
export class CrearFuncionarioUsuarioComponent {
  @ViewChild('stepper') stepper!: MatStepper;
  funcionarioData: FuncionarioDTO | null = null;

  onAvanzarPaso(data: FuncionarioDTO) {
    this.funcionarioData = data;
    this.stepper.next();
  }
}
