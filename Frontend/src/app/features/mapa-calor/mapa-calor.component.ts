import { Component, inject, OnInit, signal, effect } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { CompensacionService, PeriodoStateService } from '../../core/services';
import { MapaCalor, ItemMapaCalor } from '../../core/models';

@Component({
  selector: 'app-mapa-calor',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  template: `
    <div class="mapa-calor-view">
      <div class="loading" *ngIf="loading()">
        <div class="spinner"></div>
      </div>

      <ng-container *ngIf="!loading() && mapaCalor()">
        <div class="treemap-container">
          <h3 class="treemap-title">
            Variación porcentual de nómina por puesto: Semana actual vs anterior
          </h3>

          <div class="treemap-wrapper">
            <!-- Agrupamos por categorías -->
            <div class="treemap-grid">
              <ng-container *ngFor="let grupo of gruposOrdenados()">
                <div class="treemap-group" [style.flex]="getGroupFlex(grupo.negocio)">
                  <div class="group-label">{{ grupo.negocio }}</div>
                  <div class="group-items">
                    <div *ngFor="let item of grupo.items"
                         class="treemap-item"
                         [style.background]="getColor(item)"
                         [style.flex]="getItemFlex(item)"
                         [title]="item.puesto + ': ' + item.variacionPorcentual + '%'">
                      <span class="item-percent">{{ item.variacionPorcentual >= 0 ? '' : '' }}{{ item.variacionPorcentual | number:'1.0-0' }}%</span>
                      <span class="item-name">{{ item.puesto }}</span>
                    </div>
                  </div>
                </div>
              </ng-container>
            </div>
          </div>
        </div>
      </ng-container>
    </div>
  `,
  styles: [`
    .mapa-calor-view {
      padding-top: 10px;
    }

    .treemap-wrapper {
      background: white;
      padding: 20px;
      border-radius: 4px;
    }

    .treemap-grid {
      display: flex;
      flex-wrap: wrap;
      gap: 3px;
      min-height: 500px;
    }

    .treemap-group {
      display: flex;
      flex-direction: column;
      position: relative;
      min-width: 120px;
    }

    .group-label {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 32px;
      font-weight: bold;
      color: rgba(0, 0, 0, 0.08);
      white-space: nowrap;
      pointer-events: none;
      z-index: 1;
    }

    .group-items {
      display: flex;
      flex-wrap: wrap;
      gap: 2px;
      height: 100%;
      position: relative;
      z-index: 2;
    }

    .treemap-item {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      padding: 10px 8px;
      color: white;
      text-align: center;
      min-width: 80px;
      min-height: 60px;
      cursor: pointer;
      transition: opacity 0.2s, transform 0.2s;

      &:hover {
        opacity: 0.85;
        transform: scale(1.02);
      }

      .item-percent {
        font-size: 18px;
        font-weight: bold;
      }

      .item-name {
        font-size: 10px;
        margin-top: 4px;
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  `]
})
export class MapaCalorComponent implements OnInit {
  private compensacionService = inject(CompensacionService);
  private periodoState = inject(PeriodoStateService);

  loading = signal(true);
  mapaCalor = signal<MapaCalor | null>(null);

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

  loadData() {
    this.loading.set(true);
    const periodo = this.periodoState.codigoPeriodo();
    this.compensacionService.obtenerMapaCalor(periodo).subscribe({
      next: (data) => {
        this.mapaCalor.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  gruposOrdenados(): { negocio: string; items: ItemMapaCalor[] }[] {
    const mapa = this.mapaCalor();
    if (!mapa) return [];

    return Object.keys(mapa.itemsPorNegocio)
      .sort()
      .map(negocio => ({
        negocio,
        items: mapa.itemsPorNegocio[negocio].sort((a, b) =>
          Math.abs(b.variacionPorcentual) - Math.abs(a.variacionPorcentual)
        )
      }));
  }

  getGroupFlex(negocio: string): string {
    const mapa = this.mapaCalor();
    if (!mapa) return '1';
    const items = mapa.itemsPorNegocio[negocio] || [];
    const totalValue = items.reduce((sum, i) => sum + Math.abs(i.valorSemanaActual), 0);
    const allTotal = mapa.items.reduce((sum, i) => sum + Math.abs(i.valorSemanaActual), 0);
    const flex = Math.max(1, Math.round((totalValue / allTotal) * 10));
    return flex.toString();
  }

  getItemFlex(item: ItemMapaCalor): string {
    const value = Math.abs(item.valorSemanaActual);
    const flex = Math.max(1, Math.round(value / 1000000));
    return `${flex} 1 ${Math.max(80, flex * 20)}px`;
  }

  getColor(item: ItemMapaCalor): string {
    const percent = item.variacionPorcentual;
    const absPercent = Math.abs(percent);
    const intensity = Math.min(absPercent / 15, 1); // Normalizar a 15%

    if (percent >= 0) {
      // Verde - de claro a oscuro
      const r = Math.round(40 + (1 - intensity) * 100);
      const g = Math.round(120 + (1 - intensity) * 80);
      const b = Math.round(40 + (1 - intensity) * 100);
      return `rgb(${r}, ${g}, ${b})`;
    } else {
      // Rojo - de claro a oscuro
      const r = Math.round(180 + (1 - intensity) * 50);
      const g = Math.round(50 + (1 - intensity) * 100);
      const b = Math.round(50 + (1 - intensity) * 100);
      return `rgb(${r}, ${g}, ${b})`;
    }
  }
}
