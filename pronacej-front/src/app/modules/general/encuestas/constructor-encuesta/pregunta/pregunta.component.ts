import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, Input, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormGroupDirective, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatRadioModule } from '@angular/material/radio';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckbox } from '@angular/material/checkbox';
import { CatalogoService } from 'app/core/services/catalogo.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';

@Component({
  selector: 'app-pregunta',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DragDropModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    MatIconModule,
    MatButtonModule,
    MatCheckbox
  ],
  templateUrl: './pregunta.component.html',
  styleUrl: './pregunta.component.scss'
})
export class PreguntaComponent implements OnInit {
  @Input() preguntaFormGroup!: FormGroup;
  @Input() preguntaIndex: number;
  @Input() test: any;

  nemonicoPantalla = etiquetasModel.NEMONICO_MENU_ENCUESTA;
  mainForm: FormGroup;
  listaTiposPregunta: CatalogoDTO[] = [];

  constructor(
    public rootFormGroup: FormGroupDirective,
    public fb: FormBuilder,
    private catalogoService:CatalogoService
  ) { }

  ngOnInit() {
    this.mainForm = this.rootFormGroup.control;
    this.cargarTiposPreguntas();
  }

  newRespuestasArray() {
    let respuestasArray = this.fb.array([
      this.fb.group({
        respuesta: ['', Validators.required],
        valorRespuesta: [null],
        respuestaCorrecta: false,
        orden: [0],
      })
    ]);

    return respuestasArray;
  }

  onChangeQuestionType(i: number) {
    let preguntaActual = this.listaPreguntas.controls.at(i) as FormGroup;
    const tipoSeleccionado = this.preguntaFormGroup.get('categoria').value;

    // Crear un nuevo FormArray para 'respuestas'
    switch (tipoSeleccionado) {
      case 'PREG_SEL_UNICA':
      case 'PREG_OP_MULTIPLE':
        // Asigna el nuevo FormArray a 'respuestas' usando setControl
        preguntaActual.setControl('respuestas', this.newRespuestasArray());

        break;

      default:
        break;
    }
  }

  cargarTiposPreguntas(): void {
    this.catalogoService.obtenerHijos(etiquetasModel.TIPO_PREGUNTA, this.nemonicoPantalla).subscribe({
      next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {

        if (!response.exito) {
          this.catalogoService.checkError(response);
          return;
        }

        this.listaTiposPregunta = response.data;
      },
      error: (error: any) => {
        this.catalogoService.checkError(error);
      }
    });
  }


  dropAnswer(event: CdkDragDrop<string[]>) {
    moveItemInArray(
      this.respuestas,
      event.previousIndex,
      event.currentIndex
    );
  }

  async removeAnswer(i: number) {
    // remove the target in form array
    let answers = this.preguntaFormGroup.get(
      'respuestas'
    ) as FormArray;

    answers.removeAt(i);

    await this.updateAnswerOrder();
  }


  async addNewAnswer(i: number) {
    // Insert next to the parent question

    let answers = this.preguntaFormGroup.get(
      'respuestas'
    ) as FormArray;

    let newFormGroup: FormGroup = this.fb.group({
      respuesta: ['', Validators.required],
      valorRespuesta: [null],
      respuestaCorrecta: false,
      orden: [i + 1]
    });

    answers.insert(i + 1, newFormGroup);

    await this.updateAnswerOrder();
  }

  async updateAnswerOrder() {
    await this.respuestas.forEach((respuesta, indice) => {
      respuesta.patchValue({
        orden: indice,
      });
    });
  }

  get listaPreguntas() {
    return this.mainForm.get('preguntas') as FormArray;
  }

  get tipoPregunta() {
    return this.preguntaFormGroup.get('categoria');
  }

  get respuestas() {
    return this.preguntaFormGroup.get('respuestas')['controls'];
  }
}
