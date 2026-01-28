import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterOutlet, RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';
import { CompensacionService, PeriodoStateService } from './core/services';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, RouterModule],
  template: `
    <div class="app">
      <!-- Header -->
      <header class="app-header">
        <div class="header-left">
          <h1 class="title">Compensaciones | Red Grupo Elektra</h1>
          <div class="subtitle">Inteligencia de Datos</div>
          <div class="periodo-selector">
            <label>Periodo:</label>
            <select [ngModel]="periodoState.periodoSeleccionado()"
                    (ngModelChange)="onPeriodoChange($event)"
                    class="periodo-select">
              <option *ngFor="let p of periodoState.periodos()" [value]="p">{{ p }}</option>
            </select>
            <span class="vs-label">vs {{ periodoState.periodoAnterior() }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="logo">
            <div class="grupo">grupo</div>
            <div class="elektra">elektra</div>
          </div>
        </div>
      </header>

      <!-- Navegación Principal -->
      <nav class="main-nav">
        <a routerLink="/nacional" routerLinkActive="active" class="nav-item">Nacional</a>
        <a routerLink="/mapa-calor" routerLinkActive="active" class="nav-item">Mapa de Calor</a>
        <a routerLink="/tabla-resultados" routerLinkActive="active" class="nav-item">Tabla de Resultados</a>
        <a *ngFor="let neg of negocios()"
           [routerLink]="['/negocio', neg]"
           routerLinkActive="active"
           class="nav-item">{{ neg }}</a>
        <a routerLink="/alertas" routerLinkActive="active" class="nav-item">Alertas</a>
        <a routerLink="/carga-datos" routerLinkActive="active" class="nav-item admin-link">Carga de Datos</a>
      </nav>

      <!-- Contenido -->
      <main class="main-content">
        <router-outlet />
      </main>
    </div>
  `
})
export class App implements OnInit {
  private compensacionService = inject(CompensacionService);
  private router = inject(Router);
  periodoState = inject(PeriodoStateService);

  negocios = signal<string[]>([]);

  ngOnInit() {
    this.loadNegocios();
  }

  loadNegocios() {
    this.compensacionService.obtenerNegocios().subscribe({
      next: (negocios) => this.negocios.set(negocios)
    });
  }

  onPeriodoChange(periodo: string) {
    this.periodoState.seleccionarPeriodo(periodo);
  }
}
