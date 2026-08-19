export class FormDataRequest<T> {

    data: {
        clave: string,
        valor: string | Blob
    }

    body: {
        clave: string,
        valor: T
    };
}