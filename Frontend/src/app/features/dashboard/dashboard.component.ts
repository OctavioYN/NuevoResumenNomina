import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { CompensacionService } from '../../core/services';
import { ResumenNacional, SerieTemporal } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, BaseChartDirective, DecimalPipe],
  template: `
    <div class="dashboard">
      <!-- Header con selector de periodo -->
      <div class="page-header">
        <h2>Vista Nacional</h2>
        <div class="controls">
          <select class="form-select" [(ngModel)]="selectedPeriodo" (change)="onPeriodoChange()">
            <option *ngFor="let p of periodos()" [value]="p">{{ p }}</option>
          </select>
        </div>
      </div>

      <!-- Loading -->
      <div class="loading-container" *ngIf="loading()">
        <div class="spinner"></div>
      </div>

      <!-- Content -->
      <ng-container *ngIf="!loading() && resumen()">
        <!-- KPIs -->
        <div class="grid grid-4">
          <div class="kpi-card">
            <div class="kpi-label">Compensación Total</div>
            <div class="kpi-value">\${{ resumen()!.totalSemanaActual | number:'1.0-0' }}</div>
            <div class="kpi-change" [class.positive]="resumen()!.variacionPorcentual >= 0" [class.negative]="resumen()!.variacionPorcentual < 0">
              {{ resumen()!.variacionPorcentual >= 0 ? '▲' : '▼' }}
              {{ resumen()!.variacionPorcentual | number:'1.2-2' }}%
            </div>
          </div>

          <div class="kpi-card">
            <div class="kpi-label">Semana Anterior</div>
            <div class="kpi-value">\${{ resumen()!.totalSemanaAnterior | number:'1.0-0' }}</div>
            <div class="kpi-change text-muted">{{ resumen()!.periodoAnterior }}</div>
          </div>

          <div class="kpi-card">
            <div class="kpi-label">Diferencia</div>
            <div class="kpi-value" [class.text-success]="resumen()!.diferencia >= 0" [class.text-danger]="resumen()!.diferencia < 0">
              {{ resumen()!.diferencia >= 0 ? '+' : '' }}\${{ resumen()!.diferencia | number:'1.0-0' }}
            </div>
          </div>

          <div class="kpi-card">
            <div class="kpi-label">Total Negocios</div>
            <div class="kpi-value">{{ resumen()!.negocios.length }}</div>
          </div>
        </div>

        <!-- Gráfico de Serie Temporal -->
        <div class="card" *ngIf="serie()">
          <div class="card-header">
            <h3>Tendencia Nacional - Compensación Total</h3>
            <div class="stats">
              <span class="stat">μ: \${{ serie()!.media | number:'1.0-0' }}</span>
              <span class="stat">σ: \${{ serie()!.desviacionEstandar | number:'1.0-0' }}</span>
            </div>
          </div>
          <div class="chart-container">
            <canvas baseChart
              [data]="chartData"
              [options]="chartOptions"
              [type]="'line'">
            </canvas>
          </div>
        </div>

        <!-- Tabla de Resumen por Negocio -->
        <div class="card">
          <div class="card-header">
            <h3>Resumen por Negocio</h3>
          </div>
          <table class="data-table">
            <thead>
              <tr>
                <th>Negocio</th>
                <th class="numeric">Semana Actual</th>
                <th class="numeric">Semana Anterior</th>
                <th class="numeric">Diferencia</th>
                <th class="numeric">Variación</th>
                <th class="numeric">Empleados</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let n of resumen()!.negocios">
                <td>
                  <a [routerLink]="['/negocio', n.negocio]" class="negocio-link">{{ n.negocio }}</a>
                </td>
                <td class="numeric">\${{ n.semanaActual | number:'1.0-0' }}</td>
                <td class="numeric">\${{ n.semanaAnterior | number:'1.0-0' }}</td>
                <td class="numeric" [class.text-success]="n.diferencia >= 0" [class.text-danger]="n.diferencia < 0">
                  {{ n.diferencia >= 0 ? '+' : '' }}\${{ n.diferencia | number:'1.0-0' }}
                </td>
                <td class="numeric">
                  <span class="badge" [class.badge-success]="n.variacionPorcentual >= 0" [class.badge-danger]="n.variacionPorcentual < 0">
                    {{ n.variacionPorcentual >= 0 ? '+' : '' }}{{ n.variacionPorcentual | number:'1.2-2' }}%
                  </span>
                </td>
                <td class="numeric">{{ n.empleados | number }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </ng-container>
    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--spacing-lg);

      h2 {
        font-size: 1.5rem;
        color: var(--primary-color);
      }
    }

    .chart-container {
      height: 350px;
      position: relative;
    }

    .stats {
      display: flex;
      gap: var(--spacing-md);

      .stat {
        font-size: 0.85rem;
        color: var(--text-muted);
        background: var(--bg-color);
        padding: 4px 12px;
        border-radius: 12px;
      }
    }

    .negocio-link {
      color: var(--primary-color);
      text-decoration: none;
      font-weight: 500;

      &:hover {
        text-decoration: underline;
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  private compensacionService = inject(CompensacionService);

  loading = signal(true);
  periodos = signal<string[]>([]);
  selectedPeriodo = '';
  resumen = signal<ResumenNacional | null>(null);
  serie = signal<SerieTemporal | null>(null);

  chartData: ChartData<'line'> = { labels: [], datasets: [] };
  chartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      intersect: false,
      mode: 'index'
    },
    plugins: {
      legend: {
        display: true,
        position: 'top'
      },
      tooltip: {
        callbacks: {
          label: (context) => {
            const value = context.raw as number;
            return `${context.dataset.label}: $${value.toLocaleString()}`;
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: false,
        ticks: {
          callback: (value) => '$' + (value as number).toLocaleString()
        }
      }
    }
  };

  ngOnInit() {
    this.loadPeriodos();
  }

  loadPeriodos() {
    this.compensacionService.obtenerPeriodos().subscribe({
      next: (periodos) => {
        this.periodos.set(periodos);
        if (periodos.length > 0) {
          this.selectedPeriodo = periodos[periodos.length - 1];
          this.loadData();
        }
      }
    });
  }

  onPeriodoChange() {
    this.loadData();
  }

  loadData() {
    this.loading.set(true);
    const periodoNum = this.periodoToNumber(this.selectedPeriodo);

    this.compensacionService.obtenerResumenNacional(periodoNum).subscribe({
      next: (resumen) => {
        this.resumen.set(resumen);
        this.loadSerie();
      },
      error: () => this.loading.set(false)
    });
  }

  loadSerie() {
    this.compensacionService.obtenerSerieNacional().subscribe({
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

    this.chartData = {
      labels,
      datasets: [
        {
          label: 'Compensación',
          data: valores,
          borderColor: '#1a365d',
          backgroundColor: 'rgba(26, 54, 93, 0.1)',
          fill: true,
          tension: 0.3,
          pointRadius: 2,
          pointHoverRadius: 6
        },
        {
          label: 'Media (μ)',
          data: Array(valores.length).fill(serie.media),
          borderColor: '#718096',
          borderDash: [5, 5],
          pointRadius: 0,
          fill: false
        },
        {
          label: '+1σ',
          data: Array(valores.length).fill(serie.lineasConfianza.limiteSuperior1Sigma),
          borderColor: '#38a169',
          borderDash: [3, 3],
          pointRadius: 0,
          fill: false
        },
        {
          label: '-1σ',
          data: Array(valores.length).fill(serie.lineasConfianza.limiteInferior1Sigma),
          borderColor: '#38a169',
          borderDash: [3, 3],
          pointRadius: 0,
          fill: false
        },
        {
          label: '+1.5σ',
          data: Array(valores.length).fill(serie.lineasConfianza.limiteSuperior15Sigma),
          borderColor: '#e53e3e',
          borderDash: [2, 2],
          pointRadius: 0,
          fill: false
        },
        {
          label: '-1.5σ',
          data: Array(valores.length).fill(serie.lineasConfianza.limiteInferior15Sigma),
          borderColor: '#e53e3e',
          borderDash: [2, 2],
          pointRadius: 0,
          fill: false
        }
      ]
    };
  }

  private periodoToNumber(periodo: string): number {
    return parseInt(periodo.replace('-', ''));
  }
}
