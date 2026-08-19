import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { EvaluacionComponent } from 'app/core/components/evaluacion/evaluacion.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { EncuestaService } from 'app/modules/general/services/encuesta.service';

@Component({
  selector: 'app-nivel-riesgo-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    MatExpansionModule,
    ReactiveFormsModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatCheckboxModule,
    MatRadioModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    EvaluacionComponent
  ],
  templateUrl: './nivel-riesgo-crear-editar.component.html',
  styleUrl: './nivel-riesgo-crear-editar.component.scss'
})
export class NivelRiesgoCrearEditarComponent implements OnInit {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_NIVEL_RIESGO;
  tituloPantalla = "Valoración de nivel de riesgo";
  esEdicion = false;

  uuid_fp: string;
  tokenEncabezado: string;
  completada: boolean;
  listaPrev: boolean = false;

  encuestaSeleccionForm: FormGroup;
  listaEncuestas: EncuestaDTO[] = [];

  constructor(
    private formBuilder: FormBuilder,
    private fb: FormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private encuestaService: EncuestaService,
    private router: Router,
    private route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.paramMap.get('uuid_fp');
    
    // Obtener datos del state para determinar si es edición o creación
    const state = history.state;
    if (state) {
      this.listaPrev = state.listaPrev || false;
      this.tokenEncabezado = state.tokenEncabezado;
      this.completada = state.completada;
      
      if (state.uuid_fp) {
        this.uuid_fp = state.uuid_fp;
      }
    }

    // Determinar si es edición basado en la presencia del tokenEncabezado
    this.esEdicion = !!this.tokenEncabezado;

    this.encuestaSeleccionForm = this.formBuilder.group({
      encuesta: ["", [Validators.required]],
    });

    this.cargarEncuestas();
  }

  cargarEncuestas() {
    let encuestaDTO = new EncuestaDTO();
    encuestaDTO.nemonicoCategoria = etiquetasModel.CATEGORIA_RIESGO;

    this.encuestaService.obtenerEncuestas(encuestaDTO, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<EncuestaDTO[]>) => {
        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar las evaluaciones de nivel de riesgo. Inténtalo de nuevo.'
          );
          return;
        }

        this.listaEncuestas = response.data;

        // Si es edición, no necesitamos seleccionar encuesta (ya está determinada)
        // Si es creación y solo hay una encuesta, la seleccionamos automáticamente
        if (!this.esEdicion && this.listaEncuestas.length === 1) {
          this.encuestaSeleccionForm.patchValue({
            encuesta: this.listaEncuestas[0].tokenIdentificador
          });
        }

        // Si es creación y hay un tokenEncuesta en el state, lo usamos
        if (!this.esEdicion && history.state.tokenEncuesta) {
          this.encuestaSeleccionForm.patchValue({
            encuesta: history.state.tokenEncuesta
          });
        }
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar las evaluaciones de nivel de riesgo. Inténtalo de nuevo.'
        );
      }
    });
  }

  regresar() {
    if (this.listaPrev) {
      this.router.navigate(['../'], { relativeTo: this.route });
    } else {
      this.router.navigate(['../../'], { relativeTo: this.route });
    }
  }
}