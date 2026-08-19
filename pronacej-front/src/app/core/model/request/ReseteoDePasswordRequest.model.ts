import { CampoDTO } from "../both/campoDTO.model";

export class ReseteoDePasswordRequest extends CampoDTO {

    declare recaptchaV3: string;
    declare password: string;
    declare passwordConfirm: string;
}