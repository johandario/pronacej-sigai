import { AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import Viewer from "viewerjs";

@Component({
  selector: 'app-visualizar-imagen-comp',
  standalone: true,
  imports: [
    MatIconModule,
    MatDialogModule
  ],
  templateUrl: './visualizar-imagen-comp.component.html',
  styleUrl: './visualizar-imagen-comp.component.scss'
})
export class VisualizarImagenCompComponent implements OnInit, AfterViewInit {

  @Input({ required: true }) declare titulo: string;
  @Input({ required: true }) declare base64Img: string;

  @ViewChild("imgHtml") imgHmtl: ElementRef;

  imageIndex: number;

  constructor(public domSanitizer: DomSanitizer) { }

  ngAfterViewInit(): void {
    let naviteHtml = this.imgHmtl.nativeElement as HTMLElement;
    naviteHtml.id = "no_show"
    var oImg = document.createElement("img");
    oImg.className = "h-auto max-w-full";
    oImg.alt = "image description";
    oImg.src = this.base64Img;
    naviteHtml.appendChild(
      oImg
    );
    new Viewer(
      naviteHtml,
      {
        inline: true,
        tooltip: true,
        toolbar: {
          prev: false,
          next: false,


          flipHorizontal: true,
          flipVertical: true,
          oneToOne: true,
          reset: true,

          play: true,
          rotateLeft: true,
          rotateRight: true,
          zoomIn: true,
          zoomOut: true,
        }
      }
    );

  }

  ngOnInit(): void {
    console.log(this.imgHmtl);
  }

  getBase64Sanitaizer() {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(this.base64Img);
  }
}
