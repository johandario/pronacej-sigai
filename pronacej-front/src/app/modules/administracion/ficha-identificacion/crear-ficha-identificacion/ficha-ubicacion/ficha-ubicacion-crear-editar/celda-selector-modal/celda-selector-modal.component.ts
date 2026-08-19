import { NestedTreeControl } from '@angular/cdk/tree';
import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import { MatTooltipModule } from '@angular/material/tooltip';
import { UbicacionJerarquiaDTO } from 'app/core/model/both/ubicacionJerarquiaDTO.model';
import { UBICACION_TIPO_CELDA } from 'app/modules/ubicacion/ubicacion.types';

interface CeldaTreeNode {
  tokenIdentificador: string;
  nombre: string;
  rutaCompleta: string;
  esCelda: boolean;
  hijos: CeldaTreeNode[];
  ubicacionJerarquia: UbicacionJerarquiaDTO;
}

interface CeldaSelectorDialogData {
  ubicaciones: UbicacionJerarquiaDTO[];
  tokenSeleccionado?: string | null;
}

@Component({
  selector: 'app-celda-selector-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTreeModule,
    MatTooltipModule
  ],
  templateUrl: './celda-selector-modal.component.html',
  styleUrl: './celda-selector-modal.component.scss'
})
export class CeldaSelectorModalComponent implements OnInit {

  readonly treeControl = new NestedTreeControl<CeldaTreeNode>(node => node.hijos);
  readonly dataSource = new MatTreeNestedDataSource<CeldaTreeNode>();

  terminoBusqueda = '';
  private arbolCompleto: CeldaTreeNode[] = [];

  constructor(
    private readonly dialogRef: MatDialogRef<CeldaSelectorModalComponent, UbicacionJerarquiaDTO | null>,
    @Inject(MAT_DIALOG_DATA) public readonly data: CeldaSelectorDialogData
  ) {}

  ngOnInit(): void {
    this.arbolCompleto = this.construirArbol(this.data.ubicaciones ?? []);
    this.dataSource.data = this.arbolCompleto;
  }

  childrenAccessor = (node: CeldaTreeNode): CeldaTreeNode[] => node.hijos ?? [];

  hasChild = (_: number, node: CeldaTreeNode): boolean => !!node.hijos?.length;

  buscar(valor: string): void {
    this.terminoBusqueda = valor;
    const terminoNormalizado = this.normalizar(valor);

    if (!terminoNormalizado) {
      this.dataSource.data = this.arbolCompleto;
      this.treeControl.collapseAll();
      this.expandirSeleccionActual();
      return;
    }

    this.dataSource.data = this.filtrarArbol(this.arbolCompleto, terminoNormalizado);
    this.treeControl.expandAll();
  }

  expandirTodo(): void {
    this.treeControl.expandAll();
  }

  colapsarTodo(): void {
    this.treeControl.collapseAll();
  }

  seleccionar(node: CeldaTreeNode): void {
    if (!node.esCelda) {
      return;
    }

    this.dialogRef.close(node.ubicacionJerarquia);
  }

  cancelar(): void {
    this.dialogRef.close(null);
  }

  esSeleccionable(node: CeldaTreeNode): boolean {
    return node.esCelda;
  }

  private obtenerTokenPadre(ubicacion: UbicacionJerarquiaDTO): string | undefined {
    return ubicacion.ubicacionJerarquiaPadre?.tokenIdentificador
      ?? (ubicacion as any).tokenIdentificadorPadre
      ?? undefined;
  }

  private construirArbol(ubicaciones: UbicacionJerarquiaDTO[]): CeldaTreeNode[] {
    const nodosPorToken = new Map<string, CeldaTreeNode>();

    ubicaciones.forEach(ubicacion => {
      if (!ubicacion?.tokenIdentificador) {
        return;
      }

      nodosPorToken.set(ubicacion.tokenIdentificador, {
        tokenIdentificador: ubicacion.tokenIdentificador,
        nombre: ubicacion.nombre,
        rutaCompleta: '',
        esCelda: ubicacion.jerarquiaTipo?.nemonico === UBICACION_TIPO_CELDA,
        hijos: [],
        ubicacionJerarquia: ubicacion
      });
    });

    const raices: CeldaTreeNode[] = [];

    nodosPorToken.forEach((node) => {
      const padreToken = this.obtenerTokenPadre(node.ubicacionJerarquia);
      const padre = padreToken && padreToken !== node.tokenIdentificador
        ? nodosPorToken.get(padreToken)
        : undefined;

      if (padre) {
        padre.hijos.push(node);
      } else {
        raices.push(node);
      }
    });

    this.ordenarNodos(raices);
    this.completarRutas(raices);

    return raices;
  }

  private ordenarNodos(nodes: CeldaTreeNode[]): void {
    nodes.sort((a, b) => a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' }));

    nodes.forEach(node => this.ordenarNodos(node.hijos));
  }

  private completarRutas(nodes: CeldaTreeNode[], rutaPadre: string[] = []): void {
    nodes.forEach(node => {
      const rutaActual = [...rutaPadre, node.nombre];
      node.rutaCompleta = rutaActual.join(' / ');
      this.completarRutas(node.hijos, rutaActual);
    });
  }

  private filtrarArbol(nodes: CeldaTreeNode[], terminoNormalizado: string): CeldaTreeNode[] {
    const resultado: CeldaTreeNode[] = [];

    nodes.forEach(node => {
      const hijosFiltrados = this.filtrarArbol(node.hijos, terminoNormalizado);
      const coincideRuta = this.normalizar(node.rutaCompleta).includes(terminoNormalizado);

      if (coincideRuta || hijosFiltrados.length > 0) {
        resultado.push({
          ...node,
          hijos: hijosFiltrados
        });
      }
    });

    return resultado;
  }

  private expandirSeleccionActual(): void {
    const tokenSeleccionado = this.data.tokenSeleccionado;

    if (!tokenSeleccionado) {
      return;
    }

    this.expandirHastaToken(this.dataSource.data, tokenSeleccionado);
  }

  private expandirHastaToken(nodes: CeldaTreeNode[], token: string, ancestros: CeldaTreeNode[] = []): boolean {
    for (const node of nodes) {
      const nuevosAncestros = [...ancestros, node];

      if (node.tokenIdentificador === token) {
        ancestros.forEach(ancestor => this.treeControl.expand(ancestor));
        return true;
      }

      if (this.expandirHastaToken(node.hijos, token, nuevosAncestros)) {
        return true;
      }
    }

    return false;
  }

  private normalizar(valor: string): string {
    return (valor || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }
}
