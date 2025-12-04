import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import { GenerateCertificateRequest, ResourceId, CertificateResponse, PageResponse } from '../models/template.models';

@Injectable({
  providedIn: 'root'
})
export class CertificateService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/certificates`;

  generateCertificate(request: GenerateCertificateRequest): Observable<ResourceId> {
    return this.http.post<ResourceId>(this.apiUrl, request);
  }

  getCertificatesByTemplate(templateId: string, page: number = 0, size: number = 20): Observable<PageResponse<CertificateResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdAt,desc');

    return this.http.get<PageResponse<CertificateResponse>>(`${this.apiUrl}/by-template/${templateId}`, { params });
  }
}
