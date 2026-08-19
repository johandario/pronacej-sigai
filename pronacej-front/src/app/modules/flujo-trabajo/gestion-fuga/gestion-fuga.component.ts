import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router, RouterLink, RouterModule } from '@angular/router';
import { GestionFugaDTO } from 'app/core/model/both/GestionFugaDTO.model';
import { Location } from '@angular/common'
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { TareaDTO } from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import { FlujoTrabajoService } from '../flujo-trabajo.service';
import { MatStepperModule } from '@angular/material/stepper';
import { MatIconModule } from '@angular/material/icon';
import { catchError, concatMap, Observable, tap, throwError } from 'rxjs';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';

@Component({
  selector: 'app-traslado',
  standalone: true,
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatStepperModule,
    MatIconModule,
    RouterModule,
  ],
  templateUrl: './gestion-fuga.component.html',
  styleUrl: './gestion-fuga.component.scss'
})

export class GestionFugaComponent {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INICIO;

  indiceSeleccionado: number = 0;

  tokenTarea: string;

  tokenID: string;
  token: string;
  estado: string = '';
  fuga: GestionFugaDTO = new GestionFugaDTO();
  centros: JerarquiaDTO[];
  tareaPrincipal: TareaDTO = new TareaDTO;
  tareaSeleccionada: TareaDTO = new TareaDTO;
  tareasFlujo: TareaDTO[];
  listadoTareasFlujoCargado: boolean = false;
  bloquearEnCurso: boolean = false;
  funcionarioActivo: FuncionarioDTO;


  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private _location: Location,
    private flujoTrabajoService: FlujoTrabajoService,
    private dialogMensajeService: DialogMensajeService,
    public funcionesUtils: FuncionesUtils,
    public funcionarioService: FuncionarioService,
  ) { }



  cargarDatos(): void {
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.obtenerParametrosDeConsulta().pipe(
      concatMap(() => this.obtenerTareasFlujoPorTarea()),
    ).subscribe({
      next: () => {
        load.close();
      },
      error: (err) => {
        console.error('Error durante la ejecución:', err);
        load.close();
      },
      complete: () => load.close(),
    });
  }

  obtenerParametrosDeConsulta(): Observable<any> {
    return this.route.queryParams.pipe(
      tap((params) => {
        const tokenTarea = params['tokenTarea'];
        if (tokenTarea) {
          this.tokenTarea = tokenTarea;
          this.tareaPrincipal.tokenIdentificador = tokenTarea;
        }
        const token = params['token'];
        if (token) {
          this.token = token;
          this.bloquearEnCurso = true;
        }
      })
    );
  }

  obtenerTareasFlujoPorTarea(): Observable<any> {
    return this.flujoTrabajoService.obtenerTareasFlujoPorTarea(this.tareaPrincipal, '').pipe(
      tap((response) => {
        this.tareasFlujo = response.data;
        for (let tarea of this.tareasFlujo) {
          // lógica regular de los estados de las tareas
          if (tarea.estado === 'Completada' || tarea.estado === 'Rechazada') {
            tarea.completada = true;
            tarea.editable = true;
          } else if (tarea.estado === 'En curso') {
            tarea.completada = false;
            tarea.editable = true;
          }

          // lógica en caso de que la tarea venga de la bandeja de salida
          if (this.token && tarea.tokenIdentificador === this.tokenTarea) {
            tarea.completada = false;
            tarea.editable = false;
          }
        }
        this.listadoTareasFlujoCargado = true;
        this.tareaPrincipal = this.tareasFlujo.find(tarea => tarea.tokenIdentificador === this.tareaPrincipal.tokenIdentificador);
        //Seleccionar tarea actual
        this.indiceSeleccionado = this.tareaPrincipal.orden - 1;
        this.irATareaSeleccionada(this.indiceSeleccionado);
      }),
      catchError(err => {
        this.flujoTrabajoService.checkError(err);
        return throwError(() => err);
      })
    );
  }


  onStepChange(event: any) {
    this.irATareaSeleccionada(event.selectedIndex);
  }

  irATareaSeleccionada(index: number) {
    this.tareaSeleccionada = this.tareasFlujo[index];
    if (this.tareaSeleccionada) {
      const urlSeparada = this.tareaSeleccionada.url.split('/');
      const tokenID = urlSeparada[urlSeparada.length - 1];

      const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
      let esUuid = UUID_REGEX.test(tokenID);

      if (esUuid) {
        this.router.navigate([this.tareaSeleccionada.paso.url, tokenID], {
          queryParams: {
            tokenTarea: this.tokenTarea,
            token: this.token
          },
          state: {
            tareaEntrante: this.tareaSeleccionada,
            listaTareas: this.tareasFlujo
          },
          onSameUrlNavigation: 'reload'
        });
      } else {
        this.router.navigate([this.tareaSeleccionada.paso.url], {
          queryParams: {
            tokenTarea: this.tokenTarea,
            token: this.token
          },
          state: {
            tareaEntrante: this.tareaSeleccionada,
            listaTareas: this.tareasFlujo
          },
          onSameUrlNavigation: 'reload'
        });
      }

    }
  }

  cancelar() {
    this._location.back();
  }
}