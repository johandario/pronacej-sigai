import { Component, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatDrawer, MatSidenavModule } from '@angular/material/sidenav';
import { PanelDrawer } from "app/core/model/internos/panelDrawer.model";
import { Subject } from "rxjs";
import { CommonModule, NgClass } from '@angular/common';
import { ActivatedRoute } from "@angular/router";
import { FormsModule, ReactiveFormsModule, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";

@Component(
    {
        selector: "app-formulario-dinamico",
        templateUrl: "./formulario-dinamico.component.html",
        standalone: true,
        imports: [
            CommonModule,
            MatButtonModule,
            MatIconModule,
            MatSidenavModule,
            NgClass,
            FormsModule,
            ReactiveFormsModule
        ]
    }
)
export class FormularioDinamicoComponent implements OnInit, OnDestroy {

    @ViewChild('drawer') drawer: MatDrawer;
    drawerMode: 'over' | 'side' = 'side';
    drawerOpened: boolean = true;

    panels: PanelDrawer[] = [];
    public ELEMENT_DATA: any;
    selectedPanel: string = 'cuenta';
    private _unsubscribeAll: Subject<any> = new Subject<any>();

    tokenIdentificadorUsuario: string;
    solicitarFormGroup: UntypedFormGroup;

    constructor(
        private activatedRoute: ActivatedRoute,
        private _formBuilder: UntypedFormBuilder
    ) {
        this.solicitarFormGroup = this._formBuilder.group({
            plantilla: ['', Validators.required],
            tipoSolicitante: ['', null],
            regionSolicitante: ['', null],
            solicitaUsuario: [true, Validators.required],
            observacion: ['', Validators.required]
        });
    }

    ngOnInit(): void {
        this.tokenIdentificadorUsuario = this.activatedRoute.snapshot.queryParamMap.get("id");

        this.panels = [
            {
                id: "cuenta",
                icono: 'heroicons_outline:user-circle',
                titulo: "Cuenta",
                descripcion: "Administra los datos de tu cuenta"
            },
            {
                id: "seguridad",
                icono: 'heroicons_outline:lock-closed',
                titulo: "Seguridad",
                descripcion: "Administra la contraseña de tu cuenta"
            }
        ]
        
        this.ELEMENT_DATA = [
            {
                "titulo": "¿Cuál es tu sexo?",
                "tituloEncuesta": "Encuesta de satisfacción",
                "element": [
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id01", "nombre": "Masculino" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id02", "nombre": "Femenino" }
                ]
            },
            {
                "titulo": "¿Cuál es tu rango de edad?",
                "tituloEncuesta": "Encuesta de satisfacción",
                "element": [
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id03", "nombre": "18-24" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id04", "nombre": "25-34" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id05", "nombre": "35-44" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id06", "nombre": "45-54" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id07", "nombre": "55+" }
                ]
            },
            {
                "titulo": "¿Cuál es tu nivel educativo?",
                "tituloEncuesta": "Encuesta de satisfacción",
                "element": [
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id08", "nombre": "Primaria" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id09", "nombre": "Secundaria" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id10", "nombre": "Preparatoria" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id11", "nombre": "Universidad" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id12", "nombre": "Postgrado" }
                ]
            },
            {
                "titulo": "¿Cuál es tu estado civil?",
                "tituloEncuesta": "Encuesta de satisfacción",
                "element": [
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id13", "nombre": "Soltero" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id14", "nombre": "Casado" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id15", "nombre": "Divorciado" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id16", "nombre": "Viudo" }
                ]
            },
            {
                "titulo": "¿Cuál es tu ocupación?",
                "tituloEncuesta": "Encuesta de satisfacción",
                "element": [
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id17", "nombre": "Estudiante" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id18", "nombre": "Empleado" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id19", "nombre": "Desempleado" },
                    { "tipoDato": "TIPO_DATO_CHECKBOX", "nombreForm": "id20", "nombre": "Jubilado" }
                ]
            }
        ];
    }

    /**
    * Get the details of the panel
    *
    * @param id
    */
    getPanelInfo(id: string): any {
        return this.panels.find((panel) => panel.id === id);
    }

    /**
     * Navigate to the panel
     *
     * @param panel
     */
    goToPanel(panel: string): void {
        this.selectedPanel = panel;

        // Close the drawer on 'over' mode
        if (this.drawerMode === 'over') {
            this.drawer.close();
        }
    }

    /**
    * On destroy
    */
    ngOnDestroy(): void {
        // Unsubscribe from all subscriptions
        this._unsubscribeAll.next(null);
        this._unsubscribeAll.complete();
    }

    /**
     * Track by function for ngFor loops
     *
     * @param index
     * @param item
     */
    trackByFn(index: number, item: any): any {
        return item.id || index;
    }

    isValidField(field: string): boolean {
        ////console.log(this.solicitarFormGroup.get(field));
        return (
            (this.solicitarFormGroup.get(field)?.touched || this.solicitarFormGroup.get(field)?.dirty)
            && !this.solicitarFormGroup.get(field)?.valid)!;

    }

    getErrorMessage(field: string, messageField: string): string {
        let message = "";

        if (this.solicitarFormGroup.get(field)?.errors?.required) {
            message = 'El campo ' + messageField + ' es requerido.';
        } else if (this.solicitarFormGroup.get(field)?.hasError('pattern')) {
            message = 'El campo ' + messageField + ' debe cumplir con el formato requerido.';
        } else if (this.solicitarFormGroup.get(field)?.hasError('minlength')) {
            const minLength = this.solicitarFormGroup.get(field)?.errors?.minlength.requiredLength;
            message = "El campo " + messageField + " debe tener una longitud mínima de " + minLength + " caracteres.";
        } else if (this.solicitarFormGroup.get(field)?.hasError('maxlength')) {
            const maxLength = this.solicitarFormGroup.get(field)?.errors?.maxlength.requiredLength;
            message = "El campo " + messageField + " debe tener una longitud máxima de " + maxLength + " caracteres.";
        }

        return message;
    }

    registrarValor(campoFormulario: any, valorIngresado: any) {
        console.log(campoFormulario);
        console.log(valorIngresado);
        console.log(this.ELEMENT_DATA);
        this.ELEMENT_DATA[campoFormulario].valor = valorIngresado;
    }

}