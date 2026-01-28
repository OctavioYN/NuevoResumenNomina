import { Component, inject, OnInit, signal, effect } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { AlertasService, PeriodoStateService } from '../../core/services';
import { AlertaZScore, AlertaPrediccion } from '../../core/models';

@Component({
  selector: 'app-alertas',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  template: `
    <div class="alertas-view">
      <!-- Loading -->
      <div class="loading" *ngIf="loading()">
        <div class="spinner"></div>
      </div>

      <ng-container *ngIf="!loading()">
        <!-- Sub-tabs -->
        <div class="sub-tabs">
          <button class="sub-tab" [class.active]="selectedTab() === 'zscore'" (click)="selectTab('zscore')">
            Z-Score
          </button>
          <button class="sub-tab" [class.active]="selectedTab() === 'prediccion'" (click)="selectTab('prediccion')">
            Intervalo de Predicción 95%
          </button>
        </div>

        <!-- Z-Score Tab Content -->
        <div class="tab-content" *ngIf="selectedTab() === 'zscore'">
          <p class="tab-description">
            Se activa una alerta cuando la variación semanal (vs. semana anterior) sale del rango habitual, considerando:
          </p>
          <p class="tab-info">
            <strong>El comportamiento histórico</strong> del indicador (media y variabilidad típica).<br>
            <strong>La severidad del cambio:</strong>
          </p>
          <div class="severity-legend">
            <span class="severity-item critica"><span class="dot"></span> CRÍTICA: Desviación excepcionalmente abrupta (inusual y muy pronunciada) que requiere revisión prioritaria.</span>
            <span class="severity-item alta"><span class="dot"></span> ALTA: Desviación importante.</span>
            <span class="severity-item moderada"><span class="dot"></span> MODERADA: Desviación marginalmente fuera de los parámetros esperados.</span>
          </div>

          <button class="btn-export" (click)="exportToExcel('zscore')">Excel</button>

          <div class="alerts-table" *ngIf="alertasZScore().length > 0">
            <table>
              <thead>
                <tr>
                  <th>Puesto</th>
                  <th>Indicador</th>
                  <th>Variación Porcentual vs S.A.</th>
                  <th>Variación Media</th>
                  <th>Límite Inferior</th>
                  <th>Límite Superior</th>
                  <th>Severidad</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let alerta of alertasZScore()">
                  <td>{{ alerta.puesto }}</td>
                  <td>{{ alerta.indicador }}</td>
                  <td>{{ alerta.variacionPorcentualVsSA | number:'1.2-2' }}%</td>
                  <td>{{ alerta.variacionMedia | number:'1.2-2' }}%</td>
                  <td>{{ alerta.limiteInferior | number:'1.2-2' }}%</td>
                  <td>{{ alerta.limiteSuperior | number:'1.2-2' }}%</td>
                  <td>
                    <span class="severity-badge" [class]="alerta.severidad.toLowerCase()">
                      <span class="dot"></span> {{ alerta.severidad }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="no-data" *ngIf="alertasZScore().length === 0">
            No se encontraron alertas Z-Score para este periodo.
          </div>
        </div>

        <!-- Predicción Tab Content -->
        <div class="tab-content" *ngIf="selectedTab() === 'prediccion'">
          <p class="tab-description">
            Se activa una alerta cuando el valor observado está fuera del intervalo de predicción del 95% construido con el modelo ARIMA (p,d,q)
          </p>
          <p class="tab-note">
            *Puestos sin suficiente historia. Por lo tanto, este modelo no es robusto en ellos.
          </p>

          <button class="btn-export" (click)="exportToExcel('prediccion')">Excel</button>

          <div class="alerts-table" *ngIf="alertasPrediccion().length > 0">
            <table>
              <thead>
                <tr>
                  <th>Puesto</th>
                  <th>Indicador</th>
                  <th>Observación Real</th>
                  <th>Límite Inferior</th>
                  <th>Límite Superior</th>
                  <th>Variación fuera del rango</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let alerta of alertasPrediccion()">
                  <td>{{ alerta.puesto }}{{ alerta.historiaInsuficiente ? ' *' : '' }}</td>
                  <td>{{ alerta.indicador }}</td>
                  <td>{{ formatValue(alerta.observacionReal) }}</td>
                  <td>{{ formatValue(alerta.limiteInferior) }}</td>
                  <td>{{ formatValue(alerta.limiteSuperior) }}</td>
                  <td>{{ alerta.variacionFueraRango | number:'1.0-0' }}%</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="no-data" *ngIf="alertasPrediccion().length === 0">
            No se encontraron alertas de predicción para este periodo.
          </div>
        </div>
      </ng-container>
    </div>
  `,
  styles: [`
    .alertas-view {
      padding-top: 10px;
    }

    .tab-description {
      font-size: 13px;
      color: #333;
      margin-bottom: 10px;
    }

    .tab-info {
      font-size: 13px;
      color: #333;
      margin-bottom: 10px;
    }

    .tab-note {
      font-size: 12px;
      color: #666;
      font-style: italic;
      margin-bottom: 15px;
    }

    .severity-legend {
      display: flex;
      flex-direction: column;
      gap: 6px;
      margin-bottom: 20px;
      padding-left: 20px;
    }

    .severity-item {
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .severity-item .dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      display: inline-block;
    }

    .severity-item.critica .dot { background-color: #dc3545; }
    .severity-item.alta .dot { background-color: #ffc107; }
    .severity-item.moderada .dot { background-color: #28a745; }

    .btn-export {
      padding: 6px 16px;
      font-size: 12px;
      border: 1px solid #e0e0e0;
      background: white;
      cursor: pointer;
      border-radius: 4px;
      margin-bottom: 15px;
    }

    .btn-export:hover {
      background: #f5f5f5;
    }

    .alerts-table {
      overflow-x: auto;
    }

    .alerts-table table {
      width: 100%;
      border-collapse: collapse;
      font-size: 13px;
    }

    .alerts-table th,
    .alerts-table td {
      padding: 10px 12px;
      text-align: left;
      border-bottom: 1px solid #e0e0e0;
    }

    .alerts-table th {
      background: #f5f5f5;
      font-weight: 600;
      color: #666;
    }

    .alerts-table tr:hover {
      background: #fafafa;
    }

    .severity-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      border-radius: 4px;
      font-size: 11px;
      font-weight: 600;
    }

    .severity-badge .dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
    }

    .severity-badge.critica {
      background-color: #f8d7da;
      color: #721c24;
    }
    .severity-badge.critica .dot { background-color: #dc3545; }

    .severity-badge.alta {
      background-color: #fff3cd;
      color: #856404;
    }
    .severity-badge.alta .dot { background-color: #ffc107; }

    .severity-badge.moderada {
      background-color: #d4edda;
      color: #155724;
    }
    .severity-badge.moderada .dot { background-color: #28a745; }

    .no-data {
      text-align: center;
      padding: 40px;
      color: #666;
      font-size: 14px;
    }
  `]
})
export class AlertasComponent implements OnInit {
  private alertasService = inject(AlertasService);
  private periodoState = inject(PeriodoStateService);

  loading = signal(true);
  selectedTab = signal<'zscore' | 'prediccion'>('zscore');
  alertasZScore = signal<AlertaZScore[]>([]);
  alertasPrediccion = signal<AlertaPrediccion[]>([]);

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

  selectTab(tab: 'zscore' | 'prediccion') {
    this.selectedTab.set(tab);
  }

  loadData() {
    this.loading.set(true);
    const periodo = this.periodoState.codigoPeriodo();

    // Cargar alertas Z-Score
    this.alertasService.obtenerAlertasZScore(periodo).subscribe({
      next: (alertas) => {
        this.alertasZScore.set(alertas);
      },
      error: () => {
        this.alertasZScore.set([]);
      }
    });

    // Cargar alertas de predicción
    this.alertasService.obtenerAlertasPrediccion(periodo).subscribe({
      next: (alertas) => {
        this.alertasPrediccion.set(alertas);
        this.loading.set(false);
      },
      error: () => {
        this.alertasPrediccion.set([]);
        this.loading.set(false);
      }
    });
  }

  formatValue(value: number): string {
    if (Math.abs(value) >= 1000) {
      return '$' + value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
    return value.toFixed(2);
  }

  exportToExcel(type: 'zscore' | 'prediccion') {
    const data = type === 'zscore' ? this.alertasZScore() : this.alertasPrediccion();
    if (data.length === 0) return;

    let csv = '';
    if (type === 'zscore') {
      csv = 'Puesto,Indicador,Variacion % vs SA,Variacion Media,Limite Inferior,Limite Superior,Severidad\n';
      (data as AlertaZScore[]).forEach(a => {
        csv += `"${a.puesto}","${a.indicador}",${a.variacionPorcentualVsSA},${a.variacionMedia},${a.limiteInferior},${a.limiteSuperior},${a.severidad}\n`;
      });
    } else {
      csv = 'Puesto,Indicador,Observacion Real,Limite Inferior,Limite Superior,Variacion Fuera Rango\n';
      (data as AlertaPrediccion[]).forEach(a => {
        csv += `"${a.puesto}${a.historiaInsuficiente ? ' *' : ''}","${a.indicador}",${a.observacionReal},${a.limiteInferior},${a.limiteSuperior},${a.variacionFueraRango}\n`;
      });
    }

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `alertas_${type}_${this.periodoState.periodoSeleccionado()}.csv`;
    link.click();
  }
}
