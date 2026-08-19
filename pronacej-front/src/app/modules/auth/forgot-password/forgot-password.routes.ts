import { Routes } from '@angular/router';
import { AuthForgotPasswordComponent } from 'app/modules/auth/forgot-password/forgot-password.component';
import { OlvidoContraseniaResetearComponent } from './olvido-contrasenia-resetear/olvido-contrasenia-resetear.component';
import { OlvidoContraseniaCancelarComponent } from './olvido-contrasenia-cancelar/olvido-contrasenia-cancelar.component';

export default [
    {
        path: '',
        component: AuthForgotPasswordComponent,
    },
    {
        path: "resetear",
        component: OlvidoContraseniaResetearComponent
    },
    {
        path: "cancelar",
        component: OlvidoContraseniaCancelarComponent
    },
] as Routes;
