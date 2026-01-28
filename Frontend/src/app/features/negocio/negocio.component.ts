import { Component, inject, OnInit, signal, effect } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { CompensacionService, PeriodoStateService } from '../../core/services';
import { SerieTemporal, ResumenIndicador } from '../../core/models';

@Component({
  selector: 'app-negocio',
  standalone: true,
  imports: [CommonModule, BaseChartDirective, DecimalPipe],
  template: `
    <div class="negocio-view">
      <div class="loading" *ngIf="loading()">
        <div class="spinner"></div>
      </div>

      <ng-container *ngIf="!loading()">
        <!-- Tabs de Puestos -->
        <div class="sub-tabs" *ngIf="puestos().length > 0">
          <button *ngFor="let puesto of puestos()"
                  class="sub-tab"
                  [class.active]="selectedPuesto() === puesto"
                  (click)="selectPuesto(puesto)">
            {{ puesto }}
          </button>
        </div>

        <!-- Tabla de todos los indicadores -->
        <div class="summary-table" *ngIf="resumenIndicadores().length > 0">
          <table>
            <thead>
              <tr>
                <th>Indicador</th>
                <th>{{ periodoState.periodoSeleccionado() }}</th>
                <th>{{ periodoState.periodoAnterior() }}</th>
                <th>Diferencia $ / #</th>
                <th>Variación %</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of resumenIndicadores()"
                  [class.selected]="selectedIndicador() === item.indicador"
                  (click)="selectIndicador(item.indicador)"
                  style="cursor: pointer;">
                <td>{{ item.indicador }}</td>
                <td>\${{ item.valorActual | number:'1.0-0' }}</td>
                <td>\${{ item.valorAnterior | number:'1.0-0' }}</td>
                <td [class.positive]="item.variacionPositiva" [class.negative]="!item.variacionPositiva">
                  {{ item.variacionPositiva ? '' : '-' }}\${{ (item.diferencia < 0 ? -item.diferencia : item.diferencia) | number:'1.0-0' }}
                </td>
                <td [class.positive]="item.variacionPositiva" [class.negative]="!item.variacionPositiva">
                  {{ item.variacionPorcentual | number:'1.1-1' }}%
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Tabs de indicadores para selección rápida -->
        <div class="indicador-tabs" *ngIf="indicadores().length > 0">
          <button *ngFor="let indicador of indicadores()"
                  class="indicador-tab"
                  [class.active]="selectedIndicador() === indicador"
                  (click)="selectIndicador(indicador)">
            {{ indicador }}
          </button>
        </div>

        <!-- Gráfico -->
        <div class="chart-section" *ngIf="serie()">
          <h3 class="chart-title">{{ selectedIndicador() }} - {{ selectedPuesto() }}</h3>
          <div class="chart-container">
            <canvas baseChart
              [data]="chartData"
              [options]="chartOptions"
              [type]="'line'">
            </canvas>
          </div>
        </div>
      </ng-container>
    </div>
  `,
  styles: [`
    .negocio-view { padding-top: 10px; }
    .chart-section { margin-top: 20px; }
    .summary-table tr.selected {
      background-color: #e3f2fd;
    }
    .summary-table tr:hover {
      background-color: #f5f5f5;
    }
  `]
})
export class NegocioComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private compensacionService = inject(CompensacionService);
  periodoState = inject(PeriodoStateService);

  negocio = signal('');
  loading = signal(true);
  puestos = signal<string[]>([]);
  selectedPuesto = signal('');
  indicadores = signal<string[]>([]);
  selectedIndicador = signal('Compensación');
  resumenIndicadores = signal<ResumenIndicador[]>([]);
  serie = signal<SerieTemporal | null>(null);

  constructor() {
    // Recargar datos cuando cambie el periodo seleccionado
    effect(() => {
      const periodo = this.periodoState.codigoPeriodo();
      if (periodo && this.negocio() && this.selectedPuesto()) {
        this.loadIndicadoresYResumen();
      }
    });
  }

  chartData: ChartData<'line'> = { labels: [], datasets: [] };
  chartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(0, 123, 255, 0.9)',
        callbacks: {
          title: (items) => `Semana: ${items[0].label}`,
          label: (ctx) => ctx.datasetIndex === 0 ? `$${(ctx.raw as number).toLocaleString()}` : ''
        }
      }
    },
    scales: {
      y: {
        beginAtZero: false,
        ticks: { callback: (v) => '$' + (v as number).toLocaleString() },
        grid: { color: '#f0f0f0' }
      },
      x: {
        grid: { display: false },
        ticks: { maxRotation: 45, minRotation: 45, font: { size: 9 } }
      }
    }
  };

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.negocio.set(params['negocio']);
      this.loadPuestos();
    });
  }

  loadPuestos() {
    this.loading.set(true);
    this.compensacionService.obtenerPuestosPorNegocio(this.negocio()).subscribe({
      next: (puestos) => {
        this.puestos.set(puestos);
        if (puestos.length > 0) {
          this.selectedPuesto.set(puestos[0]);
          this.loadIndicadoresYResumen();
        } else {
          this.loading.set(false);
        }
      },
      error: () => this.loading.set(false)
    });
  }

  loadIndicadoresYResumen() {
    // Cargar indicadores disponibles
    this.compensacionService.obtenerIndicadoresPorNegocioYPuesto(
      this.negocio(),
      this.selectedPuesto()
    ).subscribe({
      next: (indicadores) => {
        this.indicadores.set(indicadores);
        if (indicadores.length > 0) {
          const compensacion = indicadores.find(i => i === 'Compensación');
          this.selectedIndicador.set(compensacion || indicadores[0]);
        }
      }
    });

    // Cargar resumen de todos los indicadores
    this.compensacionService.obtenerResumenIndicadores(
      this.negocio(),
      this.selectedPuesto(),
      this.periodoState.codigoPeriodo()
    ).subscribe({
      next: (resumen) => {
        this.resumenIndicadores.set(resumen);
        // Establecer periodos desde la primera serie
        this.loadSerie();
      },
      error: () => this.loadSerie()
    });
  }

  selectPuesto(puesto: string) {
    this.selectedPuesto.set(puesto);
    this.loadIndicadoresYResumen();
  }

  selectIndicador(indicador: string) {
    this.selectedIndicador.set(indicador);
    this.loadSerie();
  }

  loadSerie() {
    this.loading.set(true);
    const hasta = this.periodoState.codigoPeriodo();
    this.compensacionService.obtenerSeriePorNegocioPuestoIndicador(
      this.negocio(),
      this.selectedPuesto(),
      this.selectedIndicador(),
      undefined,
      hasta
    ).subscribe({
      next: (serie) => {
        this.serie.set(serie);
        this.updateChart(serie);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  updateChart(serie: SerieTemporal) {
    const labels = serie.serie.map(p => p.semana);
    const valores = serie.serie.map(p => p.valor);
    const len = valores.length;

    this.chartData = {
      labels,
      datasets: [
        {
          label: this.selectedIndicador(),
          data: valores,
          borderColor: '#1976d2',
          backgroundColor: 'transparent',
          borderWidth: 1.5,
          pointRadius: 2,
          tension: 0
        },
        {
          label: '+1.5σ',
          data: Array(len).fill(serie.lineasConfianza.limiteSuperior15Sigma),
          borderColor: '#dc3545',
          borderDash: [8, 4],
          borderWidth: 2,
          pointRadius: 0
        },
        {
          label: '+1σ',
          data: Array(len).fill(serie.lineasConfianza.limiteSuperior1Sigma),
          borderColor: '#dc3545',
          borderDash: [4, 4],
          borderWidth: 1,
          pointRadius: 0
        },
        {
          label: '-1σ',
          data: Array(len).fill(serie.lineasConfianza.limiteInferior1Sigma),
          borderColor: '#dc3545',
          borderDash: [4, 4],
          borderWidth: 1,
          pointRadius: 0
        },
        {
          label: '-1.5σ',
          data: Array(len).fill(serie.lineasConfianza.limiteInferior15Sigma),
          borderColor: '#dc3545',
          borderDash: [8, 4],
          borderWidth: 2,
          pointRadius: 0
        }
      ]
    };
  }
}
