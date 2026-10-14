export interface Municipio {
  codigo: string;
  nombre: string;
}

export interface TramoPrecipitacion {
  probabilidad: number;
  periodo: string;
}

export interface PrevisionDia {
  mediaTemperatura: number;
  unidadTemperatura: 'G_CEL' | 'G_FAH';
  probPrecipitacion: TramoPrecipitacion[];
}
