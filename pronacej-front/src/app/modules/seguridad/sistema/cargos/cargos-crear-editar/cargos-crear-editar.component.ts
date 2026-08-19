import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { RolService } from 'app/modules/seguridad/services/rol.service';
import { environment } from 'environments/environment.development';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { forkJoin } from 'rxjs';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { CargosJerarquiaDTO } from 'app/core/model/both/cargosJerarquiaDTO.model';
import { CargosJerarquiaService } from 'app/modules/seguridad/services/cargosJerarquia.service';

@Component({
  selector: 'app-cargos-crear-editar',
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
  templateUrl: './cargos-crear-editar.component.html',
  styleUrl: './cargos-crear-editar.component.scss'
})
export class CargosCrearEditarComponent {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_CARGOS_JERARQUIA;

  @Input() esFlujo: boolean = false;
  
  esEdicion = false;
  crearCargoForm: FormGroup;
  item: CargosJerarquiaDTO;

  @Output() completoOperacion = new EventEmitter<boolean>();
  @Output() canceloEdicion = new EventEmitter<boolean>();

  cargoEdicion: CargosJerarquiaDTO;
  listaDepartamentos: JerarquiaDTO[] = [];

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private rolService: RolService,
    private authSerguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private router: Router,
    private catalogoService: CatalogoService,
    private jerarquiaService: JerarquiaService,
    private cargosJerarquiaService: CargosJerarquiaService,
  ) {
    this.construirForm();

  }

  async ngOnInit(): Promise<void> {
    // this.obtenerTiposDocumentoIdentificacion();
    // this.obtenerRoles();
    
    await this.cargarDatosCentros();

    this.item = history.state.item;

    if (this.item) {
      // Si existe el objeto, significa que estamos en modo edición
      this.esEdicion = true;
      this.empezarEdicion(this.item);
    }
  }

  async cargarDatosCentros(): Promise<void> {
    return new Promise((resolve) => {
        forkJoin({
            departamentosAbiertos: this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre("SOA", this.nemonicoMenu),
            departamentosCerrados: this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre("CJDR", this.nemonicoMenu)
        }).subscribe({
            next: ({ departamentosAbiertos, departamentosCerrados }) => {
                if (!departamentosAbiertos.exito || !departamentosCerrados.exito) {
                    this.jerarquiaService.checkError(departamentosAbiertos || departamentosCerrados);
                    resolve();
                    return;
                }

                if (!environment.production) {
                    console.log(departamentosAbiertos.data, departamentosCerrados.data);
                }

                // Combinar resultados
                this.listaDepartamentos = [...departamentosAbiertos.data, ...departamentosCerrados.data];
                resolve();
            },
            error: (error: any) => {
                this.jerarquiaService.checkError(error);
                resolve();
            }
        });
    });
  }

  construirForm() {
    this.crearCargoForm = this.fb.group(
      {
        nombre: ['', [Validators.required]],
        departamento: [0, ],
        esJefe: [false],
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
    return this.crearCargoForm.get(key)?.value;
  }

  empezarEdicion(cargosJerarquiaDTO: CargosJerarquiaDTO) {
    this.esEdicion = true;
    this.cargoEdicion = cargosJerarquiaDTO;
    console.log(cargosJerarquiaDTO);
    this.crearCargoForm.get("nombre")?.setValue(cargosJerarquiaDTO.nombre);
    // this.crearCargoForm.get("departamento")?.setValue(cargosJerarquiaDTO.idJerarquia);
    this.crearCargoForm.get("esJefe")?.setValue(cargosJerarquiaDTO.esJefe);
  }

  cancelar() {
    this.crearCargoForm.reset();
    this.cargoEdicion = null;
    this.esEdicion = false;
    
    if(!this.esFlujo){
      this.router.navigate(['../'], { relativeTo: this.route });
    } 
    // else {
    //   this.router.navigate(['/seguridad/sistema/funcionarios']);
    // }
  }

  ejecutarAccion() {

    if (this.crearCargoForm.invalid) {
      return;
    }

    this.crearCargoForm.disable();

    let cargosJerarquiaDTO = new CargosJerarquiaDTO();
    cargosJerarquiaDTO.nombre = this.obtenerValor("nombre");
    // cargosJerarquiaDTO.idJerarquia = this.obtenerValor("departamento");
    cargosJerarquiaDTO.esJefe = this.obtenerValor("esJefe");
    cargosJerarquiaDTO.tokenIdentificador = this.cargoEdicion?.tokenIdentificador;
    cargosJerarquiaDTO.esEdicion = this.esEdicion;

    this.cargosJerarquiaService.crearCargoJerarquia(cargosJerarquiaDTO, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CargosJerarquiaDTO>) => {
          this.crearCargoForm.enable();

          this.completoOperacion.emit(response.exito);
          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo,
              response.mensaje);
            return;
          }
          this.cancelar();
          const dialogRef = this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          dialogRef.afterClosed().subscribe(() => {
            this.router.navigate(['/seguridad/sistema/cargos']);
          });
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
          this.crearCargoForm.enable();
        }
      }
    );
  }

}
