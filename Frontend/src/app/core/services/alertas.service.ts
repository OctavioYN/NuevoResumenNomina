import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, AlertaZScore, AlertaPrediccion, AlertasResponse } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AlertasService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  /**
   * Obtiene alertas Z-Score para un periodo
   */
  obtenerAlertasZScore(periodo?: number): Observable<AlertaZScore[]> {
    let params = new HttpParams();
    if (periodo) {
      params = params.set('periodo', periodo.toString());
    }
    return this.http.get<ApiResponse<AlertaZScore[]>>(`${this.baseUrl}/alertas/zscore`, { params })
      .pipe(map(res => res.data));
  }

  /**
   * Obtiene alertas de intervalo de predicción 95% para un periodo
   */
  obtenerAlertasPrediccion(periodo?: number): Observable<AlertaPrediccion[]> {
    let params = new HttpParams();
    if (periodo) {
      params = params.set('periodo', periodo.toString());
    }
    return this.http.get<ApiResponse<AlertaPrediccion[]>>(`${this.baseUrl}/alertas/prediccion`, { params })
      .pipe(map(res => res.data));
  }

  /**
   * Obtiene todas las alertas para un periodo
   */
  obtenerTodasLasAlertas(periodo?: number): Observable<AlertasResponse> {
    let params = new HttpParams();
    if (periodo) {
      params = params.set('periodo', periodo.toString());
    }
    return this.http.get<ApiResponse<AlertasResponse>>(`${this.baseUrl}/alertas`, { params })
      .pipe(map(res => res.data));
  }
}
