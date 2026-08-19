import { Component, Inject} from '@angular/core';
import { MAT_SNACK_BAR_DATA } from '@angular/material/snack-bar';
import { CustomSnackBarAnimations } from './custom-snackbar.animations';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
  selector: 'app-custom-snackbar',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatProgressBarModule,
  ],
  templateUrl: './custom-snackbar.component.html',
  styleUrls: ['./custom-snackbar.component.scss'],
  animations: [CustomSnackBarAnimations]
})
export class CustomSnackbarComponent{

  progressValue: number = 100;
  intervalId: any;

  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: any) { }


  ngOnInit() {
    const duration = 3000;
    const intervalTime = 50;

    const decrement = (intervalTime / duration) * 100;

    this.intervalId = setInterval(() => {
      this.progressValue -= decrement;

      if (this.progressValue <= 0) {
        clearInterval(this.intervalId);
      }
    }, intervalTime);
  }

  ngOnDestroy() {
    clearInterval(this.intervalId);
  }

  /*
   * Determina si el snackbar es de exito o error
   */
  get isSuccess() {
    return this.data.type === 'success';
  }
  
}
