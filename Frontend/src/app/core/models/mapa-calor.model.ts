export interface MapaCalor {
  periodoActual: string;
  periodoAnterior: string;
  items: ItemMapaCalor[];
  itemsPorNegocio: { [negocio: string]: ItemMapaCalor[] };
}

export interface ItemMapaCalor {
  negocio: string;
  puesto: string;
  variacionPorcentual: number;
  valorAbsoluto: number;
  esPositivo: boolean;
  intensidadColor: number;
  valorSemanaActual: number;
  valorSemanaAnterior: number;
}
