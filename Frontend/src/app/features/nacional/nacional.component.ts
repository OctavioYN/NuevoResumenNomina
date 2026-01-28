import { Component, inject, OnInit, signal, effect, Pipe, PipeTransform } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { CompensacionService, PeriodoStateService } from '../../core/services';
import { ResumenNacional, SerieTemporal } from '../../core/models';

// Pipe para agregar $ al principio
@Pipe({ name: 'prefixCurrency', standalone: true })
export class PrefixCurrencyPipe implements PipeTransform {
  transform(value: string | null): string {
    if (!value) return '$0';
    return '$' + value;
  }
}

@Component({
  selector: 'app-nacional',
  standalone: true,
  imports: [CommonModule, BaseChartDirective, DecimalPipe, PrefixCurrencyPipe],
  template: `
    <div class="nacional-view">
      <!-- Loading -->
      <div class="loading" *ngIf="loading()">
        <div class="spinner"></div>
      </div>

      <ng-container *ngIf="!loading() && resumen()">
        <!-- Tabla Resumen Superior -->
        <div class="summary-table">
          <table>
            <thead>
              <tr>
                <th></th>
                <th>{{ resumen()!.periodoActual }}</th>
                <th>{{ resumen()!.periodoAnterior }}</th>
                <th>Diferencia $</th>
                <th>Variación %</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Total</td>
                <td>{{ resumen()!.totalSemanaActual | number:'1.0-0' | prefixCurrency }}</td>
                <td>{{ resumen()!.totalSemanaAnterior | number:'1.0-0' | prefixCurrency }}</td>
                <td [class.negative]="resumen()!.diferencia < 0" [class.positive]="resumen()!.diferencia >= 0">
                  {{ resumen()!.diferencia | number:'1.0-0' | prefixCurrency }}
                </td>
                <td [class.negative]="resumen()!.variacionPorcentual < 0" [class.positive]="resumen()!.variacionPorcentual >= 0">
                  {{ resumen()!.variacionPorcentual | number:'1.1-1' }}%
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Sub-tabs de negocios -->
        <div class="sub-tabs">
          <button class="sub-tab" [class.active]="selectedNegocio() === 'Total'" (click)="selectNegocio('Total')">
            Total
          </button>
          <button *ngFor="let neg of resumen()!.negocios"
                  class="sub-tab"
                  [class.active]="selectedNegocio() === neg.negocio"
                  (click)="selectNegocio(neg.negocio)">
            {{ neg.negocio }}
          </button>
        </div>

        <!-- Info de puestos (solo cuando se selecciona un negocio) -->
        <div class="puestos-info" *ngIf="selectedNegocio() !== 'Total' && puestosNegocio().length > 0">
          Puestos: {{ puestosNegocio().join(', ') }}
        </div>

        <!-- Gráfico -->
        <div class="chart-section" *ngIf="serie()">
          <h3 class="chart-title">Compensación Total</h3>
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
    .nacional-view { padding-top: 10px; }
  `]
})
export class NacionalComponent implements OnInit {
  private compensacionService = inject(CompensacionService);
  private periodoState = inject(PeriodoStateService);

  loading = signal(true);
  resumen = signal<ResumenNacional | null>(null);
  serie = signal<SerieTemporal | null>(null);
  selectedNegocio = signal('Total');
  puestosNegocio = signal<string[]>([]);

  constructor() {
    // Recargar datos cuando cambie el periodo seleccionado
    effect(() => {
      const periodo = this.periodoState.codigoPeriodo();
      if (periodo) {
        this.loadData();
      }
    });
  }

  chartData: ChartData<'line'> = { labels: [], datasets: [] };
  chartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { intersect: false, mode: 'index' },
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(0, 123, 255, 0.9)',
        titleFont: { size: 12 },
        bodyFont: { size: 11 },
        padding: 10,
        callbacks: {
          title: (items) => `Semana: ${items[0].label}`,
          label: (ctx) => {
            if (ctx.datasetIndex === 0) {
              const value = ctx.raw as number;
              const serie = this.serie();
              const punto = serie?.serie[ctx.dataIndex];
              return [
                `Total: $${value.toLocaleString()}`,
                `Variación vs SA: ${punto?.variacionVsSemanaAnterior?.toFixed(1) || 0}%`
              ];
            }
            return '';
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: false,
        ticks: {
          callback: (value) => '$' + (value as number).toLocaleString()
        },
        grid: { color: '#f0f0f0' }
      },
      x: {
        grid: { display: false },
        ticks: { maxRotation: 45, minRotation: 45, font: { size: 10 } }
      }
    }
  };

  ngOnInit() {
    // La carga inicial se hace en el effect cuando se establece el periodo
  }

  loadData() {
    this.loading.set(true);
    const periodo = this.periodoState.codigoPeriodo();

    this.compensacionService.obtenerResumenNacional(periodo).subscribe({
      next: (resumen) => {
        this.resumen.set(resumen);
        this.loadSerie();
      },
      error: () => this.loading.set(false)
    });
  }

  selectNegocio(negocio: string) {
    this.selectedNegocio.set(negocio);
    this.loadSerie();

    if (negocio !== 'Total') {
      this.compensacionService.obtenerPuestosPorNegocio(negocio).subscribe({
        next: (puestos) => this.puestosNegocio.set(puestos)
      });
    } else {
      this.puestosNegocio.set([]);
    }
  }

  loadSerie() {
    const negocio = this.selectedNegocio();
    const hasta = this.periodoState.codigoPeriodo();
    const obs = negocio === 'Total'
      ? this.compensacionService.obtenerSerieNacional(undefined, hasta)
      : this.compensacionService.obtenerSeriePorNegocio(negocio, undefined, hasta);

    obs.subscribe({
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
          label: 'Compensación',
          data: valores,
          borderColor: '#1976d2',
          backgroundColor: 'transparent',
          borderWidth: 1.5,
          pointRadius: 2,
          pointHoverRadius: 5,
          tension: 0
        },
        {
          label: '+1.5σ',
          data: Array(len).fill(serie.lineasConfianza.limiteSuperior15Sigma),
          borderColor: '#dc3545',
          borderDash: [8, 4],
          borderWidth: 2,
          pointRadius: 0,
          fill: false
        },
        {
          label: '+1σ',
          data: Array(len).fill(serie.lineasConfianza.limiteSuperior1Sigma),
          borderColor: '#dc3545',
          borderDash: [4, 4],
          borderWidth: 1,
          pointRadius: 0,
          fill: false
        },
        {
          label: '-1σ',
          data: Array(len).fill(serie.lineasConfianza.limiteInferior1Sigma),
          borderColor: '#dc3545',
          borderDash: [4, 4],
          borderWidth: 1,
          pointRadius: 0,
          fill: false
        },
        {
          label: '-1.5σ',
          data: Array(len).fill(serie.lineasConfianza.limiteInferior15Sigma),
          borderColor: '#dc3545',
          borderDash: [8, 4],
          borderWidth: 2,
          pointRadius: 0,
          fill: false
        }
      ]
    };
  }
}
