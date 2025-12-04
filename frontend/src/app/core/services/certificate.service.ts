import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import { GenerateCertificateRequest, ResourceId } from '../models/template.models';

@Injectable({
  providedIn: 'root'
})
export class CertificateService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/certificates`;

  generateCertificate(request: GenerateCertificateRequest): Observable<ResourceId> {
    return this.http.post<ResourceId>(this.apiUrl, request);
  }
}
