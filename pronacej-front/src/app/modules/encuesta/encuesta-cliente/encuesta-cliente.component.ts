import { AsyncPipe, CommonModule, CurrencyPipe, NgClass, NgTemplateOutlet } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatOptionModule, MatRippleModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';

@Component({
    selector: 'app-encuesta-cliente',
    templateUrl: './encuesta-cliente.component.html',
    styleUrls: ['./encuesta-cliente.component.css'],
    styles: [
        /* language=SCSS */
        `
            .inventory-grid {
                grid-template-columns: 48px auto 40px;

                @screen sm {
                    grid-template-columns: 48px auto 112px 72px;
                }

                @screen md {
                    grid-template-columns: 48px 112px auto 112px 72px;
                }

                @screen lg {
                    grid-template-columns: 48px 112px auto 112px 96px 96px 72px;
                }
            }
        `,
    ],
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatSidenavModule,
        NgClass,
        FormsModule,
        ReactiveFormsModule,
        MatPaginatorModule,
        MatProgressBarModule,
        MatFormFieldModule,
        MatInputModule,
        MatSortModule,
        NgTemplateOutlet,
        MatSlideToggleModule,
        MatSelectModule,
        MatOptionModule,
        MatCheckboxModule,
        MatRippleModule,
        AsyncPipe,
        CurrencyPipe,
        MatTableModule
    ]
})
export class EncuestaClienteComponent implements OnInit {
    ELEMENT_DATA = [
        { titulo: "¿Cuál es tu rango de edad?", idPregunta: "p01", tieneAdjunto: true, idAdjunto: "ad02", idObservacion: "ob02", tieneObservacion: true, tituloEncuesta: "Encuesta de satisfacción", element: [{ tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id03", nombre: "18-24", esMultipleOpcion: false, idPregunta: "p01" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id04", nombre: "25-34", esMultipleOpcion: false, idPregunta: "p01" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id05", nombre: "35-44", esMultipleOpcion: false, idPregunta: "p01" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id06", nombre: "45-54", esMultipleOpcion: false, idPregunta: "p01" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id07", nombre: "55+", esMultipleOpcion: false, idPregunta: "p01" }] },
        { titulo: "¿Cuál es tu nivel educativo?", idPregunta: "p02", tieneAdjunto: true, idAdjunto: "ad03", idObservacion: "ob03", tieneObservacion: false, tituloEncuesta: "Encuesta de satisfacción", element: [{ tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id08", nombre: "Primaria", esMultipleOpcion: false, idPregunta: "p02" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id09", nombre: "Secundaria", esMultipleOpcion: false, idPregunta: "p02" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id10", nombre: "Preparatoria", esMultipleOpcion: false, idPregunta: "p02" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id11", nombre: "Universidad", esMultipleOpcion: false, idPregunta: "p02" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id12", nombre: "Postgrado", esMultipleOpcion: false, idPregunta: "p02" }] },
        { titulo: "¿Cuál es tu estado civil?", idPregunta: "p03", tieneAdjunto: true, idAdjunto: "ad04", idObservacion: "ob04", tieneObservacion: false, tituloEncuesta: "Encuesta de satisfacción", element: [{ tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id13", nombre: "Soltero", esMultipleOpcion: false, idPregunta: "p03" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id14", nombre: "Casado", esMultipleOpcion: false, idPregunta: "p03" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id15", nombre: "Divorciado", esMultipleOpcion: false, idPregunta: "p03" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id16", nombre: "Viudo", esMultipleOpcion: false, idPregunta: "p03" }] },
        { titulo: "¿Cuál es tu ocupación?", idPregunta: "p04", tieneAdjunto: true, idAdjunto: "ad05", idObservacion: "ob05", tieneObservacion: false, tituloEncuesta: "Encuesta de satisfacción", element: [{ tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id17", nombre: "Estudiante", esMultipleOpcion: true, idPregunta: "p04" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id18", nombre: "Empleado", esMultipleOpcion: true, idPregunta: "p04" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id19", nombre: "Desempleado", esMultipleOpcion: true, idPregunta: "p04" }, { tipoDato: "TIPO_DATO_CHECKBOX", nombreForm: "id20", nombre: "Jubilado", esMultipleOpcion: true, idPregunta: "p04" }] }
    ];

    surveyForm: FormGroup;
    currentStep = 0;
    steps = [];
    pageSize = 3;

    constructor(private fb: FormBuilder) { }

    ngOnInit() {
        this.steps = this.chunkArray(this.ELEMENT_DATA, this.pageSize);
        this.surveyForm = this.fb.group({

        });
        // Agrega las preguntas al formulario
        this.ELEMENT_DATA.forEach((question: any) => {
            question.element.forEach((element: any) => {
                this.surveyForm.addControl(element.nombreForm, new FormControl(''));
            })
            if (question.tieneObservacion) {
                this.surveyForm.addControl(question.idObservacion, new FormControl(''));
            }
            if (question.tieneAdjunto) {
                this.surveyForm.addControl(question.idAdjunto, new FormControl(''));
            }
        });
    }

    createQuestionGroup(question): FormGroup {
        const elements = question.element.map(el => new FormControl(false));
        const questionGroup = this.fb.group({
            title: [question.titulo],
            elements: this.fb.array(elements),
            observation: [""],
            attachment: [null]
        });
        return questionGroup;
    }

    get questions(): FormArray {
        return this.surveyForm.get('questions') as FormArray;
    }

    chunkArray(arr: any[], size: number) {
        const result = [];
        for (let i = 0; i < arr.length; i += size) {
            result.push(arr.slice(i, i + size));
        }
        return result;
    }

    next() {
        if (this.currentStep < this.steps.length - 1) this.currentStep++;
    }

    previous() {
        if (this.currentStep > 0) this.currentStep--;
    }

    registrarValor(option: any, target: any) {
        if (!option.esMultipleOpcion) {
            this.ELEMENT_DATA.forEach((question: any) => {
                if (question.idPregunta == option.idPregunta) {
                    question.element.forEach((el: any) => {
                        if (el.nombreForm != option.nombreForm) {
                            this.surveyForm.get(el.nombreForm).setValue(false);
                        }
                    });
                }
            });
        }
    }

    onSubmit() {
        alert("Encuesta enviada. ¡Gracias por participar!");
    }
}