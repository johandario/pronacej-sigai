import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
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
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { fuseAnimations } from '@fuse/animations';
import { FuseAlertComponent, FuseAlertType } from '@fuse/components/alert';
import { AuthService } from 'app/core/auth/auth.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { ReseteoDePasswordDTO } from 'app/core/model/both/seguridad/ReseteoDePasswordDTO.model';
import { ReseteoDeContraseniaRequest } from 'app/core/model/request/ReseteoDeContraseniaRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { RecaptchaV3Service } from 'app/modules/seguridad/services/recaptchav3.service';
import { ReseteoDeContraseniaService } from 'app/modules/seguridad/services/reseteoContrasenia.service';
import { environment } from 'environments/environment';
import { finalize } from 'rxjs';

@Component({
    selector: 'auth-forgot-password',
    templateUrl: './forgot-password.component.html',
    encapsulation: ViewEncapsulation.None,
    animations: fuseAnimations,
    standalone: true,
    imports: [
        FuseAlertComponent,
        FormsModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatCardModule,
        MatInputModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        RouterLink,
    ],
})
export class AuthForgotPasswordComponent implements OnInit {
    @ViewChild('forgotPasswordNgForm') forgotPasswordNgForm: NgForm;

    alert: { type: FuseAlertType; message: string } = {
        type: 'success',
        message: '',
    };
    forgotPasswordForm: UntypedFormGroup;
    showAlert: boolean = false;

    nemonicoMenu = etiquetasModel.NEMONICO_MENU_REESTABLECER_CONTRASENIA_EMPEZAR;

    /**
     * Constructor
     */
    constructor(
        private _authService: AuthService,
        private _formBuilder: UntypedFormBuilder,
        private reseteoDeContraseniaService: ReseteoDeContraseniaService,
        private recaptchaV3Service: RecaptchaV3Service
    ) { }

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */
    ngOnInit(): void {
        // Create the form
        this.forgotPasswordForm = this._formBuilder.group({
            email: ['', [Validators.required, Validators.email]],
        });
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Send the reset link
     */
    sendResetLink(): void {
        // Return if the form is invalid
        if (this.forgotPasswordForm.invalid) {
            return;
        }

        // Disable the form
        this.forgotPasswordForm.disable();

        // Hide the alert
        this.showAlert = false;

        // Forgot password
        this.recaptchaV3Service.execute(
            etiquetasModel.ACCION_CREACION_RESETEO_PASSWORD
        ).subscribe(
            {
                next: (tokenRecapthcaV3: string) => {
                    let reseteoDeContraseniaRequest = new ReseteoDeContraseniaRequest();
                    reseteoDeContraseniaRequest.email = this.forgotPasswordForm.get("email").value;
                    reseteoDeContraseniaRequest.recaptchaV3 = tokenRecapthcaV3;

                    this.reseteoDeContraseniaService.empezar(
                        reseteoDeContraseniaRequest,
                        this.nemonicoMenu
                    ).subscribe(
                        {
                            next: (response: RespuestaPorDefecto<ReseteoDePasswordDTO>) => {
                                this.forgotPasswordForm.enable();

                                if (!environment.production) {
                                    console.log(response);
                                }

                                if (!response.exito) {
                                    this.reseteoDeContraseniaService.checkError(response);
                                    return;
                                }

                                this.showAlert = true;
                                this.alert = {
                                    type: "success",
                                    message: response.mensaje
                                }

                                this.forgotPasswordForm.reset();
                            },
                            error: (error: any) => {
                                this.forgotPasswordForm.enable();

                                this.reseteoDeContraseniaService.checkError(error);
                            }
                        }
                    );
                },
                error: (error: any) => {
                    this.showAlert = true;
                    this.alert = {
                        type: "error",
                        message: JSON.stringify(error)
                    }
                    this.forgotPasswordForm.enable();
                }
            }
        );

    }
}
