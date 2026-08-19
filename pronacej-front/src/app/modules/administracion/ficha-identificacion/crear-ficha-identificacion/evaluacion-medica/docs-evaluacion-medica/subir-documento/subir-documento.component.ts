import { DragDropModule } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
  selector: 'app-subir-documento',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    
    CommonModule,


    MatProgressBarModule,
    DragDropModule,


  ],
  templateUrl: './subir-documento.component.html',
  styleUrl: './subir-documento.component.scss'
})
export class SubirDocumentoComponent {





  file: File | null = null;
  progress = 0;
  uploadInProgress = false;
  errorMessage = '';
  fileSize = 0; // Tamaño en KB
  maxSize = 5000; // 5MB en KB
  allowedTypes = ['application/pdf', 'application/msword', 'image/jpeg', 'image/png', 'application/zip', 'text/txt'];


  onFileSelected(event: any) {
    const selectedFile = event.target.files[0];
    this.validateAndUpload(selectedFile);
  }

  onDrop(event: any) {
    const droppedFile = event.item.getData('text/plain');
    this.validateAndUpload(droppedFile);
  }

  onDragOver(event: any) {
    event.preventDefault();
  }

  onDragLeave(event: any) {
    event.preventDefault();
  }

  validateAndUpload(file: File) {
    if (file) {
      this.file = file;
      this.fileSize = file.size / 1024; // Convertir a KB

      if (!this.allowedTypes.includes(file.type)) {
        this.errorMessage = 'Tipo de archivo no válido. Tipos permitidos: PDF, DOC, DOCX, JPG, PNG, ZIP';
        this.resetFile();
        return;
      }

      if (this.fileSize > this.maxSize) {
        this.errorMessage = `Archivo muy grande. Tamaño máximo ${this.maxSize / 1024} MB`;
        this.resetFile();
        return;
      }

      this.errorMessage = '';
      this.uploadFile(file);
    }
  }

  uploadFile(file: File) {
    // this.uploadInProgress = true;
    // const formData = new FormData();
    // formData.append('file', file, file.name);

    // this.http.post('YOUR_UPLOAD_ENDPOINT', formData, {
    //   reportProgress: true,
    //   observe: 'events'
    // }).subscribe(event => {
    //   if (event.type === HttpEventType.UploadProgress) {
    //     this.progress = Math.round((100 * event.loaded) / event.total!);
    //   } else if (event.type === HttpEventType.Response) {
    //     this.uploadInProgress = false;
    //     console.log('Upload complete!', event.body);
    //   }
    // }, error => {
    //   this.uploadInProgress = false;
    //   this.errorMessage = 'Upload failed. Please try again.';
    // });
  }

    resetFile() {
      this.file = null;
      this.fileSize = 0;
      this.progress = 0;
      this.uploadInProgress = false;
    }
}
