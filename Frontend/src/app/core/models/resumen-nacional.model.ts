export interface ResumenNacional {
  periodoActual: string;
  periodoAnterior: string;
  totalSemanaActual: number;
  totalSemanaAnterior: number;
  diferencia: number;
  variacionPorcentual: number;
  negocios: ResumenNegocio[];
}

export interface ResumenNegocio {
  negocio: string;
  slug: string;
  semanaActual: number;
  semanaAnterior: number;
  diferencia: number;
  variacionPorcentual: number;
  empleados: number;
}
