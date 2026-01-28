import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CompensacionService, PeriodoStateService } from '../../core/services';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <header class="header">
      <div class="header-left">
        <h1>Resumen de Nómina</h1>
        <span class="subtitle">Grupo Elektra</span>
      </div>
      <div class="header-center">
        <div class="periodo-selector" *ngIf="periodoState.periodos().length > 0">
          <label>Periodo:</label>
          <select [ngModel]="periodoState.periodoSeleccionado()"
                  (ngModelChange)="onPeriodoChange($event)">
            <option *ngFor="let periodo of periodoState.periodos()" [value]="periodo">
              {{ periodo }}
            </option>
          </select>
          <span class="periodo-comparacion" *ngIf="periodoState.periodoAnterior()">
            vs {{ periodoState.periodoAnterior() }}
          </span>
        </div>
        <div class="periodo-loading" *ngIf="periodoState.periodos().length === 0">
          Cargando periodos...
        </div>
      </div>
      <div class="header-right">
        <div class="status" [class.online]="isOnline">
          <span class="status-dot"></span>
          {{ isOnline ? 'Conectado' : 'Desconectado' }}
        </div>
        <div class="info" *ngIf="totalRegistros">
          <span>{{ totalRegistros | number }} registros</span>
        </div>
      </div>
    </header>
  `,
  styles: [`
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 24px;
      background: #1976d2;
      color: white;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .header-left {
      display: flex;
      align-items: baseline;
      gap: 12px;
      min-width: 250px;
    }

    .header-left h1 {
      font-size: 1.25rem;
      font-weight: 600;
      margin: 0;
    }

    .header-left .subtitle {
      font-size: 0.9rem;
      opacity: 0.8;
    }

    .header-center {
      flex: 1;
      display: flex;
      justify-content: center;
    }

    .periodo-selector {
      display: flex;
      align-items: center;
      gap: 10px;
      background: rgba(255, 255, 255, 0.2);
      padding: 8px 20px;
      border-radius: 6px;
      border: 1px solid rgba(255, 255, 255, 0.3);
    }

    .periodo-selector label {
      font-size: 0.9rem;
      font-weight: 600;
    }

    .periodo-selector select {
      background: white;
      border: none;
      padding: 8px 16px;
      border-radius: 4px;
      font-size: 1rem;
      font-weight: 700;
      color: #1976d2;
      cursor: pointer;
      min-width: 120px;
    }

    .periodo-selector select:focus {
      outline: 2px solid rgba(255, 255, 255, 0.5);
    }

    .periodo-comparacion {
      font-size: 0.85rem;
      opacity: 0.9;
      font-weight: 500;
    }

    .periodo-loading {
      font-size: 0.85rem;
      opacity: 0.8;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
      min-width: 200px;
      justify-content: flex-end;
    }

    .status {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 0.85rem;
    }

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #dc3545;
    }

    .status.online .status-dot {
      background: #28a745;
    }

    .info {
      font-size: 0.85rem;
      opacity: 0.9;
    }
  `]
})
export class HeaderComponent implements OnInit {
  private compensacionService = inject(CompensacionService);
  periodoState = inject(PeriodoStateService);
  isOnline = false;
  totalRegistros = 0;

  ngOnInit() {
    this.checkStatus();
  }

  checkStatus() {
    this.compensacionService.obtenerInfo().subscribe({
      next: (info) => {
        this.isOnline = true;
        this.totalRegistros = info.totalRegistros;
      },
      error: () => {
        this.isOnline = false;
      }
    });
  }

  onPeriodoChange(periodo: string) {
    this.periodoState.seleccionarPeriodo(periodo);
  }
}
