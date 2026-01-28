/**
 * Modelos para alertas estadísticas
 */

export interface AlertaZScore {
  negocio: string;
  puesto: string;
  indicador: string;
  variacionPorcentualVsSA: number;
  variacionMedia: number;
  limiteInferior: number;
  limiteSuperior: number;
  zScore: number;
  severidad: 'CRITICA' | 'ALTA' | 'MODERADA';
}

export interface AlertaPrediccion {
  negocio: string;
  puesto: string;
  indicador: string;
  observacionReal: number;
  limiteInferior: number;
  limiteSuperior: number;
  variacionFueraRango: number;
  historiaInsuficiente: boolean;
}

export interface AlertasResponse {
  periodo: string;
  totalAlertasZScore: number;
  totalAlertasPrediccion: number;
  alertasZScore: AlertaZScore[];
  alertasPrediccion: AlertaPrediccion[];
}
