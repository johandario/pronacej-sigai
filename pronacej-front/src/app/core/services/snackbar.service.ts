import { Injectable } from '@angular/core';
import { CustomSnackbarComponent } from '../components/custom-snackbar/custom-snackbar.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable(
    {
        providedIn: "root"
    }
)
export class SnackbarService {
    constructor(private readonly snackBar: MatSnackBar) {}
    
    show(message: string, action: string, type: 'success' | 'error') {
        this.snackBar.openFromComponent(CustomSnackbarComponent, {
          data: { message, action, type },
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
        });
    }
  }


    