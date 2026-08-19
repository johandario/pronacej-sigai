import { CommonModule } from '@angular/common';
import { AfterViewInit, ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, Router, RouterOutlet } from '@angular/router';
import { FuseFullscreenComponent } from '@fuse/components/fullscreen';
import { FuseLoadingBarComponent } from '@fuse/components/loading-bar';
import {
    FuseNavigationService,
    FuseVerticalNavigationComponent,
} from '@fuse/components/navigation';
import { FuseMediaWatcherService } from '@fuse/services/media-watcher';
import { AuthService } from 'app/core/auth/auth.service';
import { DialogSeleccionarJerarquiaComponent } from 'app/core/components/dialog-seleccionar-jerarquia/dialog-seleccionar-jerarquia.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { NavigationService } from 'app/core/navigation/navigation.service';
import { Navigation } from 'app/core/navigation/navigation.types';
import { LanguagesComponent } from 'app/layout/common/languages/languages.component';
import { MessagesComponent } from 'app/layout/common/messages/messages.component';
import { NotificationsComponent } from 'app/layout/common/notifications/notifications.component';
import { QuickChatComponent } from 'app/layout/common/quick-chat/quick-chat.component';
import { SearchComponent } from 'app/layout/common/search/search.component';
import { ShortcutsComponent } from 'app/layout/common/shortcuts/shortcuts.component';
import { UserComponent } from 'app/layout/common/user/user.component';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { Subject, switchMap, takeUntil, tap, throwError } from 'rxjs';

@Component({
    selector: 'dense-layout',
    templateUrl: './dense.component.html',
    encapsulation: ViewEncapsulation.None,
    standalone: true,
    imports: [
        FuseLoadingBarComponent,
        FuseVerticalNavigationComponent,
        MatButtonModule,
        MatIconModule,
        MatChipsModule,
        LanguagesComponent,
        FuseFullscreenComponent,
        SearchComponent,
        ShortcutsComponent,
        MessagesComponent,
        NotificationsComponent,
        UserComponent,
        RouterOutlet,
        QuickChatComponent,
        CommonModule,
        DialogSeleccionarJerarquiaComponent
    ],
})
export class DenseLayoutComponent implements OnInit, OnDestroy, AfterViewInit {
    nemonicoMenu = etiquetasModel.NEMONICO_MENU_INICIO;

    funcionarioActivo: FuncionarioDTO;

    isScreenSmall: boolean;
    navigation: Navigation;
    navigationAppearance: 'default' | 'dense' = 'dense';
    navOpened: boolean = false;
    private _unsubscribeAll: Subject<any> = new Subject<any>();
    @ViewChild('nav', { static: false }) navComponent: FuseVerticalNavigationComponent;

    jerarquiasFuncionario: JerarquiaDTO[];


    /**
     * Constructor
     */
    constructor(
        private _activatedRoute: ActivatedRoute,
        private _router: Router,
        private _navigationService: NavigationService,
        private _fuseMediaWatcherService: FuseMediaWatcherService,
        private _fuseNavigationService: FuseNavigationService,
        private cdr: ChangeDetectorRef,
        private funcionarioService: FuncionarioService,
        private dialog: MatDialog,
        private seguridadService: AuthSerguridadServicio,
        private authService: AuthService
    ) { }

    ngAfterViewInit() {
        this.cdr.detectChanges();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for current year
     */
    get currentYear(): number {
        return new Date().getFullYear();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */
    ngOnInit(): void {
        this.obtenerFuncionario();

        // Subscribe to navigation data
        this._navigationService.navigation$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((navigation: Navigation) => {
                this.navigation = navigation;
            });

        // Subscribe to media changes
        this._fuseMediaWatcherService.onMediaChange$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe(({ matchingAliases }) => {
                // Check if the screen is small
                this.isScreenSmall = !matchingAliases.includes('md');

                // Change the navigation appearance
                this.navigationAppearance = this.isScreenSmall
                    ? 'default'
                    : 'dense';
            });
    }

    /**
     * On destroy
     */
    ngOnDestroy(): void {
        // Unsubscribe from all subscriptions
        this._unsubscribeAll.next(null);
        this._unsubscribeAll.complete();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Toggle navigation
     *
     * @param name
     */
    toggleNavigation(name: string): void {
        // Get the navigation
        const navigation =
            this._fuseNavigationService.getComponent<FuseVerticalNavigationComponent>(
                name
            );

        if (navigation) {
            // Toggle the opened status
            navigation.toggle();
        }
    }

    /**
     * Toggle the navigation appearance
     */
    toggleNavigationAppearance(): void {
        this.navigationAppearance =
            this.navigationAppearance === 'default' ? 'dense' : 'default';

        if (this.navigationAppearance === 'default')
            this.navOpened = true;
        else
            this.navOpened = false;
    }

    obtenerFuncionario() {
        this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).subscribe(
            {
                next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {

                    if (!response.exito) {
                        return;
                    }

                    this.funcionarioActivo = response.data;
                },
                error: (error: any) => {
                    console.log('Hubo un problema al recuperar los registros. Inténtalo de nuevo.');
                }
            }
        );
    }

    cambiarJerarquia() {
        this.funcionarioService.obtenerJerarquiasFuncionarioDelUsuario(this.nemonicoMenu).subscribe(
            {
                next: (response: RespuestaPorDefecto<JerarquiaDTO[]>) => {

                    if (!response.exito) {
                        return;
                    }

                    this.jerarquiasFuncionario = response.data;
                    if (this.jerarquiasFuncionario && this.jerarquiasFuncionario.length > 1) {
                        this.dialog
                            .open(DialogSeleccionarJerarquiaComponent, {
                                width: '400px',
                                data: this.jerarquiasFuncionario,
                                disableClose: true,
                            })
                            .afterClosed()
                            .pipe(
                                switchMap((jerarquiaSeleccionada: JerarquiaDTO) => {
                                    if (!jerarquiaSeleccionada) {
                                        return throwError(
                                            () => new Error('Debe seleccionar una jerarquía.')
                                        );
                                    }
                                    console.log('jerarquiaSeleccionada', jerarquiaSeleccionada);
                                    const loginRequest = {
                                        tokenIdentificadorJerarquia:
                                            jerarquiaSeleccionada.tokenIdentificador,
                                    };
                                    const nuevoLoginRequest = {
                                        ...loginRequest,
                                        tokenIdentificadorJerarquia:
                                            jerarquiaSeleccionada.tokenIdentificador,
                                    };
                                    return this.seguridadService.cambioJerarquia(
                                        nuevoLoginRequest,
                                        etiquetasModel.NEMONICO_MENU_AUTH_LOGIN_USUARIO
                                    );
                                }),
                                tap((loginResponseFinal) => {
                                    if (!loginResponseFinal.success) {
                                        throw new Error(
                                            'Falló el login con la empresa seleccionada'
                                        );
                                    }
                                    // Guardar token, usuario, etc.
                                    this.authService.finalizarCambioJerarquia(loginResponseFinal);
                                    this._router.navigate(['/home']).then(() => {
                                        window.location.reload();
                                    });
                                })
                            )
                            .subscribe({
                                next: () => {
                                    /* Si quieres hacer algo al finalizar */
                                },
                                error: (err) => {
                                    // Manejo de errores aquí
                                    console.error(err);
                                },
                            });

                        // ¡Ahora sí se ejecutará el flujo!
                        return;
                    }
                },
                error: (error: any) => {
                    console.log('Hubo un problema al recuperar los registros. Inténtalo de nuevo.');
                }
            }
        );
    }

    validarJerarquia() {
        if (this.funcionarioActivo.numeroCentros > 1) {
            this.cambiarJerarquia();
        }
        return;
    }
}
