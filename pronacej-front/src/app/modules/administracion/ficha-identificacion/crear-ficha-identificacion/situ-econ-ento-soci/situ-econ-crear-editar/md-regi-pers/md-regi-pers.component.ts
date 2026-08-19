import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormsModule, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioButton, MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';

@Component({
  selector: 'app-md-regi-pers',
  standalone: true,
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatRadioButton,
    FormsModule,
    MatButtonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatIconModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatRadioModule,
  ],
  templateUrl: './md-regi-pers.component.html',
  styleUrl: './md-regi-pers.component.scss'
})
export class MdRegiPersComponent implements OnInit {
  // Variables de estado
  esEdicion = false;
  personaParaEditar: PersonaRelacionadaDTO;

  // Listas de datos
  listaPersonasRelacionadasTotales: PersonaRelacionadaDTO[] = [];
  listaCondicionesLaborales: CatalogoDTO[] = [];

  // Formulario - Solo incluye los campos que están en el HTML
  formularioRegistroPersona = this.constructorFormulario.group({
    personaRelacionada: ['0', [Validators.required, Validators.pattern(/^(?!0$).*$/)]],
    condicionLaboral: ['0'], // No es requerido según el HTML actual
    ocupacion: ['', [Validators.required, this.validarNoEspacios()]],
    otros: ['', [this.validarNoEspacios()]],
    ingresoPromedio: [0, [Validators.required, Validators.min(0), Validators.max(999999)]],
    numeroHijos: [0, [Validators.required, Validators.min(0), Validators.max(99)]],
    observaciones: ['', [this.validarNoEspacios()]],
    esResponsableEconom: ['', [Validators.required]]
  });

  constructor(
    private constructorFormulario: FormBuilder,
    private referenciaDialogo: MatDialogRef<MdRegiPersComponent>,
    @Inject(MAT_DIALOG_DATA) public datos: any,
  ) { }

  ngOnInit(): void {
    // Inicializar listas desde los datos recibidos
    this.listaPersonasRelacionadasTotales = this.datos.listaPersonasRelacionadasTotales;
    this.listaCondicionesLaborales = this.datos.listaCondicionesLaborales;
    
    // Si estamos en modo edición, cargamos los datos de la persona
    if (this.datos.fila) {
      this.esEdicion = true;
      this.personaParaEditar = this.obtenerPersonaRelacionada(this.datos.fila);
      this.cargarDatosFormulario(this.personaParaEditar);
    }
  }

  /**
   * Valida que el campo no contenga solo espacios en blanco
   * @returns Validador personalizado
   */
  validarNoEspacios(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const esInvalido = control?.value && control?.value?.trim().length === 0;
      return esInvalido ? { 'soloEspacios': true } : null;
    };
  }

  /**
   * Obtiene la persona relacionada a partir de los datos recibidos
   * @param datos Datos de la persona relacionada
   * @returns Objeto PersonaRelacionadaDTO
   */
  obtenerPersonaRelacionada(datos: any): PersonaRelacionadaDTO {
    const personaRelacionada = new PersonaRelacionadaDTO();
  
    // Datos principales
    personaRelacionada.tokenIdentificador = datos.tokenIdentificador;
    personaRelacionada.esEdicion = true;
    personaRelacionada.tokenIdentificadorEvaluacionSocial = "0";
    
    // Campos editables en el formulario
    personaRelacionada.tokenIdentificadorCondicionLaboral = datos.tokenIdentificadorCondicionLaboral || "0";
    personaRelacionada.ocupacion = datos.ocupacion || null;
    personaRelacionada.otros = datos.otros || null;
    personaRelacionada.ingresoPromedio = datos.ingresoPromedio || null;
    personaRelacionada.numeroHijos = datos.numeroHijos || null;
    personaRelacionada.observaciones = datos.observaciones || null;
    personaRelacionada.esResponsableEconom = datos.esResponsableEconom ?? false;
  
    // Campos informativos (no editables en este formulario)
    personaRelacionada.apellidoPaterno = datos.apellidoPaterno || null;
    personaRelacionada.apellidoMaterno = datos.apellidoMaterno || null;
    personaRelacionada.nombres = datos.nombres || null;
    personaRelacionada.primerNombre = datos.primerNombre || null;
    personaRelacionada.segundoNombre = datos.segundoNombre || null;
    personaRelacionada.primerApellido = datos.primerApellido || null;
    personaRelacionada.segundoApellido = datos.segundoApellido || null;
    personaRelacionada.tipoIdentificacion = datos.tipoIdentificacion || null;
    personaRelacionada.numeroDocumento = datos.numeroDocumento || null;
    personaRelacionada.tipoOcupacion = datos.tipoOcupacion || null;
    personaRelacionada.estadoCivil = datos.estadoCivil || null;
    personaRelacionada.parentesco = datos.parentesco || null;
    personaRelacionada.tipoParentesco = datos.tipoParentesco || null;
    personaRelacionada.fechaNacimiento = datos.fechaNacimiento || null;
    personaRelacionada.tipoSexo = datos.tipoSexo || null;
    personaRelacionada.telefono = datos.telefono || null;
    personaRelacionada.esTutor = datos.esTutor || null;
    personaRelacionada.visitaAutorizada = datos.visitaAutorizada || null;
    personaRelacionada.fallecido = datos.fallecido || null;
    personaRelacionada.informacionUbicaciones = datos.informacionUbicaciones || [];
    personaRelacionada.informacionUbicacionesEliminar = datos.informacionUbicacionesEliminar || [];
    personaRelacionada.tokenIdentificadorFicha = datos.tokenIdentificadorFicha || null;
  
    return personaRelacionada;
  }

  /**
   * Carga los datos de la persona relacionada en el formulario
   * @param personaRelacionada Persona relacionada a cargar
   */
  cargarDatosFormulario(personaRelacionada: PersonaRelacionadaDTO) {
    this.formularioRegistroPersona.patchValue({
      personaRelacionada: personaRelacionada.tokenIdentificador || "0",
      condicionLaboral: personaRelacionada.tokenIdentificadorCondicionLaboral || "0",
      ocupacion: personaRelacionada.ocupacion || "",
      otros: personaRelacionada.otros || "",
      ingresoPromedio: personaRelacionada.ingresoPromedio || 0,
      numeroHijos: personaRelacionada.numeroHijos || 0,
      observaciones: personaRelacionada.observaciones || "",
      esResponsableEconom: personaRelacionada.esResponsableEconom ? "S" : "N"
    });
  }

  /**
   * Guarda los datos del formulario y cierra el diálogo
   */
  guardarPersonaRelacionada() {
    const valoresFormulario = this.formularioRegistroPersona.value;
    
    // Si es un nuevo registro
    if (!this.esEdicion) {
      // Buscar la persona seleccionada en la lista
      let persona = this.listaPersonasRelacionadasTotales.find(persona => 
          persona.tokenIdentificador === valoresFormulario.personaRelacionada);

      if (!persona) {
        console.error("No se encontró la persona seleccionada");
        return;
      }

      // Configurar datos básicos
      persona.esEdicion = true;
      persona.tokenIdentificadorEvaluacionSocial = "0";
      
      // Actualizar los campos con los valores del formulario
      persona.tokenIdentificadorCondicionLaboral = valoresFormulario.condicionLaboral;
      persona.ocupacion = valoresFormulario.ocupacion?.trim();
      persona.otros = valoresFormulario.otros?.trim() || null;
      persona.ingresoPromedio = valoresFormulario.ingresoPromedio;
      persona.numeroHijos = valoresFormulario.numeroHijos;
      persona.observaciones = valoresFormulario.observaciones?.trim() || null;
      persona.esResponsableEconom = valoresFormulario.esResponsableEconom === "S";

      // Cerrar el diálogo y devolver la persona
      this.referenciaDialogo.close(persona);
    } 
    // Si estamos editando
    else {
      // Configurar para edición
      this.personaParaEditar.esEdicion = true;
  
      // Actualizar los campos con los valores del formulario
      this.personaParaEditar.tokenIdentificadorCondicionLaboral = valoresFormulario.condicionLaboral;
      this.personaParaEditar.ocupacion = valoresFormulario.ocupacion?.trim();
      this.personaParaEditar.otros = valoresFormulario.otros?.trim() || null;
      this.personaParaEditar.ingresoPromedio = valoresFormulario.ingresoPromedio;
      this.personaParaEditar.numeroHijos = valoresFormulario.numeroHijos;
      this.personaParaEditar.observaciones = valoresFormulario.observaciones?.trim() || null;
      this.personaParaEditar.esResponsableEconom = valoresFormulario.esResponsableEconom === "S";
  
      // Cerrar el diálogo y devolver la persona editada
      this.referenciaDialogo.close(this.personaParaEditar);
    }
  }
}