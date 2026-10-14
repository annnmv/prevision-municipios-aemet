import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { MeteoApiAdapter } from '../../infrastructure/meteo-api.adapter';
import { Municipio, PrevisionDia } from '../../core/models/prevision';
import { WeatherIconComponent } from '../icons/weather-icon.component';
import { Condicion } from '../../core/weather.util';

const CLAVE_ULTIMO = 'ultimoMunicipio';

/** Buscador de previsión: autocompletado + unidad, con refresco ante cada cambio. */
@Component({
  selector: 'app-buscador',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, WeatherIconComponent],
  templateUrl: './buscador.component.html',
  styleUrl: './buscador.component.css',
})
export class BuscadorComponent implements OnInit, OnDestroy {
  private api = inject(MeteoApiAdapter);
  private tecleado = new Subject<string>();
  private sub?: Subscription;

  texto = signal('');
  sugerencias = signal<Municipio[]>([]);
  buscando = signal(false);
  listaAbierta = signal(false);
  seleccionado = signal<Municipio | null>(null);
  unidad = signal(''); // vacío = G_CEL por defecto en el backend
  prevision = signal<PrevisionDia | null>(null);
  cargandoPrevision = signal(false);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.sub = this.tecleado.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((q) => {
        if (q.trim().length < 2) {
          this.sugerencias.set([]);
          return of([]);
        }
        this.buscando.set(true);
        return this.api.buscarMunicipios(q.trim()).pipe(catchError(() => of([])));
      })
    ).subscribe((ms) => {
      this.sugerencias.set(ms);
      this.listaAbierta.set(ms.length > 0);
      this.buscando.set(false);
    });

    // Recupera el último municipio elegido.
    try {
      const raw = localStorage.getItem(CLAVE_ULTIMO);
      if (raw) {
        const m = JSON.parse(raw) as Municipio;
        if (m?.codigo && m?.nombre) {
          this.seleccionado.set(m);
          this.texto.set(m.nombre);
          this.cargarPrevision();
        }
      }
    } catch {
      /* sin localStorage, sin recuerdo */
    }
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  alTeclear(v: string): void {
    this.texto.set(v);
    this.tecleado.next(v);
  }

  elegir(m: Municipio): void {
    this.seleccionado.set(m);
    this.texto.set(m.nombre);
    this.sugerencias.set([]);
    this.listaAbierta.set(false);
    try {
      localStorage.setItem(CLAVE_ULTIMO, JSON.stringify(m));
    } catch {
      /* modo privado o bloqueado: sin recuerdo */
    }
    this.cargarPrevision();
  }

  alCambiarUnidad(v: string): void {
    this.unidad.set(v);
    this.cargarPrevision();
  }

  limpiar(): void {
    this.seleccionado.set(null);
    this.texto.set('');
    this.sugerencias.set([]);
    this.prevision.set(null);
    this.error.set(null);
    try {
      localStorage.removeItem(CLAVE_ULTIMO);
    } catch {
      /* nada que hacer */
    }
  }

  private cargarPrevision(): void {
    const m = this.seleccionado();
    if (!m) return;
    this.cargandoPrevision.set(true);
    this.error.set(null);
    this.api.getPrevision(m.codigo, this.unidad() || undefined).subscribe({
      next: (p) => {
        this.prevision.set(p);
        this.cargandoPrevision.set(false);
      },
      error: () => {
        this.prevision.set(null);
        this.error.set('No se pudo cargar la previsión. Revisa que el backend esté en :8080.');
        this.cargandoPrevision.set(false);
      },
    });
  }

  condicion(): Condicion {
    const max = Math.max(...(this.prevision()?.probPrecipitacion.map((t) => t.probabilidad) ?? [0]));
    if (max >= 50) return 'lluvia';
    if (max >= 20) return 'intervalos';
    return 'sol';
  }

  simboloUnidad(): string {
    return this.prevision()?.unidadTemperatura === 'G_FAH' ? '°F' : '°C';
  }
}
