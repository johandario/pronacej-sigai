import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, computed, inject, model, OnInit, signal, ViewEncapsulation } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { PermisoRolUsuarioDTO, PermisoRolUsuarioMenuAccionDTO, PermisoRolUsuarioMenuDTO } from 'app/core/model/both/permisoRolUsuario.model';
import { MenuDTO } from 'app/core/model/both/seguridad/MenuDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MenuService } from 'app/modules/seguridad/services/menu.service';
import { PermisoRolUsuarioService } from 'app/modules/seguridad/services/permiso-rol-usuario.service';
import { environment } from 'environments/environment';
import { MatTreeModule } from '@angular/material/tree';
import { MatTableModule } from '@angular/material/table';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, concatMap, forkJoin, iif, map, Observable, of, startWith, tap, throwError } from 'rxjs';
import { RolDTO } from 'app/core/model/both/seguridad/rolDTO.model';
import { RolService } from 'app/modules/seguridad/services/rol.service';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { ActivatedRoute, Router, RouterLink, RouterModule } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { cloneDeep } from 'lodash';
import { A11yModule, LiveAnnouncer } from "@angular/cdk/a11y";
import { MatTooltip, MatTooltipModule } from "@angular/material/tooltip";
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { MatChipInputEvent, MatChipsModule } from '@angular/material/chips';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import { FuncionarioJerarquiaRolDTO } from 'app/core/model/both/seguridad/FuncionarioJerarquiaRolDTO.model';

@Component({
  selector: 'app-crear-editar-menu-permiso',
  standalone: true,
  encapsulation: ViewEncapsulation.None,
  imports: [
    MatButtonModule,
    MatTreeModule,
    MatTableModule,
    CommonModule,
    MatCheckboxModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    A11yModule,
    MatTooltipModule,
    MatChipsModule,
    MatAutocompleteModule,
    //AsyncPipe
],
  templateUrl: './crear-editar-menu-permiso.component.html',
  styleUrl: './crear-editar-menu-permiso.component.scss'
})
export class CrearEditarMenuPermisoComponent implements OnInit {

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_PERMISOS;
  nemonicoColaborador = etiquetasModel.NEMONICO_PERMISO_TIPO_ASIGNACION_COLABORADOR_INDIVIDUAL;
  nemonicoRol = etiquetasModel.NEMONICO_PERMISO_TIPO_ASIGNACION_ROL;

  jsonObject!: any;
  menus: any[];  
  menusOriginal: any[];
  roles: RolDTO[];
  rolesSeleccionados: RolDTO[] = [];

  rolesFiltrados$: any;

  rolesOriginal: RolDTO[];

  mensajeRol: string;

  funcionarios: FuncionarioDTO[];
  funcionariosFiltrados$!: Observable<any[]>;

  asignaciones: FuncionarioJerarquiaRolDTO[];
  acciones: CatalogoDTO[];
  tiposAsignacion: CatalogoDTO[];
  tiposPermiso: CatalogoDTO[];
  usuarioRolForm!: FormGroup;
  funcionarioSeleccionado!: FuncionarioDTO;
  permisoRolUsuarioEntrante!: PermisoRolUsuarioDTO;

  private menuService = inject(MenuService);
  private permisoRolUsuarioService = inject(PermisoRolUsuarioService);
  private rolService = inject(RolService);
  private funcionarioService = inject(FuncionarioService);
  private catalogoService = inject(CatalogoService);
  private dialogMensajeService = inject(DialogMensajeService);
  private formBuilder = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private funcionesUtils = inject(FuncionesUtils);

  constructor() { 
    this.resetForm();
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {    
    const token = this.route.snapshot.paramMap.get('token');
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.obtenerCatalogos().pipe(
      concatMap(() => 
        forkJoin({
          menus: this.obtenerMenus(),
          roles: this.obtenerRoles(),
          funcionarios: this.obtenerFuncionarios(),
        })
      ),
      concatMap(() =>
        iif(
          () => token != null, 
          this.obtenerPermisoPorToken(token),          
          of(null)
        )
      )      
    )
    .subscribe({
      next: () => {
        load.close();
      },
      error: (err) => {
        console.error('Error durante la ejecución:', err);
        load.close();
      },
      complete: () => load.close(),
    });
  }

  obtenerMenus(): Observable<any> {
    return this.menuService.obtenerMenusPermisos(this.nemonicoMenu).pipe(
      tap((response) => {
        this.menus = this.flattenTree(response.data);
        this.menusOriginal = [...this.menus];
      }),
      catchError(err => {
        // this.menuService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  obtenerRoles(): Observable<any> {
    return this.rolService.obtenerRoles(this.nemonicoMenu).pipe(
      tap((response) => {
        this.roles = this.funcionesUtils.ordenarLista(response.data, 'nombre');   
        
        this.rolesFiltrados$ = this.usuarioRolForm.get('rol').valueChanges.pipe(
          startWith(''),
          map(value => this.filtrarRoles(value))
        );
      }),
      catchError(err => {
        // this.rolService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  obtenerFuncionarios(): Observable<any> {
    return this.funcionarioService.obtenerFuncionariosSinPaginacion(this.nemonicoMenu).pipe(
      tap((response) => {
        this.funcionarios = response.data;

        this.funcionariosFiltrados$ = this.usuarioRolForm
          .get('funcionario')!
          .valueChanges.pipe(
            startWith(''),
            map(value => typeof value === 'string' ? value : this.displayFuncionario(value)),
            map(nombre => this.filtrarFuncionarios(nombre))
          );
      }),
      catchError(err => {
        // this.rolService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  // obtenerCatalogos(catalogoPadre: string): Observable<any> {
  //   return this.catalogoService.obtenerHijos(catalogoPadre, this.nemonicoMenu).pipe(
  //     tap((response) => {
  //       this.catalogos = response.data;
  //     }),
  //     catchError(err => {
  //       this.rolService.checkError(err);
  //       return throwError(() => err); 
  //     })
  //   );
  // }

  obtenerCatalogos() : Observable<any> {
    const nemonicosCatalogos = [
      etiquetasModel.ACCIONES_MENU_PERMISO_ROL_USUARIO,
      etiquetasModel.NEMONICO_PERMISO_TIPO_ASIGNACION,
      etiquetasModel.NEMONICO_PERMISO_TIPO_PERMISO
    ];

    const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, this.nemonicoMenu));
    
    return forkJoin(solicitudes).pipe(
      tap((results: any[]) => {
        this.acciones = results[0]?.data;         
        this.tiposAsignacion = results[1]?.data;         
        this.tiposPermiso = results[2]?.data;         
      }),
      catchError(err => {
        this.catalogoService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  obtenerPermisoPorToken(token: string): Observable<any> {
    return this.permisoRolUsuarioService.obtenerPermisosPorToken(token, this.nemonicoMenu).pipe(
      tap((response) => {
        this.permisoRolUsuarioEntrante = response.data;

        this.permisoRolUsuarioEntrante.esEdicion = true;
        // si se tiene tokenFuncionarioJerarquiaRol, caso contrario con el tokenRol únicamente
        this.usuarioRolForm.get('tipoAsignacion').setValue(this.permisoRolUsuarioEntrante.tipoAsignacion);
        this.usuarioRolForm.get('tipoPermiso').setValue(this.permisoRolUsuarioEntrante.tipoPermiso);      

        switch (this.permisoRolUsuarioEntrante.tipoAsignacion.nemonico) {
          case etiquetasModel.NEMONICO_PERMISO_TIPO_ASIGNACION_COLABORADOR_INDIVIDUAL:
            this.mensajeRol = 'Si lo deja vacío aplicará a todos los roles por colaborador';
            this.usuarioRolForm.get('funcionario').setValidators([Validators.required]);
          break;
          case etiquetasModel.NEMONICO_PERMISO_TIPO_ASIGNACION_ROL:
            this.mensajeRol = '*Seleccione al menos un rol';
          break;
        } 

        if (this.permisoRolUsuarioEntrante.funcionario != null) {
          // encontrar el funcionario donde el tokenFuncionarioJerarquiaRol corresponda a una asignación
          // const funcionario = this.funcionarios.find(func =>
          //   func.asignaciones?.some(asig => asig.tokenIdentificador === this.permisoRolUsuarioEntrante.tokenFuncionarioJerarquiaRol)
          // );

          // encontrar el rol que tiene la asignación
          // const asignacion = funcionario.asignaciones.find(asig => asig.tokenIdentificador === this.permisoRolUsuarioEntrante.tokenFuncionarioJerarquiaRol);
          // const rol = this.roles.find(rol => rol.tokenIdentificador === asignacion.tokenIdentificadorRol);

          // lista de roles correspondiente únicamente a los roles de las asignaciones      
          
          // asignar a controles de formulario
          const funcionario = this.funcionarios.find(func => func.tokenIdentificador === this.permisoRolUsuarioEntrante.funcionario.tokenIdentificador);
          this.usuarioRolForm.get('funcionario').setValue(funcionario);
          this.cambiarSeleccionUsuario({ option: { value: funcionario } } as MatAutocompleteSelectedEvent);
          
          // this.usuarioRolForm.get('rol').setValue(rol);

        } else {
          // const rol = this.roles.find(rol => rol.tokenIdentificador === this.permisoRolUsuarioEntrante.tokenRol);
          // this.usuarioRolForm.get('rol').setValue(rol);
        }

        this.rolesSeleccionados = this.permisoRolUsuarioEntrante.roles || [];
        
        // TODO: setear con true los menús entrantes
        this.menusOriginal = cloneDeep(this.menus);
        this.menus = this.mergeMenusByToken(this.menus, this.permisoRolUsuarioEntrante.menus);
      }),
      catchError(err => {
        // this.permisoRolUsuarioService.checkError(err);
        return throwError(() => err); 
      })
    );
  }

  guardarPermisos() {
    // Tomar accion en caso de que sea un nuevo permiso o una edicion
    let dto: PermisoRolUsuarioDTO;
    if (this.permisoRolUsuarioEntrante != null) {
      dto = this.armarPermisos(this.permisoRolUsuarioEntrante);
    } else {      
      dto = this.armarPermisos();
    }

    const loading = this.dialogMensajeService.mensajeLoading('Guardando permisos...');
    this.permisoRolUsuarioService.crearEditarPermisos(dto, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<PermisoRolUsuarioDTO>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensajeError);    
            // this.permisoRolUsuarioService.checkError(response);
            return;
          }

          loading.close();
          this.router.navigate(['/seguridad/sistema/menu-permiso']);
          this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);          
          
        },
        error: (error: any) => {
          loading.close();          
          this.permisoRolUsuarioService.checkError(error);
        },
        complete: () => {
          loading.close();
        }
      }
    );
  }  

  cambiarSeleccionUsuario(event: MatAutocompleteSelectedEvent) {
    this.rolesSeleccionados = [];
    this.obtenerRolesIniciales();
    this.funcionarioSeleccionado = event.option.value;    
    let tokensAsignaciones: string[] = this.funcionarioSeleccionado.asignaciones.map(asignacion => asignacion['tokenIdentificadorRol']);
    this.rolesOriginal = [...this.roles];
    this.roles = this.roles.filter(rol => tokensAsignaciones.includes(rol.tokenIdentificador));
    this.rolesFiltrados$ = this.usuarioRolForm.get('rol').valueChanges.pipe(
      startWith(''),
      map(value => this.filtrarRoles(value))
    );
  }  

  cambiarTipoAsignacion(event: MatSelectChange) {
    this.usuarioRolForm.get('funcionario').clearValidators();
    switch (event.value?.nemonico) {
      case etiquetasModel.NEMONICO_PERMISO_TIPO_ASIGNACION_COLABORADOR_INDIVIDUAL:
        this.mensajeRol = 'Si lo deja vacío aplicará a todos los roles por colaborador';
        this.usuarioRolForm.get('funcionario').setValidators([Validators.required]);
      break;
      case etiquetasModel.NEMONICO_PERMISO_TIPO_ASIGNACION_ROL:
        this.mensajeRol = '*Seleccione al menos un rol';
      break;
    }    
    this.usuarioRolForm.get('funcionario').setValue(null);
    this.funcionarioSeleccionado = null;
    this.obtenerRolesIniciales();
    this.rolesSeleccionados = [];
  }
  
  obtenerRolesIniciales() {
    if (this.rolesOriginal != null && this.rolesOriginal.length > 0) {
      this.roles = [...this.rolesOriginal];
    }
    this.rolesFiltrados$ = this.usuarioRolForm.get('rol').valueChanges.pipe(
      startWith(''),
      map(value => this.filtrarRoles(value))
    );
  }

  obtenerMenusIniciales() {
    if (this.menusOriginal != null && this.menusOriginal.length > 0) {
      this.menus = cloneDeep(this.menusOriginal);
    }
  }

  limpiar() {
    const dialogRef = this.dialogMensajeService.mensajeConConfirmacion(
      'Confirmar limpieza',
      '¿Está seguro que desea restablecer el formulario?'
    );

    dialogRef.afterClosed().subscribe(resp => {
      if (resp == "confirmed") {
        this.obtenerRolesIniciales();
        this.rolesSeleccionados = [];
        // this.obtenerMenusIniciales();
        this.resetForm();
        this.funcionarioSeleccionado = null;
      }
    });
  }  

  resetForm() {
    this.usuarioRolForm = this.formBuilder.group({
      funcionario: [null],
      rol: [null],
      tipoAsignacion: [null, Validators.required],
      tipoPermiso: [null, Validators.required]
    });
    this.usuarioRolForm.markAllAsTouched();
  }

  seleccionarTodo() {
    let menus = cloneDeep(this.menusOriginal);
    menus.forEach(menu => {
      menu.acciones.forEach(accion => {
        accion.activo = true;
      }); 
    });
    this.menus = menus;
  }

  private flattenTree(items: MenuDTO[], level = 0, result: any[] = []): any[] {
    for (const item of items) {
      result.push({
        ...item,
        level,
        hasChildren: item.children?.length > 0,
        acciones: this.acciones.map(cat => ({
          tokenCatalogoAccion: cat.tokenIdentificador,
          activo: false     // estado del checkbox
        }))
      });

      if (item.children?.length) {
        this.flattenTree(item.children, level + 1, result);
      }
    }
    return result;
  }  

  private armarPermisos(dto = new PermisoRolUsuarioDTO()): PermisoRolUsuarioDTO {
    //const dto = new PermisoRolUsuarioDTO();
    
    Object.assign(dto, this.usuarioRolForm.value);
    dto.roles = this.rolesSeleccionados;
    // if (this.funcionarioSeleccionado != null) {
    //   let rolSeleccionado = this.usuarioRolForm.get('rol').value;
    //   let asignacion = this.funcionarioSeleccionado.asignaciones.find(asignacion => asignacion.tokenIdentificadorRol === rolSeleccionado.tokenIdentificador);
    //   dto.tokenFuncionarioJerarquiaRol = asignacion.tokenIdentificador;
    //   dto.tokenRol = null;
    // } else {
    //   let rolSeleccionado = this.usuarioRolForm.get('rol').value;
    //   dto.tokenRol = rolSeleccionado.tokenIdentificador;
    // }

    dto.menus = this.menus
      .filter(menu => menu.acciones.some(a => a.activo))
      .map(menu => {
        const menuDto = new PermisoRolUsuarioMenuDTO();
        menuDto.tokenMenu = menu.tokenIdentificador;

        menuDto.acciones = menu.acciones
          .filter(a => a.activo)
          .map(a => {
            const accion = new PermisoRolUsuarioMenuAccionDTO();
            accion.tokenCatalogoAccion = a.tokenCatalogoAccion;
            accion.activo = a.activo;
            return accion;
          });

        return menuDto;
      });

    return dto;
  }

  mergeMenusByToken(
    base: any[],
    incoming: PermisoRolUsuarioMenuDTO[]
  ): PermisoRolUsuarioMenuDTO[] {

    // Índice de menús entrantes
    const incomingMenuMap = new Map<string, PermisoRolUsuarioMenuDTO>();
    for (const menu of incoming) {
        incomingMenuMap.set(menu.tokenMenu, menu);
    }

    return base.map(baseMenu => {
      const incomingMenu = incomingMenuMap.get(baseMenu.tokenIdentificador);

      if (!incomingMenu) {
        // No existe en incoming → acciones desactivadas
        return {
          ...baseMenu,
          acciones: baseMenu.acciones.map(a => ({
            ...a,
            activo: false
          }))
        };
      }

      // Índice de acciones entrantes
      const incomingActionsMap = new Map(
        incomingMenu.acciones.map(a => [a.tokenCatalogoAccion, a.activo])
      );

      return {
        ...baseMenu,
        acciones: baseMenu.acciones.map(a => ({
            ...a,
            activo: incomingActionsMap.get(a.tokenCatalogoAccion) ?? false
        }))
      };
    });
  }

  compareByTokenIdentificador(o1: any, o2: any) {
    if (o1 == null || o2 == null)
      return false;

    if (o1.tokenIdentificador == o2.tokenIdentificador)
      return true;
    else 
      return false;
  }

  isAccionChecked(indexAccion: number): boolean {
    const menusConAccion = this.menus?.filter(m => m.mostrarAccionesPermisos);

    return menusConAccion?.length > 0 &&
        menusConAccion?.every(menu => menu.acciones[indexAccion]?.activo);
  }

  isAccionIndeterminate(indexAccion: number): boolean {
    const menusConAccion = this.menus?.filter(m => m.mostrarAccionesPermisos);

    const activos = menusConAccion?.filter(
        menu => menu.acciones[indexAccion]?.activo
    )?.length;

    return activos > 0 && activos < menusConAccion?.length;
  }

  toggleAccion(indexAccion: number, value: boolean): void {
    this.menus?.forEach(menu => {
        if (menu.mostrarAccionesPermisos && menu.acciones[indexAccion]) {
            menu.acciones[indexAccion].activo = value;
        }
    });
  }

  selectRol(event: MatAutocompleteSelectedEvent): void {
    const rol = event.option.value as RolDTO;

    if (
      !this.rolesSeleccionados.some(
        r => r.tokenIdentificador === rol.tokenIdentificador
      )
    ) {
      this.rolesSeleccionados.push(rol);
      this.usuarioRolForm
        .get('roles')
        ?.setValue(this.rolesSeleccionados);
    }

    this.usuarioRolForm.get('rol').setValue('');
  }

  removeRol(rol: RolDTO): void {       

    this.rolesSeleccionados = this.rolesSeleccionados.filter(
      r => r.tokenIdentificador !== rol.tokenIdentificador
    );

    this.rolesFiltrados$ = this.usuarioRolForm.get('rol').valueChanges.pipe(
      startWith(''),
      map(value => this.filtrarRoles(value))
    );

    this.usuarioRolForm
      .get('roles')
      ?.setValue(this.rolesSeleccionados);
  }

  displayFuncionario(funcionario: any): string {
    return funcionario
      ? `${funcionario.nombres} ${funcionario.apellidos}`
      : '';
  }

  esFormularioValido() : boolean {
    let validadorFuncionario = this.usuarioRolForm.get('funcionario').hasValidator(Validators.required);
    if (validadorFuncionario && this.usuarioRolForm.valid) return true;
    else if (!validadorFuncionario && this.rolesSeleccionados?.length > 0) return true;
    else return false;
  }

  private filtrarFuncionarios(valor: string): any[] {
    const filtro = valor.toLowerCase();

    return this.funcionarios.filter(funcionario =>
      `${funcionario.nombres} ${funcionario.apellidos}`
        .toLowerCase()
        .includes(filtro)
    );
  }

  private filtrarRoles(value: string | RolDTO): RolDTO[] {
    const filtro =
      typeof value === 'string' ? value.toLowerCase() : '';

    return this.roles.filter(
      rol =>
        rol.nombre.toLowerCase().includes(filtro) &&
        !this.rolesSeleccionados.some(
          r => r.tokenIdentificador === rol.tokenIdentificador
        )
    );
  }

}
