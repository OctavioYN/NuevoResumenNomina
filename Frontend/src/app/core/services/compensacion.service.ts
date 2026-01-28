import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ApiResponse,
  ResumenNacional,
  SerieTemporal,
  MapaCalor,
  TablaResultados,
  ResumenIndicador
} from '../models';

@Injectable({
  providedIn: 'root'
})
export class CompensacionService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  // ==================== PERIODOS Y CATALOGOS ====================

  obtenerPeriodos(): Observable<string[]> {
    return this.http.get<ApiResponse<string[]>>(`${this.baseUrl}/compensacion/periodos`)
      .pipe(map(res => res.data));
  }

  obtenerPeriodoActual(): Observable<string> {
    return this.http.get<ApiResponse<string>>(`${this.baseUrl}/compensacion/periodo-actual`)
      .pipe(map(res => res.data));
  }

  obtenerNegocios(): Observable<string[]> {
    return this.http.get<ApiResponse<string[]>>(`${this.baseUrl}/compensacion/negocios`)
      .pipe(map(res => res.data));
  }

  obtenerPuestosPorNegocio(negocio: string): Observable<string[]> {
    return this.http.get<ApiResponse<string[]>>(`${this.baseUrl}/compensacion/negocios/${negocio}/puestos`)
      .pipe(map(res => res.data));
  }

  obtenerIndicadores(): Observable<string[]> {
    return this.http.get<ApiResponse<string[]>>(`${this.baseUrl}/compensacion/indicadores`)
      .pipe(map(res => res.data));
  }

  obtenerIndicadoresPorNegocio(negocio: string): Observable<string[]> {
    return this.http.get<ApiResponse<string[]>>(`${this.baseUrl}/compensacion/negocios/${negocio}/indicadores`)
      .pipe(map(res => res.data));
  }

  obtenerIndicadoresPorNegocioYPuesto(negocio: string, puesto: string): Observable<string[]> {
    return this.http.get<ApiResponse<string[]>>(
      `${this.baseUrl}/compensacion/negocios/${encodeURIComponent(negocio)}/puestos/${encodeURIComponent(puesto)}/indicadores`
    ).pipe(map(res => res.data));
  }

  obtenerResumenIndicadores(negocio: string, puesto: string, periodo?: number): Observable<ResumenIndicador[]> {
    let params = new HttpParams();
    if (periodo) {
      params = params.set('periodo', periodo.toString());
    }
    return this.http.get<ApiResponse<ResumenIndicador[]>>(
      `${this.baseUrl}/compensacion/negocios/${encodeURIComponent(negocio)}/puestos/${encodeURIComponent(puesto)}/resumen-indicadores`,
      { params }
    ).pipe(map(res => res.data));
  }

  // ==================== VISTA NACIONAL ====================

  obtenerResumenNacional(periodo?: number): Observable<ResumenNacional> {
    let params = new HttpParams();
    if (periodo) {
      params = params.set('periodo', periodo.toString());
    }
    return this.http.get<ApiResponse<ResumenNacional>>(`${this.baseUrl}/compensacion/nacional`, { params })
      .pipe(map(res => res.data));
  }

  obtenerSerieNacional(desde?: number, hasta?: number): Observable<SerieTemporal> {
    let params = new HttpParams();
    if (desde) params = params.set('desde', desde.toString());
    if (hasta) params = params.set('hasta', hasta.toString());
    return this.http.get<ApiResponse<SerieTemporal>>(`${this.baseUrl}/compensacion/nacional/serie`, { params })
      .pipe(map(res => res.data));
  }

  // ==================== MAPA DE CALOR ====================

  obtenerMapaCalor(periodo?: number): Observable<MapaCalor> {
    let params = new HttpParams();
    if (periodo) {
      params = params.set('periodo', periodo.toString());
    }
    return this.http.get<ApiResponse<MapaCalor>>(`${this.baseUrl}/compensacion/mapa-calor`, { params })
      .pipe(map(res => res.data));
  }

  // ==================== TABLAS DE RESULTADOS ====================

  obtenerTablaResultadosTotal(periodo?: number): Observable<TablaResultados> {
    let params = new HttpParams();
    if (periodo) {
      params = params.set('periodo', periodo.toString());
    }
    return this.http.get<ApiResponse<TablaResultados>>(`${this.baseUrl}/compensacion/tabla-resultados/total`, { params })
      .pipe(map(res => res.data));
  }

  obtenerTablaResultadosPromedio(periodo?: number): Observable<TablaResultados> {
    let params = new HttpParams();
    if (periodo) {
      params = params.set('periodo', periodo.toString());
    }
    return this.http.get<ApiResponse<TablaResultados>>(`${this.baseUrl}/compensacion/tabla-resultados/promedio`, { params })
      .pipe(map(res => res.data));
  }

  // ==================== VISTA POR NEGOCIO ====================

  obtenerSeriePorNegocio(negocio: string, desde?: number, hasta?: number): Observable<SerieTemporal> {
    let params = new HttpParams();
    if (desde) params = params.set('desde', desde.toString());
    if (hasta) params = params.set('hasta', hasta.toString());
    return this.http.get<ApiResponse<SerieTemporal>>(`${this.baseUrl}/compensacion/negocio/${negocio}/serie`, { params })
      .pipe(map(res => res.data));
  }

  obtenerSeriePorNegocioYPuesto(negocio: string, puesto: string, desde?: number, hasta?: number): Observable<SerieTemporal> {
    let params = new HttpParams();
    if (desde) params = params.set('desde', desde.toString());
    if (hasta) params = params.set('hasta', hasta.toString());
    return this.http.get<ApiResponse<SerieTemporal>>(
      `${this.baseUrl}/compensacion/negocios/${encodeURIComponent(negocio)}/puestos/${encodeURIComponent(puesto)}/serie`,
      { params }
    ).pipe(map(res => res.data));
  }

  obtenerSeriePorNegocioPuestoIndicador(
    negocio: string,
    puesto: string,
    indicador: string,
    desde?: number,
    hasta?: number
  ): Observable<SerieTemporal> {
    let params = new HttpParams();
    if (desde) params = params.set('desde', desde.toString());
    if (hasta) params = params.set('hasta', hasta.toString());
    return this.http.get<ApiResponse<SerieTemporal>>(
      `${this.baseUrl}/compensacion/negocios/${encodeURIComponent(negocio)}/puestos/${encodeURIComponent(puesto)}/indicadores/${encodeURIComponent(indicador)}/serie`,
      { params }
    ).pipe(map(res => res.data));
  }

  // ==================== HEALTH ====================

  obtenerHealth(): Observable<any> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/health`)
      .pipe(map(res => res.data));
  }

  obtenerInfo(): Observable<any> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/info`)
      .pipe(map(res => res.data));
  }
}
