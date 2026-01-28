import { Injectable, inject, signal, computed } from '@angular/core';
import { CompensacionService } from './compensacion.service';

@Injectable({
  providedIn: 'root'
})
export class PeriodoStateService {
  private compensacionService = inject(CompensacionService);

  // Lista de periodos disponibles
  periodos = signal<string[]>([]);

  // Periodo seleccionado actualmente
  periodoSeleccionado = signal<string>('');

  // Periodo anterior calculado
  periodoAnterior = computed(() => {
    const actual = this.periodoSeleccionado();
    if (!actual) return '';

    // Formato: "2025-44" -> año=2025, semana=44
    const [anio, semana] = actual.split('-').map(Number);
    if (semana === 1) {
      // Si es semana 1, el anterior es la última semana del año anterior
      return `${anio - 1}-52`;
    }
    return `${anio}-${(semana - 1).toString().padStart(2, '0')}`;
  });

  // Código de periodo para enviar al backend (202544)
  codigoPeriodo = computed(() => {
    const periodo = this.periodoSeleccionado();
    if (!periodo) return undefined;
    const [anio, semana] = periodo.split('-');
    return parseInt(`${anio}${semana}`);
  });

  constructor() {
    this.cargarPeriodos();
  }

  cargarPeriodos() {
    this.compensacionService.obtenerPeriodos().subscribe({
      next: (periodos) => {
        this.periodos.set(periodos);
        // Si no hay periodo seleccionado, cargar el actual
        if (!this.periodoSeleccionado()) {
          this.cargarPeriodoActual();
        }
      }
    });
  }

  cargarPeriodoActual() {
    this.compensacionService.obtenerPeriodoActual().subscribe({
      next: (periodo) => {
        this.periodoSeleccionado.set(periodo);
      }
    });
  }

  seleccionarPeriodo(periodo: string) {
    this.periodoSeleccionado.set(periodo);
  }
}
