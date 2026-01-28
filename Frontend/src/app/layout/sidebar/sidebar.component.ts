import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CompensacionService } from '../../core/services';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="sidebar">
      <div class="nav-section">
        <div class="nav-title">Vista General</div>
        <a routerLink="/nacional" routerLinkActive="active" class="nav-item">
          <span class="icon">📊</span>
          Dashboard Nacional
        </a>
        <a routerLink="/mapa-calor" routerLinkActive="active" class="nav-item">
          <span class="icon">🗺️</span>
          Mapa de Calor
        </a>
        <a routerLink="/tabla-resultados" routerLinkActive="active" class="nav-item">
          <span class="icon">📋</span>
          Tabla de Resultados
        </a>
        <a routerLink="/alertas" routerLinkActive="active" class="nav-item">
          <span class="icon">🔔</span>
          Alertas
        </a>
      </div>

      <div class="nav-section">
        <div class="nav-title">Administración</div>
        <a routerLink="/carga-datos" routerLinkActive="active" class="nav-item">
          <span class="icon">📤</span>
          Carga de Datos
        </a>
      </div>

      <div class="nav-section">
        <div class="nav-title">Por Negocio</div>
        <a *ngFor="let negocio of negocios"
           [routerLink]="['/negocio', negocio]"
           routerLinkActive="active"
           class="nav-item">
          <span class="icon">🏢</span>
          {{ negocio }}
        </a>
      </div>
    </nav>
  `,
  styles: [`
    .sidebar {
      width: 240px;
      min-height: calc(100vh - 60px);
      background: white;
      border-right: 1px solid var(--border-color);
      padding: var(--spacing-md) 0;
    }

    .nav-section {
      margin-bottom: var(--spacing-lg);
    }

    .nav-title {
      padding: var(--spacing-sm) var(--spacing-lg);
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      padding: var(--spacing-sm) var(--spacing-lg);
      color: var(--text-color);
      text-decoration: none;
      font-size: 0.9rem;
      transition: all 0.2s;

      .icon {
        font-size: 1rem;
      }

      &:hover {
        background: var(--bg-color);
      }

      &.active {
        background: rgba(26, 54, 93, 0.1);
        color: var(--primary-color);
        font-weight: 500;
        border-right: 3px solid var(--primary-color);
      }
    }
  `]
})
export class SidebarComponent implements OnInit {
  private compensacionService = inject(CompensacionService);
  negocios: string[] = [];

  ngOnInit() {
    this.loadNegocios();
  }

  loadNegocios() {
    this.compensacionService.obtenerNegocios().subscribe({
      next: (negocios) => this.negocios = negocios
    });
  }
}
