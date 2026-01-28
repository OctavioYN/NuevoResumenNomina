export interface SerieTemporal {
  negocio: string;
  puesto: string;
  indicador: string;
  media: number;
  desviacionEstandar: number;
  minimo: number;
  maximo: number;
  cantidadPuntos: number;
  lineasConfianza: LineasConfianza;
  serie: PuntoSerie[];
}

export interface LineasConfianza {
  limiteSuperior1Sigma: number;
  limiteInferior1Sigma: number;
  limiteSuperior15Sigma: number;
  limiteInferior15Sigma: number;
}

export interface PuntoSerie {
  semana: string;
  valor: number;
  variacionVsSemanaAnterior: number;
  fueraDe1Sigma: boolean;
  fueraDe15Sigma: boolean;
}
