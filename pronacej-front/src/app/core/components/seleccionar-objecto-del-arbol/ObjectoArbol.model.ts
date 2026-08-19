export class ObjectoArbol<T> {
    declare id: string;
    declare nombre: string;
    declare hijos: ObjectoArbol<T>[];
    declare data: T;
    declare icono?:string;
}