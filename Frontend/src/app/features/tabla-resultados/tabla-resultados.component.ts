import { Component, inject, OnInit, signal, effect } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { CompensacionService, PeriodoStateService } from '../../core/services';
import { TablaResultados, FilaResultado } from '../../core/models';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-tabla-resultados',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  template: `
    <div class="tabla-resultados-view">
      <div class="loading" *ngIf="loading()">
        <div class="spinner"></div>
      </div>

      <ng-container *ngIf="!loading() && tabla()">
        <!-- Tabs Total / Promedio -->
        <div class="sub-tabs">
          <button class="sub-tab" [class.active]="tipoTabla() === 'TOTAL'" (click)="cambiarTipo('TOTAL')">
            Total
          </button>
          <button class="sub-tab" [class.active]="tipoTabla() === 'PROMEDIO'" (click)="cambiarTipo('PROMEDIO')">
            Promedio
          </button>
        </div>

        <!-- Botones de exportación -->
        <div class="export-buttons">
          <button class="btn-export" (click)="exportar('CSV')">CSV</button>
          <button class="btn-export" (click)="exportar('EXCEL')">Excel</button>
          <button class="btn-export" (click)="exportar('PDF')">PDF</button>
        </div>

        <!-- Tabla -->
        <table class="data-table">
          <thead>
            <tr>
              <th>Puesto</th>
              <th>Negocio</th>
              <th class="numeric">{{ tabla()!.periodoActual }}</th>
              <th class="numeric">{{ tabla()!.periodoAnterior }}</th>
              <th class="numeric">Variación $</th>
              <th class="numeric">Variación %</th>
            </tr>
          </thead>
          <tbody>
            <ng-container *ngFor="let negocio of getNegocios()">
              <!-- Header del grupo -->
              <tr class="group-header">
                <td colspan="6">{{ negocio }}</td>
              </tr>
              <!-- Filas del grupo -->
              <tr *ngFor="let fila of getFilasPorNegocio(negocio)">
                <td>{{ fila.puesto }}</td>
                <td>{{ fila.negocio }}</td>
                <td class="numeric">\${{ fila.semanaActual | number:'1.0-0' }}</td>
                <td class="numeric">\${{ fila.semanaAnterior | number:'1.0-0' }}</td>
                <td class="numeric" [class.positive]="fila.variacionPositiva" [class.negative]="!fila.variacionPositiva">
                  {{ fila.variacionPositiva ? '' : '-' }}\${{ (fila.variacionMonto < 0 ? -fila.variacionMonto : fila.variacionMonto) | number:'1.0-0' }}
                </td>
                <td class="numeric" [class.positive]="fila.variacionPositiva" [class.negative]="!fila.variacionPositiva">
                  {{ fila.variacionPorcentual | number:'1.1-1' }}%
                </td>
              </tr>
            </ng-container>
          </tbody>
        </table>
      </ng-container>
    </div>
  `,
  styles: [`
    .tabla-resultados-view {
      padding-top: 10px;
    }

    .data-table {
      background: white;
    }
  `]
})
export class TablaResultadosComponent implements OnInit {
  private compensacionService = inject(CompensacionService);
  private periodoState = inject(PeriodoStateService);

  loading = signal(true);
  tipoTabla = signal<'TOTAL' | 'PROMEDIO'>('TOTAL');
  tabla = signal<TablaResultados | null>(null);

  constructor() {
    // Recargar datos cuando cambie el periodo seleccionado
    effect(() => {
      const periodo = this.periodoState.codigoPeriodo();
      if (periodo) {
        this.loadData();
      }
    });
  }

  ngOnInit() {
    // La carga inicial se hace en el effect
  }

  cambiarTipo(tipo: 'TOTAL' | 'PROMEDIO') {
    this.tipoTabla.set(tipo);
    this.loadData();
  }

  loadData() {
    this.loading.set(true);
    const periodo = this.periodoState.codigoPeriodo();
    const obs = this.tipoTabla() === 'TOTAL'
      ? this.compensacionService.obtenerTablaResultadosTotal(periodo)
      : this.compensacionService.obtenerTablaResultadosPromedio(periodo);

    obs.subscribe({
      next: (data) => {
        this.tabla.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  getNegocios(): string[] {
    const t = this.tabla();
    return t ? Object.keys(t.filasPorNegocio).sort() : [];
  }

  getFilasPorNegocio(negocio: string): FilaResultado[] {
    const t = this.tabla();
    return t?.filasPorNegocio[negocio] || [];
  }

  exportar(formato: string) {
    const tipo = this.tipoTabla().toLowerCase();
    const periodo = this.periodoState.codigoPeriodo();
    let url = `${environment.apiUrl}/exportacion/tabla-resultados/${tipo}?formato=${formato}`;
    if (periodo) {
      url += `&periodo=${periodo}`;
    }
    window.open(url, '_blank');
  }
}
