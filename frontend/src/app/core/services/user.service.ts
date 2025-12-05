import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserResponse, CreateUserRequest, ResourceId } from '../models/user.models';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/users`;

  getAllUsers(page: number = 0, size: number = 10, sort: string = 'createdAt,desc'): Observable<PageResponse<UserResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<PageResponse<UserResponse>>(this.apiUrl, { params });
  }

  createUser(request: CreateUserRequest): Observable<ResourceId> {
    return this.http.post<ResourceId>(this.apiUrl, request);
  }
}
