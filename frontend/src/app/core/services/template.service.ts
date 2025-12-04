import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import { CreateTemplateRequest, TemplateResponse, ResourceId, PageResponse } from '../models/template.models';

@Injectable({
  providedIn: 'root'
})
export class TemplateService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/templates`;

  createTemplate(request: CreateTemplateRequest): Observable<ResourceId> {
    return this.http.post<ResourceId>(this.apiUrl, request);
  }

  getTemplates(): Observable<TemplateResponse[]> {
    return this.http.get<PageResponse<TemplateResponse>>(this.apiUrl).pipe(
      map(response => response.content)
    );
  }
}
