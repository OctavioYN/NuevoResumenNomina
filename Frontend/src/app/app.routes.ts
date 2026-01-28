import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'nacional', pathMatch: 'full' },
  {
    path: 'nacional',
    loadComponent: () => import('./features/nacional/nacional.component').then(m => m.NacionalComponent)
  },
  {
    path: 'mapa-calor',
    loadComponent: () => import('./features/mapa-calor/mapa-calor.component').then(m => m.MapaCalorComponent)
  },
  {
    path: 'tabla-resultados',
    loadComponent: () => import('./features/tabla-resultados/tabla-resultados.component').then(m => m.TablaResultadosComponent)
  },
  {
    path: 'negocio/:negocio',
    loadComponent: () => import('./features/negocio/negocio.component').then(m => m.NegocioComponent)
  },
  {
    path: 'alertas',
    loadComponent: () => import('./features/alertas/alertas.component').then(m => m.AlertasComponent)
  },
  {
    path: 'carga-datos',
    loadComponent: () => import('./features/carga-datos/carga-datos.component').then(m => m.CargaDatosComponent)
  },
  { path: '**', redirectTo: 'nacional' }
];
