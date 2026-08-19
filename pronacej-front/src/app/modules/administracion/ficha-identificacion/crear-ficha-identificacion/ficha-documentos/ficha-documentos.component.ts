import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-ficha-documentos',
  standalone: true,
  imports: [
    RouterOutlet
  ],
  templateUrl: './ficha-documentos.component.html',
  styleUrl: './ficha-documentos.component.scss'
})
export class FichaDocumentosComponent {

  constructor(private router: Router) { }
}
