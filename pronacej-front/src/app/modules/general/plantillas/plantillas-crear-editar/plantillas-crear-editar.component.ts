import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { CreacionDeUsuarioSistema } from 'app/core/model/both/CreacionDeUsuarioSistema.model';
import { RolDTO } from 'app/core/model/both/seguridad/rolDTO.model';
import { UsuarioSistemaDTO } from 'app/core/model/both/seguridad/usuarioSistemaDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { RolService } from 'app/modules/seguridad/services/rol.service';
import { environment } from 'environments/environment.development';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatTabsModule } from '@angular/material/tabs';
import { QuillModule } from 'ngx-quill';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { PlantillaFormularioDTO } from 'app/core/model/both/plantillaFormularioDTO.model';
import { PlantillaFormularioService } from '../../services/plantillaFormulario.service';
import { MatDialog } from '@angular/material/dialog';
import { ModalEditaVariableComponent } from './modal-edita-variable/modal-edita-variable.component';
import { PlantillaVariableDTO } from 'app/core/model/both/plantillaVariableDTO.model';

@Component({
  selector: 'app-plantillas-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DragDropModule,
    // Material modules
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatCardModule,
    MatDividerModule,
    MatListModule,
    MatExpansionModule,
    MatTabsModule,
    QuillModule,
    MatPaginatorModule,
    MatTableModule,
  ],
  templateUrl: './plantillas-crear-editar.component.html',
  styleUrl: './plantillas-crear-editar.component.scss'
})
export class PlantillasCrearEditarComponent {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PLANTILLAS;
  titulo: string = "Plantilla Formulario";
  esEdicion = false;

  listaFormularios: CatalogoDTO[] = [];

  crearPlantillaForm: FormGroup;
  contenidoHtmlBase = etiquetasModel.CONTENIDO_HTML_BASE;
  contenidoHtmlBody = etiquetasModel.CONTENIDO_HTML_BODY;
  contenidoHtmlEnd = etiquetasModel.CONTENIDO_HTML_END;

  keyLabelsTable: any = {
    clave: "Clave",
    enlaceCopiar: "Copiar",
    enlaceEliminar: "Eliminar",
  };

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  dataSource: CdkTableDataSourceInput<PlantillaVariableDTO>;
  listPaginadaVariables: PlantillaVariableDTO[] = [];
  listEliminarVariables: PlantillaVariableDTO[] = [];

  modules = {
    toolbar: [
      ['bold', 'italic', 'underline'], // Negrita, Cursiva, Subrayado
      [{ 'header': '1' }, { 'header': '2' }, { 'font': [] }], // Tamaño de cabecera
      [{ 'list': 'ordered' }, { 'list': 'bullet' }], // Listas ordenadas y desordenadas
      [{ 'align': [] }], // Alineación (izquierda, centro, derecha)
      [{ 'color': [] }, { 'background': [] }], // Colores de texto y fondo
      ['link'], // Enlaces
      [{ 'indent': '-1' }, { 'indent': '+1' }], // Sangrías negativas y positivas
      ['blockquote', 'code-block'], // Citas y bloques de código
      ['clean'], // Limpiar formato
    ],
  };

  item: PlantillaFormularioDTO;
  visualizar: Boolean = false;

  constructor(
    private fb: FormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService: CatalogoService,
    private router: Router,
    private plantillaFormularioService: PlantillaFormularioService,
    public dialog: MatDialog,
  ) {
    this.crearPlantillaForm = this.fb.group({
      formularioRelacionado: [0, Validators.required],
      contenidoHtml: [etiquetasModel.CONTENIDO_HTML_BODY, Validators.required],
    });
  }

  get contenidoHtmlCompleto(): string {
    return this.contenidoHtmlBase + this.obtenerValor("contenidoHtml") + this.contenidoHtmlEnd;
  }

  ngOnInit(): void {
    this.dataSource = this.listPaginadaVariables;
    this.obtenerFormularios();

    this.item = history.state.item;
    this.visualizar = history.state.visualizar;

    if (this.item) {
      this.esEdicion = true;
      this.empezarEdicion(this.item);
    }

    if(this.visualizar)
    {
      this.crearPlantillaForm.disable();
      this.keyLabelsTable = {
        clave: "Clave"
      };
    }
  }

  empezarEdicion(plantillaFormularioDTO: PlantillaFormularioDTO) {
    console.log(plantillaFormularioDTO);
    this.crearPlantillaForm.get('formularioRelacionado')?.setValue(plantillaFormularioDTO.tokenIdentificadorFormularioRelacionado);
    this.crearPlantillaForm.get('contenidoHtml')?.setValue(plantillaFormularioDTO.formularioString); //ESTO TOCA VERLE PORQUE SOLO DEBE SER EL CONTENIDO SIN HEADER NI PARTE FINAL
    this.crearPlantillaForm.get('contenidoHtml')?.setValue(plantillaFormularioDTO.contenidoHtml);
    for (let variable of plantillaFormularioDTO.listaVariables) {
      this.listPaginadaVariables.push(variable);
    }
    this.dataSource = new MatTableDataSource(this.listPaginadaVariables);
  }

  obtenerFormularios() {
    this.catalogoService.obtenerHijos("PLANTILLAS_FORMULARIOS", this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }
          console.log(response);
          this.listaFormularios = response.data;
        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  onContentChange(event: any) {
    console.log('Evento cambiado:', event);
    console.log('Contenido cambiado:', this.contenidoHtmlCompleto);
  }

  agregarVariable() {
    const dialogRef = this.dialog.open(ModalEditaVariableComponent, {
      data: {},
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(async (result) => {
      if (result) {
        console.log(result);
        let variable = {
          clave: result.clave,
          nombre: result.nombre,
          orden: 1,
          valor: "",
          tokenIdentificador: "",
        }
        this.listPaginadaVariables.push(variable);
        this.dataSource = new MatTableDataSource(this.listPaginadaVariables);
      }
    })
  }

  copiarVariable(index: any) {
    navigator.clipboard.writeText(this.listPaginadaVariables[index].clave);
    this.dialogMensajeService.mensajeConConfirmacion("Exito", "Contenido copiado al portapapeles");
  }

  eliminarVariable(index: any) {
    let variable = this.listPaginadaVariables[index];
    // this.dialogMensajeService.mensajeConConfirmacion("Exito","La variable fue eliminada");

    if (variable.tokenIdentificador == null || variable.tokenIdentificador == undefined || variable.tokenIdentificador == "") {
      //Si es una variable nueva se elimina sin confirmacion
      this.listPaginadaVariables.splice(index, 1);
    } else {
      //Si tiene tokenIdentificador significa que la variable existe por lo que deberia eliminarse del sistema
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        "Esta operación es irreversible",
        "¿Está seguro de eliminar la variable?"
      );

      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") {
              this.listPaginadaVariables.splice(index, 1);
              this.listEliminarVariables.push(variable);
            }
          }
        }
      );
    }
    this.dataSource = new MatTableDataSource(this.listPaginadaVariables);
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    // this.obtenerFuncionarios();
  }

  private obtenerValor(key: string) {
    return this.crearPlantillaForm.get(key)?.value;
  }

  ejecutarAccion() {

    if (this.crearPlantillaForm.invalid) {
      return;
    }

    this.crearPlantillaForm.disable();

    let plantillaFormularioDTO = new PlantillaFormularioDTO();
    plantillaFormularioDTO.tokenIdentificadorFormularioRelacionado = this.obtenerValor("formularioRelacionado");
    plantillaFormularioDTO.contenidoHtml = this.obtenerValor("contenidoHtml");
    plantillaFormularioDTO.formularioString = this.contenidoHtmlCompleto;
    // Primero verifica si existe un formulario en la lista con el token buscado
    let formularioSeleccionado = this.listaFormularios.find(
      formulario => formulario.tokenIdentificador === plantillaFormularioDTO.tokenIdentificadorFormularioRelacionado
    );
    if (formularioSeleccionado) {
      plantillaFormularioDTO.nemonico = `${formularioSeleccionado.nemonico}`;
    }
    plantillaFormularioDTO.listaVariables = this.listPaginadaVariables;
    plantillaFormularioDTO.listaVariablesEliminar = this.listEliminarVariables;
    plantillaFormularioDTO.esEdicion = this.esEdicion;
    plantillaFormularioDTO.tokenIdentificador = this.esEdicion ? this.item.tokenIdentificador : null;
    console.log(plantillaFormularioDTO);
    this.plantillaFormularioService.crearPlantillaFormulario(plantillaFormularioDTO, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PlantillaFormularioDTO>) => {
          this.crearPlantillaForm.enable();

          if (!response.exito) {
            this.plantillaFormularioService.checkError(response);

            return;
          }
          this.cancelar();

        },
        error: (error: any) => {
          this.plantillaFormularioService.checkError(error);
          this.crearPlantillaForm.enable();
        }
      }
    );

  }

  cancelar() {
    this.crearPlantillaForm.reset();
    this.router.navigate(['/general/plantillas']);
  }

}
