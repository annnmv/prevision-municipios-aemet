import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Municipio, PrevisionDia } from '../core/models/prevision';

// Llamadas al backend. El resto de la app no usa HttpClient directamente.
@Injectable({ providedIn: 'root' })
export class MeteoApiAdapter {
  private http = inject(HttpClient);
  // TODO: mover a environments en producción.
  private baseUrl = 'http://localhost:8080/api';

  // Buscador de municipios para el autocompletado.
  buscarMunicipios(nombre: string): Observable<Municipio[]> {
    const params = new HttpParams().set('nombre', nombre);
    return this.http.get<Municipio[]>(`${this.baseUrl}/municipios`, { params });
  }

  // Previsión del día siguiente. Sin unidad = G_CEL (lo decide el backend).
  getPrevision(codigo: string, unidad?: string): Observable<PrevisionDia> {
    let params = new HttpParams();
    if (unidad) params = params.set('unidad', unidad);
    return this.http.get<PrevisionDia>(`${this.baseUrl}/prevision/${codigo}`, { params });
  }
}
