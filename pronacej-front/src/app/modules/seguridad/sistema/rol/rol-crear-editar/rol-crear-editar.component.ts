import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { RolDTO } from 'app/core/model/both/seguridad/rolDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { RolService } from 'app/modules/seguridad/services/rol.service';
import { MatSelectModule } from '@angular/material/select';
import { environment } from 'environments/environment.development';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { CreacionDeRol } from 'app/core/model/both/CreacionDeRol.model';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
  selector: 'app-rol-crear-editar',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule
  ],
  templateUrl: './rol-crear-editar.component.html',
  styleUrl: './rol-crear-editar.component.scss'
})
export class RolCrearEditarComponent implements OnInit {

  crearRolForm: FormGroup;
  rolEdicion: RolDTO;

  esEdicion = false;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_ROL;

  listRoles: RolDTO[] = [];

  @Output() completoOperacion = new EventEmitter<boolean>();
  @Output() canceloEdicion = new EventEmitter<boolean>();

  constructor(
    private fb: FormBuilder,
    private rolService: RolService,
    private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) {
    this.construirForm();

  }

  ngOnInit(): void {

  }

  construirForm() {
    this.crearRolForm = this.fb.group(
      {
        nombre: ['', [Validators.required]],
        codigo: ['', [Validators.required]],
        descripcion: ['', [Validators.required]],
        esSuperRol: [false],
        esRolPorDefecto: [false],
        diasExpiracionPassword: ['', [Validators.required, Validators.pattern('^[0-9]*$')]],
      }
    );
  }

  private obtenerValor(key: string) {
    return this.crearRolForm.get(key)?.value;
  }

  empezarEdicion(creacionDeRolEditar: CreacionDeRol) {
    this.esEdicion = true;
    this.rolEdicion = creacionDeRolEditar;
    this.crearRolForm.get("nombre")?.setValue(creacionDeRolEditar.nombre);
    this.crearRolForm.get("codigo")?.setValue(creacionDeRolEditar.codigo);
    this.crearRolForm.get("descripcion")?.setValue(creacionDeRolEditar.descripcion);
    this.crearRolForm.get("esSuperRol")?.setValue(creacionDeRolEditar.esSuperRol);
    this.crearRolForm.get("esRolPorDefecto")?.setValue(creacionDeRolEditar.esRolPorDefecto);
    this.crearRolForm.get("diasExpiracionPassword")?.setValue(creacionDeRolEditar.diasExpiracionPassword);
  }

  cancelarEdicion() {
    this.esEdicion = false;
    this.crearRolForm.reset();
    this.rolEdicion = null;

    this.canceloEdicion.emit(true);
  }

  ejecutarAccion() {

    if (this.crearRolForm.invalid) {
      return;
    }

    this.crearRolForm.disable();

    let rolCreacion = new CreacionDeRol();
    rolCreacion.nombre = this.obtenerValor("nombre");
    rolCreacion.codigo = this.obtenerValor("codigo");
    rolCreacion.descripcion = this.obtenerValor("descripcion");
    rolCreacion.esSuperRol = this.obtenerValor("esSuperRol");
    rolCreacion.esRolPorDefecto = this.obtenerValor("esRolPorDefecto");
    rolCreacion.diasExpiracionPassword = this.obtenerValor("diasExpiracionPassword");    
    rolCreacion.tokenIdentificador = this.rolEdicion?.tokenIdentificador;
    rolCreacion.esEdicion = this.esEdicion;

    this.authSerguridadServicio.crearRol(rolCreacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CreacionDeRol>) => {
          this.crearRolForm.enable();

          this.completoOperacion.emit(response.exito);
          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo,
              response.mensaje);
            return;
          }
          this.cancelarEdicion();
          const dialogRef = this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          dialogRef.afterClosed().subscribe(() => {
            this.router.navigate(['/seguridad/sistema/rol']);
          });
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
          this.crearRolForm.enable();
        }
      }
    );
  }

  onSuperRolChange(isChecked: boolean): void {
    if (isChecked) {
      this.crearRolForm.get('esRolPorDefecto').setValue(false);
    }
    this.crearRolForm.get('esSuperRol').setValue(isChecked);
    this.cd.detectChanges(); // Asegura actualización en la UI
  }

  onRolPorDefectoChange(isChecked: boolean): void {
    if (isChecked) {
      this.crearRolForm.get('esSuperRol').setValue(false);
    }
    this.crearRolForm.get('esRolPorDefecto').setValue(isChecked);
    this.cd.detectChanges(); // Asegura actualización en la UI
  }

}
