import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import {
    FormBuilder,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogModule,
    MatDialogRef,
    MatDialogTitle,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { InformacionUbicacionDTO } from 'app/core/model/both/InformacionUbicacionDTO.model';
import { LocalidadDTO } from 'app/core/model/both/localidadDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { InformacionUbicacionService } from 'app/modules/seguridad/services/informacionUbicacion.service';
import { LocalidadService } from 'app/modules/seguridad/services/localidad.service';
import { environment } from 'environments/environment';

@Component({
    selector: 'app-modal-editar-informacion',
    standalone: true,
    imports: [
        MatFormFieldModule,
        MatInputModule,
        FormsModule,
        MatButtonModule,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatIconModule,
        MatDialogModule,
        ReactiveFormsModule,
        MatSelectModule,
        CommonModule,
    ],
    templateUrl: './modal-editar-informacion.component.html',
    styleUrl: './modal-editar-informacion.component.scss',
})
export class ModalEditarInformacionComponent implements OnInit {
    tiposInformacion: CatalogoDTO[];

    ingresoInformacionUbicacionForm = this.fb.group({
        tipoInformacion: [null, [Validators.required]],
        valor: [null],
        id_pais: [null, []],
        id_departamento: [null, []],
        departamento_no_peru: [null, []],
        id_provincia: [null, []],
        provincia_no_peru: [null, []],
        id_distrito: [null, []],
        distrito_no_peru: [null, []],
        domicilio: ['', []],
    });

    ES_PAIS_PERU: boolean = false;
    paises: LocalidadDTO[] = [];
    departamentos: LocalidadDTO[] = [];
    provincias: LocalidadDTO[] = [];
    distritos: LocalidadDTO[] = [];

    tipoInformacionSeleccionado: string;

    constructor(
        private fb: FormBuilder,
        private cd: ChangeDetectorRef,
        private catalogoService: CatalogoService,
        public dialogRef: MatDialogRef<ModalEditarInformacionComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
        private dialogMensajeService: DialogMensajeService,
        private informacionUbicacionService: InformacionUbicacionService,
        private localidadService: LocalidadService
    ) { }

    ngOnInit(): void {
        this.obtenerListaCatalogo('TIPO_INFORMACION_PERSONAL');
        this.listarLocalidad('PAIS');
        if (this.data.informacion) {
            this.ingresoInformacionUbicacionForm
                .get('valor')
                .setValue(this.data.informacion.valor);
            this.ingresoInformacionUbicacionForm
                .get('tipoInformacion')
                .setValue(this.data.informacion.tipoInformacionUbicacion);
            console.log('datos a editar', this.data);
            this.tipoInformacionSeleccionado =
                this.data.informacion.tipoInformacionUbicacion;
            if (this.tipoInformacionSeleccionado == 'INFORMACION_PERSONAL_DIRECCION') {
                // this.listarLocalidad('PAIS');
                const departamento = this.data.informacion.valor.split('/')[1];
                const provincia = this.data.informacion.valor.split('/')[2];
                const distrito = this.data.informacion.valor.split('/')[3];
                const direccion = this.data.informacion.valor.split('/')[4];
                this.ingresoInformacionUbicacionForm.get('id_pais').setValue(this.data.informacion.valor.split('/')[0]);
                if (this.data.informacion.valor.split('/')[0] == 'PAIS-PERU') {
                    this.ingresoInformacionUbicacionForm.get('id_departamento').setValue(departamento);
                    this.ingresoInformacionUbicacionForm.get('id_provincia').setValue(provincia);
                    this.ingresoInformacionUbicacionForm.get('id_distrito').setValue(distrito);
                    this.obtenerLocalidadesPorPadre('PAIS-PERU', 'DEPARTAMENTO');
                    this.obtenerLocalidadesPorPadre(departamento, 'PROVINCIA');
                    this.obtenerLocalidadesPorPadre(provincia, 'DISTRITO');
                    this.ES_PAIS_PERU=true;
                    
                }else{
                    this.ingresoInformacionUbicacionForm.get('departamento_no_peru').setValue(departamento);
                    this.ingresoInformacionUbicacionForm.get('provincia_no_peru').setValue(provincia);
                    this.ingresoInformacionUbicacionForm.get('distrito_no_peru').setValue(distrito);
                }

                this.ingresoInformacionUbicacionForm.get('domicilio').setValue(direccion);


            }
        }
        // this.cd.detectChanges();
    }

    obtenerListaCatalogo(nemonicoPadre: string) {
        this.catalogoService
            .obtenerHijos(
                nemonicoPadre,
                etiquetasModel.NEMONICO_MENU_EVALUACION_SOCIAL
            )
            .subscribe({
                next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
                    if (!environment.production) {
                        // console.log(response);
                    }

                    if (!response.exito) {
                        this.dialogMensajeService.mensajeErrorConTitulo(
                            response.titulo,
                            response.mensaje
                        );
                        return;
                    }

                    this.tiposInformacion = response.data;
                },
                error: (error: any) => {
                    console.log(error);
                },
            });
    }

    ejecutarAccionInformacion() {
        this.ingresoInformacionUbicacionForm.disable();

        let informacion = new InformacionUbicacionDTO();

        informacion.valor =
            this.ingresoInformacionUbicacionForm.get('valor').value;
        informacion.tipoInformacionUbicacion =
            this.ingresoInformacionUbicacionForm.get('tipoInformacion').value;
        informacion.idPersonaRelacionada = this.data.idPersonaRelacionada;

        if (this.data.informacion) {
            informacion.tokenIdentificador =
                this.data.informacion.tokenIdentificador;
            informacion.esEdicion = true;
        }

        this.informacionUbicacionService
            .crearInformacionUbicacion(
                informacion,
                etiquetasModel.NEMONICO_MENU_EVALUACION_SOCIAL
            )
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<InformacionUbicacionDTO>
                ) => {
                    this.ingresoInformacionUbicacionForm.enable();

                    // this.completoOperacion.emit(response.exito);
                    if (!response.exito) {
                        this.informacionUbicacionService.checkError(response);

                        return;
                    }
                    this.dialogMensajeService.mensajeExitoso(
                        response.titulo,
                        response.mensaje
                    );
                    this.ingresoInformacionUbicacionForm.enable();
                    this.ingresoInformacionUbicacionForm.reset();
                    this.dialogRef.close(true);
                },
                error: (error: any) => {
                    this.informacionUbicacionService.checkError(error);
                    this.ingresoInformacionUbicacionForm.enable();
                    this.dialogRef.close(false);
                },
            });
    }

    registroUbicacion() {
        if (this.ingresoInformacionUbicacionForm.valid) {
            let informacion = new InformacionUbicacionDTO();
            informacion.tipoInformacionUbicacion = this.ingresoInformacionUbicacionForm.get(
                'tipoInformacion'
            ).value;

            if (informacion.tipoInformacionUbicacion === "INFORMACION_PERSONAL_DIRECCION") {
                console.log('INFORMACION_PERSONAL_DIRECCION', this.ES_PAIS_PERU)
                if (this.ES_PAIS_PERU) {
                    const valorDireccion = this.obtenerValor('id_pais') + '/' + this.obtenerValor('id_departamento') + '/' + this.obtenerValor('id_provincia')
                        + '/' + this.obtenerValor('id_distrito') + '/' + this.obtenerValor('domicilio')
                    informacion.valor = valorDireccion;
                } else {
                    const valorDireccion = this.obtenerValor('id_pais') + '/' + this.obtenerValor('departamento_no_peru') + '/' + this.obtenerValor('provincia_no_peru')
                        + '/' + this.obtenerValor('distrito_no_peru') + '/' + this.obtenerValor('domicilio');
                    informacion.valor = valorDireccion;
                }

            } else {
                informacion.valor = this.ingresoInformacionUbicacionForm.get('valor').value;
            }


            informacion.idPersonaRelacionada = this.data.idPersonaRelacionada;
            informacion.nombreTipoInformacion = this.tiposInformacion.find(
                (x) => x.nemonico == informacion.tipoInformacionUbicacion
            ).nombre;

            if (this.data.informacion) {
                informacion.tokenIdentificador =
                    this.data.informacion.tokenIdentificador;
                informacion.esEdicion = true;
            } else {
                informacion.id_temporal = Date.now();
            }

            this.dialogRef.close(informacion);
        }
    }

    cerrar() {
        this.dialogRef.close(false);
    }

    listarLocalidad(nemonicoTipo: string) {
        this.localidadService
            .obtenerPorTipo(
                nemonicoTipo,
                etiquetasModel.NEMONICO_MENU_EVALUACION_SOCIAL
            )
            .subscribe({
                next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
                    if (!environment.production) {
                        console.log(response);
                    }

                    if (!response.exito) {
                        this.dialogMensajeService.mensajeErrorConTitulo(
                            response.titulo,
                            response.mensaje
                        );
                        return;
                    }

                    if (nemonicoTipo === 'PAIS') {
                        this.paises = response.data;
                    } else if (nemonicoTipo === 'DEPARTAMENTO') {
                        this.departamentos = response.data;
                    }
                },
                error: (error: any) => {
                    console.log(error);
                },
            });
    }

    obtenerLocalidadesPorPadre(nemonicoPadre: string, nemonicoTipo: string) {
        console.log('datos', nemonicoPadre + nemonicoTipo);
        this.localidadService
            .obtenerHijos(
                nemonicoPadre,
                etiquetasModel.NEMONICO_MENU_EVALUACION_SOCIAL
            )
            .subscribe({
                next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
                    if (!environment.production) {
                        console.log(response);
                    }

                    if (!response.exito) {
                        this.dialogMensajeService.mensajeErrorConTitulo(
                            response.titulo,
                            response.mensaje
                        );
                        return;
                    }

                    if (nemonicoTipo === 'PAIS') {
                        this.paises = response.data;
                    } else if (nemonicoTipo === 'DEPARTAMENTO') {
                        this.departamentos = response.data;
                    } else if (nemonicoTipo === 'PROVINCIA') {
                        this.provincias = response.data;
                    } else if (nemonicoTipo === 'DISTRITO') {
                        this.distritos = response.data;
                    }
                },
                error: (error: any) => {
                    console.log(error);
                },
            });
    }

    verificarPais(event: any) {
        this.ingresoInformacionUbicacionForm
            .get('distrito_no_peru')
            .setValue(null);
        this.ingresoInformacionUbicacionForm
            .get('id_departamento')
            .setValue(null);
        this.ingresoInformacionUbicacionForm
            .get('departamento_no_peru')
            .setValue(null);
        this.ingresoInformacionUbicacionForm.get('id_provincia').setValue(null);
        this.ingresoInformacionUbicacionForm
            .get('provincia_no_peru')
            .setValue(null);
        this.ingresoInformacionUbicacionForm.get('id_distrito').setValue(null);
        let nemonico = event.value;
        this.ES_PAIS_PERU =
            this.paises?.find((pais) => pais.nemonico === 'PAIS-PERU')
                ?.nemonico === nemonico;
        console.log('nemonico escogido', nemonico);
        const departamentoControl = this.ingresoInformacionUbicacionForm.get('id_departamento');
        const provinciaControl = this.ingresoInformacionUbicacionForm.get('id_provincia');
        const distritoControl = this.ingresoInformacionUbicacionForm.get('id_distrito');
        const departamentoNoControl = this.ingresoInformacionUbicacionForm.get('departamento_no_peru');
        const provinciaNOControl = this.ingresoInformacionUbicacionForm.get('provincia_no_peru');
        const distritoNOControl = this.ingresoInformacionUbicacionForm.get('distrito_no_peru');

        if (this.ES_PAIS_PERU) {
            this.obtenerLocalidadesPorPadre(nemonico, 'DEPARTAMENTO');
            departamentoControl?.setValidators([Validators.required]);
            provinciaControl?.setValidators([Validators.required]);
            distritoControl?.setValidators([Validators.required]);
            departamentoNoControl?.clearValidators();
            provinciaNOControl?.clearValidators();
            distritoNOControl?.clearValidators();
        } else {
            this.provincias = [];
            this.provincias = [];
            this.distritos = [];
            departamentoNoControl?.setValidators([Validators.required]);
            provinciaNOControl?.setValidators([Validators.required]);
            distritoNOControl?.setValidators([Validators.required]);
            departamentoControl?.clearValidators();
            provinciaControl?.clearValidators();
            distritoControl?.clearValidators();
        }
        this.updateAllControls();
    }

    tipoCambio(event: any) {
        this.tipoInformacionSeleccionado = event.value;
        const valorControl = this.ingresoInformacionUbicacionForm.get('valor');
        const paisControl = this.ingresoInformacionUbicacionForm.get('id_pais');
        const departamentoControl = this.ingresoInformacionUbicacionForm.get('id_departamento');
        const provinciaControl = this.ingresoInformacionUbicacionForm.get('id_provincia');
        const distritoControl = this.ingresoInformacionUbicacionForm.get('id_distrito');
        const departamentoNoControl = this.ingresoInformacionUbicacionForm.get('departamento_no_peru');
        const provinciaNOControl = this.ingresoInformacionUbicacionForm.get('provincia_no_peru');
        const distritoNOControl = this.ingresoInformacionUbicacionForm.get('distrito_no_peru');
        const domicilio = this.ingresoInformacionUbicacionForm.get('domicilio');
        console.log('this.tipoInformacion', this.tipoInformacionSeleccionado);
        if (this.tipoInformacionSeleccionado === 'INFORMACION_PERSONAL_DIRECCION') {
            valorControl?.clearValidators();
            paisControl?.setValidators([Validators.required]);
            departamentoNoControl?.setValidators([Validators.required]);
            provinciaNOControl?.setValidators([Validators.required]);
            distritoNOControl?.setValidators([Validators.required]);
            domicilio?.setValidators([Validators.required]);
        } else {
            valorControl?.setValidators([Validators.required]);
            paisControl?.clearValidators();
            departamentoControl?.clearValidators();
            provinciaControl?.clearValidators();
            distritoControl?.clearValidators();
            domicilio?.clearValidators();
            distritoNOControl?.clearValidators();
            provinciaNOControl?.clearValidators();
            departamentoNoControl?.clearValidators();

        }
        this.updateAllControls();

    }

    consultarProvincias(event: any) {
        this.provincias = [];
        this.distritos = [];
        this.ingresoInformacionUbicacionForm.get('id_provincia').setValue(null);
        this.ingresoInformacionUbicacionForm
            .get('provincia_no_peru')
            .setValue(null);
        this.ingresoInformacionUbicacionForm
            .get('distrito_no_peru')
            .setValue(null);
        this.ingresoInformacionUbicacionForm.get('id_distrito').setValue(null);
        let nemonico = event.value;
        this.obtenerLocalidadesPorPadre(nemonico, 'PROVINCIA');
    }

    consultarDistritos(event: any) {
        this.distritos = [];
        this.ingresoInformacionUbicacionForm
            .get('distrito_no_peru')
            .setValue(null);
        this.ingresoInformacionUbicacionForm.get('id_distrito').setValue(null);
        let nemonico = event.value;
        this.obtenerLocalidadesPorPadre(nemonico, 'DISTRITO');
    }

    updateAllControls() {
        Object.keys(this.ingresoInformacionUbicacionForm.controls).forEach(controlName => {
            this.ingresoInformacionUbicacionForm.get(controlName)?.updateValueAndValidity();
        });
    }

    private obtenerValor(key: string) {
        return this.ingresoInformacionUbicacionForm.get(key)?.value;
    }
}
