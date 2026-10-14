import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { Condicion } from '../../core/weather.util';

/** Iconos SVG del tiempo. */
@Component({
  selector: 'app-weather-icon',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
         stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      @switch (tipo()) {
        @case ('sol') {
          <circle cx="12" cy="12" r="4" />
          <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
        }
        @case ('calor') {
          <path d="M12 2v2M4.9 4.9l1.4 1.4M2 12h2M20 12h2M17.7 6.3l1.4-1.4" />
          <path d="M8 21h8M10 17h4M12 13v4" />
          <circle cx="12" cy="10" r="3" />
        }
        @case ('intervalos') {
          <circle cx="8" cy="8" r="2.6" />
          <path d="M8 2.5v1.5M2.5 8h1.5M4.1 4.1l1 1M11.9 4.1l-1 1" />
          <path d="M9 20h9.5a3.5 3.5 0 0 0 .6-6.95A5.5 5.5 0 0 0 8.3 14.5 2.8 2.8 0 0 0 9 20Z" />
        }
        @case ('nublado') {
          <path d="M7 20h10.5a4 4 0 0 0 .7-7.94A6 6 0 0 0 6.4 13.6 3.2 3.2 0 0 0 7 20Z" />
        }
        @case ('lluvia') {
          <path d="M7 15h10.5a4 4 0 0 0 .7-7.94A6 6 0 0 0 6.4 8.6 3.2 3.2 0 0 0 7 15Z" />
          <path d="M8 18v2.5M12 18v2.5M16 18v2.5" />
        }
        @case ('frio') {
          <path d="M12 2v20M3.3 7l17.4 10M20.7 7 3.3 17M12 2l-2.5 2.5M12 2l2.5 2.5M12 22l-2.5-2.5M12 22l2.5-2.5" />
        }
        @default {
          <path d="M10 13.6V4a2 2 0 1 1 4 0v9.6a4.5 4.5 0 1 1-4 0Z" />
          <circle cx="12" cy="18" r="1.6" />
        }
      }
    </svg>
  `,
  styles: [':host{display:inline-flex}:host svg{width:100%;height:100%}'],
})
export class WeatherIconComponent {
  tipo = input<Condicion>('na');
}
