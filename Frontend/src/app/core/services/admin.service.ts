import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType, HttpProgressEvent, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ResultadoCarga {
  success: boolean;
  mensaje: string;
  fechaCarga: string;
  usuarioCarga: string;
  archivoOriginal: string;
  totalRegistros: number;
  registrosExitosos: number;
  registrosConError: number;
  tiempoProcesamientoMs: number;
  errores: string[];
}

export interface EstadisticasAnio {
  anio: number;
  totalRegistros: number;
  totalPeriodos: number;
  periodos: number[];
  totalNegocios: number;
  negocios: string[];
}

export interface Totales {
  totalRegistros: number;
  totalPeriodos: number;
  totalNegocios: number;
  negocios: string[];
}

export interface Estadisticas {
  estadisticasPorAnio: EstadisticasAnio[];
  totales: Totales;
}

export interface UploadProgress {
  status: 'progress' | 'complete' | 'error';
  progress: number;
  message?: string;
  data?: ResultadoCarga;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  /**
   * Carga archivo reemplazando todos los datos
   */
  cargarReemplazar(file: File, usuario: string = 'sistema'): Observable<UploadProgress> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('usuario', usuario);

    return this.http.post<{ success: boolean; data: ResultadoCarga }>(
      `${this.apiUrl}/admin/carga/reemplazar`,
      formData,
      {
        reportProgress: true,
        observe: 'events'
      }
    ).pipe(
      map(event => this.mapProgress(event))
    );
  }

  /**
   * Carga archivo de forma incremental
   */
  cargarIncremental(file: File, usuario: string = 'sistema'): Observable<UploadProgress> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('usuario', usuario);

    return this.http.post<{ success: boolean; data: ResultadoCarga }>(
      `${this.apiUrl}/admin/carga/incremental`,
      formData,
      {
        reportProgress: true,
        observe: 'events'
      }
    ).pipe(
      map(event => this.mapProgress(event))
    );
  }

  /**
   * Carga archivo para un periodo específico
   */
  cargarPorPeriodo(file: File, periodo: number, usuario: string = 'sistema'): Observable<UploadProgress> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('usuario', usuario);

    return this.http.post<{ success: boolean; data: ResultadoCarga }>(
      `${this.apiUrl}/admin/carga/periodo/${periodo}`,
      formData,
      {
        reportProgress: true,
        observe: 'events'
      }
    ).pipe(
      map(event => this.mapProgress(event))
    );
  }

  /**
   * Elimina datos de un periodo
   */
  eliminarPeriodo(periodo: number, usuario: string = 'sistema'): Observable<ResultadoCarga> {
    return this.http.delete<{ success: boolean; data: ResultadoCarga }>(
      `${this.apiUrl}/admin/periodo/${periodo}`,
      { params: { usuario } }
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Obtiene estadísticas de datos cargados
   */
  obtenerEstadisticas(): Observable<Estadisticas> {
    return this.http.get<{ success: boolean; data: Estadisticas }>(
      `${this.apiUrl}/admin/estadisticas`
    ).pipe(
      map(response => response.data)
    );
  }

  private mapProgress(event: HttpEvent<{ success: boolean; data: ResultadoCarga }>): UploadProgress {
    switch (event.type) {
      case HttpEventType.UploadProgress:
        const progressEvent = event as HttpProgressEvent;
        const progress = progressEvent.total
          ? Math.round(100 * progressEvent.loaded / progressEvent.total)
          : 0;
        return { status: 'progress', progress };

      case HttpEventType.Response:
        const response = event as HttpResponse<{ success: boolean; data: ResultadoCarga }>;
        if (response.body?.success) {
          return {
            status: 'complete',
            progress: 100,
            message: response.body.data.mensaje,
            data: response.body.data
          };
        } else {
          return {
            status: 'error',
            progress: 100,
            message: response.body?.data?.mensaje || 'Error desconocido'
          };
        }

      default:
        return { status: 'progress', progress: 0 };
    }
  }
}
