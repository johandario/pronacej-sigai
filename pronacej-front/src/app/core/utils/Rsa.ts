export class RSA {

    private pemToArrayBuffer(pem: string): ArrayBuffer {
        const b64 = pem.replace(/-----[^-]+-----/g, "").replace(/\s+/g, "");
        const binary = atob(b64);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
        return bytes.buffer;
    }

    private arrayBufferToBase64(ab: ArrayBuffer): string {
        const bytes = new Uint8Array(ab);
        let s = "";
        for (let i = 0; i < bytes.length; i++) s += String.fromCharCode(bytes[i]);
        return btoa(s);
    }

    // Importa clave pública SPKI
    private async importRsaPublicKey(spkiPem: string): Promise<CryptoKey> {
        const keyData = this.pemToArrayBuffer(spkiPem);
        return crypto.subtle.importKey(
            "spki",
            keyData,
            { name: "RSA-OAEP", hash: "SHA-256" },
            true,
            ["encrypt"]
        );
    }

    // Calcula el tamaño máximo de bloque para OAEP: k - 2*hLen - 2
    // hLen para SHA-256 = 32; k = modulusLength/8 en bytes
    private maxOaepBlockSize(cryptoKey: CryptoKey): number {
        const alg = cryptoKey.algorithm as RsaHashedKeyGenParams & { modulusLength: number };
        const k = Math.floor(alg.modulusLength / 8);
        const hLen = 32; // SHA-256
        return k - 2 * hLen - 2;
    }

    private chunkUint8(src: Uint8Array, size: number): Uint8Array[] {
        const out: Uint8Array[] = [];
        for (let i = 0; i < src.length; i += size) {
            out.push(src.subarray(i, i + size));
        }
        return out;
    }

    /**
 * Cifra una cadena larga con RSA-OAEP (SHA-256) por bloques.
 * @param publicPem Clave pública SPKI en PEM
 * @param plaintext Texto a cifrar (por ejemplo, tu Base64 resultante de AES)
 * @returns Payload listo para enviar al backend
 */
    async rsaChunkedEncrypt(publicPem: string, plaintext: string) {
        const publicKey = await this.importRsaPublicKey(publicPem);

        const enc = new TextEncoder().encode(plaintext);

        const maxChunk = this.maxOaepBlockSize(publicKey);

        let chunks: string[] = [];
        for (let i = 0; i < enc.length; i += maxChunk) {
            const chunk = enc.slice(i, i + maxChunk); // ← Uint8Array válido
            const encrypted = await crypto.subtle.encrypt(
                { name: "RSA-OAEP" },
                publicKey,
                chunk
            );
            chunks.push(this.arrayBufferToBase64(encrypted));
        }

        return chunks;
    }


    async rsaEncryptBase64(publicPem: string, plaintext: string): Promise<string> {
        const publicKey = await this.importRsaPublicKey(publicPem);
        const enc = new TextEncoder().encode(plaintext);
        console.log(enc.byteLength);
        console.log(enc.length);
        const ciphertext = await crypto.subtle.encrypt({ name: "RSA-OAEP" }, publicKey, enc);
        return this.arrayBufferToBase64(ciphertext);
    }
}
