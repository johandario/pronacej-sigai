import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import etiquetasModel from 'app/core/etiquetas.model';
import { MenuDTO } from 'app/core/model/both/seguridad/MenuDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { SeleccionarObjectoDelArbolComponent } from 'app/core/components/seleccionar-objecto-del-arbol/seleccionar-objecto-del-arbol.component';
import { ObjectoArbol } from 'app/core/components/seleccionar-objecto-del-arbol/ObjectoArbol.model';
import { MenuService } from 'app/modules/seguridad/services/menu.service';
import { environment } from 'environments/environment';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-menu-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    SeleccionarObjectoDelArbolComponent
  ],
  templateUrl: './menu-crear-editar.component.html',
  styleUrl: './menu-crear-editar.component.scss'
})
export class MenuCrearEditarComponent implements OnInit {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_MENU;
  crearMenuForm: FormGroup;
  menuEdicion: MenuDTO;
  listaTipos: { key: string, value: string }[] = [
    { key: "basic", value: "Básico" },
    { key: "group", value: "Grupo" },
    { key: "collapsable", value: "Colapsable" }
  ];
  listaMenusPadres: MenuDTO[] = [];
  objectosArbolMenu: ObjectoArbol<MenuDTO>[] = [];
  listaMenusDisponibles: MenuDTO[] = [];

  isMenuPadreDisabled: boolean = false;  // Variable booleana para controlar el estado
  esEdicion = false;
  mostrarComponenteSeleccion: boolean = false;

  // funcionesUtils = new FuncionesUtils();

  @Output() completoOperacion = new EventEmitter<boolean>();
  @Output() canceloEdicion = new EventEmitter<boolean>();

  constructor(
    private fb: FormBuilder,
    private menuService: MenuService,
    private authSeguridadServicio: AuthSerguridadServicio,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService: CatalogoService,
    private funcionesUtils: FuncionesUtils
  ) {
    this.construirForm();
  }

  ngOnInit(): void {
    this.obtenerMenusPadres();
    this.obtenerMenusDisponibles();
  }

  validarNoEspacios(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const esInvalido = control.value && control.value.trim().length === 0;
      return esInvalido ? { 'soloEspacios': true } : null;
    };
  }

  construirForm() {
    this.crearMenuForm = this.fb.group(
      {
        realizaAuditoria: [false],
        mostrarEnFront: [false],
        title: ["", [Validators.required, this.validarNoEspacios()]],
        subtitle: ["", [this.validarNoEspacios()]],
        nemonico: [null, [this.validarNoEspacios()]],
        type: [null, this.esEdicion ? null : Validators.required],
        icon: [null, [this.validarNoEspacios()]],
        link: [null, [this.validarNoEspacios()]],
        tokenIdentificadorPadre: [null],
      }
    );

    this.crearMenuForm.get('type').valueChanges.subscribe(value => {
      this.onTypeChange(value);
    });
  }

  private obtenerValor(key: string) {
    return this.crearMenuForm.get(key)?.value;
  }

  empezarEdicion(menuDTO: MenuDTO) {
    this.esEdicion = true;
    this.mostrarComponenteSeleccion = false;
    this.menuEdicion = menuDTO;
    this.crearMenuForm.get("realizaAuditoria")?.setValue(menuDTO.realizaAuditoria);
    this.crearMenuForm.get("title")?.setValue(menuDTO.title);
    this.crearMenuForm.get("subtitle")?.setValue(menuDTO.subtitle);
    this.crearMenuForm.get("nemonico")?.setValue(menuDTO.nemonico);
    this.crearMenuForm.get("type")?.setValue(menuDTO.type ? menuDTO.type : "basic");
    this.crearMenuForm.get("mostrarEnFront")?.setValue(menuDTO.mostrarEnFront);
    this.crearMenuForm.get("icon")?.setValue(menuDTO.icon);
    this.crearMenuForm.get("link")?.setValue(menuDTO.link);
    this.crearMenuForm.get("tokenIdentificadorPadre")?.setValue(menuDTO.tokenIdentificadorPadre);
  }

  cancelarEdicion() {
    this.esEdicion = false;
    this.crearMenuForm.reset();
    this.menuEdicion = null;

    this.canceloEdicion.emit(true);
  }

  crearActualizar() {
    if (this.crearMenuForm.invalid) {
      return;
    }

    this.crearMenuForm.disable();

    let menuCreacion = new MenuDTO();
    menuCreacion.realizaAuditoria = this.obtenerValor("realizaAuditoria");
    menuCreacion.title = this.obtenerValor("title");
    menuCreacion.subtitle = this.obtenerValor("subtitle");
    menuCreacion.nemonico = this.obtenerValor("nemonico");
    menuCreacion.type = this.obtenerValor("type");
    menuCreacion.mostrarEnFront = this.obtenerValor("mostrarEnFront");
    menuCreacion.icon = this.obtenerValor("icon");
    menuCreacion.link = this.obtenerValor("link");
    menuCreacion.tokenIdentificadorPadre = this.obtenerValor("tokenIdentificadorPadre");
    menuCreacion.id = this.menuEdicion?.id;

    this.authSeguridadServicio.crearMenu(menuCreacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<MenuDTO>) => {
          this.crearMenuForm.enable();

          this.completoOperacion.emit(response.exito);
          if (!response.exito) {
            this.authSeguridadServicio.checkError(response);

            return;
          }
          this.cancelarEdicion();
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
          this.obtenerMenusDisponibles()
        },
        error: (error: any) => {
          this.authSeguridadServicio.checkError(error);
          this.crearMenuForm.enable();
        }
      }
    );
  }

  obtenerMenusPadres() {
    this.menuService.obtenerMenusPadres(etiquetasModel.NEMONICO_MENU_MENU).subscribe(
        {
          next: (resp: RespuestaPorDefecto<MenuDTO[]>) => {
            if (!environment.production) {
              console.log(resp);
            }
  
            if (!resp.exito) {
              this.dialogMensajeService.mensajeError(resp.mensaje);
              return;
            }
  
            this.listaMenusPadres = resp.data;
          },
          error: (error: any) => {
            this.menuService.checkError(error);
          }
        }
      );
  }

  obtenerMenusDisponibles() {
    this.menuService.obtenerTodosLosMenu(this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<MenuDTO[]>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaMenusDisponibles = response.data;
        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }

  inicializarMenuAPrevisualizar() {
    let listaMenusDisponiblesTemp = JSON.parse(JSON.stringify(this.listaMenusDisponibles));
    if (!this.esEdicion) {
      let nuevoMenu: MenuDTO = {
        id: "0",
        tokenIdentificador: "",
        realizaAuditoria: true,
        title: this.obtenerValor("title"),
        subtitle: this.obtenerValor("subtitle"),
        nemonico: "",
        type: this.obtenerValor("type"),
        mostrarEnFront: true,
        icon: "",
        link: "",
        esPadre: false,
        tokenIdentificadorPadre: this.obtenerValor("tokenIdentificadorPadre"),
        children: []  // Inicializando con un array vacío
      };
    
      if (nuevoMenu.type == "group") {
        nuevoMenu.children = [{}] as MenuDTO[];
        listaMenusDisponiblesTemp.push(nuevoMenu);
      } else if (nuevoMenu.type == "collapsable") {
        for (let menu of listaMenusDisponiblesTemp) {
          if (menu.id == nuevoMenu.tokenIdentificadorPadre) {
            nuevoMenu.children = [{}] as MenuDTO[];
            menu.children.push(nuevoMenu);
          }
        }
      } else if (nuevoMenu.type == "basic") {
        for (let menu of listaMenusDisponiblesTemp) {
          if (menu.id == nuevoMenu.tokenIdentificadorPadre) {
            nuevoMenu.children = [];
            menu.children.push(nuevoMenu);
          }
        }
    
        for (let menu of listaMenusDisponiblesTemp) {
          if (menu.children != null) {
            for (let subMenu of menu.children) {
              let uuid = subMenu.id.split(".");
              if (subMenu.type != "basic" && uuid == nuevoMenu.tokenIdentificadorPadre) {
                nuevoMenu.children = [];
                subMenu.children.push(nuevoMenu);
              }
            }
          }
        }
      }
    }else {
      const encontrado = this.encontrarMenuYActualizar(
        listaMenusDisponiblesTemp, 
        this.menuEdicion.id, 
        this.obtenerValor.bind(this)  // Pasamos el método para obtener valores del formulario
      );
    }

    this.objectosArbolMenu = this.funcionesUtils.crearObjectoArbolConData<MenuDTO>(
      listaMenusDisponiblesTemp, "id", "title", "children"
    );

    this.mostrarComponenteSeleccion = true;
  }
  

  previsualizarNuevoMenu(object: MenuDTO) {
    this.mostrarComponenteSeleccion = false;
  }

  onTypeChange(value: string): void {
    const tokenIdentificadorPadreControl = this.crearMenuForm.get('tokenIdentificadorPadre');

    if (value === 'group') {
      this.isMenuPadreDisabled = true;  // Deshabilitar el menú padre
      this.crearMenuForm.get('tokenIdentificadorPadre')?.setValue(null);  // Establecer el valor en null
      tokenIdentificadorPadreControl.clearValidators();
    } else {
      this.isMenuPadreDisabled = false;  // Habilitar el menú padre
      tokenIdentificadorPadreControl.setValidators(Validators.required);
    }

    tokenIdentificadorPadreControl.updateValueAndValidity();
  }

  // Función recursiva para buscar en la jerarquía de menús
  encontrarMenuYActualizar(
    menus: MenuDTO[],
    tokenIdentificadorBuscado: string,
    obtenerValor: (key: string) => any
  ): boolean {
    // Iteramos sobre cada menú en la lista
    for (let menu of menus) {
      // Si encontramos el menú con el tokenIdentificador correspondiente
      if (menu.tokenIdentificador === tokenIdentificadorBuscado) {
        // Actualizamos el título y el tipo
        menu.title = obtenerValor('title');  // Obtener el nuevo title del formulario
        // menu.type = obtenerValor('type');    // Obtener el nuevo type del formulario
        return true;  // Salimos de la función si lo encontramos
      }

      // Si el menú tiene hijos, hacemos la llamada recursiva para buscar en ellos
      if (menu.children && menu.children.length > 0) {
        const encontrado = this.encontrarMenuYActualizar(menu.children, tokenIdentificadorBuscado, obtenerValor);
        if (encontrado) {
          return true;  // Si lo encontramos en algún nivel inferior, salimos
        }
      }
    }
    return false;  // Si no encontramos el menú en esta rama, devolvemos false
  }

}
