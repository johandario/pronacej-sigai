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
  selector: 'app-evaluaciones-psicologicas',
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
  templateUrl: './pruebas-psicologicas.component.html',
  styleUrl: './pruebas-psicologicas.component.scss'
})
export class PruebasPsicologicasComponent {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PRUEBAS_PSICOLOGICAS;
  tituloPantalla = "Pruebas Psicológicas";
  esEdicion = false;

  uuid_fp: string;

  encuestaSeleccionForm: FormGroup;

  listaEncuestas: EncuestaDTO[] = [];

  constructor(
    private formBuilder: FormBuilder,
    private fb: FormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private encuestaService: EncuestaService,
    private router: Router,
    private route: ActivatedRoute,
  ) {

  }

  ngOnInit(): void {
    this.uuid_fp = this.route.snapshot.paramMap.get('uuid_fp');

    this.encuestaSeleccionForm = this.formBuilder.group(
      {
        encuesta: ["", [Validators.required]],
      }
    );

    this.cargarEncuestas();
  }

  cargarEncuestas() {
    let encuestaDTO = new EncuestaDTO();
    encuestaDTO.nemonicoCategoria = etiquetasModel.CATEGORIA_PRUEBA_PSICOLOGICA;

    this.encuestaService.obtenerEncuestas(encuestaDTO, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<EncuestaDTO[]>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.listaEncuestas = response.data;
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  regresar() {
    this.router.navigate(['../../'], { relativeTo: this.route });
  }
}