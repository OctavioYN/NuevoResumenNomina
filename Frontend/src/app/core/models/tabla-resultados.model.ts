export interface TablaResultados {
  periodoActual: string;
  periodoAnterior: string;
  tipo: 'TOTAL' | 'PROMEDIO';
  filas: FilaResultado[];
  filasPorNegocio: { [negocio: string]: FilaResultado[] };
}

export interface FilaResultado {
  puesto: string;
  negocio: string;
  semanaActual: number;
  semanaAnterior: number;
  variacionMonto: number;
  variacionPorcentual: number;
  variacionPositiva: boolean;
}

export interface ResumenIndicador {
  indicador: string;
  valorActual: number;
  valorAnterior: number;
  diferencia: number;
  variacionPorcentual: number;
  variacionPositiva: boolean;
}
