import { Buffer } from "buffer";
export class Aes {
    private async deriveKey(
        password: string,
        salt: Uint8Array
    ): Promise<CryptoKey> {
        const enc = new TextEncoder();
        const keyMaterial = await crypto.subtle.importKey(
            'raw',
            enc.encode(password),
            'PBKDF2',
            false,
            ['deriveKey']
        );

        return crypto.subtle.deriveKey(
            {
                name: 'PBKDF2',
                salt,
                iterations: 100000,
                hash: 'SHA-256',
            },
            keyMaterial,
            { name: 'AES-GCM', length: 256 },
            false,
            ['encrypt', 'decrypt']
        );
    }

    async encryptData(passwordBase64: string, data: string): Promise<string> {
        const iv = crypto.getRandomValues(new Uint8Array(12));
        const salt = crypto.getRandomValues(new Uint8Array(16));
        const password = Buffer.from(passwordBase64, "base64").toString("utf-8");
        const key = await this.deriveKey(password, salt);
        const encrypted = await crypto.subtle.encrypt(
            { name: 'AES-GCM', iv },
            key,
            new TextEncoder().encode(data)
        );

        // Combine salt + iv + ciphertext
        const combined = new Uint8Array(
            salt.length + iv.length + encrypted.byteLength
        );
        combined.set(salt, 0);
        combined.set(iv, salt.length);
        combined.set(new Uint8Array(encrypted), salt.length + iv.length);

        return btoa(String.fromCharCode(...combined));
    }

    async decryptData(passwordBase64: string, base64: string): Promise<string> {
        const data = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
        const salt = data.slice(0, 16);
        const iv = data.slice(16, 28);
        const ciphertext = data.slice(28);
        const password = Buffer.from(passwordBase64, "base64").toString("utf-8");

        const key = await this.deriveKey(password, salt);
        const decrypted = await crypto.subtle.decrypt(
            { name: 'AES-GCM', iv },
            key,
            ciphertext
        );

        return new TextDecoder().decode(decrypted);
    }
}
