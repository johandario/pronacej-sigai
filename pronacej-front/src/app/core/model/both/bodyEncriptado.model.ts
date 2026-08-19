import { Aes } from 'app/core/utils/Aes';
import { RSA } from 'app/core/utils/Rsa';

export class BodyEncriptado {
    declare body: string;
    declare llave: string;
    declare chunkedBody: string[];
    logOut = false;

    async desencriptarData<T>(password: string): Promise<T> {
        const aes = new Aes();
        let body = await aes.decryptData(password, this.body);
        return JSON.parse(body) as T;
    }

    async encriptarData(password: string) {
        const aes = new Aes();
        this.body = await aes.encryptData(password, this.body);
    }


    async encriptarDataRSA(clavePEMPublica: string) {
        const rsa = new RSA();
        var resp = await rsa.rsaChunkedEncrypt(clavePEMPublica, this.body);

        this.chunkedBody = resp;
        this.body = '';
    }
}
