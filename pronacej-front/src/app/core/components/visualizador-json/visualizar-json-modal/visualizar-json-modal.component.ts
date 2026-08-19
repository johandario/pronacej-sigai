import {
    ChangeDetectionStrategy,
    Component,
    inject,
    OnInit,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { NgxJsonViewerModule } from 'ngx-json-viewer';

export class DataJsonViewer {
    declare titulo: string;
    declare jsonString: string;
}

@Component({
    selector: 'app-visualizar-json-modal',
    standalone: true,
    imports: [
        MatDialogModule,
        MatButtonModule,
        NgxJsonViewerModule,
        MatIconModule,
    ],
    templateUrl: './visualizar-json-modal.component.html',
    styleUrl: './visualizar-json-modal.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisualizarJsonModalComponent implements OnInit {
    declare json: any;

    readonly dialogRef = inject(MatDialogRef<VisualizarJsonModalComponent>);
    readonly data = inject<DataJsonViewer>(MAT_DIALOG_DATA);

    ngOnInit(): void {
        this.json = this.getJSON(this.data.jsonString);
    }

    getJSON(json: string) {
        return JSON.parse(json);
    }
}
