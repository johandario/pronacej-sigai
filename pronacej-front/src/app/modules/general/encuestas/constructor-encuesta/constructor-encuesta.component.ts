import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormGroupDirective, ReactiveFormsModule, Validators } from '@angular/forms';
import { PreguntaComponent } from './pregunta/pregunta.component';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatExpansionModule } from '@angular/material/expansion';
import { PreguntaDTO } from 'app/core/model/both/encuesta/preguntaDTO.model';
import { Subscription } from 'rxjs';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import { SeccionDTO } from 'app/core/model/both/encuesta/seccionDTO.model';
import { RespuestaDTO } from 'app/core/model/both/encuesta/respuestaDTO.model';
import { EncuestaService } from 'app/modules/general/services/encuesta.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { EncabezadoDTO } from 'app/core/model/both/encuesta/encabezadoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';

@Component({
  selector: 'app-constructor-encuesta',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    PreguntaComponent,
    DragDropModule,
    MatLabel,
    MatCardModule,
    MatIconModule,
    MatDividerModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatExpansionModule
  ],
  templateUrl: './constructor-encuesta.component.html',
  styleUrl: './constructor-encuesta.component.scss',
  providers: [FormGroupDirective]
})
export class ConstructorEncuestaComponent implements OnInit {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_ENCUESTAS;
  encuestaForm: FormGroup;

  tokenEncuesta: string;
  esEdicion: boolean;

  subscription: Subscription;
  allowDropOnFirstHalf: boolean = false;

  listaTiposCentro: CatalogoDTO[];
  listaCategorias: CatalogoDTO[];

  isPanelExpanded: boolean[][] = [];

  onCardClick(element, event) {
    let test = event.srcElement as HTMLDivElement;
  }

  constructor(
    private fb: FormBuilder,
    private rootFormGroup: FormGroupDirective,
    private router: Router,
    private encuestaService: EncuestaService,
    private dialogMensajeService: DialogMensajeService,
    public funcionesUtils: FuncionesUtils
  ) {
  }

  ngOnInit() {
    this.encuestaForm = this.rootFormGroup.control;

    this.tokenEncuesta = history.state.tokenEncuesta;

    this.funcionesUtils.obtenerListaCatalogo(etiquetasModel.TIPO_CENTRO, this.nemonicoMenu).subscribe({
      next: (data) => {
        this.listaTiposCentro = data;
      },
      error: (error) => console.error('Error cargando tipos de centro:', error)
    });

    this.funcionesUtils.obtenerListaCatalogo(etiquetasModel.CATEGORIAS_EVALUACIONES, this.nemonicoMenu).subscribe({
      next: (data) => {
        this.listaCategorias = data;
      },
      error: (error) => console.error('Error cargando categorías de las evaluaciones:', error)
    });

    this.formInit();

    if (this.tokenEncuesta) {
      this.esEdicion = true;
      this.obtenerEncuesta();
    }
  }

  newPreguntaForm() {
    let newPreguntaFormGroup = this.fb.group({
      texto: ['', Validators.required],
      categoria: ['PREG_SI_NO'],
      cuestionarioJerarquiaId: [null],
      orden: [0],
      requerido: true,
      respuestasOrdenadas: false,
      tieneObservaciones: false,
      permiteDocumentos: false,
      respuestas: this.fb.array([]),
    });

    return newPreguntaFormGroup;
  }

  guardar() {
    if (this.esEdicion)
      this.editarEncuesta();
    else
      this.crearEncuesta();
  }

  cancelar() {
    this.encuestaForm.reset();
    this.router.navigate(['/general/encuestas']);
  }

  crearEncuesta() {
    let encuestaDTO = new EncuestaDTO();

    encuestaDTO = this.transformFormToDTO();

    this.encuestaService.crearEncuesta(encuestaDTO, this.nemonicoMenu).subscribe({
      next: () => {
        this.dialogMensajeService.mensajeExitoso(
          'Guardar',
          'Evaluación creada correctamente.'
        ).afterClosed().subscribe(result => {
          this.cancelar();
        });
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al guardar el registro. Inténtalo de nuevo.'
        );
      }
    });
  }

  editarEncuesta() {
    let encuestaDTO = new EncuestaDTO();

    encuestaDTO = this.transformFormToDTO();

    encuestaDTO.tokenIdentificador = this.tokenEncuesta;

    this.encuestaService.actualizarEncuesta(encuestaDTO, this.nemonicoMenu).subscribe({
      next: () => {
        this.dialogMensajeService.mensajeExitoso(
          'Editar',
          'Evaluación actualizada correctamente.'
        ).afterClosed().subscribe(result => {
          this.cancelar();
        });
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al guardar el registro. Inténtalo de nuevo.'
        );
      }
    });
  }


  dropCardElement(sectionIndex: number, event: CdkDragDrop<string[]>) {

    const previousSectionIndex = Number(event.previousContainer.id.at(event.previousContainer.id.length - 1)) - 1;

    if (event.previousContainer === event.container) {
      moveItemInArray(
        this.getPreguntas(sectionIndex).controls,
        event.previousIndex,
        event.currentIndex
      );
    }
    else {
      const pregunta = this.getPreguntas(previousSectionIndex).controls[event.previousIndex];

      this.getPreguntas(previousSectionIndex).removeAt(event.previousIndex);
      this.getPreguntas(sectionIndex).insert(event.currentIndex, pregunta);
    }

    // Update Order no in the form group
    this.updateQuestionOrder(previousSectionIndex);
    this.updateQuestionOrder(sectionIndex);
  }

  dropSectionElement(event: CdkDragDrop<string[]>) {
    moveItemInArray(
      this.listaSeccionesArray,
      event.previousIndex,
      event.currentIndex
    );

    // Update Order no in the form group
    this.updateSectionOrder();
  }

  async addNewQuestion(sectionIndex: number, questionIndex: number, type: string) {
    let listaPreguntas = this.getPreguntas(sectionIndex);

    let nuevaPreguntaFormGroup = this.fb.group({
      texto: ['', Validators.required],
      categoria: ['PREG_SI_NO'],
      orden: [questionIndex + 1],
      requerido: true,
      respuestasOrdenadas: false,
      tieneObservaciones: false,
      permiteDocumentos: false,
      respuestas: this.fb.array([]),
    });

    listaPreguntas.insert(questionIndex + 1, nuevaPreguntaFormGroup);

    // Update the order No
    await this.updateQuestionOrder(sectionIndex);
  }

  async addNewSection(i: number, type: string) {

    let nuevaSeccionFormGroup = this.fb.group({
      id: [i + 1],
      nombre: ['', Validators.required],
      orden: [i + 1],
      preguntasOrdenadas: [false],
      tienePuntuacion: [false],
      preguntas: this.fb.array([this.newPreguntaForm()]),
    });

    this.listaSecciones.insert(i + 1, nuevaSeccionFormGroup);

    // Update the order No
    await this.updateSectionOrder();

    // Actualiza isPanelExpanded para incluir la nueva sección
    this.isPanelExpanded.splice(i + 1, 0, [true]); // Agrega un array con un valor `true` para la primera pregunta de la nueva sección
  }

  duplicateQuestion(sectionIndex: number, questionIndex: number): void {
    // Obtener el FormArray de preguntas
    const preguntas = this.getPreguntas(sectionIndex);

    // Obtener el FormGroup de la pregunta original
    const preguntaOriginal = preguntas.at(questionIndex) as FormGroup;

    // Clonar los valores de la pregunta original
    const preguntaClonada = preguntaOriginal.value;

    // Clonar las respuestas si existen
    const respuestasOriginales = preguntaOriginal.get('respuestas') as FormArray;
    const respuestasClonadas = this.fb.array(
      respuestasOriginales.controls.map((respuesta) =>
        this.fb.group({ ...respuesta.value })
      )
    );

    // Crear un nuevo FormGroup para la nueva pregunta
    const nuevaPregunta = this.fb.group({
      ...preguntaClonada,
      orden: [questionIndex + 1],
      respuestas: respuestasClonadas,
    });

    // Insertar la nueva pregunta en el FormArray
    preguntas.insert(questionIndex + 1, nuevaPregunta);
  }

  deleteQuestion(sectionIndex: number, questionIndex: number) {
    // remove the target in form array
    let listaPreguntas = this.getPreguntas(sectionIndex);
    listaPreguntas.removeAt(questionIndex);
    this.updateQuestionOrder(sectionIndex);
  }

  deleteSection(i: number) {
    // remove the target in form array
    this.listaSecciones.removeAt(i);
    this.updateSectionOrder();
  }

  async updateQuestionOrder(sectionIndex: number) {
    // Update question order no after every Remove/ Add / duplicate
    let listaPreguntas = this.getPreguntas(sectionIndex).controls;
    await listaPreguntas.forEach((pregunta, indice) => {
      pregunta.patchValue({
        orden: indice,
      });
      //console.log(`q${indice}`, pregunta.value);
    });
  }

  async updateSectionOrder() {
    // Update question order no after every Remove/ Add / duplicate
    let listaSecciones = this.encuestaForm.get('secciones')['controls'];
    await listaSecciones.forEach((seccion, indice) => {
      seccion.patchValue({
        orden: indice,
      });
      //console.log(`q${indice}`, pregunta.value);
    });
  }

  get listaSeccionesArray() {
    return this.encuestaForm.get('secciones')['controls'];
  }

  get listaSecciones() {
    return this.encuestaForm.get('secciones') as FormArray;
  }

  getPreguntas(seccionIndex: number): FormArray {
    return this.listaSecciones.at(seccionIndex).get('preguntas') as FormArray;
  }

  getRespuestas(sectionIndex: number, questionIndex: number): FormArray {
    return this.getPreguntas(sectionIndex).at(questionIndex).get('respuestas') as FormArray;
  }

  formInit() {
    let seccionFormGroup = this.fb.group({
      id: [0],
      nombre: ['', Validators.required],
      orden: [0],
      preguntasOrdenadas: [false],
      tienePuntuacion: [false],
      preguntas: this.fb.array([this.newPreguntaForm()])
    });

    this.encuestaForm = this.fb.group({
      nombre: ['', Validators.required],
      descripcion: [''],
      tipoCentro: ['', Validators.required],
      categoria: ['', Validators.required],
      seccionesOrdenadas: [false],
      idJerarquia: [null],
      secciones: this.fb.array([
        seccionFormGroup
      ])
    });

    this.isPanelExpanded = this.listaSeccionesArray.map((seccion) =>
      Array(seccion.get('preguntas').length).fill(true)
    );
  }

  trackByFn(index: number, item: any): any {
    return item.id; // o cualquier identificador único
  }

  transformFormToDTO(): EncuestaDTO {
    const formValue = this.encuestaForm.value;

    return {
      nombre: formValue.nombre,
      descripcion: formValue.descripcion,
      nemonicoCentro: formValue.tipoCentro,
      nemonicoCategoria: formValue.categoria,
      seccionesOrdenadas: formValue.seccionesOrdenadas,
      idJerarquia: formValue.idJerarquia,
      secciones: formValue.secciones.map((seccion: SeccionDTO) => ({
        nombre: seccion.nombre,
        orden: seccion.orden + 1,
        preguntasOrdenadas: seccion.preguntasOrdenadas,
        tienePuntuacion: seccion.tienePuntuacion,
        preguntas: seccion.preguntas.map((pregunta: PreguntaDTO) => ({
          texto: pregunta.texto,
          categoria: pregunta.categoria,
          orden: pregunta.orden + 1,
          requerido: pregunta.requerido,
          respuestasOrdenadas: pregunta.respuestasOrdenadas,
          tieneObservaciones: pregunta.tieneObservaciones,
          permiteDocumentos: pregunta.permiteDocumentos,
          respuestas: pregunta.respuestas.map((respuesta: RespuestaDTO) => ({
            respuesta: respuesta.respuesta,
            valorRespuesta: respuesta.valorRespuesta,
            respuestaCorrecta: respuesta.respuestaCorrecta,
            orden: respuesta.orden + 1,
          }))
        }))
      }))
    };
  }

  obtenerEncuesta() {

    let encabezadoDTO = new EncabezadoDTO();
    encabezadoDTO.encuesta = this.tokenEncuesta

    this.encuestaService.obtenerEncuestaPorTokenEncuesta(encabezadoDTO, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<EncuestaDTO>) => {

          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'La evaluación no se encuentra configurada. Por favor contacte a su administrador.'
            ).afterClosed().subscribe(result => {
              this.cancelar();
            });
            return;
          }

          this.cargarEncuesta(response.data);

        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  cargarEncuesta(encuesta: EncuestaDTO) {
    console.log(encuesta);
    this.encuestaForm.patchValue({
      nombre: encuesta.nombre,
      descripcion: encuesta.descripcion,
      tipoCentro: encuesta.nemonicoCentro,
      categoria: encuesta.nemonicoCategoria,
      seccionesOrdenadas: encuesta.seccionesOrdenadas,
      idJerarquia: encuesta.idJerarquia,
    });

    const seccionesFormArray = this.encuestaForm.get('secciones') as FormArray;
    seccionesFormArray.clear(); // Limpia las secciones existentes si las hay

    encuesta.secciones.forEach((seccion) => {
      const seccionFormGroup = this.fb.group({
        id: [seccion.idSeccion],
        nombre: [seccion.nombre, Validators.required],
        orden: [seccion.orden],
        preguntasOrdenadas: [seccion.preguntasOrdenadas],
        tienePuntuacion: [seccion.tienePuntuacion],
        preguntas: this.fb.array([]), // Inicializamos vacío
      });

      // Agrega preguntas a la sección
      const preguntasFormArray = seccionFormGroup.get('preguntas') as FormArray;
      seccion.preguntas.forEach((pregunta) => {
        const preguntaFormGroup = this.fb.group({
          texto: [pregunta.texto, Validators.required],
          categoria: [pregunta.categoria],
          orden: [pregunta.orden],
          requerido: [pregunta.requerido],
          respuestasOrdenadas: [pregunta.respuestasOrdenadas],
          tieneObservaciones: [pregunta.tieneObservaciones],
          permiteDocumentos: [pregunta.permiteDocumentos],
          respuestas: this.fb.array([]), // Inicializamos vacío
        });

        // Agrega respuestas a la pregunta
        const respuestasFormArray = preguntaFormGroup.get('respuestas') as FormArray;
        pregunta.respuestas.forEach((respuesta) => {
          const respuestaFormGroup = this.fb.group({
            respuesta: [respuesta.respuesta],
            valorRespuesta: [respuesta.valorRespuesta],
            respuestaCorrecta: [respuesta.respuestaCorrecta],
            orden: [respuesta.orden],
          });
          respuestasFormArray.push(respuestaFormGroup);
        });

        preguntasFormArray.push(preguntaFormGroup);
      });

      seccionesFormArray.push(seccionFormGroup);
    });

    this.isPanelExpanded = this.listaSeccionesArray.map((seccion) =>
      Array(seccion.get('preguntas').length).fill(true)
    );
  }

  onPanelOpen(sectionIndex: number, questionIndex: number): void {
    this.isPanelExpanded[sectionIndex][questionIndex] = true;
  }

  onPanelClose(sectionIndex: number, questionIndex: number): void {
    this.isPanelExpanded[sectionIndex][questionIndex] = false;
  }
}
