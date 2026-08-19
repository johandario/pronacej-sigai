import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { RolDTO } from 'app/core/model/both/seguridad/rolDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { RolService } from 'app/modules/seguridad/services/rol.service';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { CreacionDeRol } from 'app/core/model/both/CreacionDeRol.model';
import { MatCheckboxModule } from '@angular/material/checkbox';

@Component({
  selector: 'app-roles-crear-editar',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    CommonModule,
  ],
  templateUrl: './roles-crear-editar.component.html',
  styleUrl: './roles-crear-editar.component.scss'
})
export class RolesCrearEditarComponent {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_ROL;

  @Input() esFlujo: boolean = false;

  // private _funcionarioData: FuncionarioDTO | null = null;
  // @Input()
  // set funcionarioData(data: FuncionarioDTO | null) {
  //   this._funcionarioData = data;
  //   if (this.esFlujo && data) {
  //     // Llenar el formulario con los datos recibidos
  //     console.log("Entro como flujo");
  //     this.crearUsuarioForm.get("nombres")?.setValue(data.nombres);
  //     this.crearUsuarioForm.get("apellidos")?.setValue(data.apellidos);
  //     this.crearUsuarioForm.get("tipoDeDocumento")?.setValue(data.tokenIdentificadorTipoDeDocumento);
  //     this.crearUsuarioForm.get("numeroDeDocumento")?.setValue(data.numeroDeDocumento);
  //     this.crearUsuarioForm.get("email")?.setValue(data.email);
  //     this.crearUsuarioForm.get("telefono")?.setValue(data.telefono);
  //     this.crearUsuarioForm.get("numeroDeCelular")?.setValue(data.numeroDeCelular);
  //   }
  // }
  // get funcionarioData(): FuncionarioDTO | null {
  //   return this._funcionarioData;
  // }

  esEdicion = false;
  crearRolForm: FormGroup;
  rolEdicion: RolDTO;
  item: CreacionDeRol;

  @Output() completoOperacion = new EventEmitter<boolean>();
  @Output() canceloEdicion = new EventEmitter<boolean>();

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private rolService: RolService,
    private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private catalogoService: CatalogoService
  ) {
    this.construirForm();

  }

  ngOnInit(): void {
    // this.obtenerTiposDocumentoIdentificacion();
    // this.obtenerRoles();

    this.item = history.state.item;

    if (this.item) {
      // Si existe el objeto, significa que estamos en modo edición
      this.esEdicion = true;
      this.empezarEdicion(this.item);
    }
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

  noSpacesValidator(control: AbstractControl): { [key: string]: boolean } | null {
    const value = control.value || '';

    const onlySpaces = value.trim().length === 0;          // Verifica si solo tiene espacios

    if (onlySpaces) {
      return { 'onlySpaces': true };    // Si son solo espacios, retorna error
    }

    return null; // Si es válido, retorna null
  }

  soloLetrasNumeros(event: KeyboardEvent) {
    const regex = /^[a-zA-Z0-9]$/; // Permite solo letras y números
    if (!regex.test(event.key)) {
      event.preventDefault(); // Impide la entrada de caracteres especiales
    }
  }

  soloNumeros(event: KeyboardEvent) {
    const charCode = event.key.charCodeAt(0);
    if (charCode < 48 || charCode > 57) {
      event.preventDefault(); // Impide que se ingresen caracteres no numéricos
    }
  }

  private obtenerValor(key: string) {
    return this.crearRolForm.get(key)?.value;
  }

  empezarEdicion(creacionDeRol: CreacionDeRol) {
    this.esEdicion = true;
    this.rolEdicion = creacionDeRol;
    console.log(creacionDeRol);
    this.crearRolForm.get("nombre")?.setValue(creacionDeRol.nombre);
    this.crearRolForm.get("codigo")?.setValue(creacionDeRol.codigo);
    this.crearRolForm.get("descripcion")?.setValue(creacionDeRol.descripcion);
    this.crearRolForm.get("esSuperRol")?.setValue(creacionDeRol.esSuperRol);
    this.crearRolForm.get("esRolPorDefecto")?.setValue(creacionDeRol.esRolPorDefecto);
    this.crearRolForm.get("diasExpiracionPassword")?.setValue(creacionDeRol.diasExpiracionPassword);
  }

  cancelar() {
    this.crearRolForm.reset();
    this.rolEdicion = null;
    this.esEdicion = false;

    if (!this.esFlujo) {
      this.router.navigate(['../'], { relativeTo: this.route });
    }
    // else {
    //   this.router.navigate(['/seguridad/sistema/funcionarios']);
    // }
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
          this.cancelar();
          const dialogRef = this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          dialogRef.afterClosed().subscribe(() => {
            this.router.navigate(['/seguridad/sistema/roles']);
          });
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
          this.crearRolForm.enable();
        }
      }
    );
  }

  onCheckboxChange(checkbox: string): void {
    if (checkbox === 'esSuperRol') {
      this.crearRolForm.get('esRolPorDefecto').setValue(false);
    } else if (checkbox === 'esRolPorDefecto') {
      this.crearRolForm.get('esSuperRol').setValue(false);
    }
  }

  soloNumero(event: KeyboardEvent): void {
    const allowedKeys = [
        'Backspace',
        'ArrowLeft',
        'ArrowRight',
        'Tab',
        'Delete',
    ];
    const isNumberKey = event.key >= '0' && event.key <= '9';

    if (!isNumberKey && !allowedKeys.includes(event.key)) {
        event.preventDefault();
    }
}

}
