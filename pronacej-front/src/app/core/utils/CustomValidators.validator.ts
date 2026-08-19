import { AbstractControl, ValidationErrors } from "@angular/forms";
import { ValidatorFn } from "@iplab/ngx-file-upload";

export function autocompleteObjectValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {

        const value = control.value;

        if (
            value &&
            typeof value === 'object' &&
            'tokenIdentificador' in value
        ) {
            return null;
        }

        return { invalidAutocompleteObject: true };
    };
}

export function noWhitespaceValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {

        const value = control.value;

        if (value == null || value === '') {
            return null; // dejar que Validators.required maneje esto
        }

        if (typeof value === 'string' && value.trim().length === 0) {
            return { whitespace: true };
        }

        return null;
    };
}