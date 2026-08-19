import { CommonModule } from '@angular/common';
import {
    AfterViewInit,
    Component,
    OnInit,
    ViewChild,
    ViewEncapsulation,
} from '@angular/core';
import {
    FormsModule,
    NgForm,
    ReactiveFormsModule,
    UntypedFormBuilder,
    UntypedFormGroup,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router } from '@angular/router';
import { fuseAnimations } from '@fuse/animations';
import { FuseAlertComponent, FuseAlertType } from '@fuse/components/alert';
import { AuthService } from 'app/core/auth/auth.service';
import { DialogSeleccionarJerarquiaComponent } from 'app/core/components/dialog-seleccionar-jerarquia/dialog-seleccionar-jerarquia.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { LoginRequest } from 'app/core/model/request/loginRequest.model';
import { LoginResponseFuse } from 'app/core/model/response/LoginResponseFuse.model';
import { RecaptchaV3Service } from 'app/modules/seguridad/services/recaptchav3.service';
import { of, switchMap, throwError } from 'rxjs';

@Component({
    selector: 'auth-sign-in',
    templateUrl: './sign-in.component.html',
    encapsulation: ViewEncapsulation.None,
    animations: fuseAnimations,
    standalone: true,
    imports: [
        CommonModule,
        FuseAlertComponent,
        FormsModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatCheckboxModule,
        MatProgressSpinnerModule,
    ],
})
export class AuthSignInComponent implements OnInit, AfterViewInit {
    @ViewChild('signInNgForm') signInNgForm: NgForm;

    alert: { type: FuseAlertType; message: string } = {
        type: 'success',
        message: '',
    };
    signInForm: UntypedFormGroup;
    showAlert: boolean = false;
    passwordVisible: boolean = false;

    /**
     * Constructor
     */
    constructor(
        private _activatedRoute: ActivatedRoute,
        private _authService: AuthService,
        private _formBuilder: UntypedFormBuilder,
        private _router: Router,
        private dialog: MatDialog,
        private recaptchaV3Service: RecaptchaV3Service
    ) { }

    ngAfterViewInit(): void {
        //limpiando el localStorage
        // Remove the access token from the local storage
        localStorage.removeItem(etiquetasModel.LS_TOKEN_DE_ACCESO);
        localStorage.removeItem(etiquetasModel.LS_TOKEN_EMPRESA);
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */
    ngOnInit(): void {
        // Create the form
        this.signInForm = this._formBuilder.group({
            username: [null, [Validators.required]],
            password: [null, Validators.required],
            rememberMe: [''],
        });
    }

    togglePasswordVisibility() {
        this.passwordVisible = !this.passwordVisible;
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Sign in
     */
    signIn(): void {
        if (this.signInForm.invalid) return;

        this.signInForm.disable();
        this.showAlert = false;

        const loginRequest = new LoginRequest();
        loginRequest.password = this.signInForm.get('password')?.value;
        loginRequest.userName = this.signInForm.get('username')?.value;

        // 1️⃣ Ejecutar reCAPTCHA antes del primer login
        this.recaptchaV3Service.execute(etiquetasModel.ACCION_LOGIN_USUARIO).subscribe({
            next: (tokenRecapchaV3: string) => {
                loginRequest.recaptchaV3 = tokenRecapchaV3;

                // 2️⃣ Primer login
                this._authService.signIn(loginRequest, etiquetasModel.NEMONICO_MENU_AUTH_LOGIN_USUARIO).subscribe({
                    next: (response: LoginResponseFuse) => {
                        if (!response.success) {
                            this.mostrarError(response.message);
                            return;
                        }

                        const necesitaSeleccionEmpresa = response.estado === "LOGIN_SELECCION_DE_JERARQUIA";
                        const jerarquias = response.listaJerarquias || [];

                        if (necesitaSeleccionEmpresa && jerarquias.length > 1) {
                            // 3️⃣ Selección de jerarquía
                            this.dialog.open(DialogSeleccionarJerarquiaComponent, {
                                width: '400px',
                                data: jerarquias,
                                disableClose: true
                            }).afterClosed().subscribe((jerarquiaSeleccionada: JerarquiaDTO) => {
                                if (!jerarquiaSeleccionada) {
                                    this.mostrarError('Debe seleccionar una jerarquía.');
                                    return;
                                }

                                // 4️⃣ Nuevo token recaptcha antes del login con jerarquía
                                this.recaptchaV3Service.execute(etiquetasModel.ACCION_LOGIN_USUARIO).subscribe({
                                    next: (nuevoToken: string) => {
                                        const nuevoLoginRequest = {
                                            ...loginRequest,
                                            tokenIdentificadorJerarquia: jerarquiaSeleccionada.tokenIdentificador,
                                            recaptchaV3: nuevoToken
                                        };

                                        // 5️⃣ Login final con jerarquía seleccionada
                                        this._authService.signIn(nuevoLoginRequest, etiquetasModel.NEMONICO_MENU_AUTH_LOGIN_USUARIO).subscribe({
                                            next: (finalResponse: LoginResponseFuse) => {
                                                if (!finalResponse.success) {
                                                    this.mostrarError('Falló el login con la jerarquía seleccionada');
                                                    return;
                                                }
                                                this.finalizarYRedirigir(finalResponse);
                                            },
                                            error: (err: any) => this.mostrarError(err)
                                        });
                                    },
                                    error: (err: any) => this.mostrarError(err)
                                });
                            });
                        } else {
                            // 6️⃣ Login sin selección de jerarquía
                            this.finalizarYRedirigir(response);
                        }
                    },
                    error: (err: any) => this.mostrarError(err)
                });
            },
            error: (err: any) => this.mostrarError(err)
        });
    }

    // Función auxiliar para mostrar error y reactivar el formulario
    private mostrarError(message: any) {
        const mensaje = message?.message || message || 'Ocurrió un error durante el inicio de sesión.';
        this.alert = { type: 'error', message: mensaje };
        this.showAlert = true;
        this.signInForm.enable();
    }

    // Función auxiliar para finalizar login y redirigir
    private finalizarYRedirigir(response: LoginResponseFuse) {
        this._authService.finalizarLogin(response);
        const redirectURL = this._activatedRoute.snapshot.queryParamMap.get('redirectURL') || '/home';
        this._router.navigateByUrl(redirectURL);
    }
}
