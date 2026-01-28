import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, ResultadoCarga, Estadisticas, UploadProgress } from '../../core/services';

type TipoCarga = 'reemplazar' | 'incremental' | 'periodo';

@Component({
  selector: 'app-carga-datos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="carga-view">
      <h2 class="title">Carga de Datos de Inteligencia</h2>

      <!-- Estadísticas Totales -->
      <div class="stats-card" *ngIf="estadisticas()">
        <h3>Totales Generales</h3>
        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-value">{{ estadisticas()!.totales.totalRegistros | number }}</span>
            <span class="stat-label">Registros totales</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ estadisticas()!.totales.totalPeriodos }}</span>
            <span class="stat-label">Periodos</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ estadisticas()!.totales.totalNegocios }}</span>
            <span class="stat-label">Negocios</span>
          </div>
        </div>
      </div>

      <!-- Tipo de carga -->
      <div class="section">
        <h3>Tipo de Carga</h3>
        <div class="radio-group">
          <label class="radio-option" [class.selected]="tipoCarga() === 'reemplazar'">
            <input type="radio" name="tipoCarga" value="reemplazar"
                   [checked]="tipoCarga() === 'reemplazar'"
                   (change)="tipoCarga.set('reemplazar')">
            <div class="radio-content">
              <span class="radio-title">Reemplazar Todo</span>
              <span class="radio-desc">Elimina todos los datos existentes y carga los nuevos</span>
            </div>
          </label>

          <label class="radio-option" [class.selected]="tipoCarga() === 'incremental'">
            <input type="radio" name="tipoCarga" value="incremental"
                   [checked]="tipoCarga() === 'incremental'"
                   (change)="tipoCarga.set('incremental')">
            <div class="radio-content">
              <span class="radio-title">Incremental</span>
              <span class="radio-desc">Agrega o actualiza registros sin eliminar los existentes</span>
            </div>
          </label>

          <label class="radio-option" [class.selected]="tipoCarga() === 'periodo'">
            <input type="radio" name="tipoCarga" value="periodo"
                   [checked]="tipoCarga() === 'periodo'"
                   (change)="tipoCarga.set('periodo')">
            <div class="radio-content">
              <span class="radio-title">Por Periodo</span>
              <span class="radio-desc">Reemplaza solo los datos del periodo especificado</span>
            </div>
          </label>
        </div>

        <!-- Selector de periodo (solo si es tipo periodo) -->
        <div class="periodo-input" *ngIf="tipoCarga() === 'periodo'">
          <label>Periodo (YYYYWW):</label>
          <input type="number" [(ngModel)]="periodoSeleccionado"
                 placeholder="ej: 202544" min="202301" max="203052">
        </div>
      </div>

      <!-- Zona de carga de archivo -->
      <div class="section">
        <h3>Archivo</h3>
        <div class="upload-zone"
             [class.dragover]="isDragOver()"
             [class.has-file]="archivoSeleccionado()"
             (dragover)="onDragOver($event)"
             (dragleave)="onDragLeave($event)"
             (drop)="onDrop($event)"
             (click)="fileInput.click()">

          <input type="file" #fileInput hidden
                 accept=".csv,.xlsx,.xls"
                 (change)="onFileSelected($event)">

          <div class="upload-content" *ngIf="!archivoSeleccionado()">
            <div class="upload-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/>
                <line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
            </div>
            <p class="upload-text">Arrastra un archivo aquí o haz clic para seleccionar</p>
            <p class="upload-hint">Formatos aceptados: CSV, Excel (.xlsx, .xls)</p>
          </div>

          <div class="file-info" *ngIf="archivoSeleccionado()">
            <div class="file-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="file-details">
              <span class="file-name">{{ archivoSeleccionado()!.name }}</span>
              <span class="file-size">{{ formatFileSize(archivoSeleccionado()!.size) }}</span>
            </div>
            <button class="btn-remove" (click)="removeFile($event)">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Progreso de carga -->
      <div class="progress-section" *ngIf="uploading()">
        <div class="progress-bar">
          <div class="progress-fill" [style.width.%]="uploadProgress()"></div>
        </div>
        <span class="progress-text">{{ uploadProgress() }}%</span>
      </div>

      <!-- Botón de carga -->
      <div class="actions">
        <button class="btn-primary"
                [disabled]="!archivoSeleccionado() || uploading() || (tipoCarga() === 'periodo' && !periodoSeleccionado)"
                (click)="cargarArchivo()">
          {{ uploading() ? 'Cargando...' : 'Cargar Datos' }}
        </button>
      </div>

      <!-- Resultado de la carga -->
      <div class="result-card" *ngIf="resultado()" [class.success]="resultado()!.success" [class.error]="!resultado()!.success">
        <div class="result-header">
          <span class="result-icon" *ngIf="resultado()!.success">&#10004;</span>
          <span class="result-icon" *ngIf="!resultado()!.success">&#10008;</span>
          <span class="result-title">{{ resultado()!.success ? 'Carga Exitosa' : 'Error en la Carga' }}</span>
        </div>
        <p class="result-message">{{ resultado()!.mensaje }}</p>
        <div class="result-details" *ngIf="resultado()!.success">
          <div class="detail-item">
            <span class="detail-label">Registros procesados:</span>
            <span class="detail-value">{{ resultado()!.totalRegistros | number }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Registros exitosos:</span>
            <span class="detail-value">{{ resultado()!.registrosExitosos | number }}</span>
          </div>
          <div class="detail-item" *ngIf="resultado()!.registrosConError">
            <span class="detail-label">Registros con error:</span>
            <span class="detail-value error">{{ resultado()!.registrosConError | number }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Tiempo de procesamiento:</span>
            <span class="detail-value">{{ resultado()!.tiempoProcesamientoMs | number }} ms</span>
          </div>
        </div>
        <div class="result-errors" *ngIf="resultado()!.errores && resultado()!.errores.length > 0">
          <p class="errors-title">Errores:</p>
          <ul>
            <li *ngFor="let error of resultado()!.errores">{{ error }}</li>
          </ul>
        </div>
      </div>

      <!-- Estadísticas por Año -->
      <div class="stats-by-year" *ngIf="estadisticas() && estadisticas()!.estadisticasPorAnio.length > 0">
        <h3>Estadísticas por Año</h3>
        <div class="year-cards">
          <div class="year-card" *ngFor="let anio of estadisticas()!.estadisticasPorAnio">
            <div class="year-header">
              <span class="year-title">{{ anio.anio }}</span>
            </div>
            <div class="year-stats">
              <div class="year-stat">
                <span class="year-stat-value">{{ anio.totalRegistros | number }}</span>
                <span class="year-stat-label">Registros</span>
              </div>
              <div class="year-stat">
                <span class="year-stat-value">{{ anio.totalPeriodos }}</span>
                <span class="year-stat-label">Periodos</span>
              </div>
              <div class="year-stat">
                <span class="year-stat-value">{{ anio.totalNegocios }}</span>
                <span class="year-stat-label">Negocios</span>
              </div>
            </div>
            <div class="year-negocios">
              <span class="negocio-tag" *ngFor="let negocio of anio.negocios">{{ negocio }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .carga-view {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
    }

    .title {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin-bottom: 20px;
    }

    .section {
      margin-bottom: 25px;
    }

    .section h3 {
      font-size: 14px;
      font-weight: 600;
      color: #666;
      margin-bottom: 12px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .stats-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 20px;
      border-radius: 12px;
      margin-bottom: 25px;
    }

    .stats-card h3 {
      color: rgba(255,255,255,0.9);
      margin-bottom: 15px;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 20px;
    }

    .stat-item {
      text-align: center;
    }

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      display: block;
    }

    .stat-label {
      font-size: 12px;
      opacity: 0.85;
    }

    .stats-by-year {
      margin-bottom: 25px;
    }

    .stats-by-year h3 {
      font-size: 14px;
      font-weight: 600;
      color: #666;
      margin-bottom: 12px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .year-cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 15px;
    }

    .year-card {
      background: white;
      border: 1px solid #e0e0e0;
      border-radius: 10px;
      padding: 15px;
      transition: box-shadow 0.2s;
    }

    .year-card:hover {
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    .year-header {
      margin-bottom: 12px;
      padding-bottom: 10px;
      border-bottom: 2px solid #667eea;
    }

    .year-title {
      font-size: 20px;
      font-weight: 700;
      color: #667eea;
    }

    .year-stats {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 10px;
      margin-bottom: 12px;
    }

    .year-stat {
      text-align: center;
    }

    .year-stat-value {
      display: block;
      font-size: 18px;
      font-weight: 700;
      color: #333;
    }

    .year-stat-label {
      font-size: 11px;
      color: #888;
      text-transform: uppercase;
    }

    .year-negocios {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
    }

    .negocio-tag {
      font-size: 11px;
      padding: 3px 8px;
      background: #f0f0f0;
      border-radius: 12px;
      color: #555;
    }

    .radio-group {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .radio-option {
      display: flex;
      align-items: flex-start;
      padding: 15px;
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .radio-option:hover {
      border-color: #667eea;
    }

    .radio-option.selected {
      border-color: #667eea;
      background-color: #f8f9ff;
    }

    .radio-option input {
      margin-right: 12px;
      margin-top: 4px;
    }

    .radio-content {
      display: flex;
      flex-direction: column;
    }

    .radio-title {
      font-weight: 600;
      color: #333;
    }

    .radio-desc {
      font-size: 12px;
      color: #666;
      margin-top: 2px;
    }

    .periodo-input {
      margin-top: 15px;
      padding: 15px;
      background: #f5f5f5;
      border-radius: 8px;
    }

    .periodo-input label {
      display: block;
      font-size: 13px;
      font-weight: 500;
      margin-bottom: 8px;
    }

    .periodo-input input {
      width: 150px;
      padding: 8px 12px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 14px;
    }

    .upload-zone {
      border: 2px dashed #ccc;
      border-radius: 12px;
      padding: 40px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s;
      background: #fafafa;
    }

    .upload-zone:hover,
    .upload-zone.dragover {
      border-color: #667eea;
      background: #f8f9ff;
    }

    .upload-zone.has-file {
      border-style: solid;
      border-color: #28a745;
      background: #f8fff8;
    }

    .upload-icon {
      color: #999;
      margin-bottom: 15px;
    }

    .upload-text {
      font-size: 16px;
      color: #666;
      margin-bottom: 8px;
    }

    .upload-hint {
      font-size: 12px;
      color: #999;
    }

    .file-info {
      display: flex;
      align-items: center;
      gap: 15px;
    }

    .file-icon {
      color: #28a745;
    }

    .file-details {
      flex: 1;
      text-align: left;
    }

    .file-name {
      display: block;
      font-weight: 600;
      color: #333;
    }

    .file-size {
      font-size: 12px;
      color: #666;
    }

    .btn-remove {
      background: none;
      border: none;
      color: #dc3545;
      cursor: pointer;
      padding: 5px;
    }

    .btn-remove:hover {
      color: #a71d2a;
    }

    .progress-section {
      display: flex;
      align-items: center;
      gap: 15px;
      margin-bottom: 20px;
    }

    .progress-bar {
      flex: 1;
      height: 8px;
      background: #e0e0e0;
      border-radius: 4px;
      overflow: hidden;
    }

    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #667eea, #764ba2);
      transition: width 0.3s;
    }

    .progress-text {
      font-size: 14px;
      font-weight: 600;
      color: #667eea;
      min-width: 40px;
    }

    .actions {
      margin-bottom: 25px;
    }

    .btn-primary {
      width: 100%;
      padding: 14px 24px;
      font-size: 16px;
      font-weight: 600;
      color: white;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      border-radius: 8px;
      cursor: pointer;
      transition: opacity 0.2s;
    }

    .btn-primary:hover:not(:disabled) {
      opacity: 0.9;
    }

    .btn-primary:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .result-card {
      padding: 20px;
      border-radius: 12px;
      border: 1px solid;
    }

    .result-card.success {
      background: #d4edda;
      border-color: #c3e6cb;
    }

    .result-card.error {
      background: #f8d7da;
      border-color: #f5c6cb;
    }

    .result-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;
    }

    .result-icon {
      font-size: 24px;
    }

    .result-card.success .result-icon {
      color: #28a745;
    }

    .result-card.error .result-icon {
      color: #dc3545;
    }

    .result-title {
      font-size: 18px;
      font-weight: 600;
    }

    .result-card.success .result-title {
      color: #155724;
    }

    .result-card.error .result-title {
      color: #721c24;
    }

    .result-message {
      color: #333;
      margin-bottom: 15px;
    }

    .result-details {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 10px;
    }

    .detail-item {
      display: flex;
      justify-content: space-between;
      font-size: 13px;
    }

    .detail-label {
      color: #666;
    }

    .detail-value {
      font-weight: 600;
      color: #333;
    }

    .detail-value.error {
      color: #dc3545;
    }

    .result-errors {
      margin-top: 15px;
      padding-top: 15px;
      border-top: 1px solid rgba(0,0,0,0.1);
    }

    .errors-title {
      font-weight: 600;
      color: #721c24;
      margin-bottom: 8px;
    }

    .result-errors ul {
      margin: 0;
      padding-left: 20px;
    }

    .result-errors li {
      font-size: 12px;
      color: #721c24;
      margin-bottom: 4px;
    }
  `]
})
export class CargaDatosComponent {
  private adminService = inject(AdminService);

  tipoCarga = signal<TipoCarga>('reemplazar');
  periodoSeleccionado: number | null = null;
  archivoSeleccionado = signal<File | null>(null);
  isDragOver = signal(false);
  uploading = signal(false);
  uploadProgress = signal(0);
  resultado = signal<ResultadoCarga | null>(null);
  estadisticas = signal<Estadisticas | null>(null);

  constructor() {
    this.cargarEstadisticas();
  }

  cargarEstadisticas() {
    this.adminService.obtenerEstadisticas().subscribe({
      next: (stats) => this.estadisticas.set(stats),
      error: () => this.estadisticas.set(null)
    });
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(true);
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(false);
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(false);

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.selectFile(files[0]);
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectFile(input.files[0]);
    }
  }

  selectFile(file: File) {
    const validExtensions = ['.csv', '.xlsx', '.xls'];
    const extension = '.' + file.name.split('.').pop()?.toLowerCase();

    if (!validExtensions.includes(extension)) {
      alert('Formato no válido. Use archivos CSV o Excel (.xlsx, .xls)');
      return;
    }

    this.archivoSeleccionado.set(file);
    this.resultado.set(null);
  }

  removeFile(event: Event) {
    event.stopPropagation();
    this.archivoSeleccionado.set(null);
    this.resultado.set(null);
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  cargarArchivo() {
    const archivo = this.archivoSeleccionado();
    if (!archivo) return;

    this.uploading.set(true);
    this.uploadProgress.set(0);
    this.resultado.set(null);

    let upload$;

    switch (this.tipoCarga()) {
      case 'reemplazar':
        upload$ = this.adminService.cargarReemplazar(archivo);
        break;
      case 'incremental':
        upload$ = this.adminService.cargarIncremental(archivo);
        break;
      case 'periodo':
        if (!this.periodoSeleccionado) return;
        upload$ = this.adminService.cargarPorPeriodo(archivo, this.periodoSeleccionado);
        break;
    }

    upload$.subscribe({
      next: (progress: UploadProgress) => {
        this.uploadProgress.set(progress.progress);

        if (progress.status === 'complete' && progress.data) {
          this.resultado.set(progress.data);
          this.uploading.set(false);
          this.cargarEstadisticas(); // Actualizar estadísticas
        } else if (progress.status === 'error') {
          this.resultado.set({
            success: false,
            mensaje: progress.message || 'Error durante la carga',
            fechaCarga: new Date().toISOString(),
            usuarioCarga: 'sistema',
            archivoOriginal: archivo.name,
            totalRegistros: 0,
            registrosExitosos: 0,
            registrosConError: 0,
            tiempoProcesamientoMs: 0,
            errores: [progress.message || 'Error desconocido']
          });
          this.uploading.set(false);
        }
      },
      error: (err) => {
        this.resultado.set({
          success: false,
          mensaje: 'Error de conexión: ' + (err.message || 'No se pudo conectar al servidor'),
          fechaCarga: new Date().toISOString(),
          usuarioCarga: 'sistema',
          archivoOriginal: archivo.name,
          totalRegistros: 0,
          registrosExitosos: 0,
          registrosConError: 0,
          tiempoProcesamientoMs: 0,
          errores: [err.message || 'Error de conexión']
        });
        this.uploading.set(false);
      }
    });
  }
}
